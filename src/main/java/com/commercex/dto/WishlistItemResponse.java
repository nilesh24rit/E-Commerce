package com.commercex.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class WishlistItemResponse {
    private UUID id;
    private ProductResponse product;
    private LocalDateTime createdAt;
}
