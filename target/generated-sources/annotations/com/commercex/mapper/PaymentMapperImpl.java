package com.commercex.mapper;

import com.commercex.dto.PaymentHistoryResponse;
import com.commercex.dto.PaymentResponse;
import com.commercex.dto.PaymentStatusResponse;
import com.commercex.entity.Order;
import com.commercex.entity.Payment;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-16T05:35:35+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class PaymentMapperImpl implements PaymentMapper {

    @Override
    public PaymentResponse toDto(Payment payment) {
        if ( payment == null ) {
            return null;
        }

        PaymentResponse.PaymentResponseBuilder paymentResponse = PaymentResponse.builder();

        paymentResponse.orderNumber( paymentOrderOrderNumber( payment ) );
        paymentResponse.id( payment.getId() );
        paymentResponse.paymentId( payment.getPaymentId() );
        paymentResponse.amount( payment.getAmount() );
        paymentResponse.currency( payment.getCurrency() );
        paymentResponse.paymentMethod( payment.getPaymentMethod() );
        paymentResponse.paymentStatus( payment.getPaymentStatus() );
        paymentResponse.gatewayName( payment.getGatewayName() );
        paymentResponse.gatewayTransactionId( payment.getGatewayTransactionId() );
        paymentResponse.createdAt( payment.getCreatedAt() );
        paymentResponse.updatedAt( payment.getUpdatedAt() );

        return paymentResponse.build();
    }

    @Override
    public PaymentStatusResponse toStatusDto(Payment payment) {
        if ( payment == null ) {
            return null;
        }

        PaymentStatusResponse.PaymentStatusResponseBuilder paymentStatusResponse = PaymentStatusResponse.builder();

        paymentStatusResponse.paymentId( payment.getPaymentId() );
        paymentStatusResponse.paymentStatus( payment.getPaymentStatus() );
        paymentStatusResponse.failureReason( payment.getFailureReason() );

        return paymentStatusResponse.build();
    }

    @Override
    public PaymentHistoryResponse toHistoryDto(Payment payment) {
        if ( payment == null ) {
            return null;
        }

        PaymentHistoryResponse.PaymentHistoryResponseBuilder paymentHistoryResponse = PaymentHistoryResponse.builder();

        paymentHistoryResponse.orderNumber( paymentOrderOrderNumber( payment ) );
        paymentHistoryResponse.id( payment.getId() );
        paymentHistoryResponse.paymentId( payment.getPaymentId() );
        paymentHistoryResponse.amount( payment.getAmount() );
        paymentHistoryResponse.paymentMethod( payment.getPaymentMethod() );
        paymentHistoryResponse.paymentStatus( payment.getPaymentStatus() );
        paymentHistoryResponse.createdAt( payment.getCreatedAt() );

        return paymentHistoryResponse.build();
    }

    private String paymentOrderOrderNumber(Payment payment) {
        if ( payment == null ) {
            return null;
        }
        Order order = payment.getOrder();
        if ( order == null ) {
            return null;
        }
        String orderNumber = order.getOrderNumber();
        if ( orderNumber == null ) {
            return null;
        }
        return orderNumber;
    }
}
