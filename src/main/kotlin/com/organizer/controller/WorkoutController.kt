package com.organizer.controller

import com.organizer.dto.WorkoutResponse
import com.organizer.service.WorkoutService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class WorkoutController(
    private val workoutService: WorkoutService,
) {
    @GetMapping(
        "/starter-workouts",
        "/starter-workout-templates",
        "/workout-templates",
        "/starter-templates",
        "/workouts/starter",
        "/workouts/templates",
        "/workouts",
        "/starterWorkouts",
    )
    fun getStarterWorkouts(): ResponseEntity<List<WorkoutResponse>> {
        return ResponseEntity.ok().body(workoutService.findAllStarterTemplates())
    }
}