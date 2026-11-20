package com.organizer.security

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import org.springframework.stereotype.Component

@Component
class FirebaseTokenVerifierImpl : FirebaseTokenVerifier {

    override fun verify(idToken: String): FirebaseToken {
        if (FirebaseApp.getApps().isEmpty()) {
            throw IllegalStateException("Firebase is not initialized")
        }
        return FirebaseAuth.getInstance().verifyIdToken(idToken)
    }
}