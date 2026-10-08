package com.organizer.repository

import com.organizer.entity.ExerciseEntity
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ExerciseRepository : JpaRepository<ExerciseEntity, Long> {

    @EntityGraph(attributePaths = ["instructions", "images"])
    @Query("SELECT e FROM ExerciseEntity e WHERE e IN :exercises ORDER BY e.exerciseId ASC")
    fun findAllWithDetails(@Param("exercises") exercises: List<ExerciseEntity>): List<ExerciseEntity>
}
