package com.commercex.mapper;

import com.commercex.dto.CreateInventoryRequest;
import com.commercex.dto.InventoryResponse;
import com.commercex.dto.UpdateInventoryRequest;
import com.commercex.entity.Inventory;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InventoryMapper {

    @Mapping(target = "product", ignore = true)
    Inventory toEntity(CreateInventoryRequest request);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    InventoryResponse toDto(Inventory inventory);

    @Mapping(target = "product", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateInventoryRequest request, @MappingTarget Inventory inventory);
}
