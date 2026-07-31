package com.commercex.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_review_product_user", columnList = "product_id, user_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 200)
    private String title;

    @Column(length = 2000)
    private String comment;

    @Builder.Default
    @Column(nullable = false)
    private boolean verifiedPurchase = false;
}
