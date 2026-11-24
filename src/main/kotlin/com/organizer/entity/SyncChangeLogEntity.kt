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
    name = "sync_change_log",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "sequence"])]
)
class SyncChangeLogEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @Column(name = "entity_type", nullable = false, length = 64)
    val entityType: String,

    @Column(name = "sync_id", nullable = false, length = 128)
    val syncId: String,

    @Column(name = "record_version", nullable = false)
    val recordVersion: Long,

    @Column(name = "sequence", nullable = false)
    val sequence: Long,

    @Column(name = "changed_at", nullable = false)
    val changedAt: Instant = Instant.now(),

    @Column(name = "deleted", nullable = false)
    val deleted: Boolean = false,
)