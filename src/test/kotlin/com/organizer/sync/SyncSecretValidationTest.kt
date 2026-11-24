package com.organizer.sync

import com.fasterxml.jackson.databind.ObjectMapper
import com.organizer.repository.SyncChangeLogRepository
import com.organizer.repository.UserDataRecordRepository
import com.organizer.service.SyncMutationProcessor
import com.organizer.service.SyncService
import com.organizer.service.UserService
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class SyncSecretValidationTest {

    private fun build(secret: String): SyncService = SyncService(
        userService = mock(UserService::class.java),
        mutationProcessor = mock(SyncMutationProcessor::class.java),
        recordRepository = mock(UserDataRecordRepository::class.java),
        changeLogRepository = mock(SyncChangeLogRepository::class.java),
        objectMapper = mock(ObjectMapper::class.java),
        canonicalizer = mock(SyncCanonicalizer::class.java),
        cursorSecret = secret,
    )

    @Test
    fun `blank cursor secret is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { build("") }
    }

    @Test
    fun `short cursor secret is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { build("too-short") }
    }

    @Test
    fun `development cursor secret is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { build("exercise-organizer-sync-cursor") }
    }

    @Test
    fun `strong cursor secret is accepted`() {
        assertDoesNotThrow { build("a-strong-production-secret-with-more-than-32-chars-1234") }
    }
}