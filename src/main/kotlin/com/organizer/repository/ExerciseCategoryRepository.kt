package com.organizer.repository

import com.organizer.entity.ExerciseCategoryEntity
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ExerciseCategoryRepository : CrudRepository<ExerciseCategoryEntity, Long> {

    @EntityGraph(attributePaths = ["exercise", "category"])
    fun findAllByOrderByExerciseCategoryIdAsc(): List<ExerciseCategoryEntity>
}
