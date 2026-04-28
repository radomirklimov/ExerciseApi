package com.organizer.repository

import com.organizer.entity.CategoryEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface CategoryReposiroty: CrudRepository<CategoryEntity, Long>  {
    
    override fun findAll(): MutableList<CategoryEntity>
    
    override fun findById(id: Long): Optional<CategoryEntity>
}
