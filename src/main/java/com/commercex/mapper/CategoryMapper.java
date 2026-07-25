package com.commercex.mapper;

import com.commercex.dto.CategoryResponse;
import com.commercex.dto.CreateCategoryRequest;
import com.commercex.dto.UpdateCategoryRequest;
import com.commercex.entity.Category;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {
    Category toEntity(CreateCategoryRequest request);
    CategoryResponse toDto(Category category);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateCategoryRequest request, @MappingTarget Category category);
}
