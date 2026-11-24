package com.organizer.repository

import com.organizer.entity.UserDataRecordEntity
import com.organizer.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserDataRecordRepository : JpaRepository<UserDataRecordEntity, UUID> {
    fun findByUserAndEntityTypeAndClientId(user: UserEntity, entityType: String, clientId: String): UserDataRecordEntity?
    fun findByUserAndEntityTypeAndSyncId(user: UserEntity, entityType: String, syncId: String): UserDataRecordEntity?
}