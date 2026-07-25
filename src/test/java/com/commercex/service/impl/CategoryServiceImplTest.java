package com.commercex.service.impl;

import com.commercex.dto.CategoryResponse;
import com.commercex.dto.CreateCategoryRequest;
import com.commercex.entity.Category;
import com.commercex.exception.ResourceAlreadyExistsException;
import com.commercex.mapper.CategoryMapper;
import com.commercex.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;
    
    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private CreateCategoryRequest createRequest;
    private Category category;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        createRequest = new CreateCategoryRequest();
        createRequest.setName("Electronics");
        createRequest.setSlug("electronics");

        category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Electronics");
        category.setSlug("electronics");

        categoryResponse = CategoryResponse.builder()
                .id(category.getId())
                .name("Electronics")
                .slug("electronics")
                .build();
    }

    @Test
    void createCategory_Success() {
        when(categoryRepository.existsByName(createRequest.getName())).thenReturn(false);
        when(categoryRepository.existsBySlug(createRequest.getSlug())).thenReturn(false);
        when(categoryMapper.toEntity(createRequest)).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toDto(category)).thenReturn(categoryResponse);

        CategoryResponse result = categoryService.createCategory(createRequest);

        assertNotNull(result);
        assertEquals("Electronics", result.getName());
    }

    @Test
    void createCategory_DuplicateName_ThrowsException() {
        when(categoryRepository.existsByName(createRequest.getName())).thenReturn(true);
        assertThrows(ResourceAlreadyExistsException.class, () -> categoryService.createCategory(createRequest));
    }
}
