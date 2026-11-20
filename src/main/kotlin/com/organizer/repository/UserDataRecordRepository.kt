package com.organizer.repository

import com.organizer.entity.UserDataRecordEntity
import com.organizer.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface UserDataRecordRepository : JpaRepository<UserDataRecordEntity, UUID> {
    fun findAllByUserAndUpdatedAtAfter(user: UserEntity, since: Instant): List<UserDataRecordEntity>
    fun findByUserAndEntityTypeAndClientId(user: UserEntity, entityType: String, clientId: String): UserDataRecordEntity?
}