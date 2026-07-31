package com.commercex.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class AddWishlistItemRequest {
    @NotNull(message = "Product ID is required")
    private UUID productId;
}
