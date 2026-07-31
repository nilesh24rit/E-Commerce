package com.commercex.dto;

import com.commercex.entity.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class OrderSummaryResponse {
    private UUID id;
    private String orderNumber;
    private BigDecimal totalAmount;
    private OrderStatus orderStatus;
    private Integer totalItems;
    private LocalDateTime createdAt;
}
