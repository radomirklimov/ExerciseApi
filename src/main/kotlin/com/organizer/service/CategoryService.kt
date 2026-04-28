package com.organizer.service

import com.organizer.entity.CategoryEntity
import com.organizer.repository.CategoryReposiroty
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class CategoryService(
    private val categoryReposiroty: CategoryReposiroty,
) {
   fun findAll(): MutableList<CategoryEntity> {
       return categoryReposiroty.findAll()
   }

    fun findById(id: Long): Optional<CategoryEntity> {
        return categoryReposiroty.findById(id)
    }
}
