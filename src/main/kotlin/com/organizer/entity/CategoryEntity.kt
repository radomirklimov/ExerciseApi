package com.organizer.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
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
@Table(name = "category")
@JsonIgnoreProperties("hibernateLazyInitializer", "handler")
class CategoryEntity (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val categoryId: Long = 0,

    @Column(nullable = false)
    val name: String,

    @Column(name = "icon_url")
    val iconUrl: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    val parent: CategoryEntity?
)
