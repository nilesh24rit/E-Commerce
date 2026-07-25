package com.commercex.service.impl;

import com.commercex.dto.AddToCartRequest;
import com.commercex.dto.CartResponse;
import com.commercex.dto.InventoryResponse;
import com.commercex.entity.Cart;
import com.commercex.entity.CartItem;
import com.commercex.entity.Product;
import com.commercex.entity.User;
import com.commercex.exception.ProductOutOfStockException;
import com.commercex.mapper.CartMapper;
import com.commercex.repository.CartItemRepository;
import com.commercex.repository.CartRepository;
import com.commercex.repository.ProductRepository;
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
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserService userService;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private User user;
    private Cart cart;
    private Product product;
    private AddToCartRequest addRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());

        cart = new Cart();
        cart.setId(UUID.randomUUID());
        cart.setUser(user);
        cart.setItems(new ArrayList<>());

        product = new Product();
        product.setId(UUID.randomUUID());
        product.setPrice(BigDecimal.valueOf(100.00));
        product.setActive(true);

        addRequest = new AddToCartRequest();
        addRequest.setProductId(product.getId());
        addRequest.setQuantity(2);
    }

    @Test
    void addItemToCart_Success() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        
        InventoryResponse inventory = InventoryResponse.builder()
                .availableQuantity(10)
                .build();
        when(inventoryService.getInventoryByProductId(product.getId())).thenReturn(inventory);
        
        when(cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId()))
                .thenReturn(Optional.empty());
                
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        
        CartResponse cartResponse = CartResponse.builder().build();
        when(cartMapper.toDto(any(Cart.class))).thenReturn(cartResponse);

        CartResponse result = cartService.addItemToCart(addRequest);
        
        assertNotNull(result);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void addItemToCart_OutOfStock_ThrowsException() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(cartRepository.findByUserId(user.getId())).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        
        InventoryResponse inventory = InventoryResponse.builder()
                .availableQuantity(1) // Requesting 2
                .build();
        when(inventoryService.getInventoryByProductId(product.getId())).thenReturn(inventory);

        assertThrows(ProductOutOfStockException.class, () -> cartService.addItemToCart(addRequest));
    }
}
