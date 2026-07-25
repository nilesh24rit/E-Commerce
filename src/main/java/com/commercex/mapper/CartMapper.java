package com.commercex.mapper;

import com.commercex.dto.CartItemResponse;
import com.commercex.dto.CartResponse;
import com.commercex.entity.Cart;
import com.commercex.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "grandTotal", expression = "java(calculateGrandTotal(cart.getItems()))")
    @Mapping(target = "totalItems", expression = "java(calculateTotalItems(cart.getItems()))")
    CartResponse toDto(Cart cart);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productSlug", source = "product.slug")
    @Mapping(target = "productSku", source = "product.sku")
    CartItemResponse toDto(CartItem cartItem);

    default BigDecimal calculateGrandTotal(List<CartItem> items) {
        if (items == null) return BigDecimal.ZERO;
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default Integer calculateTotalItems(List<CartItem> items) {
        if (items == null) return 0;
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}
