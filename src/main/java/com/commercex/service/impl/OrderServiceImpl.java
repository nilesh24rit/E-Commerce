package com.commercex.service.impl;

import com.commercex.dto.*;
import com.commercex.entity.*;
import com.commercex.entity.enums.OrderStatus;
import com.commercex.entity.enums.PaymentStatus;
import com.commercex.event.CouponAppliedEvent;
import com.commercex.event.OrderCreatedEvent;
import com.commercex.exception.*;
import com.commercex.mapper.OrderMapper;
import com.commercex.repository.CartRepository;
import com.commercex.repository.OrderRepository;
import com.commercex.service.CartService;
import com.commercex.service.InventoryService;
import com.commercex.service.CouponService;
import com.commercex.service.OrderService;
import com.commercex.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final InventoryService inventoryService;
    private final UserService userService;
    private final CouponService couponService;
    private final OrderMapper orderMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating new order for current user");
        User currentUser = userService.getCurrentUser();

        Cart cart = cartRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new EmptyCartException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new EmptyCartException("Cannot create order with an empty cart");
        }

        // Validate Inventory
        for (CartItem item : cart.getItems()) {
            InventoryResponse inventory = inventoryService.getInventoryByProductId(item.getProduct().getId());
            if (inventory.getAvailableQuantity() < item.getQuantity()) {
                throw new ProductOutOfStockException("Product " + item.getProduct().getName() + " is out of stock. Requested: " + item.getQuantity() + ", Available: " + inventory.getAvailableQuantity());
            }
        }

        // Generate Order Number
        String orderNumber = generateOrderNumber();

        // Calculate Totals
        BigDecimal subtotal = cart.getItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discount = BigDecimal.ZERO;

        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            CouponValidationResponse validation = couponService.validateCoupon(request.getCouponCode(), subtotal);
            discount = validation.getDiscountAmount();
            couponService.incrementUsage(request.getCouponCode());
        }

        BigDecimal shippingCharge = BigDecimal.valueOf(10.00); // Fixed for now
        BigDecimal totalAmount = subtotal.subtract(discount).add(shippingCharge);

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(currentUser)
                .subtotal(subtotal)
                .discount(discount)
                .shippingCharge(shippingCharge)
                .totalAmount(totalAmount)
                .orderStatus(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        List<OrderItem> orderItems = cart.getItems().stream().map(cartItem -> 
            OrderItem.builder()
                .order(order)
                .product(cartItem.getProduct())
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getUnitPrice())
                .subtotal(cartItem.getSubtotal())
                .build()
        ).collect(Collectors.toList());

        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // Reserve/Deduct Inventory
        for (CartItem item : cart.getItems()) {
            StockOperationRequest stockRequest = new StockOperationRequest();
            stockRequest.setProductId(item.getProduct().getId());
            stockRequest.setQuantity(item.getQuantity());
            
            // First reserve, then deduct to maintain invariant in InventoryService
            inventoryService.reserveStock(stockRequest);
            inventoryService.deductStock(stockRequest);
        }

        // Clear shopping cart
        cartService.clearCart();

        eventPublisher.publishEvent(new OrderCreatedEvent(savedOrder.getId(), currentUser.getEmail()));
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            eventPublisher.publishEvent(new CouponAppliedEvent(request.getCouponCode(), currentUser.getEmail()));
        }

        log.info("Order created successfully: {}", orderNumber);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getMyOrders() {
        User currentUser = userService.getCurrentUser();
        return orderRepository.findByUserId(currentUser.getId()).stream()
                .map(orderMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        Order order = getOrderEntity(orderNumber);
        
        // Ensure user can only view their own orders unless they are ADMIN
        User currentUser = userService.getCurrentUser();
        if (!order.getUser().getId().equals(currentUser.getId()) && currentUser.getRoles().stream().noneMatch(r -> r.getName().name().equals("ROLE_ADMIN"))) {
            throw new OrderNotFoundException("Order not found or you don't have access");
        }
        
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(String orderNumber, UpdateOrderStatusRequest request) {
        log.info("Updating order status for {}: {}", orderNumber, request.getStatus());
        Order order = getOrderEntity(orderNumber);
        order.setOrderStatus(request.getStatus());
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(String orderNumber) {
        log.info("Cancelling order {}", orderNumber);
        Order order = getOrderEntity(orderNumber);

        // Allow Customer to cancel only their own order, and only if PENDING or CONFIRMED
        User currentUser = userService.getCurrentUser();
        if (!order.getUser().getId().equals(currentUser.getId()) && currentUser.getRoles().stream().noneMatch(r -> r.getName().name().equals("ROLE_ADMIN"))) {
            throw new OrderNotFoundException("Order not found or you don't have access");
        }

        if (order.getOrderStatus() == OrderStatus.SHIPPED || order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new InvalidCartOperationException("Cannot cancel an order that has already been shipped or delivered");
        }

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new InvalidCartOperationException("Order is already cancelled");
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        
        // Restore Inventory
        for (OrderItem item : order.getItems()) {
            StockOperationRequest stockRequest = new StockOperationRequest();
            stockRequest.setProductId(item.getProduct().getId());
            stockRequest.setQuantity(item.getQuantity());
            inventoryService.restockProduct(stockRequest);
        }

        return orderMapper.toDto(orderRepository.save(order));
    }

    private Order getOrderEntity(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with number: " + orderNumber));
    }

    private String generateOrderNumber() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomStr = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + dateStr + "-" + randomStr;
    }
}
