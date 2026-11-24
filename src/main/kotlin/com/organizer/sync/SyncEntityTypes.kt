package com.organizer.sync

/** Allowlist of user-owned entity types that can be synchronized. */
enum class SyncEntityType(val wireName: String) {
    WORKOUT("WORKOUT"),
    CUSTOM_EXERCISE("CUSTOM_EXERCISE"),
    WORKOUT_EXERCISE("WORKOUT_EXERCISE"),
    EXERCISE_PROGRESS("EXERCISE_PROGRESS"),
    PLANNED_SET("PLANNED_SET"),
    WORKOUT_COMPLETION("WORKOUT_COMPLETION"),
    SCHEDULED_WORKOUT("SCHEDULED_WORKOUT"),
    FAVORITE_EXERCISE("FAVORITE_EXERCISE"),
    ;

    companion object {
        private val byWireName = entries.associateBy { it.wireName }

        fun from(wireName: String): SyncEntityType? = byWireName[wireName]

        val ALL: Set<String> = byWireName.keys
    }
}