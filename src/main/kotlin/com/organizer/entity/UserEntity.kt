package com.organizer.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "app_user")
class UserEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "firebase_uid", nullable = false, unique = true, length = 128)
    val firebaseUid: String,

    @Column(nullable = true)
    var email: String? = null,

    @Column(name = "display_name", nullable = true)
    var displayName: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @Column(name = "last_login_at", nullable = false)
    var lastLoginAt: Instant = Instant.now(),
)