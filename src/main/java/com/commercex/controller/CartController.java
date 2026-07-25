package com.commercex.controller;

import com.commercex.dto.AddToCartRequest;
import com.commercex.dto.ApiResponse;
import com.commercex.dto.CartResponse;
import com.commercex.dto.UpdateCartItemRequest;
import com.commercex.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "Shopping Cart Management APIs")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get current user cart")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        CartResponse response = cartService.getCurrentUserCart();
        return ResponseEntity.ok(ApiResponse.success(response, "Cart retrieved successfully"));
    }

    @Operation(summary = "Add item to cart")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(@Valid @RequestBody AddToCartRequest request) {
        CartResponse response = cartService.addItemToCart(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Item added to cart"));
    }

    @Operation(summary = "Update item quantity")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @PutMapping("/items/{id}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItemQuantity(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartResponse response = cartService.updateCartItemQuantity(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Cart item updated"));
    }

    @Operation(summary = "Remove item from cart")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(@PathVariable UUID id) {
        CartResponse response = cartService.removeCartItem(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Item removed from cart"));
    }

    @Operation(summary = "Clear the entire cart")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @DeleteMapping
    public ResponseEntity<ApiResponse<CartResponse>> clearCart() {
        CartResponse response = cartService.clearCart();
        return ResponseEntity.ok(ApiResponse.success(response, "Cart cleared successfully"));
    }
}
