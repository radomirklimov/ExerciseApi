package com.organizer.controller

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.firebase.auth.FirebaseToken
import com.organizer.dto.SyncChangesResponse
import com.organizer.dto.SyncMutationRequest
import com.organizer.dto.SyncPushRequest
import com.organizer.dto.SyncPushResponse
import com.organizer.repository.SyncChangeLogRepository
import com.organizer.repository.SyncMutationReceiptRepository
import com.organizer.repository.UserDataRecordRepository
import com.organizer.repository.UserRepository
import com.organizer.repository.UserSyncSequenceRepository
import com.organizer.security.FirebaseTokenVerifier
import com.organizer.sync.SyncLimits
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest
@AutoConfigureMockMvc
class SyncApiIntegrationTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper,
    @Autowired val recordRepository: UserDataRecordRepository,
    @Autowired val changeLogRepository: SyncChangeLogRepository,
    @Autowired val receiptRepository: SyncMutationReceiptRepository,
    @Autowired val sequenceRepository: UserSyncSequenceRepository,
    @Autowired val userRepository: UserRepository,
) {

    @BeforeEach
    fun cleanDatabase() {
        changeLogRepository.deleteAll()
        receiptRepository.deleteAll()
        recordRepository.deleteAll()
        sequenceRepository.deleteAll()
        userRepository.deleteAll()
    }

    @TestConfiguration
    class FakeVerifierConfig {
        @Bean
        @Primary
        fun fakeVerifier(): FirebaseTokenVerifier = object : FirebaseTokenVerifier {
            override fun verify(idToken: String): FirebaseToken {
                val mocked = Mockito.mock(FirebaseToken::class.java)
                `when`(mocked.uid).thenReturn(idToken)
                `when`(mocked.email).thenReturn("$idToken@example.com")
                `when`(mocked.name).thenReturn("Test $idToken")
                return mocked
            }
        }
    }

    private fun auth(uid: String) = "Bearer $uid"

    private fun mutation(
        mutationId: String = UUID.randomUUID().toString(),
        syncId: String = UUID.randomUUID().toString(),
        entityType: String = "WORKOUT",
        operation: String = "UPSERT",
        baseVersion: Long = 0,
        clientUpdatedAt: Instant = Instant.now(),
        payload: Map<String, Any>? = mapOf("name" to "Workout A"),
    ): SyncMutationRequest = SyncMutationRequest(
        mutationId = mutationId,
        entityType = entityType,
        syncId = syncId,
        operation = operation,
        baseVersion = baseVersion,
        clientUpdatedAt = clientUpdatedAt,
        payload = payload,
    )

    private fun push(uid: String, vararg mutations: SyncMutationRequest): SyncPushResponse {
        val body = objectMapper.writeValueAsString(SyncPushRequest(mutations.toList()))
        val result = mockMvc.post("/api/sync/push") {
            header("Authorization", auth(uid))
            contentType = MediaType.APPLICATION_JSON
            content = body
        }.andExpect { status { isOk() } }.andReturn()
        return objectMapper.readValue(result.response.contentAsString, SyncPushResponse::class.java)
    }

    private fun pull(uid: String, cursor: String? = null, limit: Int = 100): SyncChangesResponse {
        val result = mockMvc.get("/api/sync/changes") {
            header("Authorization", auth(uid))
            param("limit", limit.toString())
            if (cursor != null) param("cursor", cursor)
        }.andExpect { status { isOk() } }.andReturn()
        return objectMapper.readValue(result.response.contentAsString, SyncChangesResponse::class.java)
    }

    @Test
    fun `unauthenticated requests are rejected`() {
        mockMvc.post("/api/sync/push") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(SyncPushRequest(listOf(mutation())))
        }.andExpect { status { isUnauthorized() } }

        mockMvc.get("/api/sync/changes").andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `new record is created and appears in pull`() {
        val m = mutation()
        val response = push("uid-a", m)
        assertEquals("APPLIED", response.results.single().status)
        assertEquals(1L, response.results.single().serverVersion)

        val changes = pull("uid-a")
        val record = changes.records.single { it.syncId == m.syncId }
        assertEquals("WORKOUT", record.entityType)
        assertEquals(1L, record.serverVersion)
        assertFalse(record.deleted)
        assertEquals("Workout A", record.payload?.get("name"))
    }

    @Test
    fun `duplicate mutation returns original response without new version or change log`() {
        val m = mutation()
        val first = push("uid-a", m).results.single()
        val before = changeLogRepository.findAllByUserOrderBySequenceAsc(userRepository.findByFirebaseUid("uid-a")!!).size

        val duplicate = push("uid-a", m).results.single()
        assertEquals("DUPLICATE", duplicate.status)
        assertEquals(first.serverVersion, duplicate.serverVersion)
        assertEquals(before, changeLogRepository.findAllByUserOrderBySequenceAsc(userRepository.findByFirebaseUid("uid-a")!!).size)

        val record = recordRepository.findByUserAndEntityTypeAndSyncId(
            userRepository.findByFirebaseUid("uid-a")!!, "WORKOUT", m.syncId
        )!!
        assertEquals(1L, record.version)
    }

    @Test
    fun `same mutation id with different request is rejected`() {
        val m = mutation()
        push("uid-a", m)
        val reused = mutation(mutationId = m.mutationId, syncId = m.syncId, payload = mapOf("name" to "Other"))
        val result = push("uid-a", reused).results.single()
        assertEquals("REJECTED", result.status)
        assertEquals("MUTATION_ID_REUSE", result.errorCode)
    }

    @Test
    fun `stale version returns conflict with current server payload`() {
        val syncId = UUID.randomUUID().toString()
        push("uid-a", mutation(syncId = syncId, payload = mapOf("name" to "First")))
        push("uid-a", mutation(syncId = syncId, baseVersion = 1, payload = mapOf("name" to "Second")))

        val conflict = push("uid-a", mutation(syncId = syncId, baseVersion = 0, payload = mapOf("name" to "Stale"))).results.single()
        assertEquals("CONFLICT", conflict.status)
        assertEquals("VERSION_CONFLICT", conflict.errorCode)
        assertEquals(2L, conflict.serverVersion)
        assertEquals("Second", conflict.serverPayload?.get("name"))
    }

    @Test
    fun `duplicate conflict keeps original error details`() {
        val syncId = UUID.randomUUID().toString()
        push("uid-a", mutation(syncId = syncId))
        push("uid-a", mutation(syncId = syncId, baseVersion = 1))
        val stale = mutation(syncId = syncId, baseVersion = 0)
        val first = push("uid-a", stale).results.single()
        val duplicate = push("uid-a", stale).results.single()

        assertEquals("CONFLICT", first.status)
        assertEquals("DUPLICATE", duplicate.status)
        assertEquals(first.errorCode, duplicate.errorCode)
        assertEquals(first.serverVersion, duplicate.serverVersion)
        assertEquals(first.serverPayload, duplicate.serverPayload)
    }

    @Test
    fun `delete tombstone and explicit restore work`() {
        val syncId = UUID.randomUUID().toString()
        push("uid-a", mutation(syncId = syncId))
        val deleted = push("uid-a", mutation(syncId = syncId, operation = "DELETE", baseVersion = 1, payload = null)).results.single()
        assertEquals("APPLIED", deleted.status)
        assertTrue(deleted.deleted)

        val tombstone = pull("uid-a").records.last { it.syncId == syncId }
        assertTrue(tombstone.deleted)

        val staleUpsert = push("uid-a", mutation(syncId = syncId, baseVersion = 1)).results.single()
        assertEquals("REJECTED", staleUpsert.status)
        assertEquals("RECORD_DELETED", staleUpsert.errorCode)

        val restored = push("uid-a", mutation(syncId = syncId, operation = "RESTORE", baseVersion = 2)).results.single()
        assertEquals("APPLIED", restored.status)
        assertFalse(restored.deleted)
        assertEquals(3L, restored.serverVersion)
    }

    @Test
    fun `pagination is deterministic and later writes are not skipped`() {
        repeat(3) { push("uid-a", mutation(entityType = "WORKOUT")) }

        var cursor: String? = null
        val seen = mutableSetOf<String>()
        repeat(3) {
            val page = pull("uid-a", cursor, 1)
            assertEquals(1, page.records.size)
            assertTrue(seen.add(page.records.single().syncId))
            cursor = page.nextCursor
        }

        val newSyncId = UUID.randomUUID().toString()
        push("uid-a", mutation(syncId = newSyncId))
        val next = pull("uid-a", cursor)
        assertTrue(next.records.any { it.syncId == newSyncId })
    }

    @Test
    fun `invalid and foreign cursors are rejected`() {
        push("uid-a", mutation())
        val foreignCursor = pull("uid-a").nextCursor

        mockMvc.get("/api/sync/changes") {
            header("Authorization", auth("uid-b"))
            param("cursor", foreignCursor)
        }.andExpect { status { isBadRequest() } }

        mockMvc.get("/api/sync/changes") {
            header("Authorization", auth("uid-a"))
            param("cursor", "not-a-cursor")
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `users cannot see each other's records`() {
        val m = mutation()
        push("uid-a", m)

        val other = pull("uid-b")
        assertTrue(other.records.none { it.syncId == m.syncId })
    }

    @Test
    fun `concurrent duplicate mutations apply once`() {
        val m = mutation()
        val executor = Executors.newFixedThreadPool(2)
        val start = CountDownLatch(1)
        try {
            val futures = (1..2).map {
                executor.submit<SyncPushResponse> {
                    start.await()
                    push("uid-a", m)
                }
            }
            start.countDown()
            val results = futures.map { it.get(30, TimeUnit.SECONDS) }
            val statuses = results.flatMap { it.results.map { r -> r.status } }
            assertEquals(1, statuses.count { it == "APPLIED" })
            assertEquals(1, statuses.count { it == "DUPLICATE" })

            val user = userRepository.findByFirebaseUid("uid-a")!!
            val record = recordRepository.findByUserAndEntityTypeAndSyncId(user, "WORKOUT", m.syncId)!!
            assertEquals(1L, record.version)
            assertEquals(1, changeLogRepository.findAllByUserOrderBySequenceAsc(user).count { it.syncId == m.syncId })
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun `request supplied identifiers cannot override authenticated identity`() {
        val m = mutation()
        val body = objectMapper.createObjectNode().apply {
            putArray("mutations").add(objectMapper.valueToTree<JsonNode>(m).apply {
                (this as com.fasterxml.jackson.databind.node.ObjectNode).put("userId", "uid-b")
                put("firebaseUid", "uid-b")
            })
        }
        mockMvc.post("/api/sync/push") {
            header("Authorization", auth("uid-a"))
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect { status { isOk() } }

        assertTrue(pull("uid-b").records.none { it.syncId == m.syncId })
        assertTrue(pull("uid-a").records.any { it.syncId == m.syncId })
    }
}