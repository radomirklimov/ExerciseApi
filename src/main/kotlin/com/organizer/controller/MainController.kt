package com.organizer.controller

import com.organizer.dto.CategoryResponse
import com.organizer.dto.ExerciseResponse
import com.organizer.service.CategoryService
import com.organizer.service.ExerciseService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MainController(
    private val categoryService: CategoryService,
    private val exerciseService: ExerciseService,
) {
    // returns all sports
    @GetMapping("/sports")
    fun getAllSports(): ResponseEntity<List<CategoryResponse>> {
        return ResponseEntity.ok().body(categoryService.findAllSports())
    }

    // returns all non-sports categories
    @GetMapping("/categories")
    fun getAllCategories(): ResponseEntity<List<CategoryResponse>> {
        return ResponseEntity.ok().body(categoryService.findAllCategories())
    }

    // returns all exercises
    @GetMapping("/exercises")
    fun getAllExercises(): List<ExerciseResponse> {
        return exerciseService.findAll()
    }
}
