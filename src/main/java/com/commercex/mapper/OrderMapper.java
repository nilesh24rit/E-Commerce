package com.commercex.mapper;

import com.commercex.dto.OrderItemResponse;
import com.commercex.dto.OrderResponse;
import com.commercex.dto.OrderSummaryResponse;
import com.commercex.entity.Order;
import com.commercex.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "userId", source = "user.id")
    OrderResponse toDto(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productSku", source = "product.sku")
    OrderItemResponse toDto(OrderItem orderItem);

    @Mapping(target = "totalItems", expression = "java(order.getItems().stream().mapToInt(OrderItem::getQuantity).sum())")
    OrderSummaryResponse toSummaryDto(Order order);
}
