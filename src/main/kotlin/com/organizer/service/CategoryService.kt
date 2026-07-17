package com.organizer.service

import com.organizer.dto.CategoryResponse
import com.organizer.mapper.toResponse
import com.organizer.repository.CategoryRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
) {
    @Cacheable("sports")
    fun findAllSports(): List<CategoryResponse> =
        categoryRepository.findAllSports()
            .map { it.toResponse() }

    @Cacheable("categories")
    fun findAllCategories(): List<CategoryResponse> =
        categoryRepository.findAllCategories()
            .map { it.toResponse() }
}
