package com.commercex.service.impl;

import com.commercex.dto.ProductResponse;
import com.commercex.dto.ProductSearchRequest;
import com.commercex.dto.ProductSearchResponse;
import com.commercex.entity.Product;
import com.commercex.mapper.ProductMapper;
import com.commercex.repository.ProductRepository;
import com.commercex.service.ProductSearchService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional(readOnly = true)
    public ProductSearchResponse searchProducts(ProductSearchRequest request) {
        log.info("Searching products with keyword: {}", request.getKeyword());
        
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (request.getSortBy() != null) {
            switch (request.getSortBy().toLowerCase()) {
                case "price_asc":
                    sort = Sort.by(Sort.Direction.ASC, "price");
                    break;
                case "price_desc":
                    sort = Sort.by(Sort.Direction.DESC, "price");
                    break;
                case "rating":
                    sort = Sort.by(Sort.Direction.DESC, "averageRating");
                    break;
                case "newest":
                default:
                    sort = Sort.by(Sort.Direction.DESC, "createdAt");
                    break;
            }
        }
        
        int pageNumber = Math.max(0, request.getPage());
        int size = (request.getSize() <= 0) ? 10 : Math.min(request.getSize(), 100);
        Pageable pageable = PageRequest.of(pageNumber, size, sort);
        
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                String pattern = "%" + request.getKeyword().toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern),
                    cb.like(cb.lower(root.get("shortDescription")), pattern)
                ));
            }
            
            if (request.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), request.getCategoryId()));
            }
            
            if (request.getBrand() != null && !request.getBrand().trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("brand")), request.getBrand().toLowerCase()));
            }
            
            if (request.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), request.getMinPrice()));
            }
            
            if (request.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), request.getMaxPrice()));
            }
            
            if (request.getMinRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("averageRating"), request.getMinRating()));
            }
            
            if (Boolean.TRUE.equals(request.getActiveOnly())) {
                predicates.add(cb.isTrue(root.get("active")));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        Page<Product> page = productRepository.findAll(spec, pageable);
        
        List<ProductResponse> content = page.getContent().stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
                
        return ProductSearchResponse.builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
