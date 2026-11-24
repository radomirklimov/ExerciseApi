package com.organizer.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "user_sync_sequence")
class UserSyncSequenceEntity(

    @Id
    val userId: UUID,

    @Column(name = "next_sequence", nullable = false)
    var nextSequence: Long = 1,
)