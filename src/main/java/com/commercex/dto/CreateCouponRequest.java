package com.commercex.dto;

import com.commercex.entity.enums.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateCouponRequest {
    @NotBlank(message = "Coupon code is required")
    private String code;
    
    private String description;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    private BigDecimal discountValue;

    private BigDecimal minimumOrderAmount;
    private BigDecimal maximumDiscountAmount;
    
    private Integer usageLimit;
    
    @NotNull(message = "Valid from date is required")
    private LocalDateTime validFrom;
    
    @NotNull(message = "Valid until date is required")
    private LocalDateTime validUntil;
    
    private boolean active = true;
}
