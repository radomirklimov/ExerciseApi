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
    name = "sync_mutation_receipt",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "mutation_id"])]
)
class SyncMutationReceiptEntity(

    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @Column(name = "mutation_id", nullable = false, length = 128)
    val mutationId: String,

    @Column(name = "request_hash", nullable = false, length = 128)
    val requestHash: String,

    @Column(name = "response_json", columnDefinition = "TEXT", nullable = false)
    val responseJson: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),
)