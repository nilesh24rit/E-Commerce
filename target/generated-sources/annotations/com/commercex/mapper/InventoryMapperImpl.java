package com.commercex.mapper;

import com.commercex.dto.CreateInventoryRequest;
import com.commercex.dto.InventoryResponse;
import com.commercex.dto.UpdateInventoryRequest;
import com.commercex.entity.Inventory;
import com.commercex.entity.Product;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-16T05:35:35+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class InventoryMapperImpl implements InventoryMapper {

    @Override
    public Inventory toEntity(CreateInventoryRequest request) {
        if ( request == null ) {
            return null;
        }

        Inventory.InventoryBuilder inventory = Inventory.builder();

        inventory.availableQuantity( request.getAvailableQuantity() );
        inventory.reorderLevel( request.getReorderLevel() );
        inventory.warehouseLocation( request.getWarehouseLocation() );

        return inventory.build();
    }

    @Override
    public InventoryResponse toDto(Inventory inventory) {
        if ( inventory == null ) {
            return null;
        }

        InventoryResponse.InventoryResponseBuilder inventoryResponse = InventoryResponse.builder();

        inventoryResponse.productId( inventoryProductId( inventory ) );
        inventoryResponse.productName( inventoryProductName( inventory ) );
        inventoryResponse.id( inventory.getId() );
        inventoryResponse.availableQuantity( inventory.getAvailableQuantity() );
        inventoryResponse.reservedQuantity( inventory.getReservedQuantity() );
        inventoryResponse.soldQuantity( inventory.getSoldQuantity() );
        inventoryResponse.reorderLevel( inventory.getReorderLevel() );
        inventoryResponse.warehouseLocation( inventory.getWarehouseLocation() );
        inventoryResponse.lastRestockedAt( inventory.getLastRestockedAt() );
        inventoryResponse.createdAt( inventory.getCreatedAt() );
        inventoryResponse.updatedAt( inventory.getUpdatedAt() );

        return inventoryResponse.build();
    }

    @Override
    public void updateEntityFromDto(UpdateInventoryRequest request, Inventory inventory) {
        if ( request == null ) {
            return;
        }

        if ( request.getAvailableQuantity() != null ) {
            inventory.setAvailableQuantity( request.getAvailableQuantity() );
        }
        if ( request.getReorderLevel() != null ) {
            inventory.setReorderLevel( request.getReorderLevel() );
        }
        if ( request.getWarehouseLocation() != null ) {
            inventory.setWarehouseLocation( request.getWarehouseLocation() );
        }
    }

    private UUID inventoryProductId(Inventory inventory) {
        if ( inventory == null ) {
            return null;
        }
        Product product = inventory.getProduct();
        if ( product == null ) {
            return null;
        }
        UUID id = product.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String inventoryProductName(Inventory inventory) {
        if ( inventory == null ) {
            return null;
        }
        Product product = inventory.getProduct();
        if ( product == null ) {
            return null;
        }
        String name = product.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
