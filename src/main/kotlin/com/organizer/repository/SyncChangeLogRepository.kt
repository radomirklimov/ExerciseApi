package com.organizer.repository

import com.organizer.entity.SyncChangeLogEntity
import com.organizer.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SyncChangeLogRepository : JpaRepository<SyncChangeLogEntity, UUID> {
    fun findAllByUserOrderBySequenceAsc(user: UserEntity): List<SyncChangeLogEntity>
    fun findFirstByUserOrderBySequenceDesc(user: UserEntity): SyncChangeLogEntity?
    fun findByUserAndSequenceGreaterThanAndSequenceLessThanEqualOrderBySequenceAsc(
        user: UserEntity,
        fromSequence: Long,
        toSequence: Long,
    ): List<SyncChangeLogEntity>
}