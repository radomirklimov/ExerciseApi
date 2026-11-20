package com.organizer.dto

import java.time.Instant
import java.util.UUID

data class UserResponse(
    val id: UUID,
    val firebaseUid: String? = null,
    val email: String?,
    val displayName: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val lastLoginAt: Instant,
)