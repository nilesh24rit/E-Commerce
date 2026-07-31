package com.commercex.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ProductSearchRequest {
    private String keyword;
    private UUID categoryId;
    private String brand;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Double minRating;
    private Boolean activeOnly = true;
    
    private String sortBy = "newest"; // "newest", "price_asc", "price_desc", "rating"
    private int page = 0;
    private int size = 10;
}
