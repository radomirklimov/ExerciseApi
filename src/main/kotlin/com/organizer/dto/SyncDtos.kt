package com.organizer.dto

import java.time.Instant

data class SyncMutationRequest(
    val mutationId: String,
    val entityType: String,
    val syncId: String,
    val operation: String,
    val baseVersion: Long,
    val clientUpdatedAt: Instant? = null,
    val payload: Map<String, Any>? = null,
)

data class SyncPushRequest(
    val mutations: List<SyncMutationRequest> = emptyList(),
)

data class SyncMutationResult(
    val mutationId: String,
    val syncId: String,
    val status: String,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val serverVersion: Long? = null,
    val serverUpdatedAt: Instant? = null,
    val deleted: Boolean = false,
    val serverPayload: Map<String, Any>? = null,
)

data class SyncPushResponse(
    val results: List<SyncMutationResult>,
    val serverTime: Instant,
)

data class SyncChangeRecord(
    val entityType: String,
    val syncId: String,
    val serverVersion: Long,
    val serverUpdatedAt: Instant,
    val deleted: Boolean,
    val payload: Map<String, Any>?,
)

data class SyncChangesResponse(
    val records: List<SyncChangeRecord>,
    val nextCursor: String,
    val hasMore: Boolean,
    val serverTime: Instant,
)

data class ApiError(
    val error: String,
    val message: String,
)
