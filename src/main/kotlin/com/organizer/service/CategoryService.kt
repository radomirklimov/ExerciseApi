package com.organizer.service

import com.organizer.entity.CategoryEntity
import com.organizer.repository.CategoryRepository
import org.springframework.stereotype.Service

@Service
class CategoryService(
    private val categoryRepository: CategoryRepository,
) {
   fun findAll(): List<CategoryEntity> {
       return categoryRepository.findAll()
   }
}
