package com.commercex.service;

import com.commercex.dto.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CouponService {
    CouponResponse createCoupon(CreateCouponRequest request);
    CouponResponse updateCoupon(UUID id, UpdateCouponRequest request);
    CouponResponse deactivateCoupon(UUID id);
    void deleteCoupon(UUID id);
    CouponResponse getCouponByCode(String code);
    List<CouponResponse> getAllCoupons();
    
    CouponValidationResponse validateCoupon(String code, BigDecimal orderAmount);
    CouponValidationResponse applyCoupon(ApplyCouponRequest request);
    
    // Internal use for order completion
    void incrementUsage(String code);
}
