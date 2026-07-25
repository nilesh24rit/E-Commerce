package com.commercex.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CartResponse {
    private UUID id;
    private UUID userId;
    private List<CartItemResponse> items;
    private BigDecimal grandTotal;
    private Integer totalItems;
}
