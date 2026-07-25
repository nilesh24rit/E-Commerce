package com.commercex.service.impl;

import com.commercex.dto.InventoryResponse;
import com.commercex.dto.StockOperationRequest;
import com.commercex.entity.Inventory;
import com.commercex.entity.Product;
import com.commercex.exception.InsufficientStockException;
import com.commercex.mapper.InventoryMapper;
import com.commercex.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryMapper inventoryMapper;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Inventory inventory;
    private Product product;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        product = new Product();
        product.setId(productId);

        inventory = new Inventory();
        inventory.setId(UUID.randomUUID());
        inventory.setProduct(product);
        inventory.setAvailableQuantity(50);
        inventory.setReservedQuantity(5);
        inventory.setSoldQuantity(10);
    }

    @Test
    void reserveStock_Success() {
        StockOperationRequest request = new StockOperationRequest();
        request.setProductId(productId);
        request.setQuantity(10);

        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);
        
        InventoryResponse response = InventoryResponse.builder()
                .availableQuantity(40)
                .reservedQuantity(15)
                .build();
        when(inventoryMapper.toDto(any(Inventory.class))).thenReturn(response);

        InventoryResponse result = inventoryService.reserveStock(request);

        assertNotNull(result);
        assertEquals(40, result.getAvailableQuantity());
        assertEquals(15, result.getReservedQuantity());
    }

    @Test
    void reserveStock_InsufficientStock_ThrowsException() {
        StockOperationRequest request = new StockOperationRequest();
        request.setProductId(productId);
        request.setQuantity(100);

        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        assertThrows(InsufficientStockException.class, () -> inventoryService.reserveStock(request));
    }
}
