package com.commercex.service.impl;

import com.commercex.dto.CategoryResponse;
import com.commercex.dto.CreateCategoryRequest;
import com.commercex.dto.UpdateCategoryRequest;
import com.commercex.entity.Category;
import com.commercex.exception.ResourceAlreadyExistsException;
import com.commercex.exception.ResourceNotFoundException;
import com.commercex.mapper.CategoryMapper;
import com.commercex.repository.CategoryRepository;
import com.commercex.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        log.info("Creating category with name: {}", request.getName());
        
        if (categoryRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Category name already exists: " + request.getName());
        }
        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new ResourceAlreadyExistsException("Category slug already exists: " + request.getSlug());
        }

        Category category = categoryMapper.toEntity(request);
        category.setActive(true);
        Category saved = categoryRepository.save(category);
        return categoryMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request) {
        log.info("Updating category with ID: {}", id);
        Category category = findEntityById(id);

        if (request.getName() != null && !request.getName().equals(category.getName()) && categoryRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Category name already exists: " + request.getName());
        }
        if (request.getSlug() != null && !request.getSlug().equals(category.getSlug()) && categoryRepository.existsBySlug(request.getSlug())) {
            throw new ResourceAlreadyExistsException("Category slug already exists: " + request.getSlug());
        }

        categoryMapper.updateEntityFromDto(request, category);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        log.info("Soft deleting category with ID: {}", id);
        Category category = findEntityById(id);
        category.setActive(false);
        categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {
        return categoryMapper.toDto(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllByActiveTrue().stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Category findEntityById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
    }
}
