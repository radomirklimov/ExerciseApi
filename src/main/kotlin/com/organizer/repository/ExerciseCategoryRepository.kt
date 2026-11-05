package com.organizer.repository

import com.organizer.entity.ExerciseCategoryEntity
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.repository.CrudRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ExerciseCategoryRepository : CrudRepository<ExerciseCategoryEntity, Long> {

    @EntityGraph(attributePaths = ["exercise", "category"])
    @Query("SELECT ec FROM ExerciseCategoryEntity ec ORDER BY ec.category.categoryId ASC, ec.sortOrder ASC, ec.exerciseCategoryId ASC")
    fun findAllInDisplayOrder(): List<ExerciseCategoryEntity>
}
