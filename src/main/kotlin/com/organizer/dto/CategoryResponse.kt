package com.organizer.dto

data class CategoryResponse(
    val categoryId: Long,
    val name: String,
    val iconUrl: String?,
    val parentCategoryId: Long?
)
