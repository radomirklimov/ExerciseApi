package com.organizer.controller

import com.organizer.dto.CategoryResponse
import com.organizer.dto.ExerciseCategoryResponse
import com.organizer.dto.ExerciseResponse
import com.organizer.service.CategoryService
import com.organizer.service.ExerciseCategoryService
import com.organizer.service.ExerciseService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class MainController(
    private val categoryService: CategoryService,
    private val exerciseService: ExerciseService,
    private val exerciseCategoryService: ExerciseCategoryService,
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

    // returns one page of exercises, e.g. /exercises?page=0&size=20
    // keep requesting next page until the list comes back empty
    @GetMapping("/exercises")
    fun getAllExercises(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): List<ExerciseResponse> {
        return exerciseService.findAll(page, size)
    }

    // return relationships of exercises and categories
    @GetMapping("/exercise-category")
    fun getExerciseCategory(): List<ExerciseCategoryResponse> {
        return exerciseCategoryService.findAll()
    }
}
