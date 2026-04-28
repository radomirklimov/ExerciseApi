package com.organizer.controller

import com.organizer.entity.CategoryEntity
import com.organizer.service.CategoryService
import com.organizer.service.ExerciseService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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

    // returns sub-categories or exercises
    @GetMapping("/categories/{id}")
    fun getCategoryById(@PathVariable("id") id: String): ResponseEntity<Any> {
        val category = categoryService.findById(id.toLong())
        if (category.isPresent) {
            return ResponseEntity.ok().body(category)
        }
        return ResponseEntity.notFound().build()
    }

    // returns all exercises
    @GetMapping("/exercises")
    fun getAllExercises(): ResponseEntity<Any> {
        return ResponseEntity.ok().body(exerciseService.findAll())
    }

    // finds exercise by its id
    @GetMapping("/exercises/{id}")
    fun getExerciseById(@PathVariable("id") id: String): ResponseEntity<Any> {
        val category = exerciseService.findById(id.toLong())
        if (category.isPresent) {
            return ResponseEntity.ok().body(category)
        }
        return ResponseEntity.notFound().build()
    }
}
