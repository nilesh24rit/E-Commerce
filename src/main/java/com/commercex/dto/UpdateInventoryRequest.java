package com.commercex.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateInventoryRequest {
    @Min(value = 0, message = "Available quantity cannot be negative")
    private Integer availableQuantity;

    @Min(value = 0, message = "Reorder level cannot be negative")
    private Integer reorderLevel;

    private String warehouseLocation;
}
