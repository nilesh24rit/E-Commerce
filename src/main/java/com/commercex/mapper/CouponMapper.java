package com.commercex.mapper;

import com.commercex.dto.CouponResponse;
import com.commercex.entity.Coupon;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CouponMapper {
    CouponResponse toDto(Coupon coupon);
}
