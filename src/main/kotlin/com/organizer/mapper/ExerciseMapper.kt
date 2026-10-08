package com.organizer.mapper

import com.organizer.dto.ExerciseResponse
import com.organizer.entity.ExerciseEntity

fun ExerciseEntity.toResponse() =
    ExerciseResponse(
        exerciseId = exerciseId,
        name = name,
        instructions = instructions.map { it.text },
        images = images.map { it.imageUrl }
    )
