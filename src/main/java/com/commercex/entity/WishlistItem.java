package com.commercex.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wishlist_items", indexes = {
        @Index(name = "idx_wishlist_item_product", columnList = "wishlist_id, product_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wishlist_id", nullable = false)
    private Wishlist wishlist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
