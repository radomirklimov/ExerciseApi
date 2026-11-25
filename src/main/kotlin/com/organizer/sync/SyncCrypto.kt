package com.organizer.sync

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/** Canonical JSON helpers used for deterministic request hashing. */
class SyncCanonicalizer(private val mapper: ObjectMapper) {
    fun canonicalize(value: Any?): String {
        if (value == null) return "null"
        val node = if (value is JsonNode) value else mapper.valueToTree(value)
        return mapper.writeValueAsString(sort(node))
    }

    private fun sort(node: JsonNode): JsonNode = when {
        node.isObject -> {
            val sorted = mapper.createObjectNode()
            node.fieldNames().asSequence().sorted().forEach { field ->
                sorted.set<JsonNode>(field, sort(node.get(field)))
            }
            sorted
        }
        node.isArray -> {
            val sorted = mapper.createArrayNode()
            node.forEach { sorted.add(sort(it)) }
            sorted
        }
        else -> node.deepCopy<JsonNode>()
    }

    fun requestHash(
        mutationId: String,
        entityType: String,
        syncId: String,
        operation: String,
        baseVersion: Long,
        clientUpdatedAt: String,
        payload: Any?,
    ): String {
        val node = mapper.createObjectNode().apply {
            put("mutationId", mutationId)
            put("entityType", entityType)
            put("syncId", syncId)
            put("operation", operation)
            put("baseVersion", baseVersion)
            put("clientUpdatedAt", clientUpdatedAt)
            set<JsonNode>("payload", if (payload == null) null else mapper.valueToTree(payload))
        }
        val bytes = canonicalize(node).toByteArray(StandardCharsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }
}

/** Opaque, user-bound, expiring cursor for incremental sync pagination. */
class SyncCursorCodec(secret: String) {
    private val key = SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")

    fun encode(cursor: SyncCursor): String {
        val payload = listOf(
            VERSION,
            cursor.userScope,
            cursor.lastSequence.toString(),
            cursor.highWaterMark?.toString() ?: "-",
            cursor.expiresAtEpochSecond.toString(),
        ).joinToString("|")
        val mac = hmac(payload)
        return "${b64(payload.toByteArray(StandardCharsets.UTF_8))}.${b64(mac)}"
    }

    fun decodeAndVerify(value: String, expectedUserScope: String): CursorDecodeResult {
        val parts = value.split(".")
        if (parts.size != 2) return CursorDecodeResult.Invalid
        val payload = runCatching { String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8) }
            .getOrNull() ?: return CursorDecodeResult.Invalid
        val suppliedMac = runCatching { Base64.getUrlDecoder().decode(parts[1]) }
            .getOrNull() ?: return CursorDecodeResult.Invalid
        if (!MessageDigest.isEqual(hmac(payload), suppliedMac)) return CursorDecodeResult.Invalid

        val fields = payload.split("|")
        if (fields.size != 5 || fields[0] != VERSION) return CursorDecodeResult.Invalid
        if (fields[1] != expectedUserScope) return CursorDecodeResult.Invalid

        val lastSequence = fields[2].toLongOrNull() ?: return CursorDecodeResult.Invalid
        val highWaterMark = if (fields[3] == "-") null else fields[3].toLongOrNull() ?: return CursorDecodeResult.Invalid
        val expiresAt = fields[4].toLongOrNull() ?: return CursorDecodeResult.Invalid

        // An internally inconsistent cursor is invalid regardless of expiry.
        // The service still validates cursors whose high-water mark is null,
        // since that bound is derived from the current change log there.
        if (highWaterMark != null && lastSequence > highWaterMark) return CursorDecodeResult.Invalid

        // Only a structurally valid, correctly signed, user-bound cursor whose
        // sole defect is expiry receives the distinct expiry outcome.
        if (expiresAt < System.currentTimeMillis() / 1000) return CursorDecodeResult.Expired

        return CursorDecodeResult.Valid(
            SyncCursor(expectedUserScope, lastSequence, highWaterMark, expiresAt)
        )
    }

    private fun hmac(payload: String): ByteArray =
        Mac.getInstance("HmacSHA256").apply { init(key) }.doFinal(payload.toByteArray(StandardCharsets.UTF_8))

    private fun b64(bytes: ByteArray): String = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    companion object {
        private const val VERSION = "v1"
    }
}

sealed interface CursorDecodeResult {
    data class Valid(val cursor: SyncCursor) : CursorDecodeResult
    object Expired : CursorDecodeResult
    object Invalid : CursorDecodeResult
}

data class SyncCursor(
    val userScope: String,
    val lastSequence: Long,
    val highWaterMark: Long?,
    val expiresAtEpochSecond: Long,
)
