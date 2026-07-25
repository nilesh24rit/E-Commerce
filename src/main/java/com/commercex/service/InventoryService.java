package com.commercex.service;

import com.commercex.dto.*;
import java.util.List;
import java.util.UUID;

public interface InventoryService {
    InventoryResponse createInventory(CreateInventoryRequest request);
    InventoryResponse updateInventory(UUID id, UpdateInventoryRequest request);
    InventoryResponse getInventoryByProductId(UUID productId);
    InventoryResponse getInventoryById(UUID id);
    List<InventoryResponse> getAllInventory();
    
    InventoryResponse restockProduct(StockOperationRequest request);
    InventoryResponse reserveStock(StockOperationRequest request);
    InventoryResponse releaseStock(StockOperationRequest request);
    InventoryResponse deductStock(StockOperationRequest request);
    
    List<InventoryResponse> getLowStockProducts();
    InventorySummaryResponse getInventorySummary();
}
