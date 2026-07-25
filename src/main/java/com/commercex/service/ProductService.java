package com.commercex.service;

import com.commercex.dto.CreateProductRequest;
import com.commercex.dto.ProductResponse;
import com.commercex.dto.UpdateProductRequest;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductResponse createProduct(CreateProductRequest request);
    ProductResponse updateProduct(UUID id, UpdateProductRequest request);
    void deleteProduct(UUID id);
    ProductResponse getProductById(UUID id);
    ProductResponse getProductBySlug(String slug);
    List<ProductResponse> getAllProducts();
}
