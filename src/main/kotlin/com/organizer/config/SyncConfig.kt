package com.organizer.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.organizer.sync.SyncCanonicalizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SyncConfig {
    @Bean
    fun syncCanonicalizer(objectMapper: ObjectMapper): SyncCanonicalizer = SyncCanonicalizer(objectMapper)
}