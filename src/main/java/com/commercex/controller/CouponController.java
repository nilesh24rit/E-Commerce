package com.commercex.controller;

import com.commercex.dto.*;
import com.commercex.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupon Management", description = "Coupon and discount APIs")
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "Create a new coupon (Admin)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        CouponResponse response = couponService.createCoupon(request);
        return new ResponseEntity<>(ApiResponse.success(response, "Coupon created successfully"), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing coupon (Admin)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CouponResponse>> updateCoupon(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCouponRequest request) {
        CouponResponse response = couponService.updateCoupon(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Coupon updated successfully"));
    }

    @Operation(summary = "Deactivate a coupon (Admin)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<CouponResponse>> deactivateCoupon(@PathVariable UUID id) {
        CouponResponse response = couponService.deactivateCoupon(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Coupon deactivated successfully"));
    }

    @Operation(summary = "Delete a coupon (Admin)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable UUID id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Coupon deleted successfully"));
    }

    @Operation(summary = "Get a coupon by code (Admin)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<CouponResponse>> getCouponByCode(@PathVariable String code) {
        CouponResponse response = couponService.getCouponByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response, "Coupon retrieved"));
    }

    @Operation(summary = "Get all coupons (Admin)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CouponResponse>>> getAllCoupons() {
        List<CouponResponse> response = couponService.getAllCoupons();
        return ResponseEntity.ok(ApiResponse.success(response, "Coupons retrieved"));
    }

    @Operation(summary = "Apply a coupon to an order amount")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<CouponValidationResponse>> applyCoupon(@Valid @RequestBody ApplyCouponRequest request) {
        CouponValidationResponse response = couponService.applyCoupon(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Coupon applied"));
    }
}
