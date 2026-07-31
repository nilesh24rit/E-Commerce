package com.commercex.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class WishlistResponse {
    private UUID id;
    private UUID userId;
    private List<WishlistItemResponse> items;
}
