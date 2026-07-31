package com.commercex.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReviewResponse {
    private UUID id;
    private UUID productId;
    private UUID userId;
    private String userName;
    private Integer rating;
    private String title;
    private String comment;
    private boolean verifiedPurchase;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
