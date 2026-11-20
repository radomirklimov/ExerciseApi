package com.organizer.security

import com.google.firebase.auth.FirebaseToken

interface FirebaseTokenVerifier {
    fun verify(idToken: String): FirebaseToken
}