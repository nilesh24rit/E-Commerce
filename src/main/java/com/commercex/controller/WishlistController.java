package com.commercex.controller;

import com.commercex.dto.AddWishlistItemRequest;
import com.commercex.dto.ApiResponse;
import com.commercex.dto.WishlistResponse;
import com.commercex.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist Management", description = "Wishlist operations")
public class WishlistController {

    private final WishlistService wishlistService;

    @Operation(summary = "Get my wishlist")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @GetMapping
    public ResponseEntity<ApiResponse<WishlistResponse>> getMyWishlist() {
        return ResponseEntity.ok(ApiResponse.success(wishlistService.getMyWishlist(), "Wishlist retrieved"));
    }

    @Operation(summary = "Add a product to wishlist")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<WishlistResponse>> addProductToWishlist(@Valid @RequestBody AddWishlistItemRequest request) {
        WishlistResponse response = wishlistService.addProductToWishlist(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Product added to wishlist"));
    }

    @Operation(summary = "Remove a product from wishlist")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<WishlistResponse>> removeProductFromWishlist(@PathVariable UUID productId) {
        WishlistResponse response = wishlistService.removeProductFromWishlist(productId);
        return ResponseEntity.ok(ApiResponse.success(response, "Product removed from wishlist"));
    }
}
