package com.organizer.repository

import com.organizer.entity.SyncMutationReceiptEntity
import com.organizer.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface SyncMutationReceiptRepository : JpaRepository<SyncMutationReceiptEntity, UUID> {
    fun findByUserAndMutationId(user: UserEntity, mutationId: String): SyncMutationReceiptEntity?
}