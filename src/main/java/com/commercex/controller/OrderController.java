package com.commercex.controller;

import com.commercex.dto.ApiResponse;
import com.commercex.dto.CreateOrderRequest;
import com.commercex.dto.OrderResponse;
import com.commercex.dto.OrderSummaryResponse;
import com.commercex.dto.UpdateOrderStatusRequest;
import com.commercex.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Management", description = "Order processing APIs")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Place a new order from current cart")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return new ResponseEntity<>(ApiResponse.success(response, "Order created successfully"), HttpStatus.CREATED);
    }

    @Operation(summary = "Get current user orders")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderSummaryResponse>>> getMyOrders() {
        List<OrderSummaryResponse> response = orderService.getMyOrders();
        return ResponseEntity.ok(ApiResponse.success(response, "Orders retrieved successfully"));
    }

    @Operation(summary = "Get order details by order number")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    @GetMapping("/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByOrderNumber(@PathVariable String orderNumber) {
        OrderResponse response = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(response, "Order retrieved successfully"));
    }

    @Operation(summary = "Get all orders (Admin only)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<List<OrderSummaryResponse>>> getAllOrders() {
        List<OrderSummaryResponse> response = orderService.getAllOrders();
        return ResponseEntity.ok(ApiResponse.success(response, "All orders retrieved successfully"));
    }

    @Operation(summary = "Update order status (Admin only)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{orderNumber}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable String orderNumber,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse response = orderService.updateOrderStatus(orderNumber, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Order status updated successfully"));
    }

    @Operation(summary = "Cancel an order")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    @DeleteMapping("/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable String orderNumber) {
        OrderResponse response = orderService.cancelOrder(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(response, "Order cancelled successfully"));
    }
}
