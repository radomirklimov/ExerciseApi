package com.organizer.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class MainController {

    // returns all categories
    @GetMapping("/categories")
    fun getAllCategories(): ResponseEntity<Any> {
        return ResponseEntity.ok().build()
    }

    // returns sub-categories or exercises
    @GetMapping("/categories/{id}")
    fun getCategoryById(@PathVariable("id") id: String): ResponseEntity<Any> {
        return ResponseEntity.ok().build()
    }

    // returns all exercises
    @GetMapping("/exercises")
    fun getAllExercises(): ResponseEntity<Any> {
        return ResponseEntity.ok().build()
    }

    // finds exercise by its id
    @GetMapping("/exercises/{id}")
    fun getExerciseById(@PathVariable("id") id: String): ResponseEntity<Any> {
        return ResponseEntity.ok().build()
    }
}
