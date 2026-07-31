package com.commercex.repository;

import com.commercex.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, UUID> {
    boolean existsByWishlistIdAndProductId(UUID wishlistId, UUID productId);
}
