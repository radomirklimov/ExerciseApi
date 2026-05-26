package com.organizer.service

import com.organizer.entity.ExerciseEntity
import com.organizer.repository.ExerciseRepository
import org.springframework.stereotype.Service

@Service
class ExerciseService(
    private val exerciseRepository: ExerciseRepository,
) {
   fun findAll(): List<ExerciseEntity> {
       return exerciseRepository.findAll()
   }
}
