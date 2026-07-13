package com.organizer.service

import com.organizer.entity.CategoryEntity
import com.organizer.repository.CategoryRepository
import org.springframework.stereotype.Service

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
) {
    fun findAllSports(): List<CategoryEntity> {
        return categoryRepository.findAllSports()
    }

   fun findAllCategories(): List<CategoryEntity> {
       return categoryRepository.findAllCategories()
   }
}
