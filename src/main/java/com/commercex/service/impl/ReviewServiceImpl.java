package com.commercex.service.impl;

import com.commercex.dto.CreateReviewRequest;
import com.commercex.dto.ReviewResponse;
import com.commercex.dto.UpdateReviewRequest;
import com.commercex.entity.Order;
import com.commercex.entity.Product;
import com.commercex.entity.Review;
import com.commercex.entity.User;
import com.commercex.exception.DuplicateReviewException;
import com.commercex.exception.ResourceNotFoundException;
import com.commercex.exception.ReviewNotAllowedException;
import com.commercex.event.ReviewAddedEvent;
import com.commercex.mapper.ReviewMapper;
import com.commercex.repository.OrderRepository;
import com.commercex.repository.ProductRepository;
import com.commercex.repository.ReviewRepository;
import com.commercex.service.ReviewService;
import com.commercex.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ReviewMapper reviewMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ReviewResponse addReview(CreateReviewRequest request) {
        log.info("Adding review for product: {}", request.getProductId());
        
        User currentUser = userService.getCurrentUser();
        
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
                
        if (reviewRepository.existsByProductIdAndUserId(product.getId(), currentUser.getId())) {
            throw new DuplicateReviewException("You have already reviewed this product");
        }
        
        boolean verifiedPurchase = hasPurchasedProduct(currentUser.getId(), product.getId());
        
        if (!verifiedPurchase) {
            throw new ReviewNotAllowedException("You can only review products you have purchased");
        }
        
        Review review = Review.builder()
                .product(product)
                .user(currentUser)
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .verifiedPurchase(true)
                .build();
                
        Review savedReview = reviewRepository.save(review);
        updateProductRating(product.getId());
        
        eventPublisher.publishEvent(new ReviewAddedEvent(product.getId(), currentUser.getEmail()));
        
        return reviewMapper.toDto(savedReview);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(UUID reviewId, UpdateReviewRequest request) {
        log.info("Updating review: {}", reviewId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
                
        User currentUser = userService.getCurrentUser();
        if (!review.getUser().getId().equals(currentUser.getId())) {
            throw new ReviewNotAllowedException("You can only update your own reviews");
        }
        
        if (request.getRating() != null) review.setRating(request.getRating());
        if (request.getTitle() != null) review.setTitle(request.getTitle());
        if (request.getComment() != null) review.setComment(request.getComment());
        
        Review savedReview = reviewRepository.save(review);
        updateProductRating(review.getProduct().getId());
        
        return reviewMapper.toDto(savedReview);
    }

    @Override
    @Transactional
    public void deleteReview(UUID reviewId) {
        log.info("Deleting review: {}", reviewId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
                
        User currentUser = userService.getCurrentUser();
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        
        if (!review.getUser().getId().equals(currentUser.getId()) && !isAdmin) {
            throw new ReviewNotAllowedException("You don't have permission to delete this review");
        }
        
        UUID productId = review.getProduct().getId();
        reviewRepository.delete(review);
        // flush to ensure average rating calculation is correct
        reviewRepository.flush();
        updateProductRating(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> getProductReviews(UUID productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }
    
    private boolean hasPurchasedProduct(UUID userId, UUID productId) {
        List<Order> userOrders = orderRepository.findByUserId(userId);
        return userOrders.stream()
                .flatMap(order -> order.getItems().stream())
                .anyMatch(item -> item.getProduct().getId().equals(productId));
    }
    
    private void updateProductRating(UUID productId) {
        Double avgRating = reviewRepository.getAverageRatingForProduct(productId);
        Long totalReviews = reviewRepository.getTotalReviewsForProduct(productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
                
        product.setAverageRating(avgRating != null ? BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP).doubleValue() : 0.0);
        product.setTotalReviews(totalReviews != null ? totalReviews : 0L);
        
        productRepository.save(product);
    }
}
