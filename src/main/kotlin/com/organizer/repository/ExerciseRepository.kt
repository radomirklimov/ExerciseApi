package com.organizer.repository

import com.organizer.entity.ExerciseEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ExerciseRepository: CrudRepository<ExerciseEntity, Long>  {

    override fun findAll(): List<ExerciseEntity>
}
