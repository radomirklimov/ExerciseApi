package com.organizer.controller

import com.organizer.dto.UserResponse
import com.organizer.security.FirebasePrincipal
import com.organizer.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
) {

    @GetMapping("/me")
    fun me(): ResponseEntity<UserResponse> {
        val principal = SecurityContextHolder.getContext().authentication?.principal as? FirebasePrincipal
            ?: return ResponseEntity.status(401).build()
        val user = userService.getOrCreate(
            firebaseUid = principal.uid,
            email = principal.email,
            displayName = principal.displayName,
        )
        return ResponseEntity.ok(
            UserResponse(
                id = user.id,
                firebaseUid = user.firebaseUid,
                email = user.email,
                displayName = user.displayName,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt,
                lastLoginAt = user.lastLoginAt,
            )
        )
    }
}