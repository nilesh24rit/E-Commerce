package com.commercex.controller;

import com.commercex.dto.*;
import com.commercex.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory Management APIs")
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "Create inventory for a product")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SELLER')")
    @PostMapping
    public ResponseEntity<ApiResponse<InventoryResponse>> createInventory(@Valid @RequestBody CreateInventoryRequest request) {
        InventoryResponse response = inventoryService.createInventory(request);
        return new ResponseEntity<>(ApiResponse.success(response, "Inventory created successfully"), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an inventory record")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SELLER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateInventory(@PathVariable UUID id, @Valid @RequestBody UpdateInventoryRequest request) {
        InventoryResponse response = inventoryService.updateInventory(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Inventory updated successfully"));
    }

    @Operation(summary = "Get all inventory records")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SELLER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getAllInventory() {
        List<InventoryResponse> response = inventoryService.getAllInventory();
        return ResponseEntity.ok(ApiResponse.success(response, "Inventory list retrieved successfully"));
    }

    @Operation(summary = "Get inventory by inventory ID")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SELLER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryById(@PathVariable UUID id) {
        InventoryResponse response = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Inventory retrieved successfully"));
    }

    @Operation(summary = "Get inventory by product ID")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SELLER')")
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryByProductId(@PathVariable UUID productId) {
        InventoryResponse response = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(ApiResponse.success(response, "Inventory retrieved successfully"));
    }

    @Operation(summary = "Restock a product")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SELLER')")
    @PostMapping("/restock")
    public ResponseEntity<ApiResponse<InventoryResponse>> restockProduct(@Valid @RequestBody StockOperationRequest request) {
        InventoryResponse response = inventoryService.restockProduct(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock successfully restocked"));
    }

    @Operation(summary = "Reserve stock for checkout")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SELLER')")
    @PostMapping("/reserve")
    public ResponseEntity<ApiResponse<InventoryResponse>> reserveStock(@Valid @RequestBody StockOperationRequest request) {
        InventoryResponse response = inventoryService.reserveStock(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock successfully reserved"));
    }

    @Operation(summary = "Release reserved stock")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SELLER')")
    @PostMapping("/release")
    public ResponseEntity<ApiResponse<InventoryResponse>> releaseStock(@Valid @RequestBody StockOperationRequest request) {
        InventoryResponse response = inventoryService.releaseStock(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock successfully released"));
    }

    @Operation(summary = "Deduct reserved stock (sold)")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SELLER')")
    @PostMapping("/deduct")
    public ResponseEntity<ApiResponse<InventoryResponse>> deductStock(@Valid @RequestBody StockOperationRequest request) {
        InventoryResponse response = inventoryService.deductStock(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock successfully deducted"));
    }

    @Operation(summary = "Get low stock products")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SELLER')")
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getLowStockProducts() {
        List<InventoryResponse> response = inventoryService.getLowStockProducts();
        return ResponseEntity.ok(ApiResponse.success(response, "Low stock products retrieved"));
    }
    
    @Operation(summary = "Get inventory summary")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SELLER')")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<InventorySummaryResponse>> getInventorySummary() {
        InventorySummaryResponse response = inventoryService.getInventorySummary();
        return ResponseEntity.ok(ApiResponse.success(response, "Inventory summary retrieved"));
    }
}
