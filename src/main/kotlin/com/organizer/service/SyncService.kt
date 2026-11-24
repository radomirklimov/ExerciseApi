package com.organizer.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.organizer.dto.SyncChangesResponse
import com.organizer.dto.SyncPushRequest
import com.organizer.dto.SyncPushResponse
import com.organizer.dto.SyncRecordRequest
import com.organizer.dto.SyncRecordResponse
import com.organizer.entity.UserDataRecordEntity
import com.organizer.entity.UserEntity
import com.organizer.repository.UserDataRecordRepository
import com.organizer.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class SyncService(
    private val userRepository: UserRepository,
    private val recordRepository: UserDataRecordRepository,
    private val objectMapper: ObjectMapper,
) {

    @Transactional(readOnly = true)
    fun changes(firebaseUid: String, since: Instant?): SyncChangesResponse {
        val user = userRepository.findByFirebaseUid(firebaseUid)
            ?: throw IllegalArgumentException("User not found")
        val cutoff = since ?: Instant.EPOCH
        val records = recordRepository.findAllByUserAndUpdatedAtAfter(user, cutoff)
            .map { it.toResponse(objectMapper) }
        return SyncChangesResponse(records = records, serverTime = Instant.now())
    }

    @Transactional
    fun push(firebaseUid: String, request: SyncPushRequest): SyncPushResponse {
        val user = userRepository.findByFirebaseUid(firebaseUid)
            ?: throw IllegalArgumentException("User not found")
        val responses = mutableListOf<SyncRecordResponse>()
        for (incoming in request.records) {
            val existing = recordRepository.findByUserAndEntityTypeAndClientId(
                user, incoming.entityType, incoming.clientId
            )
            val now = Instant.now()
            val incomingUpdated = incoming.updatedAt ?: now
            if (existing == null) {
                val entity = UserDataRecordEntity(
                    id = UUID.randomUUID(),
                    user = user,
                    entityType = incoming.entityType,
                    clientId = incoming.clientId,
                    syncId = incoming.clientId,
                    clientUpdatedAt = incomingUpdated,
                    serverUpdatedAt = now,
                    payload = incoming.payload?.toString(),
                    createdAt = now,
                    updatedAt = incomingUpdated,
                    deletedAt = if (incoming.deleted) incomingUpdated else null,
                    version = 1,
                )
                val saved = recordRepository.save(entity)
                responses.add(saved.toResponse(objectMapper))
            } else {
                // Last-write-wins on updatedAt, but never resurrect a deleted record silently.
                if (incomingUpdated.isAfter(existing.updatedAt) || incomingUpdated == existing.updatedAt) {
                    existing.payload = incoming.payload?.toString() ?: existing.payload
                    existing.updatedAt = incomingUpdated
                    existing.clientUpdatedAt = incomingUpdated
                    existing.serverUpdatedAt = now
                    existing.deletedAt = if (incoming.deleted) incomingUpdated else existing.deletedAt
                    existing.version = existing.version + 1
                    val saved = recordRepository.save(existing)
                    responses.add(saved.toResponse(objectMapper))
                } else {
                    responses.add(existing.toResponse(objectMapper))
                }
            }
        }
        return SyncPushResponse(records = responses, serverTime = Instant.now())
    }
}

private fun UserDataRecordEntity.toResponse(objectMapper: ObjectMapper): SyncRecordResponse {
    val node = payload?.let { runCatching { objectMapper.readTree(it) }.getOrNull() }
    return SyncRecordResponse(
        entityType = entityType,
        clientId = clientId,
        payload = node,
        updatedAt = updatedAt,
        deleted = deletedAt != null,
        version = version,
    )
}