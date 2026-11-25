package com.organizer.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.organizer.dto.SyncMutationRequest
import com.organizer.dto.SyncMutationResult
import com.organizer.entity.SyncChangeLogEntity
import com.organizer.entity.SyncMutationReceiptEntity
import com.organizer.entity.UserDataRecordEntity
import com.organizer.entity.UserEntity
import com.organizer.entity.UserSyncSequenceEntity
import com.organizer.repository.SyncChangeLogRepository
import com.organizer.repository.SyncMutationReceiptRepository
import com.organizer.repository.UserDataRecordRepository
import com.organizer.repository.UserRepository
import com.organizer.repository.UserSyncSequenceRepository
import com.organizer.sync.SyncCanonicalizer
import com.organizer.sync.SyncErrorCode
import com.organizer.sync.SyncOperation
import com.organizer.sync.SyncStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class SyncMutationProcessor(
    private val userRepository: UserRepository,
    private val recordRepository: UserDataRecordRepository,
    private val receiptRepository: SyncMutationReceiptRepository,
    private val changeLogRepository: SyncChangeLogRepository,
    private val sequenceRepository: UserSyncSequenceRepository,
    private val objectMapper: ObjectMapper,
    private val canonicalizer: SyncCanonicalizer,
) {

    @Transactional
    fun process(firebaseUid: String, mutation: SyncMutationRequest): SyncMutationResult {
        val user = userRepository.findByFirebaseUid(firebaseUid)
            ?: return rejected(mutation, SyncErrorCode.INVALID_REQUEST, "User not resolved")

        // Pessimistic lock on the per-user sequence row serializes all accepted
        // writes for this user until this transaction commits. This makes
        // receipt checks, version checks and sequence allocation atomic per user.
        val sequence = sequenceRepository.findForUpdate(user.id)
            ?: sequenceRepository.save(UserSyncSequenceEntity(userId = user.id, nextSequence = 1))

        val requestHash = canonicalizer.requestHash(
            mutationId = mutation.mutationId,
            entityType = mutation.entityType,
            syncId = mutation.syncId,
            operation = mutation.operation,
            baseVersion = mutation.baseVersion,
            clientUpdatedAt = mutation.clientUpdatedAt!!.toString(),
            payload = mutation.payload,
        )

        val existingReceipt = receiptRepository.findByUserAndMutationId(user, mutation.mutationId)
        if (existingReceipt != null) {
            if (existingReceipt.requestHash != requestHash) {
                return rejected(mutation, SyncErrorCode.MUTATION_ID_REUSE, "Mutation ID was used with a different request")
            }
            val original = objectMapper.readValue(existingReceipt.responseJson, SyncMutationResult::class.java)
            // DUPLICATE is only a replay marker. The stored outcome travels with
            // it so clients cannot mistake a replayed failure for an ack.
            return original.copy(
                status = SyncStatus.DUPLICATE,
                originalStatus = original.status,
            )
        }

        val result = applyMutation(user, mutation)
        val responseJson = objectMapper.writeValueAsString(result)
        receiptRepository.saveAndFlush(
            SyncMutationReceiptEntity(
                user = user,
                mutationId = mutation.mutationId,
                requestHash = requestHash,
                responseJson = responseJson,
            )
        )

        if (result.status == SyncStatus.APPLIED) {
            val sequenceValue = sequence.nextSequence
            sequence.nextSequence = sequenceValue + 1
            sequenceRepository.save(sequence)

            changeLogRepository.save(
                SyncChangeLogEntity(
                    user = user,
                    entityType = mutation.entityType,
                    syncId = mutation.syncId,
                    recordVersion = result.serverVersion ?: 0,
                    sequence = sequenceValue,
                    changedAt = result.serverUpdatedAt ?: Instant.now(),
                    deleted = result.deleted,
                )
            )
        }
        return result
    }

    private fun applyMutation(user: UserEntity, mutation: SyncMutationRequest): SyncMutationResult {
        val operation = SyncOperation.from(mutation.operation)
            ?: return rejected(mutation, SyncErrorCode.INVALID_OPERATION, "Unsupported operation")

        val existing = recordRepository.findByUserAndEntityTypeAndSyncId(user, mutation.entityType, mutation.syncId)
        val now = Instant.now()

        when (operation) {
            SyncOperation.UPSERT -> {
                if (existing == null) {
                    if (mutation.payload == null) {
                        return rejected(mutation, SyncErrorCode.PAYLOAD_REQUIRED, "UPSERT requires a payload")
                    }
                    val saved = recordRepository.saveAndFlush(newRecord(user, mutation, now, deleted = false))
                    return applied(mutation, saved, now)
                }
                if (existing.deletedAt != null) {
                    return rejected(
                        mutation,
                        SyncErrorCode.RECORD_DELETED,
                        "Deleted record cannot be updated with UPSERT; use RESTORE"
                    )
                }
                if (mutation.baseVersion != existing.version) {
                    return conflict(mutation, existing)
                }
                existing.payload = payloadJson(mutation.payload) ?: existing.payload
                existing.clientUpdatedAt = mutation.clientUpdatedAt
                existing.serverUpdatedAt = now
                existing.updatedAt = now
                existing.lastMutationId = mutation.mutationId
                existing.version = existing.version + 1
                val saved = recordRepository.saveAndFlush(existing)
                return applied(mutation, saved, now)
            }

            SyncOperation.DELETE -> {
                if (existing == null) {
                    if (mutation.payload == null) {
                        return rejected(mutation, SyncErrorCode.PAYLOAD_REQUIRED, "DELETE of a new record requires a payload")
                    }
                    val saved = recordRepository.saveAndFlush(newRecord(user, mutation, now, deleted = true))
                    return applied(mutation, saved, now)
                }
                if (mutation.baseVersion != existing.version) {
                    return conflict(mutation, existing)
                }
                existing.deletedAt = now
                existing.clientUpdatedAt = mutation.clientUpdatedAt
                existing.serverUpdatedAt = now
                existing.updatedAt = now
                existing.lastMutationId = mutation.mutationId
                existing.version = existing.version + 1
                val saved = recordRepository.saveAndFlush(existing)
                return applied(mutation, saved, now)
            }

            SyncOperation.RESTORE -> {
                if (mutation.payload == null) {
                    return rejected(mutation, SyncErrorCode.PAYLOAD_REQUIRED, "RESTORE requires a payload")
                }
                if (existing == null) {
                    return rejected(mutation, SyncErrorCode.RECORD_NOT_FOUND, "Cannot restore a record that does not exist")
                }
                if (existing.deletedAt == null) {
                    return rejected(mutation, SyncErrorCode.RECORD_NOT_DELETED, "Record is already active")
                }
                if (mutation.baseVersion != existing.version) {
                    return conflict(mutation, existing)
                }
                existing.deletedAt = null
                existing.payload = payloadJson(mutation.payload)
                existing.clientUpdatedAt = mutation.clientUpdatedAt
                existing.serverUpdatedAt = now
                existing.updatedAt = now
                existing.lastMutationId = mutation.mutationId
                existing.version = existing.version + 1
                val saved = recordRepository.saveAndFlush(existing)
                return applied(mutation, saved, now)
            }
        }
    }

    private fun newRecord(
        user: UserEntity,
        mutation: SyncMutationRequest,
        now: Instant,
        deleted: Boolean,
    ) = UserDataRecordEntity(
        id = UUID.randomUUID(),
        user = user,
        entityType = mutation.entityType,
        clientId = mutation.syncId,
        syncId = mutation.syncId,
        payload = payloadJson(mutation.payload),
        clientUpdatedAt = mutation.clientUpdatedAt,
        serverUpdatedAt = now,
        createdAt = now,
        updatedAt = now,
        deletedAt = if (deleted) now else null,
        version = 1,
        lastMutationId = mutation.mutationId,
    )

    private fun applied(mutation: SyncMutationRequest, record: UserDataRecordEntity, serverTime: Instant) =
        SyncMutationResult(
            mutationId = mutation.mutationId,
            syncId = mutation.syncId,
            status = SyncStatus.APPLIED,
            serverVersion = record.version,
            serverUpdatedAt = serverTime,
            deleted = record.deletedAt != null,
            serverPayload = null,
        )

    private fun conflict(mutation: SyncMutationRequest, record: UserDataRecordEntity): SyncMutationResult {
        val serverPayload = payloadMap(record.payload)
        return SyncMutationResult(
            mutationId = mutation.mutationId,
            syncId = mutation.syncId,
            status = SyncStatus.CONFLICT,
            errorCode = SyncErrorCode.VERSION_CONFLICT,
            errorMessage = "baseVersion does not match current server version",
            serverVersion = record.version,
            serverUpdatedAt = record.serverUpdatedAt,
            deleted = record.deletedAt != null,
            serverPayload = serverPayload,
        )
    }

    private fun payloadJson(payload: Map<String, Any>?): String? =
        payload?.let { objectMapper.writeValueAsString(it) }

    private fun payloadMap(json: String?): Map<String, Any>? = json?.let {
        runCatching {
            objectMapper.convertValue(objectMapper.readTree(it), object : TypeReference<Map<String, Any>>() {})
        }.getOrNull()
    }

    private fun rejected(mutation: SyncMutationRequest, code: String, message: String) = SyncMutationResult(
        mutationId = mutation.mutationId,
        syncId = mutation.syncId,
        status = SyncStatus.REJECTED,
        errorCode = code,
        errorMessage = message,
    )
}