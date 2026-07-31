package com.commercex.service;

import com.commercex.dto.CreateReviewRequest;
import com.commercex.dto.ReviewResponse;
import com.commercex.dto.UpdateReviewRequest;
import java.util.List;
import java.util.UUID;

public interface ReviewService {
    ReviewResponse addReview(CreateReviewRequest request);
    ReviewResponse updateReview(UUID reviewId, UpdateReviewRequest request);
    void deleteReview(UUID reviewId);
    List<ReviewResponse> getProductReviews(UUID productId);
}
