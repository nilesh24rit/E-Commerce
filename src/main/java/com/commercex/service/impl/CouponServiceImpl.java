package com.commercex.service.impl;

import com.commercex.dto.*;
import com.commercex.entity.Coupon;
import com.commercex.entity.enums.DiscountType;
import com.commercex.exception.*;
import com.commercex.mapper.CouponMapper;
import com.commercex.repository.CouponRepository;
import com.commercex.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    @Override
    @Transactional
    @CacheEvict(value = "coupons", allEntries = true)
    public CouponResponse createCoupon(CreateCouponRequest request) {
        log.info("Creating coupon with code: {}", request.getCode());
        
        if (couponRepository.existsByCode(request.getCode())) {
            throw new InvalidCouponException("Coupon code already exists");
        }
        
        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minimumOrderAmount(request.getMinimumOrderAmount() != null ? request.getMinimumOrderAmount() : BigDecimal.ZERO)
                .maximumDiscountAmount(request.getMaximumDiscountAmount())
                .usageLimit(request.getUsageLimit())
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .active(request.isActive())
                .build();
                
        return couponMapper.toDto(couponRepository.save(coupon));
    }

    @Override
    @Transactional
    @CacheEvict(value = "coupons", allEntries = true)
    public CouponResponse updateCoupon(UUID id, UpdateCouponRequest request) {
        log.info("Updating coupon with id: {}", id);
        
        Coupon coupon = getCouponEntity(id);
        
        if (request.getDescription() != null) coupon.setDescription(request.getDescription());
        if (request.getDiscountType() != null) coupon.setDiscountType(request.getDiscountType());
        if (request.getDiscountValue() != null) coupon.setDiscountValue(request.getDiscountValue());
        if (request.getMinimumOrderAmount() != null) coupon.setMinimumOrderAmount(request.getMinimumOrderAmount());
        if (request.getMaximumDiscountAmount() != null) coupon.setMaximumDiscountAmount(request.getMaximumDiscountAmount());
        if (request.getUsageLimit() != null) coupon.setUsageLimit(request.getUsageLimit());
        if (request.getValidFrom() != null) coupon.setValidFrom(request.getValidFrom());
        if (request.getValidUntil() != null) coupon.setValidUntil(request.getValidUntil());
        if (request.getActive() != null) coupon.setActive(request.getActive());
        
        return couponMapper.toDto(couponRepository.save(coupon));
    }

    @Override
    @Transactional
    @CacheEvict(value = "coupons", allEntries = true)
    public CouponResponse deactivateCoupon(UUID id) {
        Coupon coupon = getCouponEntity(id);
        coupon.setActive(false);
        return couponMapper.toDto(couponRepository.save(coupon));
    }

    @Override
    @Transactional
    @CacheEvict(value = "coupons", allEntries = true)
    public void deleteCoupon(UUID id) {
        Coupon coupon = getCouponEntity(id);
        couponRepository.delete(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "coupons", key = "#code")
    public CouponResponse getCouponByCode(String code) {
        return couponMapper.toDto(getCouponEntityByCode(code));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "coupons", key = "'all'")
    public List<CouponResponse> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(couponMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CouponValidationResponse validateCoupon(String code, BigDecimal orderAmount) {
        log.info("Validating coupon: {}", code);
        
        Coupon coupon = getCouponEntityByCode(code);
        LocalDateTime now = LocalDateTime.now();
        
        if (!coupon.isActive()) {
            throw new CouponInactiveException("Coupon is inactive");
        }
        
        if (now.isBefore(coupon.getValidFrom()) || now.isAfter(coupon.getValidUntil())) {
            throw new CouponExpiredException("Coupon is expired or not yet valid");
        }
        
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new CouponUsageLimitExceededException("Coupon usage limit has been reached");
        }
        
        if (coupon.getMinimumOrderAmount() != null && orderAmount.compareTo(coupon.getMinimumOrderAmount()) < 0) {
            throw new InvalidCouponException("Order amount is below the minimum required for this coupon");
        }
        
        BigDecimal discountAmount = calculateDiscount(coupon, orderAmount);
        BigDecimal finalAmount = orderAmount.subtract(discountAmount);
        
        return CouponValidationResponse.builder()
                .valid(true)
                .code(coupon.getCode())
                .originalAmount(orderAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .message("Coupon applied successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CouponValidationResponse applyCoupon(ApplyCouponRequest request) {
        return validateCoupon(request.getCode(), request.getOrderAmount());
    }

    @Override
    @Transactional
    public void incrementUsage(String code) {
        Coupon coupon = getCouponEntityByCode(code);
        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);
    }
    
    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal orderAmount) {
        BigDecimal discount = BigDecimal.ZERO;
        
        if (coupon.getDiscountType() == DiscountType.FIXED) {
            discount = coupon.getDiscountValue();
        } else if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = orderAmount.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        
        if (coupon.getMaximumDiscountAmount() != null && discount.compareTo(coupon.getMaximumDiscountAmount()) > 0) {
            discount = coupon.getMaximumDiscountAmount();
        }
        
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }
        
        return discount;
    }

    private Coupon getCouponEntity(UUID id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found with id: " + id));
    }
    
    private Coupon getCouponEntityByCode(String code) {
        return couponRepository.findByCode(code)
                .orElseThrow(() -> new CouponNotFoundException("Coupon not found with code: " + code));
    }
}
