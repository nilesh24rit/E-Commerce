package com.commercex.mapper;

import com.commercex.dto.CreateProductRequest;
import com.commercex.dto.ProductResponse;
import com.commercex.dto.UpdateProductRequest;
import com.commercex.entity.Product;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-16T05:35:35+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public Product toEntity(CreateProductRequest request) {
        if ( request == null ) {
            return null;
        }

        Product.ProductBuilder product = Product.builder();

        product.name( request.getName() );
        product.slug( request.getSlug() );
        product.description( request.getDescription() );
        product.shortDescription( request.getShortDescription() );
        product.sku( request.getSku() );
        product.brand( request.getBrand() );
        product.price( request.getPrice() );
        product.discountPrice( request.getDiscountPrice() );
        product.quantity( request.getQuantity() );

        return product.build();
    }

    @Override
    public ProductResponse toDto(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductResponse.ProductResponseBuilder productResponse = ProductResponse.builder();

        productResponse.id( product.getId() );
        productResponse.name( product.getName() );
        productResponse.slug( product.getSlug() );
        productResponse.description( product.getDescription() );
        productResponse.shortDescription( product.getShortDescription() );
        productResponse.sku( product.getSku() );
        productResponse.brand( product.getBrand() );
        productResponse.price( product.getPrice() );
        productResponse.discountPrice( product.getDiscountPrice() );
        productResponse.quantity( product.getQuantity() );
        productResponse.status( product.getStatus() );
        productResponse.active( product.isActive() );
        productResponse.averageRating( product.getAverageRating() );
        productResponse.totalReviews( product.getTotalReviews() );
        productResponse.category( categoryMapper.toDto( product.getCategory() ) );
        productResponse.createdAt( product.getCreatedAt() );
        productResponse.updatedAt( product.getUpdatedAt() );

        return productResponse.build();
    }

    @Override
    public void updateEntityFromDto(UpdateProductRequest request, Product product) {
        if ( request == null ) {
            return;
        }

        if ( request.getName() != null ) {
            product.setName( request.getName() );
        }
        if ( request.getSlug() != null ) {
            product.setSlug( request.getSlug() );
        }
        if ( request.getDescription() != null ) {
            product.setDescription( request.getDescription() );
        }
        if ( request.getShortDescription() != null ) {
            product.setShortDescription( request.getShortDescription() );
        }
        if ( request.getSku() != null ) {
            product.setSku( request.getSku() );
        }
        if ( request.getBrand() != null ) {
            product.setBrand( request.getBrand() );
        }
        if ( request.getPrice() != null ) {
            product.setPrice( request.getPrice() );
        }
        if ( request.getDiscountPrice() != null ) {
            product.setDiscountPrice( request.getDiscountPrice() );
        }
        if ( request.getQuantity() != null ) {
            product.setQuantity( request.getQuantity() );
        }
        if ( request.getStatus() != null ) {
            product.setStatus( request.getStatus() );
        }
        if ( request.getActive() != null ) {
            product.setActive( request.getActive() );
        }
    }
}
