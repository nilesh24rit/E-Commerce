package com.commercex.service;

import com.commercex.dto.CreateOrderRequest;
import com.commercex.dto.OrderResponse;
import com.commercex.dto.OrderSummaryResponse;
import com.commercex.dto.UpdateOrderStatusRequest;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    List<OrderSummaryResponse> getMyOrders();
    OrderResponse getOrderByOrderNumber(String orderNumber);
    List<OrderSummaryResponse> getAllOrders();
    OrderResponse updateOrderStatus(String orderNumber, UpdateOrderStatusRequest request);
    OrderResponse cancelOrder(String orderNumber);
}
