package com.organizer.repository

import com.organizer.entity.CategoryEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository: CrudRepository<CategoryEntity, Long>  {

    @Query("SELECT c FROM CategoryEntity c WHERE c.parentCategoryId IS NULL ORDER BY c.categoryId ASC")
    fun findAllSports(): List<CategoryEntity>

    @Query("SELECT c FROM CategoryEntity c WHERE c.parentCategoryId IS  NOT NULL ORDER BY c.categoryId ASC")
    fun findAllCategories(): List<CategoryEntity>
}
