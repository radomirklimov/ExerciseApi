package com.organizer.repository

import com.organizer.entity.CategoryEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository: CrudRepository<CategoryEntity, Long>  {

    override fun findAll(): List<CategoryEntity>
}
