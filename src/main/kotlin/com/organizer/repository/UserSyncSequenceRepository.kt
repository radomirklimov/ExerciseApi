package com.organizer.repository

import com.organizer.entity.UserSyncSequenceEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserSyncSequenceRepository : JpaRepository<UserSyncSequenceEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from UserSyncSequenceEntity s where s.userId = :userId")
    fun findForUpdate(@Param("userId") userId: UUID): UserSyncSequenceEntity?
}