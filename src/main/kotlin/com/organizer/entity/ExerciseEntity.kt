package com.organizer.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "exercise")
class ExerciseEntity (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val exerciseId: Long = 0,

    @Column
    val name: String,

    @Column(columnDefinition = "TEXT")
    val instruction: String,

    @Column
    val imageUrl: String,

    @Column(name = "category_id")
    val categoryId: Long?
)
