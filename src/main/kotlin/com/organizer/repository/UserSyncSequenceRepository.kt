package com.organizer.repository

import com.organizer.entity.UserSyncSequenceEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserSyncSequenceRepository : JpaRepository<UserSyncSequenceEntity, UUID>