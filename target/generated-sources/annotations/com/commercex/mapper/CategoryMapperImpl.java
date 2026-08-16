package com.commercex.mapper;

import com.commercex.dto.CategoryResponse;
import com.commercex.dto.CreateCategoryRequest;
import com.commercex.dto.UpdateCategoryRequest;
import com.commercex.entity.Category;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-17T05:04:32+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class CategoryMapperImpl implements CategoryMapper {

    @Override
    public Category toEntity(CreateCategoryRequest request) {
        if ( request == null ) {
            return null;
        }

        Category.CategoryBuilder category = Category.builder();

        category.name( request.getName() );
        category.slug( request.getSlug() );
        category.description( request.getDescription() );
        category.imageUrl( request.getImageUrl() );

        return category.build();
    }

    @Override
    public CategoryResponse toDto(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryResponse.CategoryResponseBuilder categoryResponse = CategoryResponse.builder();

        categoryResponse.id( category.getId() );
        categoryResponse.name( category.getName() );
        categoryResponse.slug( category.getSlug() );
        categoryResponse.description( category.getDescription() );
        categoryResponse.imageUrl( category.getImageUrl() );
        categoryResponse.active( category.isActive() );
        categoryResponse.createdAt( category.getCreatedAt() );
        categoryResponse.updatedAt( category.getUpdatedAt() );

        return categoryResponse.build();
    }

    @Override
    public void updateEntityFromDto(UpdateCategoryRequest request, Category category) {
        if ( request == null ) {
            return;
        }

        if ( request.getName() != null ) {
            category.setName( request.getName() );
        }
        if ( request.getSlug() != null ) {
            category.setSlug( request.getSlug() );
        }
        if ( request.getDescription() != null ) {
            category.setDescription( request.getDescription() );
        }
        if ( request.getImageUrl() != null ) {
            category.setImageUrl( request.getImageUrl() );
        }
        if ( request.getActive() != null ) {
            category.setActive( request.getActive() );
        }
    }
}
