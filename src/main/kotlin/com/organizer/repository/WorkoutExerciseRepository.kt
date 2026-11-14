package com.organizer.repository

import com.organizer.entity.WorkoutExerciseEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface WorkoutExerciseRepository : CrudRepository<WorkoutExerciseEntity, Long> {
    fun findAllByWorkoutWorkoutId(workoutId: Long): List<WorkoutExerciseEntity>
}