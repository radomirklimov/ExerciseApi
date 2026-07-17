package com.organizer.repository

import com.organizer.entity.CategoryEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository: CrudRepository<CategoryEntity, Long>  {

    @Query("SELECT c FROM CategoryEntity c WHERE c.iconUrl IS NOT NULL AND c.parentCategoryId IS NULL")
    fun findAllSports(): List<CategoryEntity>

    @Query("SELECT c FROM CategoryEntity c WHERE c.parentCategoryId IS  NOT NULL")
    fun findAllCategories(): List<CategoryEntity>
}
