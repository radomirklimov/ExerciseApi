package com.organizer.service

import com.organizer.entity.ExerciseEntity
import com.organizer.repository.ExerciseReposiroty
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class ExerciseService(
    private val exerciseReposiroty: ExerciseReposiroty,
) {
   fun findAll(): MutableList<ExerciseEntity> {
       return exerciseReposiroty.findAll()
   }

    fun findById(id: Long): Optional<ExerciseEntity> {
        return exerciseReposiroty.findById(id)
    }
}
