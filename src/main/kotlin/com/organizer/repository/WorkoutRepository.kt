package com.organizer.repository

import com.organizer.entity.WorkoutEntity
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface WorkoutRepository : JpaRepository<WorkoutEntity, Long> {

    fun findAllByStarterTrue(): List<WorkoutEntity>

    @EntityGraph(attributePaths = ["exercises", "exercises.exercise", "exercises.exercise.instructions", "exercises.exercise.images"])
    @Query("SELECT w FROM WorkoutEntity w WHERE w.starter = true ORDER BY w.workoutId ASC")
    fun findAllStarterWithDetails(): List<WorkoutEntity>

    @EntityGraph(attributePaths = ["exercises", "exercises.exercise", "exercises.exercise.instructions", "exercises.exercise.images"])
    @Query("SELECT w FROM WorkoutEntity w ORDER BY w.workoutId ASC")
    fun findAllWithDetails(): List<WorkoutEntity>

    @EntityGraph(attributePaths = ["exercises", "exercises.exercise", "exercises.exercise.instructions", "exercises.exercise.images"])
    @Query("SELECT w FROM WorkoutEntity w WHERE w.workoutId = :id")
    fun findByIdWithDetails(@Param("id") id: Long): WorkoutEntity?
}