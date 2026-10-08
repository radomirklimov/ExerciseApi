package com.organizer.service

import com.organizer.dto.ExerciseResponse
import com.organizer.mapper.toResponse
import com.organizer.repository.ExerciseRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

@Service
class ExerciseService(
    private val exerciseRepo: ExerciseRepository,
) {
    fun findAll(page: Int, size: Int): List<ExerciseResponse> {
        val content = exerciseRepo.findAll(
            PageRequest.of(page, size.coerceIn(1, 100), Sort.by("exerciseId"))
        ).content
        if (content.isEmpty()) return emptyList()
        val detailsById = exerciseRepo.findAllWithDetails(content)
            .associateBy { it.exerciseId }
        return content.map { detailsById.getValue(it.exerciseId).toResponse() }
    }
}
