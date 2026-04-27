package com.organizer.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "workout")
class WorkoutEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var workoutId: Long = 0,

    @Column
    var name: String,

    @Column
    var createdDate: LocalDateTime = LocalDateTime.now(),

    @ManyToMany
    @JoinTable(
        name = "workout_exercise",
        joinColumns = [JoinColumn(name = "workout_id")],
        inverseJoinColumns = [JoinColumn(name = "exercise_id")]
    )
    var exercises: MutableList<ExerciseEntity> = mutableListOf(),
)
