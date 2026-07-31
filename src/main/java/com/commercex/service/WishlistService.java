package com.commercex.service;

import com.commercex.dto.AddWishlistItemRequest;
import com.commercex.dto.WishlistResponse;
import java.util.UUID;

public interface WishlistService {
    WishlistResponse getMyWishlist();
    WishlistResponse addProductToWishlist(AddWishlistItemRequest request);
    WishlistResponse removeProductFromWishlist(UUID productId);
    void clearWishlist();
}
