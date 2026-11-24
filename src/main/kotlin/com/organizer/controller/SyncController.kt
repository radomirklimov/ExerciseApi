package com.organizer.controller

import com.organizer.dto.ApiError
import com.organizer.dto.SyncChangesResponse
import com.organizer.dto.SyncPushRequest
import com.organizer.dto.SyncPushResponse
import com.organizer.security.FirebasePrincipal
import com.organizer.service.SyncService
import com.organizer.sync.SyncRequestException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/sync")
class SyncController(
    private val syncService: SyncService,
) {

    @GetMapping("/changes")
    fun changes(
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "100") limit: Int,
    ): ResponseEntity<Any> {
        val principal = principal() ?: return unauthorized()
        return try {
            ResponseEntity.ok(syncService.changes(principal.uid, cursor, limit))
        } catch (ex: SyncRequestException) {
            badRequest(ex.errorCode, ex.message.orEmpty())
        } catch (ex: IllegalArgumentException) {
            badRequest("INVALID_REQUEST", ex.message.orEmpty())
        }
    }

    @PostMapping("/push")
    fun push(@RequestBody request: SyncPushRequest): ResponseEntity<Any> {
        val principal = principal() ?: return unauthorized()
        return try {
            ResponseEntity.ok(
                syncService.push(principal.uid, principal.email, principal.displayName, request)
            )
        } catch (ex: SyncRequestException) {
            badRequest(ex.errorCode, ex.message.orEmpty())
        } catch (ex: IllegalArgumentException) {
            badRequest("INVALID_REQUEST", ex.message.orEmpty())
        }
    }

    private fun principal(): FirebasePrincipal? =
        SecurityContextHolder.getContext().authentication?.principal as? FirebasePrincipal

    private fun unauthorized(): ResponseEntity<Any> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError("UNAUTHENTICATED", "Authentication required"))

    private fun badRequest(code: String, message: String): ResponseEntity<Any> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiError(code, message))
}