package com.commercex.service.impl;

import com.commercex.dto.CreateOrderRequest;
import com.commercex.dto.InventoryResponse;
import com.commercex.dto.OrderResponse;
import com.commercex.entity.Cart;
import com.commercex.entity.CartItem;
import com.commercex.entity.Order;
import com.commercex.entity.Product;
import com.commercex.entity.User;
import com.commercex.exception.EmptyCartException;
import com.commercex.mapper.OrderMapper;
import com.commercex.repository.CartRepository;
import com.commercex.repository.OrderRepository;
import com.commercex.service.CartService;
import com.commercex.service.InventoryService;
import com.commercex.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartService cartService;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private UserService userService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private Cart cart;
    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());

        product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100.00));

        cart = new Cart();
        cart.setId(UUID.randomUUID());
        cart.setUser(user);
        cart.setItems(new ArrayList<>());

        cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(BigDecimal.valueOf(100.00));
        cartItem.setSubtotal(BigDecimal.valueOf(200.00));
        
        cart.getItems().add(cartItem);
    }

    @Test
    void createOrder_Success() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setShippingAddress("123 Main St");
        
        when(userService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        
        InventoryResponse inventory = InventoryResponse.builder()
                .availableQuantity(10)
                .build();
        when(inventoryService.getInventoryByProductId(product.getId())).thenReturn(inventory);
        
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });
        
        OrderResponse orderResponse = OrderResponse.builder()
                .orderNumber("ORD-TEST")
                .totalAmount(BigDecimal.valueOf(210.00))
                .build();
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderResponse);

        OrderResponse result = orderService.createOrder(request);
        
        assertNotNull(result);
        assertEquals("ORD-TEST", result.getOrderNumber());
        
        verify(inventoryService, times(1)).reserveStock(any());
        verify(inventoryService, times(1)).deductStock(any());
        verify(cartService, times(1)).clearCart();
    }

    @Test
    void createOrder_EmptyCart_ThrowsException() {
        CreateOrderRequest request = new CreateOrderRequest();
        cart.getItems().clear();
        
        when(userService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));

        assertThrows(EmptyCartException.class, () -> orderService.createOrder(request));
        
        verify(orderRepository, never()).save(any());
    }
}
