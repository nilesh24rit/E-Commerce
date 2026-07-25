package com.commercex.mapper;

import com.commercex.dto.CreateProductRequest;
import com.commercex.dto.ProductResponse;
import com.commercex.dto.UpdateProductRequest;
import com.commercex.entity.Product;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {CategoryMapper.class})
public interface ProductMapper {
    
    @Mapping(target = "category", ignore = true) // Handled in service
    Product toEntity(CreateProductRequest request);
    
    ProductResponse toDto(Product product);
    
    @Mapping(target = "category", ignore = true) // Handled in service
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateProductRequest request, @MappingTarget Product product);
}
