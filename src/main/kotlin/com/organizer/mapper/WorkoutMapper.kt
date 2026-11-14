package com.organizer.mapper

import com.organizer.dto.ExerciseResponse
import com.organizer.dto.WorkoutExerciseResponse
import com.organizer.dto.WorkoutResponse
import com.organizer.entity.WorkoutEntity
import com.organizer.entity.WorkoutExerciseEntity

fun WorkoutEntity.toResponse(): WorkoutResponse {
    val mappedExercises = exercises
        .sortedBy { it.orderIndex }
        .map { it.toResponse(workoutId) }
    return WorkoutResponse(
        workoutId = workoutId,
        name = name,
        notes = notes,
        starter = starter,
        exercises = mappedExercises,
        exerciseIds = mappedExercises.map { it.exerciseId },
    )
}

fun WorkoutExerciseEntity.toResponse(parentWorkoutId: Long = workout?.workoutId ?: 0): WorkoutExerciseResponse {
    return WorkoutExerciseResponse(
        workoutExerciseId = workoutExerciseId,
        workoutId = workout?.workoutId ?: parentWorkoutId,
        exerciseId = exercise?.exerciseId ?: 0,
        orderIndex = orderIndex,
        exercise = exercise?.toResponse(),
    )
}

fun WorkoutEntity.toStarterWorkoutResponse() = toResponse()
fun WorkoutEntity.toWorkoutTemplateResponse() = toResponse()
fun WorkoutExerciseEntity.toStarterWorkoutExerciseResponse(parentWorkoutId: Long = 0) =
    toResponse(parentWorkoutId)