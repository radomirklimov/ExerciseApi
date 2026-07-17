package com.organizer.mapper

import com.organizer.dto.CategoryResponse
import com.organizer.entity.CategoryEntity

fun CategoryEntity.toResponse() = CategoryResponse(
    categoryId = categoryId,
    name = name,
    iconUrl = iconUrl,
    parentCategoryId = parentCategoryId
)
