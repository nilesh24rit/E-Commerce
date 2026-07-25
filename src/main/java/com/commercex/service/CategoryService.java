package com.commercex.service;

import com.commercex.dto.CategoryResponse;
import com.commercex.dto.CreateCategoryRequest;
import com.commercex.dto.UpdateCategoryRequest;
import com.commercex.entity.Category;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    CategoryResponse createCategory(CreateCategoryRequest request);
    CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request);
    void deleteCategory(UUID id);
    CategoryResponse getCategoryById(UUID id);
    CategoryResponse getCategoryBySlug(String slug);
    List<CategoryResponse> getAllCategories();
    Category findEntityById(UUID id);
}
