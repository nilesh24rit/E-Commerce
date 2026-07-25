package com.commercex.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ProductResponse {
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private String shortDescription;
    private String sku;
    private String brand;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer quantity;
    private String status;
    private boolean active;
    private CategoryResponse category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
