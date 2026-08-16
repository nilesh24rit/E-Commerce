package com.commercex.mapper;

import com.commercex.dto.WishlistItemResponse;
import com.commercex.dto.WishlistResponse;
import com.commercex.entity.User;
import com.commercex.entity.Wishlist;
import com.commercex.entity.WishlistItem;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-16T05:35:35+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class WishlistMapperImpl implements WishlistMapper {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public WishlistResponse toDto(Wishlist wishlist) {
        if ( wishlist == null ) {
            return null;
        }

        WishlistResponse.WishlistResponseBuilder wishlistResponse = WishlistResponse.builder();

        wishlistResponse.userId( wishlistUserId( wishlist ) );
        wishlistResponse.id( wishlist.getId() );
        wishlistResponse.items( wishlistItemListToWishlistItemResponseList( wishlist.getItems() ) );

        return wishlistResponse.build();
    }

    @Override
    public WishlistItemResponse toItemDto(WishlistItem item) {
        if ( item == null ) {
            return null;
        }

        WishlistItemResponse.WishlistItemResponseBuilder wishlistItemResponse = WishlistItemResponse.builder();

        wishlistItemResponse.id( item.getId() );
        wishlistItemResponse.product( productMapper.toDto( item.getProduct() ) );
        wishlistItemResponse.createdAt( item.getCreatedAt() );

        return wishlistItemResponse.build();
    }

    private UUID wishlistUserId(Wishlist wishlist) {
        if ( wishlist == null ) {
            return null;
        }
        User user = wishlist.getUser();
        if ( user == null ) {
            return null;
        }
        UUID id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected List<WishlistItemResponse> wishlistItemListToWishlistItemResponseList(List<WishlistItem> list) {
        if ( list == null ) {
            return null;
        }

        List<WishlistItemResponse> list1 = new ArrayList<WishlistItemResponse>( list.size() );
        for ( WishlistItem wishlistItem : list ) {
            list1.add( toItemDto( wishlistItem ) );
        }

        return list1;
    }
}
