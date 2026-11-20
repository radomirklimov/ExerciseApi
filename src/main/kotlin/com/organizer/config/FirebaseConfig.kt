package com.organizer.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Paths

@Configuration
class FirebaseConfig(
    @Value("\${firebase.project.id:}") private val projectId: String,
    @Value("\${firebase.service.account.path:}") private val serviceAccountPath: String,
    @Value("\${firebase.service.account.json:}") private val serviceAccountJson: String,
) {

    @PostConstruct
    fun init() {
        if (FirebaseApp.getApps().isNotEmpty()) return

        val rawCredential = serviceAccountJson.ifBlank { serviceAccountPath }
        val credentials = when {
            rawCredential.isBlank() -> runCatching { GoogleCredentials.getApplicationDefault() }.getOrNull()
            rawCredential.trim().startsWith("{") -> GoogleCredentials.fromStream(
                ByteArrayInputStream(rawCredential.toByteArray(Charsets.UTF_8))
            )
            else -> runCatching {
                Files.newInputStream(Paths.get(rawCredential)).use { GoogleCredentials.fromStream(it) }
            }.getOrNull()
        } ?: return

        val optionsBuilder = FirebaseOptions.builder().setCredentials(credentials)
        if (projectId.isNotBlank()) {
            optionsBuilder.setProjectId(projectId)
        }
        FirebaseApp.initializeApp(optionsBuilder.build())
    }
}