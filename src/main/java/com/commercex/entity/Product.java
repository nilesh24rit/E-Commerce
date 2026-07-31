package com.commercex.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_slug", columnList = "slug", unique = true),
        @Index(name = "idx_product_sku", columnList = "sku", unique = true),
        @Index(name = "idx_product_category", columnList = "category_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Column(length = 2000)
    private String description;

    @Column(length = 500)
    private String shortDescription;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(length = 100)
    private String brand;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountPrice;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Column(length = 50)
    @Builder.Default
    private String status = "IN_STOCK"; 

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Builder.Default
    @Column(precision = 3, scale = 2)
    private Double averageRating = 0.0;

    @Builder.Default
    private Long totalReviews = 0L;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
