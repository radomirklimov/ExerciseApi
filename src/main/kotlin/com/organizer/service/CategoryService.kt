package com.organizer.service

import com.organizer.dto.CategoryResponse
import com.organizer.mapper.toResponse
import com.organizer.repository.CategoryRepository
import org.springframework.stereotype.Service

@Service
class CategoryService(
    private val categoryRepo: CategoryRepository,
) {
    fun findAllSports(): List<CategoryResponse> =
        categoryRepo.findAllSports()
            .map { it.toResponse() }

    fun findAllCategories(): List<CategoryResponse> =
        categoryRepo.findAllCategories()
            .map { it.toResponse() }
}
