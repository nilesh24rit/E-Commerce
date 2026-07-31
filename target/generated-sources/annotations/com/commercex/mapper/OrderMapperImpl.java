package com.commercex.mapper;

import com.commercex.dto.OrderItemResponse;
import com.commercex.dto.OrderResponse;
import com.commercex.dto.OrderSummaryResponse;
import com.commercex.entity.Order;
import com.commercex.entity.OrderItem;
import com.commercex.entity.Product;
import com.commercex.entity.User;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-31T14:37:34+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public OrderResponse toDto(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderResponse.OrderResponseBuilder orderResponse = OrderResponse.builder();

        orderResponse.userId( orderUserId( order ) );
        orderResponse.id( order.getId() );
        orderResponse.orderNumber( order.getOrderNumber() );
        orderResponse.totalAmount( order.getTotalAmount() );
        orderResponse.subtotal( order.getSubtotal() );
        orderResponse.discount( order.getDiscount() );
        orderResponse.shippingCharge( order.getShippingCharge() );
        orderResponse.orderStatus( order.getOrderStatus() );
        orderResponse.paymentStatus( order.getPaymentStatus() );
        orderResponse.items( orderItemListToOrderItemResponseList( order.getItems() ) );
        orderResponse.createdAt( order.getCreatedAt() );
        orderResponse.updatedAt( order.getUpdatedAt() );

        return orderResponse.build();
    }

    @Override
    public OrderItemResponse toDto(OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }

        OrderItemResponse.OrderItemResponseBuilder orderItemResponse = OrderItemResponse.builder();

        orderItemResponse.productId( orderItemProductId( orderItem ) );
        orderItemResponse.productName( orderItemProductName( orderItem ) );
        orderItemResponse.productSku( orderItemProductSku( orderItem ) );
        orderItemResponse.id( orderItem.getId() );
        orderItemResponse.quantity( orderItem.getQuantity() );
        orderItemResponse.unitPrice( orderItem.getUnitPrice() );
        orderItemResponse.subtotal( orderItem.getSubtotal() );

        return orderItemResponse.build();
    }

    @Override
    public OrderSummaryResponse toSummaryDto(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderSummaryResponse.OrderSummaryResponseBuilder orderSummaryResponse = OrderSummaryResponse.builder();

        orderSummaryResponse.id( order.getId() );
        orderSummaryResponse.orderNumber( order.getOrderNumber() );
        orderSummaryResponse.totalAmount( order.getTotalAmount() );
        orderSummaryResponse.orderStatus( order.getOrderStatus() );
        orderSummaryResponse.createdAt( order.getCreatedAt() );

        orderSummaryResponse.totalItems( order.getItems().stream().mapToInt(OrderItem::getQuantity).sum() );

        return orderSummaryResponse.build();
    }

    private UUID orderUserId(Order order) {
        if ( order == null ) {
            return null;
        }
        User user = order.getUser();
        if ( user == null ) {
            return null;
        }
        UUID id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected List<OrderItemResponse> orderItemListToOrderItemResponseList(List<OrderItem> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderItemResponse> list1 = new ArrayList<OrderItemResponse>( list.size() );
        for ( OrderItem orderItem : list ) {
            list1.add( toDto( orderItem ) );
        }

        return list1;
    }

    private UUID orderItemProductId(OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }
        Product product = orderItem.getProduct();
        if ( product == null ) {
            return null;
        }
        UUID id = product.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String orderItemProductName(OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }
        Product product = orderItem.getProduct();
        if ( product == null ) {
            return null;
        }
        String name = product.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private String orderItemProductSku(OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }
        Product product = orderItem.getProduct();
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
