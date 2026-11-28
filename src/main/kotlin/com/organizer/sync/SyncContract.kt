package com.organizer.sync

enum class SyncOperation(val wireName: String) {
    UPSERT("UPSERT"),
    DELETE("DELETE"),
    RESTORE("RESTORE"),
    ;

    companion object {
        private val byName = entries.associateBy { it.wireName }
        fun from(value: String): SyncOperation? = byName[value]
    }
}

object SyncLimits {
    const val MAX_BATCH_SIZE = 100
    const val DEFAULT_PAGE_SIZE = 100
    const val MAX_PAGE_SIZE = 500
    const val MAX_PAYLOAD_BYTES = 256 * 1024
    const val CURSOR_TTL_SECONDS = 24L * 60L * 60L
}

object SyncStatus {
    const val APPLIED = "APPLIED"
    const val CONFLICT = "CONFLICT"
    const val DUPLICATE = "DUPLICATE"
    const val REJECTED = "REJECTED"
}

object SyncErrorCode {
    const val INVALID_REQUEST = "INVALID_REQUEST"
    const val CURSOR_EXPIRED = "CURSOR_EXPIRED"
    const val INVALID_ENTITY_TYPE = "INVALID_ENTITY_TYPE"
    const val INVALID_SYNC_ID = "INVALID_SYNC_ID"
    const val INVALID_MUTATION_ID = "INVALID_MUTATION_ID"
    const val INVALID_OPERATION = "INVALID_OPERATION"
    const val INVALID_TIMESTAMP = "INVALID_TIMESTAMP"
    const val INVALID_VERSION = "INVALID_VERSION"
    const val PAYLOAD_REQUIRED = "PAYLOAD_REQUIRED"
    const val PAYLOAD_TOO_LARGE = "PAYLOAD_TOO_LARGE"
    const val MUTATION_ID_REUSE = "MUTATION_ID_REUSE"
    const val RECORD_DELETED = "RECORD_DELETED"
    const val RECORD_NOT_FOUND = "RECORD_NOT_FOUND"
    const val RECORD_NOT_DELETED = "RECORD_NOT_DELETED"
    const val VERSION_CONFLICT = "VERSION_CONFLICT"
}
