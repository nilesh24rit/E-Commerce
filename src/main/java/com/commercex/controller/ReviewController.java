package com.commercex.controller;

import com.commercex.dto.ApiResponse;
import com.commercex.dto.CreateReviewRequest;
import com.commercex.dto.ReviewResponse;
import com.commercex.dto.UpdateReviewRequest;
import com.commercex.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review Management", description = "Product review operations")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Add a review for a product")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADMIN', 'ROLE_SELLER')")
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> addReview(@Valid @RequestBody CreateReviewRequest request) {
        ReviewResponse response = reviewService.addReview(request);
        return new ResponseEntity<>(ApiResponse.success(response, "Review submitted successfully"), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing review")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADMIN', 'ROLE_SELLER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReviewRequest request) {
        ReviewResponse response = reviewService.updateReview(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Review updated successfully"));
    }

    @Operation(summary = "Delete a review")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADMIN', 'ROLE_SELLER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable UUID id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Review deleted successfully"));
    }
}
