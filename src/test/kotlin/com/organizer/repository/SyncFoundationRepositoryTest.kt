package com.organizer.repository

import com.organizer.entity.SyncChangeLogEntity
import com.organizer.entity.SyncMutationReceiptEntity
import com.organizer.entity.UserEntity
import com.organizer.entity.UserSyncSequenceEntity
import com.organizer.sync.SyncEntityType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException

@DataJpaTest
class SyncFoundationRepositoryTest(
    @Autowired val userRepository: UserRepository,
    @Autowired val receiptRepository: SyncMutationReceiptRepository,
    @Autowired val changeLogRepository: SyncChangeLogRepository,
    @Autowired val sequenceRepository: UserSyncSequenceRepository,
) {

    private fun user(uid: String) = userRepository.save(UserEntity(firebaseUid = uid))

    @Test
    fun `mutation receipt is unique per user and mutation id`() {
        val user = user("uid-a")
        receiptRepository.saveAndFlush(
            SyncMutationReceiptEntity(user = user, mutationId = "m1", requestHash = "h1", responseJson = "{}")
        )

        val duplicate = SyncMutationReceiptEntity(user = user, mutationId = "m1", requestHash = "h2", responseJson = "{}")
        assertThrows<DataIntegrityViolationException> { receiptRepository.saveAndFlush(duplicate) }
    }

    @Test
    fun `change log sequence is unique and ordered`() {
        val user = user("uid-c")
        changeLogRepository.saveAll(
            listOf(
                SyncChangeLogEntity(user = user, entityType = "WORKOUT", syncId = "s1", recordVersion = 2, sequence = 2),
                SyncChangeLogEntity(user = user, entityType = "WORKOUT", syncId = "s1", recordVersion = 1, sequence = 1),
            )
        )
        changeLogRepository.flush()

        assertThat(changeLogRepository.findAllByUserOrderBySequenceAsc(user).map { it.sequence })
            .containsExactly(1L, 2L)
        assertThat(changeLogRepository.findFirstByUserOrderBySequenceDesc(user)?.sequence).isEqualTo(2L)

        assertThrows<DataIntegrityViolationException> {
            changeLogRepository.saveAndFlush(
                SyncChangeLogEntity(user = user, entityType = "WORKOUT", syncId = "s2", recordVersion = 1, sequence = 1)
            )
        }
    }

    @Test
    fun `per user sync sequence can be advanced`() {
        val user = user("uid-d")
        val sequence = sequenceRepository.save(UserSyncSequenceEntity(userId = user.id, nextSequence = 1))
        sequence.nextSequence = 2
        sequenceRepository.saveAndFlush(sequence)

        assertThat(sequenceRepository.findById(user.id).orElseThrow().nextSequence).isEqualTo(2L)
    }

    @Test
    fun `entity type allowlist matches android entities`() {
        assertThat(SyncEntityType.ALL).contains("WORKOUT", "CUSTOM_EXERCISE", "FAVORITE_EXERCISE")
        assertThat(SyncEntityType.from("UNKNOWN")).isNull()
    }
}