package com.commercex.service;

import com.commercex.dto.AddToCartRequest;
import com.commercex.dto.CartResponse;
import com.commercex.dto.UpdateCartItemRequest;

import java.util.UUID;

public interface CartService {
    CartResponse getCurrentUserCart();
    CartResponse addItemToCart(AddToCartRequest request);
    CartResponse updateCartItemQuantity(UUID cartItemId, UpdateCartItemRequest request);
    CartResponse removeCartItem(UUID cartItemId);
    CartResponse clearCart();
}
