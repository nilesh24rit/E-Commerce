package com.commercex.mapper;

import com.commercex.dto.PaymentHistoryResponse;
import com.commercex.dto.PaymentResponse;
import com.commercex.dto.PaymentStatusResponse;
import com.commercex.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    @Mapping(target = "orderNumber", source = "order.orderNumber")
    PaymentResponse toDto(Payment payment);

    PaymentStatusResponse toStatusDto(Payment payment);

    @Mapping(target = "orderNumber", source = "order.orderNumber")
    PaymentHistoryResponse toHistoryDto(Payment payment);
}
