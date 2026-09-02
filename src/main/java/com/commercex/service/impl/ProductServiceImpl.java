package com.commercex.service.impl;

import com.commercex.dto.CreateProductRequest;
import com.commercex.dto.ProductResponse;
import com.commercex.dto.UpdateProductRequest;
import com.commercex.entity.Category;
import com.commercex.entity.Product;
import com.commercex.exception.ResourceAlreadyExistsException;
import com.commercex.exception.ResourceNotFoundException;
import com.commercex.mapper.ProductMapper;
import com.commercex.repository.ProductRepository;
import com.commercex.service.CategoryService;
import com.commercex.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating product with SKU: {}", request.getSku());

        if (productRepository.existsBySku(request.getSku())) {
            throw new ResourceAlreadyExistsException("Product SKU already exists: " + request.getSku());
        }
        if (productRepository.existsBySlug(request.getSlug())) {
            throw new ResourceAlreadyExistsException("Product slug already exists: " + request.getSlug());
        }

        Category category = categoryService.findEntityById(request.getCategoryId());

        Product product = productMapper.toEntity(request);
        product.setCategory(category);
        product.setActive(true);
        if(product.getStatus() == null) {
            product.setStatus("IN_STOCK");
        }

        Product saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    @Override
    @Transactional
    @Caching(
        put = { @CachePut(value = "products", key = "#id.toString()") },
        evict = { @CacheEvict(value = "products", key = "'all'"), @CacheEvict(value = "products", allEntries = true) }
    )
    public ProductResponse updateProduct(UUID id, UpdateProductRequest request) {
        log.info("Updating product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        if (request.getSku() != null && !request.getSku().equals(product.getSku()) && productRepository.existsBySku(request.getSku())) {
            throw new ResourceAlreadyExistsException("Product SKU already exists: " + request.getSku());
        }
        if (request.getSlug() != null && !request.getSlug().equals(product.getSlug()) && productRepository.existsBySlug(request.getSlug())) {
            throw new ResourceAlreadyExistsException("Product slug already exists: " + request.getSlug());
        }

        if (request.getCategoryId() != null && (product.getCategory() == null || !request.getCategoryId().equals(product.getCategory().getId()))) {
            Category category = categoryService.findEntityById(request.getCategoryId());
            product.setCategory(category);
        }

        productMapper.updateEntityFromDto(request, product);
        return productMapper.toDto(productRepository.save(product));
    }

    @Override
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(UUID id) {
        log.info("Soft deleting product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        product.setActive(false);
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id.toString()")
    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return productMapper.toDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#slug")
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
        return productMapper.toDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "'all'")
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAllByActiveTrue().stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }
}
