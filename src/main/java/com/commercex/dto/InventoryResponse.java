package com.commercex.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class InventoryResponse {
    private UUID id;
    private UUID productId;
    private String productName;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer soldQuantity;
    private Integer reorderLevel;
    private String warehouseLocation;
    private LocalDateTime lastRestockedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
