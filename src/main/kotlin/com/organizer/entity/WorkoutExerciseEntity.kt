package com.organizer.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(
    name = "workout_exercise",
    uniqueConstraints = [
        jakarta.persistence.UniqueConstraint(columnNames = ["workout_id", "exercise_id"])
    ]
)
class WorkoutExerciseEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_exercise_id")
    val workoutExerciseId: Long = 0,

    workout: WorkoutEntity? = null,
    workoutEntity: WorkoutEntity? = null,
    exercise: ExerciseEntity? = null,
    exerciseEntity: ExerciseEntity? = null,

    orderIndex: Int? = null,
    sortOrder: Int? = null,
    position: Int? = null,
) {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = false)
    val workout: WorkoutEntity? = workout ?: workoutEntity

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    val exercise: ExerciseEntity? = exercise ?: exerciseEntity

    @Column(name = "order_index", nullable = false)
    val orderIndex: Int = orderIndex ?: sortOrder ?: position ?: 0
}