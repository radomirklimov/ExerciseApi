package com.organizer.mapper

import com.organizer.dto.ExerciseResponse
import com.organizer.dto.ImageResponse
import com.organizer.dto.InstructionResponse
import com.organizer.entity.ExerciseEntity

fun ExerciseEntity.toResponse() =
    ExerciseResponse(
        exerciseId = exerciseId,
        name = name,
        instructions = instructions
            .sortedBy { it.position }
            .map {
                InstructionResponse(
                    text = it.text,
                    position = it.position
                )
            },
        images = images
            .sortedBy { it.position }
            .map {
                ImageResponse(
                    imageUrl = it.imageUrl,
                    position = it.position
                )
            }
    )
