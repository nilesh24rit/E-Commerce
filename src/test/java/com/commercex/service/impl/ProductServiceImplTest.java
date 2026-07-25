package com.commercex.service.impl;

import com.commercex.dto.CreateProductRequest;
import com.commercex.dto.ProductResponse;
import com.commercex.entity.Category;
import com.commercex.entity.Product;
import com.commercex.exception.ResourceAlreadyExistsException;
import com.commercex.mapper.ProductMapper;
import com.commercex.repository.ProductRepository;
import com.commercex.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private ProductMapper productMapper;
    
    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductServiceImpl productService;

    private CreateProductRequest createRequest;
    private Product product;
    private Category category;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(UUID.randomUUID());
        
        createRequest = new CreateProductRequest();
        createRequest.setName("Laptop");
        createRequest.setSlug("laptop");
        createRequest.setSku("LAP-001");
        createRequest.setPrice(BigDecimal.valueOf(1000));
        createRequest.setQuantity(10);
        createRequest.setCategoryId(category.getId());

        product = new Product();
        product.setId(UUID.randomUUID());
        product.setSku("LAP-001");

        productResponse = ProductResponse.builder()
                .id(product.getId())
                .sku("LAP-001")
                .build();
    }

    @Test
    void createProduct_Success() {
        when(productRepository.existsBySku(createRequest.getSku())).thenReturn(false);
        when(productRepository.existsBySlug(createRequest.getSlug())).thenReturn(false);
        when(categoryService.findEntityById(createRequest.getCategoryId())).thenReturn(category);
        when(productMapper.toEntity(createRequest)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(productResponse);

        ProductResponse result = productService.createProduct(createRequest);

        assertNotNull(result);
        assertEquals("LAP-001", result.getSku());
    }

    @Test
    void createProduct_DuplicateSku_ThrowsException() {
        when(productRepository.existsBySku(createRequest.getSku())).thenReturn(true);
        assertThrows(ResourceAlreadyExistsException.class, () -> productService.createProduct(createRequest));
    }
}
