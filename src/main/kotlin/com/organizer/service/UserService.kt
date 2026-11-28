package com.organizer.service

import com.organizer.entity.UserEntity
import com.organizer.entity.UserSyncSequenceEntity
import com.organizer.repository.UserRepository
import com.organizer.repository.UserSyncSequenceRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.Instant
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userSyncSequenceRepository: UserSyncSequenceRepository,
    transactionManager: PlatformTransactionManager,
) {
    private val requiresNew = TransactionTemplate(transactionManager).apply {
        propagationBehavior = org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW
    }

    fun getOrCreate(firebaseUid: String, email: String?, displayName: String?): UserEntity {
        userRepository.findByFirebaseUid(firebaseUid)?.let { existing ->
            return requiresNew.execute {
                if (email != null && email != existing.email) existing.email = email
                if (displayName != null && displayName != existing.displayName) existing.displayName = displayName
                existing.lastLoginAt = Instant.now()
                existing.updatedAt = Instant.now()
                userRepository.save(existing)
            } ?: existing
        }

        return try {
            requiresNew.execute { createUser(firebaseUid, email, displayName) }
                ?: throw IllegalStateException("User creation returned null")
        } catch (ex: DataIntegrityViolationException) {
            // Another request created the same Firebase user first.
            requiresNew.execute { userRepository.findByFirebaseUid(firebaseUid) }
                ?: throw ex
        }
    }

    private fun createUser(firebaseUid: String, email: String?, displayName: String?): UserEntity {
        val now = Instant.now()
        val saved = userRepository.saveAndFlush(
            UserEntity(
                id = UUID.randomUUID(),
                firebaseUid = firebaseUid,
                email = email,
                displayName = displayName,
                createdAt = now,
                updatedAt = now,
                lastLoginAt = now,
            )
        )
        if (!userSyncSequenceRepository.existsById(saved.id)) {
            userSyncSequenceRepository.save(UserSyncSequenceEntity(userId = saved.id))
        }
        return saved
    }

    fun findById(id: UUID): UserEntity? = userRepository.findById(id).orElse(null)

    fun requireUser(firebaseUid: String): UserEntity =
        userRepository.findByFirebaseUid(firebaseUid)
            ?: throw IllegalArgumentException("User not found")
}