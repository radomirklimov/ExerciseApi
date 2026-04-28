package com.organizer.repository

import com.organizer.entity.ExerciseEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ExerciseReposiroty: CrudRepository<ExerciseEntity, Long>  {

    override fun findAll(): MutableList<ExerciseEntity>

    override fun findById(id: Long): Optional<ExerciseEntity>
}
