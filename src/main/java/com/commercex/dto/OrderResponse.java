package com.commercex.dto;

import com.commercex.entity.enums.OrderStatus;
import com.commercex.entity.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {
    private UUID id;
    private String orderNumber;
    private UUID userId;
    private BigDecimal totalAmount;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal shippingCharge;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
