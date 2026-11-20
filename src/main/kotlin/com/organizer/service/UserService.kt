package com.organizer.service

import com.organizer.entity.UserEntity
import com.organizer.repository.UserRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
) {

    @Transactional
    fun getOrCreate(firebaseUid: String, email: String?, displayName: String?): UserEntity {
        val existing = userRepository.findByFirebaseUid(firebaseUid)
        if (existing != null) {
            var changed = false
            if (email != null && email != existing.email) {
                existing.email = email
                changed = true
            }
            if (displayName != null && displayName != existing.displayName) {
                existing.displayName = displayName
                changed = true
            }
            existing.lastLoginAt = Instant.now()
            existing.updatedAt = Instant.now()
            return userRepository.save(existing)
        }

        val now = Instant.now()
        val user = UserEntity(
            id = UUID.randomUUID(),
            firebaseUid = firebaseUid,
            email = email,
            displayName = displayName,
            createdAt = now,
            updatedAt = now,
            lastLoginAt = now,
        )
        return try {
            userRepository.save(user)
        } catch (ex: DataIntegrityViolationException) {
            // Concurrent first login: retrieve the row created by the other transaction.
            userRepository.findByFirebaseUid(firebaseUid)
                ?: throw ex
        }
    }

    @Transactional(readOnly = true)
    fun findById(id: UUID): UserEntity? = userRepository.findById(id).orElse(null)
}