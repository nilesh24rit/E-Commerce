package com.commercex.service.impl;

import com.commercex.dto.*;
import com.commercex.entity.Inventory;
import com.commercex.entity.Product;
import com.commercex.exception.*;
import com.commercex.mapper.InventoryMapper;
import com.commercex.repository.InventoryRepository;
import com.commercex.repository.ProductRepository;
import com.commercex.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryMapper inventoryMapper;

    @Override
    @Transactional
    public InventoryResponse createInventory(CreateInventoryRequest request) {
        log.info("Creating inventory for Product ID: {}", request.getProductId());
        
        if (inventoryRepository.existsByProductId(request.getProductId())) {
            throw new DuplicateInventoryException("Inventory already exists for product: " + request.getProductId());
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + request.getProductId()));

        Inventory inventory = inventoryMapper.toEntity(request);
        inventory.setProduct(product);
        if (request.getAvailableQuantity() != null && request.getAvailableQuantity() > 0) {
            inventory.setLastRestockedAt(LocalDateTime.now());
        }

        Inventory saved = inventoryRepository.save(inventory);
        return inventoryMapper.toDto(saved);
    }

    @Override
    @Transactional
    public InventoryResponse updateInventory(UUID id, UpdateInventoryRequest request) {
        log.info("Updating inventory with ID: {}", id);
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found with ID: " + id));

        inventoryMapper.updateEntityFromDto(request, inventory);
        return inventoryMapper.toDto(inventoryRepository.save(inventory));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(UUID productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for Product ID: " + productId));
        return inventoryMapper.toDto(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryById(UUID id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found with ID: " + id));
        return inventoryMapper.toDto(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(inventoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InventoryResponse restockProduct(StockOperationRequest request) {
        log.info("Restocking product ID: {} with quantity: {}", request.getProductId(), request.getQuantity());
        Inventory inventory = getInventoryEntityByProductId(request.getProductId());

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + request.getQuantity());
        inventory.setLastRestockedAt(LocalDateTime.now());
        return inventoryMapper.toDto(inventoryRepository.save(inventory));
    }

    @Override
    @Transactional
    public InventoryResponse reserveStock(StockOperationRequest request) {
        log.info("Reserving stock for product ID: {} quantity: {}", request.getProductId(), request.getQuantity());
        Inventory inventory = getInventoryEntityByProductId(request.getProductId());

        if (inventory.getAvailableQuantity() < request.getQuantity()) {
            throw new InsufficientStockException("Not enough available stock to reserve");
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - request.getQuantity());
        inventory.setReservedQuantity(inventory.getReservedQuantity() + request.getQuantity());
        
        return inventoryMapper.toDto(inventoryRepository.save(inventory));
    }

    @Override
    @Transactional
    public InventoryResponse releaseStock(StockOperationRequest request) {
        log.info("Releasing reserved stock for product ID: {} quantity: {}", request.getProductId(), request.getQuantity());
        Inventory inventory = getInventoryEntityByProductId(request.getProductId());

        if (inventory.getReservedQuantity() < request.getQuantity()) {
            throw new InvalidStockOperationException("Cannot release more than reserved stock");
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() - request.getQuantity());
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + request.getQuantity());
        
        return inventoryMapper.toDto(inventoryRepository.save(inventory));
    }

    @Override
    @Transactional
    public InventoryResponse deductStock(StockOperationRequest request) {
        log.info("Deducting reserved stock for product ID: {} quantity: {}", request.getProductId(), request.getQuantity());
        Inventory inventory = getInventoryEntityByProductId(request.getProductId());

        if (inventory.getReservedQuantity() < request.getQuantity()) {
            throw new InvalidStockOperationException("Cannot deduct more than reserved stock. Ensure stock is reserved first.");
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() - request.getQuantity());
        inventory.setSoldQuantity(inventory.getSoldQuantity() + request.getQuantity());
        
        return inventoryMapper.toDto(inventoryRepository.save(inventory));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStockProducts() {
        return inventoryRepository.findLowStockProducts().stream()
                .map(inventoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InventorySummaryResponse getInventorySummary() {
        List<Inventory> allInventory = inventoryRepository.findAll();
        
        long totalProducts = allInventory.size();
        long totalAvailable = allInventory.stream().mapToLong(Inventory::getAvailableQuantity).sum();
        long totalReserved = allInventory.stream().mapToLong(Inventory::getReservedQuantity).sum();
        long totalSold = allInventory.stream().mapToLong(Inventory::getSoldQuantity).sum();
        long lowStockCount = inventoryRepository.findLowStockProducts().size();

        return InventorySummaryResponse.builder()
                .totalProducts(totalProducts)
                .totalAvailableItems(totalAvailable)
                .totalReservedItems(totalReserved)
                .totalSoldItems(totalSold)
                .lowStockProductsCount(lowStockCount)
                .build();
    }

    private Inventory getInventoryEntityByProductId(UUID productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for Product ID: " + productId));
    }
}
