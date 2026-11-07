package com.organizer.mapper

import com.organizer.dto.ExerciseResponse
import com.organizer.entity.ExerciseEntity

fun ExerciseEntity.toResponse() =
    ExerciseResponse(
        exerciseId = exerciseId,
        name = name,
        instructions = instructions.map { it.text },
        images = images.map { it.imageUrl },
        exerciseType = exerciseType,
    )

fun classifyExerciseType(name: String?): String {
    val lower = name?.lowercase() ?: ""
    return when {
        lower.contains("plank") || lower.contains("hold") ||
            lower.contains("wall sit") || lower.contains("dead hang") ||
            lower.contains("hollow") || lower.contains("l-sit") ||
            lower.contains("seconds") || lower.contains("minute") ->
            "DURATION"

        lower.contains("push-up") || lower.contains("push up") ||
            lower.contains("pull-up") || lower.contains("pull up") ||
            lower.contains("squat") || lower.contains("lunge") ||
            lower.contains("crunch") || lower.contains("sit-up") ||
            lower.contains("sit up") || lower.contains("burpee") ||
            lower.contains("jump") || lower.contains("dip") ->
            "REPS"

        else -> "REPS_WEIGHT"
    }
}
