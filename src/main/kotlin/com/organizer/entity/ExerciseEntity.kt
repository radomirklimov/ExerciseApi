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
@Table(name = "exercise")
class ExerciseEntity (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var exerciseId: Long = 0,

    @Column
    var name: String = "",

    @Column
    //TODO: save full url after deploy e.g. http://api.com/images/pushup.png
    var imageUrl: String = "",

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    var category: CategoryEntity? = null,
)
