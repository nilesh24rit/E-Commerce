package com.commercex.repository;

import com.commercex.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByProductId(UUID productId);
    boolean existsByProductIdAndUserId(UUID productId, UUID userId);
    Optional<Review> findByProductIdAndUserId(UUID productId, UUID userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double getAverageRatingForProduct(UUID productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    Long getTotalReviewsForProduct(UUID productId);
}
