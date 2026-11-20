package com.organizer.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "user_data_record",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "entity_type", "client_id"])
    ]
)
class UserDataRecordEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @Column(name = "entity_type", nullable = false, length = 64)
    val entityType: String,

    @Column(name = "client_id", nullable = false, length = 128)
    val clientId: String,

    @Column(name = "payload", columnDefinition = "TEXT")
    var payload: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now(),

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,

    @Column(name = "version", nullable = false)
    var version: Long = 0,
)