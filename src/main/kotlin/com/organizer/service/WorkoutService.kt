package com.organizer.service

import com.organizer.dto.WorkoutResponse
import com.organizer.mapper.toResponse
import com.organizer.repository.WorkoutRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class WorkoutService(
    private val workoutRepo: WorkoutRepository,
) {
    fun findAllStarterTemplates(): List<WorkoutResponse> =
        workoutRepo.findAllStarterWithDetails().map { it.toResponse() }

    fun findAllStarterWorkouts(): List<WorkoutResponse> = findAllStarterTemplates()

    fun findAllStarter(): List<WorkoutResponse> = findAllStarterTemplates()

    fun findAllTemplates(): List<WorkoutResponse> = findAllStarterTemplates()

    fun findAllWorkouts(): List<WorkoutResponse> = findAllStarterTemplates()

    fun findAll(): List<WorkoutResponse> = findAllStarterTemplates()

    fun findById(id: Long): WorkoutResponse? =
        workoutRepo.findByIdWithDetails(id)?.toResponse()
}