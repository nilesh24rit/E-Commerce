package com.commercex.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApplyCouponRequest {
    @NotBlank(message = "Coupon code is required")
    private String code;
    
    @NotNull(message = "Order amount is required")
    private BigDecimal orderAmount;
}
