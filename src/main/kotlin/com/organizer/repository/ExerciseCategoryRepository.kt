package com.organizer.repository

import com.organizer.entity.ExerciseCategoryEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ExerciseCategoryRepository : CrudRepository<ExerciseCategoryEntity, Long> {

    fun findAllByOrderByExerciseCategoryIdAsc(): List<ExerciseCategoryEntity>
}
