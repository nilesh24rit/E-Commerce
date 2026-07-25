package com.commercex.repository;

import com.commercex.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
    Optional<Inventory> findByProductId(UUID productId);
    boolean existsByProductId(UUID productId);
    
    @Query("SELECT i FROM Inventory i WHERE i.availableQuantity <= i.reorderLevel")
    List<Inventory> findLowStockProducts();
}
