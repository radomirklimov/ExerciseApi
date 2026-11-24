package com.organizer.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.organizer.dto.SyncChangeRecord
import com.organizer.dto.SyncChangesResponse
import com.organizer.dto.SyncMutationRequest
import com.organizer.dto.SyncMutationResult
import com.organizer.dto.SyncPushRequest
import com.organizer.dto.SyncPushResponse
import com.organizer.entity.UserEntity
import com.organizer.repository.SyncChangeLogRepository
import com.organizer.repository.UserDataRecordRepository
import com.organizer.sync.SyncCanonicalizer
import com.organizer.sync.SyncCursor
import com.organizer.sync.SyncCursorCodec
import com.organizer.sync.SyncEntityType
import com.organizer.sync.SyncErrorCode
import com.organizer.sync.SyncLimits
import com.organizer.sync.SyncOperation
import com.organizer.sync.SyncRequestException
import com.organizer.sync.SyncStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class SyncService(
    private val userService: UserService,
    private val mutationProcessor: SyncMutationProcessor,
    private val recordRepository: UserDataRecordRepository,
    private val changeLogRepository: SyncChangeLogRepository,
    private val objectMapper: ObjectMapper,
    private val canonicalizer: SyncCanonicalizer,
    @Value("\${sync.cursor-secret:}") cursorSecret: String,
) {
    private val cursorCodec: SyncCursorCodec

    init {
        val secret = cursorSecret.trim()
        require(secret.isNotBlank()) { "sync.cursor-secret must be configured" }
        require(secret.length >= 32) { "sync.cursor-secret must be at least 32 characters" }
        require(secret != DEVELOPMENT_SECRET) { "sync.cursor-secret must not use the development default" }
        cursorCodec = SyncCursorCodec(secret)
    }

    companion object {
        private const val DEVELOPMENT_SECRET = "exercise-organizer-sync-cursor"
    }

    fun push(firebaseUid: String, email: String?, displayName: String?, request: SyncPushRequest): SyncPushResponse {
        if (request.mutations.isEmpty()) {
            throw SyncRequestException(SyncErrorCode.INVALID_REQUEST, "At least one mutation is required")
        }
        if (request.mutations.size > SyncLimits.MAX_BATCH_SIZE) {
            throw SyncRequestException(SyncErrorCode.INVALID_REQUEST, "Batch size exceeds ${SyncLimits.MAX_BATCH_SIZE}")
        }

        userService.getOrCreate(firebaseUid, email, displayName)

        val results = request.mutations.map { mutation ->
            val validationError = validate(mutation)
            if (validationError != null) {
                SyncMutationResult(
                    mutationId = mutation.mutationId,
                    syncId = mutation.syncId,
                    status = SyncStatus.REJECTED,
                    errorCode = validationError.first,
                    errorMessage = validationError.second,
                )
            } else {
                mutationProcessor.process(firebaseUid, mutation)
            }
        }
        return SyncPushResponse(results = results, serverTime = Instant.now())
    }

    @Transactional(readOnly = true)
    fun changes(firebaseUid: String, cursorValue: String?, limit: Int): SyncChangesResponse {
        val user = userService.getOrCreate(firebaseUid, null, null)
        val effectiveLimit = limit.coerceIn(1, SyncLimits.MAX_PAGE_SIZE)
        val userScope = user.id.toString()
        val now = Instant.now()

        val cursor = if (cursorValue.isNullOrBlank()) {
            SyncCursor(userScope, 0, null, now.epochSecond + SyncLimits.CURSOR_TTL_SECONDS)
        } else {
            cursorCodec.decodeAndVerify(cursorValue, userScope)
                ?: throw SyncRequestException(SyncErrorCode.INVALID_REQUEST, "Invalid or expired cursor")
        }

        val highWaterMark = cursor.highWaterMark
            ?: (changeLogRepository.findFirstByUserOrderBySequenceDesc(user)?.sequence ?: 0L)

        if (cursor.lastSequence > highWaterMark) {
            throw SyncRequestException(SyncErrorCode.INVALID_REQUEST, "Invalid cursor position")
        }

        val entries = changeLogRepository
            .findByUserAndSequenceGreaterThanAndSequenceLessThanEqualOrderBySequenceAsc(
                user, cursor.lastSequence, highWaterMark
            )
            .take(effectiveLimit + 1)

        val hasMore = entries.size > effectiveLimit
        val page = if (hasMore) entries.take(effectiveLimit) else entries
        val records = page.map { entry ->
            toChangeRecord(user, entry.entityType, entry.syncId, entry.recordVersion, entry.changedAt, entry.deleted)
        }

        val lastConsumed = page.lastOrNull()?.sequence ?: cursor.lastSequence
        val nextCursor = if (hasMore) {
            cursorCodec.encode(
                SyncCursor(userScope, lastConsumed, highWaterMark, now.epochSecond + SyncLimits.CURSOR_TTL_SECONDS)
            )
        } else {
            // Snapshot consumed: release the high-water mark so later writes become visible.
            cursorCodec.encode(
                SyncCursor(userScope, highWaterMark, null, now.epochSecond + SyncLimits.CURSOR_TTL_SECONDS)
            )
        }

        return SyncChangesResponse(
            records = records,
            nextCursor = nextCursor,
            hasMore = hasMore,
            serverTime = now,
        )
    }

    private fun toChangeRecord(
        user: UserEntity,
        entityType: String,
        syncId: String,
        version: Long,
        serverUpdatedAt: Instant,
        deleted: Boolean,
    ): SyncChangeRecord {
        val record = recordRepository.findByUserAndEntityTypeAndSyncId(user, entityType, syncId)
        val payload = record?.payload?.let {
            runCatching {
                objectMapper.convertValue(
                    objectMapper.readTree(it),
                    object : com.fasterxml.jackson.core.type.TypeReference<Map<String, Any>>() {},
                )
            }.getOrNull()
        }
        return SyncChangeRecord(
            entityType = entityType,
            syncId = syncId,
            serverVersion = version,
            serverUpdatedAt = serverUpdatedAt,
            deleted = deleted,
            payload = payload,
        )
    }

    private fun validate(mutation: SyncMutationRequest): Pair<String, String>? {
        if (!isUuid(mutation.mutationId)) return SyncErrorCode.INVALID_MUTATION_ID to "mutationId must be a UUID"
        if (!isUuid(mutation.syncId)) return SyncErrorCode.INVALID_SYNC_ID to "syncId must be a UUID"
        if (SyncEntityType.from(mutation.entityType) == null) return SyncErrorCode.INVALID_ENTITY_TYPE to "Unsupported entityType"
        if (SyncOperation.from(mutation.operation) == null) return SyncErrorCode.INVALID_OPERATION to "Unsupported operation"
        if (mutation.baseVersion < 0) return SyncErrorCode.INVALID_VERSION to "baseVersion must be non-negative"
        val timestamp = mutation.clientUpdatedAt
            ?: return SyncErrorCode.INVALID_TIMESTAMP to "clientUpdatedAt is required"
        if (timestamp.isAfter(Instant.now().plusSeconds(300))) {
            return SyncErrorCode.INVALID_TIMESTAMP to "clientUpdatedAt is in the future"
        }
        val payloadSize = canonicalizer.canonicalize(mutation.payload).toByteArray().size
        if (payloadSize > SyncLimits.MAX_PAYLOAD_BYTES) {
            return SyncErrorCode.PAYLOAD_TOO_LARGE to "Payload exceeds ${SyncLimits.MAX_PAYLOAD_BYTES} bytes"
        }
        return null
    }

    private fun isUuid(value: String): Boolean = runCatching { UUID.fromString(value) }.isSuccess
}
