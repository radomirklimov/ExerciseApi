package com.organizer.dto

data class ExerciseResponse(
    val exerciseId: Long,
    val name: String,
    val instructions: List<String>,
    val images: List<String>
)
