package com.commercex.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventorySummaryResponse {
    private long totalProducts;
    private long totalAvailableItems;
    private long totalReservedItems;
    private long totalSoldItems;
    private long lowStockProductsCount;
}
