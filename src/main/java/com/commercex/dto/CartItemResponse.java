package com.commercex.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class CartItemResponse {
    private UUID id;
    private UUID productId;
    private String productName;
    private String productSlug;
    private String productSku;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
