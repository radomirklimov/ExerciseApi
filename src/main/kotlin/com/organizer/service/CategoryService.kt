package com.organizer.service

import com.organizer.dto.CategoryResponse
import com.organizer.mapper.toResponse
import com.organizer.repository.CategoryRepository
import org.springframework.stereotype.Service

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
) {
    fun findAllSports(): List<CategoryResponse> =
        categoryRepository.findAllSports()
            .map { it.toResponse() }

    fun findAllCategories(): List<CategoryResponse> =
        categoryRepository.findAllCategories()
            .map { it.toResponse() }
}
