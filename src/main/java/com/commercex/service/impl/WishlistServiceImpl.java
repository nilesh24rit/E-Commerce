package com.commercex.service.impl;

import com.commercex.dto.AddWishlistItemRequest;
import com.commercex.dto.WishlistResponse;
import com.commercex.entity.Product;
import com.commercex.entity.User;
import com.commercex.entity.Wishlist;
import com.commercex.entity.WishlistItem;
import com.commercex.exception.DuplicateWishlistItemException;
import com.commercex.exception.ResourceNotFoundException;
import com.commercex.mapper.WishlistMapper;
import com.commercex.repository.ProductRepository;
import com.commercex.repository.WishlistRepository;
import com.commercex.repository.WishlistItemRepository;
import com.commercex.service.UserService;
import com.commercex.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final WishlistMapper wishlistMapper;

    @Override
    @Transactional
    public WishlistResponse getMyWishlist() {
        return wishlistMapper.toDto(getOrCreateWishlist());
    }

    @Override
    @Transactional
    public WishlistResponse addProductToWishlist(AddWishlistItemRequest request) {
        Wishlist wishlist = getOrCreateWishlist();
        
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
                
        if (!product.isActive()) {
            throw new IllegalArgumentException("Cannot add an inactive product to wishlist");
        }
        
        if (wishlistItemRepository.existsByWishlistIdAndProductId(wishlist.getId(), product.getId())) {
            throw new DuplicateWishlistItemException("Product is already in your wishlist");
        }
        
        WishlistItem item = WishlistItem.builder()
                .wishlist(wishlist)
                .product(product)
                .build();
                
        wishlist.getItems().add(item);
        
        return wishlistMapper.toDto(wishlistRepository.save(wishlist));
    }

    @Override
    @Transactional
    public WishlistResponse removeProductFromWishlist(UUID productId) {
        Wishlist wishlist = getOrCreateWishlist();
        
        wishlist.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        
        return wishlistMapper.toDto(wishlistRepository.save(wishlist));
    }

    @Override
    @Transactional
    public void clearWishlist() {
        Wishlist wishlist = getOrCreateWishlist();
        wishlist.getItems().clear();
        wishlistRepository.save(wishlist);
    }

    private Wishlist getOrCreateWishlist() {
        User currentUser = userService.getCurrentUser();
        return wishlistRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> wishlistRepository.save(Wishlist.builder().user(currentUser).build()));
    }
}
