package com.organizer.sync

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SyncCursorCodecTest {

    private val codec = SyncCursorCodec("test-only-sync-cursor-secret-please-change-me-1234")
    private val scope = "user-a"
    private val now = System.currentTimeMillis() / 1000

    @Test
    fun `valid unexpired cursor is accepted`() {
        val encoded = codec.encode(SyncCursor(scope, 5, 9, now + 60))
        val decoded = codec.decodeAndVerify(encoded, scope)

        assertTrue(decoded is CursorDecodeResult.Valid)
        decoded as CursorDecodeResult.Valid
        assertEquals(5L, decoded.cursor.lastSequence)
        assertEquals(9L, decoded.cursor.highWaterMark)
    }

    @Test
    fun `valid but expired cursor is reported as expired`() {
        val encoded = codec.encode(SyncCursor(scope, 5, 9, now - 60))

        assertEquals(CursorDecodeResult.Expired, codec.decodeAndVerify(encoded, scope))
    }

    @Test
    fun `expired cursor with inconsistent range is invalid`() {
        val encoded = codec.encode(SyncCursor(scope, 10, 5, now - 60))

        assertEquals(CursorDecodeResult.Invalid, codec.decodeAndVerify(encoded, scope))
    }

    @Test
    fun `tampered cursor is invalid`() {
        val encoded = codec.encode(SyncCursor(scope, 5, 9, now + 60))
        val tampered = encoded.replace(encoded.last(), if (encoded.last() == 'A') 'B' else 'A')

        assertEquals(CursorDecodeResult.Invalid, codec.decodeAndVerify(tampered, scope))
    }

    @Test
    fun `cursor for another user is invalid`() {
        val encoded = codec.encode(SyncCursor("user-b", 5, 9, now + 60))

        assertEquals(CursorDecodeResult.Invalid, codec.decodeAndVerify(encoded, scope))
    }

    @Test
    fun `malformed cursor is invalid`() {
        assertEquals(CursorDecodeResult.Invalid, codec.decodeAndVerify("not-a-cursor", scope))
        assertEquals(CursorDecodeResult.Invalid, codec.decodeAndVerify("x.!!!", scope))
    }
}
