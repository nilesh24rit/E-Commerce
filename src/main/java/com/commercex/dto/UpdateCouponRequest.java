package com.commercex.dto;

import com.commercex.entity.enums.DiscountType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateCouponRequest {
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minimumOrderAmount;
    private BigDecimal maximumDiscountAmount;
    private Integer usageLimit;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private Boolean active;
}
