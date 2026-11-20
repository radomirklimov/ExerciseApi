package com.organizer.dto

import com.fasterxml.jackson.databind.JsonNode
import java.time.Instant

data class SyncRecordRequest(
    val entityType: String,
    val clientId: String,
    val payload: JsonNode? = null,
    val updatedAt: Instant? = null,
    val deleted: Boolean = false,
)

data class SyncRecordResponse(
    val entityType: String,
    val clientId: String,
    val payload: JsonNode? = null,
    val updatedAt: Instant,
    val deleted: Boolean,
    val version: Long,
)

data class SyncPushRequest(
    val records: List<SyncRecordRequest> = emptyList(),
)

data class SyncPushResponse(
    val records: List<SyncRecordResponse>,
    val serverTime: Instant,
)

data class SyncChangesResponse(
    val records: List<SyncRecordResponse>,
    val serverTime: Instant,
)