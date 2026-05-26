package com.organizer.controller

import com.organizer.entity.CategoryEntity
import com.organizer.entity.ExerciseEntity
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

    // returns all categories
    @GetMapping("/categories")
    fun getAllCategories(): ResponseEntity<List<CategoryEntity>> {
        return ResponseEntity.ok().body(categoryService.findAll())
    }

    // returns all exercises
    @GetMapping("/exercises")
    fun getAllExercises(): ResponseEntity<List<ExerciseEntity>> {
        return ResponseEntity.ok().body(exerciseService.findAll())
    }
}
