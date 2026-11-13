package com.organizer.dto

data class WorkoutResponse(
    val workoutId: Long,
    val name: String,
    val notes: String = "",
    val starter: Boolean = false,
    val isStarter: Boolean = starter,
    val template: Boolean = starter,
    val isTemplate: Boolean = starter,
    val exercises: List<WorkoutExerciseResponse> = emptyList(),
    val exerciseIds: List<Long> = exercises.map { it.exerciseId },
)

data class WorkoutExerciseResponse(
    val workoutExerciseId: Long = 0,
    val workoutId: Long = 0,
    val exerciseId: Long = 0,
    val orderIndex: Int = 0,
    val sortOrder: Int = orderIndex,
    val position: Int = orderIndex,
    val exercise: ExerciseResponse? = null,
)