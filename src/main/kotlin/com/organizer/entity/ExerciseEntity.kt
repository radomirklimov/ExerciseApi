package com.organizer.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table

@Entity
@Table(name = "exercise")
class ExerciseEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val exerciseId: Long = 0,

    @Column(nullable = false)
    val name: String,

    @OneToMany(
        mappedBy = "exercise",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @OrderBy("position ASC")
    val instructions: List<ExerciseInstructionEntity> = mutableListOf(),

    @OneToMany(
        mappedBy = "exercise",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @OrderBy("position ASC")
    val images: List<ExerciseImageEntity> = mutableListOf()
)
