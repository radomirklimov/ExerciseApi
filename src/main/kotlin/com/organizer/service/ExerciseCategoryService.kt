package com.organizer.service

import com.organizer.dto.ExerciseCategoryResponse
import com.organizer.mapper.toResponse
import com.organizer.repository.ExerciseCategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ExerciseCategoryService(
    private val exerciseCategoryRepo: ExerciseCategoryRepository
) {
    fun findAll(): List<ExerciseCategoryResponse> {
        return exerciseCategoryRepo.findAllByOrderByExerciseCategoryIdAsc()
            .map { it.toResponse() }
    }
}
