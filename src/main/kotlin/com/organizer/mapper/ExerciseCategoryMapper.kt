package com.organizer.mapper

import com.organizer.dto.ExerciseCategoryResponse
import com.organizer.entity.ExerciseCategoryEntity

fun ExerciseCategoryEntity.toResponse() =
    ExerciseCategoryResponse(
        exerciseCategoryId = exerciseCategoryId,
        exerciseId = exercise.exerciseId,
        categoryId = category.categoryId
    )
