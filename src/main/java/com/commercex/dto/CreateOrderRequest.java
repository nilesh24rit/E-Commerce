package com.commercex.dto;

import lombok.Data;

@Data
public class CreateOrderRequest {
    private String shippingAddress;
    private String billingAddress;
    private String orderNotes;
}
