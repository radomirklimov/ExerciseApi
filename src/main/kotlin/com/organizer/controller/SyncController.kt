package com.organizer.controller

import com.organizer.dto.SyncChangesResponse
import com.organizer.dto.SyncPushRequest
import com.organizer.dto.SyncPushResponse
import com.organizer.security.FirebasePrincipal
import com.organizer.service.SyncService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/sync")
class SyncController(
    private val syncService: SyncService,
) {

    @GetMapping("/changes")
    fun changes(@RequestParam(required = false) since: String?): ResponseEntity<SyncChangesResponse> {
        val principal = SecurityContextHolder.getContext().authentication?.principal as? FirebasePrincipal
            ?: return ResponseEntity.status(401).build()
        val sinceInstant = since?.let { runCatching { Instant.parse(it) }.getOrNull() }
        return ResponseEntity.ok(syncService.changes(principal.uid, sinceInstant))
    }

    @PostMapping("/push")
    fun push(@RequestBody request: SyncPushRequest): ResponseEntity<SyncPushResponse> {
        val principal = SecurityContextHolder.getContext().authentication?.principal as? FirebasePrincipal
            ?: return ResponseEntity.status(401).build()
        return ResponseEntity.ok(syncService.push(principal.uid, request))
    }
}