package com.organizer.service

import com.organizer.dto.ExerciseResponse
import com.organizer.mapper.toResponse
import com.organizer.repository.ExerciseRepository
import org.springframework.stereotype.Service

@Service
class ExerciseService(
    private val exerciseRepository: ExerciseRepository,
) {
    fun findAll(): List<ExerciseResponse> {
        return exerciseRepository.findAll()
            .map { it.toResponse() }
    }
}
