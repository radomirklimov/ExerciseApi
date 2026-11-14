package com.organizer.entity

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "exercise_instruction")
class ExerciseInstructionEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val instructionId: Long = 0,

    val text: String,

    val position: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    val exercise: ExerciseEntity
)
