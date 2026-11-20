package com.organizer.repository

import com.organizer.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findByFirebaseUid(firebaseUid: String): UserEntity?
}