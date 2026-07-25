package com.commercex.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class CreateInventoryRequest {
    @NotNull(message = "Product ID is required")
    private UUID productId;

    @Min(value = 0, message = "Available quantity cannot be negative")
    private Integer availableQuantity = 0;

    @Min(value = 0, message = "Reorder level cannot be negative")
    private Integer reorderLevel = 10;

    private String warehouseLocation;
}
