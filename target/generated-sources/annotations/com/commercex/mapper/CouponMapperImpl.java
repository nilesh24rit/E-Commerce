package com.commercex.mapper;

import com.commercex.dto.CouponResponse;
import com.commercex.entity.Coupon;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-17T05:04:33+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.19 (Eclipse Adoptium)"
)
@Component
public class CouponMapperImpl implements CouponMapper {

    @Override
    public CouponResponse toDto(Coupon coupon) {
        if ( coupon == null ) {
            return null;
        }

        CouponResponse.CouponResponseBuilder couponResponse = CouponResponse.builder();

        couponResponse.id( coupon.getId() );
        couponResponse.code( coupon.getCode() );
        couponResponse.description( coupon.getDescription() );
        couponResponse.discountType( coupon.getDiscountType() );
        couponResponse.discountValue( coupon.getDiscountValue() );
        couponResponse.minimumOrderAmount( coupon.getMinimumOrderAmount() );
        couponResponse.maximumDiscountAmount( coupon.getMaximumDiscountAmount() );
        couponResponse.usageLimit( coupon.getUsageLimit() );
        couponResponse.usedCount( coupon.getUsedCount() );
        couponResponse.validFrom( coupon.getValidFrom() );
        couponResponse.validUntil( coupon.getValidUntil() );
        couponResponse.active( coupon.isActive() );

        return couponResponse.build();
    }
}
