package com.commercex.mapper;

import com.commercex.dto.WishlistItemResponse;
import com.commercex.dto.WishlistResponse;
import com.commercex.entity.Wishlist;
import com.commercex.entity.WishlistItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", uses = {ProductMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WishlistMapper {
    
    @Mapping(target = "userId", source = "user.id")
    WishlistResponse toDto(Wishlist wishlist);
    
    WishlistItemResponse toItemDto(WishlistItem item);
}
