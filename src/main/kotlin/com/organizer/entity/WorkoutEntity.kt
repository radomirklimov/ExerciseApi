package com.organizer.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table

@Entity
@Table(name = "workout")
class WorkoutEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "workout_id")
    val workoutId: Long = 0,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val notes: String = "",

    starter: Boolean = false,
    isStarter: Boolean = false,
    template: Boolean = false,
    isTemplate: Boolean = false,

    @OneToMany(
        mappedBy = "workout",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY,
    )
    @OrderBy("orderIndex ASC, workoutExerciseId ASC")
    val exercises: MutableSet<WorkoutExerciseEntity> = linkedSetOf(),
) {

    @Column(name = "starter", nullable = false)
    val starter: Boolean = starter || isStarter || template || isTemplate
}