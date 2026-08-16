package com.commercex.mapper;

import com.commercex.dto.CartItemResponse;
import com.commercex.dto.CartResponse;
import com.commercex.entity.Cart;
import com.commercex.entity.CartItem;
import com.commercex.entity.Product;
import com.commercex.entity.User;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-16T05:35:34+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class CartMapperImpl implements CartMapper {

    @Override
    public CartResponse toDto(Cart cart) {
        if ( cart == null ) {
            return null;
        }

        CartResponse.CartResponseBuilder cartResponse = CartResponse.builder();

        cartResponse.userId( cartUserId( cart ) );
        cartResponse.id( cart.getId() );
        cartResponse.items( cartItemListToCartItemResponseList( cart.getItems() ) );

        cartResponse.grandTotal( calculateGrandTotal(cart.getItems()) );
        cartResponse.totalItems( calculateTotalItems(cart.getItems()) );

        return cartResponse.build();
    }

    @Override
    public CartItemResponse toDto(CartItem cartItem) {
        if ( cartItem == null ) {
            return null;
        }

        CartItemResponse.CartItemResponseBuilder cartItemResponse = CartItemResponse.builder();

        cartItemResponse.productId( cartItemProductId( cartItem ) );
        cartItemResponse.productName( cartItemProductName( cartItem ) );
        cartItemResponse.productSlug( cartItemProductSlug( cartItem ) );
        cartItemResponse.productSku( cartItemProductSku( cartItem ) );
        cartItemResponse.id( cartItem.getId() );
        cartItemResponse.quantity( cartItem.getQuantity() );
        cartItemResponse.unitPrice( cartItem.getUnitPrice() );
        cartItemResponse.subtotal( cartItem.getSubtotal() );

        return cartItemResponse.build();
    }

    private UUID cartUserId(Cart cart) {
        if ( cart == null ) {
            return null;
        }
        User user = cart.getUser();
        if ( user == null ) {
            return null;
        }
        UUID id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected List<CartItemResponse> cartItemListToCartItemResponseList(List<CartItem> list) {
        if ( list == null ) {
            return null;
        }

        List<CartItemResponse> list1 = new ArrayList<CartItemResponse>( list.size() );
        for ( CartItem cartItem : list ) {
            list1.add( toDto( cartItem ) );
        }

        return list1;
    }

    private UUID cartItemProductId(CartItem cartItem) {
        if ( cartItem == null ) {
            return null;
        }
        Product product = cartItem.getProduct();
        if ( product == null ) {
            return null;
        }
        UUID id = product.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String cartItemProductName(CartItem cartItem) {
        if ( cartItem == null ) {
            return null;
        }
        Product product = cartItem.getProduct();
        if ( product == null ) {
            return null;
        }
        String name = product.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String cartItemProductSlug(CartItem cartItem) {
        if ( cartItem == null ) {
            return null;
        }
        Product product = cartItem.getProduct();
        if ( product == null ) {
            return null;
        }
        String slug = product.getSlug();
        if ( slug == null ) {
            return null;
        }
        return slug;
    }

    private String cartItemProductSku(CartItem cartItem) {
        if ( cartItem == null ) {
            return null;
        }
        Product product = cartItem.getProduct();
        if ( product == null ) {
            return null;
        }
        String sku = product.getSku();
        if ( sku == null ) {
            return null;
        }
        return sku;
    }
}
