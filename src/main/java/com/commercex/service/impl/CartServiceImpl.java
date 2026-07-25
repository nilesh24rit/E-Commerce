package com.commercex.service.impl;

import com.commercex.dto.*;
import com.commercex.entity.Cart;
import com.commercex.entity.CartItem;
import com.commercex.entity.Product;
import com.commercex.entity.User;
import com.commercex.exception.*;
import com.commercex.mapper.CartMapper;
import com.commercex.repository.CartItemRepository;
import com.commercex.repository.CartRepository;
import com.commercex.repository.ProductRepository;
import com.commercex.service.CartService;
import com.commercex.service.InventoryService;
import com.commercex.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserService userService;
    private final InventoryService inventoryService;
    private final CartMapper cartMapper;

    @Override
    @Transactional
    public CartResponse getCurrentUserCart() {
        Cart cart = getOrCreateCartForCurrentUser();
        return cartMapper.toDto(cart);
    }

    @Override
    @Transactional
    public CartResponse addItemToCart(AddToCartRequest request) {
        log.info("Adding product {} to cart with quantity {}", request.getProductId(), request.getQuantity());
        Cart cart = getOrCreateCartForCurrentUser();
        
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.isActive()) {
            throw new InvalidCartOperationException("Cannot add an inactive product to cart");
        }

        // Check Inventory
        InventoryResponse inventory = inventoryService.getInventoryByProductId(product.getId());
        
        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());
        
        int newQuantity = request.getQuantity();
        if (existingItemOpt.isPresent()) {
            newQuantity += existingItemOpt.get().getQuantity();
        }

        if (inventory.getAvailableQuantity() < newQuantity) {
            throw new ProductOutOfStockException("Not enough stock available. Requested: " + newQuantity + ", Available: " + inventory.getAvailableQuantity());
        }

        BigDecimal effectivePrice = product.getDiscountPrice() != null ? product.getDiscountPrice() : product.getPrice();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(newQuantity);
            existingItem.setSubtotal(effectivePrice.multiply(BigDecimal.valueOf(newQuantity)));
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .unitPrice(effectivePrice)
                    .subtotal(effectivePrice.multiply(BigDecimal.valueOf(request.getQuantity())))
                    .build();
            cart.getItems().add(newItem);
        }

        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toDto(savedCart);
    }

    @Override
    @Transactional
    public CartResponse updateCartItemQuantity(UUID cartItemId, UpdateCartItemRequest request) {
        log.info("Updating cart item {} to quantity {}", cartItemId, request.getQuantity());
        Cart cart = getOrCreateCartForCurrentUser();

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new InvalidCartOperationException("Cart item does not belong to your cart");
        }

        InventoryResponse inventory = inventoryService.getInventoryByProductId(item.getProduct().getId());
        if (inventory.getAvailableQuantity() < request.getQuantity()) {
            throw new ProductOutOfStockException("Not enough stock available. Requested: " + request.getQuantity() + ", Available: " + inventory.getAvailableQuantity());
        }

        item.setQuantity(request.getQuantity());
        item.setSubtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(request.getQuantity())));

        cartItemRepository.save(item);
        
        // Refresh cart to recalculate totals safely
        return cartMapper.toDto(cartRepository.findById(cart.getId()).get());
    }

    @Override
    @Transactional
    public CartResponse removeCartItem(UUID cartItemId) {
        log.info("Removing cart item {}", cartItemId);
        Cart cart = getOrCreateCartForCurrentUser();

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new InvalidCartOperationException("Cart item does not belong to your cart");
        }

        cart.getItems().remove(item);
        cartItemRepository.delete(item);

        return cartMapper.toDto(cartRepository.save(cart));
    }

    @Override
    @Transactional
    public CartResponse clearCart() {
        log.info("Clearing current user cart");
        Cart cart = getOrCreateCartForCurrentUser();
        cart.getItems().clear();
        return cartMapper.toDto(cartRepository.save(cart));
    }

    private Cart getOrCreateCartForCurrentUser() {
        User currentUser = userService.getCurrentUser();
        return cartRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(currentUser)
                            .build();
                    return cartRepository.save(newCart);
                });
    }
}
