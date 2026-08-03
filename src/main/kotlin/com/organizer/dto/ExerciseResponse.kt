package com.organizer.dto

data class ExerciseResponse(
    val exerciseId: Long,
    val name: String,
    val instruction: String,
    val imageUrl: String,
)
