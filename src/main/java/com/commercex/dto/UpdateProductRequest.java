package com.commercex.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UpdateProductRequest {
    @Size(max = 200)
    private String name;

    @Size(max = 200)
    private String slug;

    @Size(max = 2000)
    private String description;

    @Size(max = 500)
    private String shortDescription;

    @Size(max = 100)
    private String sku;

    @Size(max = 100)
    private String brand;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    private BigDecimal discountPrice;

    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    private String status;

    private Boolean active;

    private UUID categoryId;
}
