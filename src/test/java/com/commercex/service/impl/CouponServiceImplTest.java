package com.commercex.service.impl;

import com.commercex.dto.CouponValidationResponse;
import com.commercex.entity.Coupon;
import com.commercex.entity.enums.DiscountType;
import com.commercex.exception.CouponExpiredException;
import com.commercex.exception.CouponInactiveException;
import com.commercex.exception.CouponUsageLimitExceededException;
import com.commercex.exception.InvalidCouponException;
import com.commercex.mapper.CouponMapper;
import com.commercex.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceImplTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponMapper couponMapper;

    @InjectMocks
    private CouponServiceImpl couponService;

    private Coupon coupon;

    @BeforeEach
    void setUp() {
        coupon = new Coupon();
        coupon.setCode("SUMMER50");
        coupon.setDiscountType(DiscountType.FIXED);
        coupon.setDiscountValue(BigDecimal.valueOf(50));
        coupon.setActive(true);
        coupon.setValidFrom(LocalDateTime.now().minusDays(1));
        coupon.setValidUntil(LocalDateTime.now().plusDays(10));
        coupon.setUsageLimit(100);
        coupon.setUsedCount(0);
        coupon.setMinimumOrderAmount(BigDecimal.valueOf(100));
    }

    @Test
    void validateCoupon_Success_Fixed() {
        when(couponRepository.findByCode("SUMMER50")).thenReturn(Optional.of(coupon));

        CouponValidationResponse response = couponService.validateCoupon("SUMMER50", BigDecimal.valueOf(200));

        assertTrue(response.isValid());
        assertEquals(BigDecimal.valueOf(50), response.getDiscountAmount());
        assertEquals(BigDecimal.valueOf(150), response.getFinalAmount());
    }

    @Test
    void validateCoupon_Success_Percentage() {
        coupon.setDiscountType(DiscountType.PERCENTAGE);
        coupon.setDiscountValue(BigDecimal.valueOf(10)); // 10%
        when(couponRepository.findByCode("SUMMER50")).thenReturn(Optional.of(coupon));

        CouponValidationResponse response = couponService.validateCoupon("SUMMER50", BigDecimal.valueOf(200));

        assertTrue(response.isValid());
        assertEquals(BigDecimal.valueOf(20).setScale(2), response.getDiscountAmount());
        assertEquals(BigDecimal.valueOf(180).setScale(2), response.getFinalAmount());
    }

    @Test
    void validateCoupon_BelowMinimumAmount_ThrowsException() {
        when(couponRepository.findByCode("SUMMER50")).thenReturn(Optional.of(coupon));

        assertThrows(InvalidCouponException.class, () -> 
            couponService.validateCoupon("SUMMER50", BigDecimal.valueOf(50))
        );
    }

    @Test
    void validateCoupon_Expired_ThrowsException() {
        coupon.setValidUntil(LocalDateTime.now().minusDays(1));
        when(couponRepository.findByCode("SUMMER50")).thenReturn(Optional.of(coupon));

        assertThrows(CouponExpiredException.class, () -> 
            couponService.validateCoupon("SUMMER50", BigDecimal.valueOf(200))
        );
    }

    @Test
    void validateCoupon_UsageLimitExceeded_ThrowsException() {
        coupon.setUsedCount(100);
        when(couponRepository.findByCode("SUMMER50")).thenReturn(Optional.of(coupon));

        assertThrows(CouponUsageLimitExceededException.class, () -> 
            couponService.validateCoupon("SUMMER50", BigDecimal.valueOf(200))
        );
    }
}
