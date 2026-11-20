package com.organizer.security

data class FirebasePrincipal(
    val uid: String,
    val email: String?,
    val displayName: String?,
)