package com.commercex.controller;

import com.commercex.dto.ApiResponse;
import com.commercex.dto.CreatePaymentRequest;
import com.commercex.dto.PaymentHistoryResponse;
import com.commercex.dto.PaymentResponse;
import com.commercex.dto.RefundRequest;
import com.commercex.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "Payment processing APIs")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Initiate a new payment")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.initiatePayment(request);
        return new ResponseEntity<>(ApiResponse.success(response, "Payment initiated"), HttpStatus.CREATED);
    }

    @Operation(summary = "Verify a pending payment")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @PostMapping("/{paymentId}/verify")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(@PathVariable String paymentId) {
        PaymentResponse response = paymentService.verifyPayment(paymentId);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment verification processed"));
    }

    @Operation(summary = "Cancel a pending payment")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(@PathVariable String paymentId) {
        PaymentResponse response = paymentService.cancelPayment(paymentId);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment cancelled"));
    }

    @Operation(summary = "Refund a successful payment")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
            @PathVariable String paymentId,
            @Valid @RequestBody RefundRequest request) {
        PaymentResponse response = paymentService.refundPayment(paymentId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment refunded"));
    }

    @Operation(summary = "Get payment details by ID")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable String paymentId) {
        PaymentResponse response = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment retrieved"));
    }

    @Operation(summary = "Get current user payment history")
    @PreAuthorize("hasAuthority('ROLE_CUSTOMER')")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<PaymentHistoryResponse>>> getMyPaymentHistory() {
        List<PaymentHistoryResponse> response = paymentService.getMyPaymentHistory();
        return ResponseEntity.ok(ApiResponse.success(response, "Payment history retrieved"));
    }

    @Operation(summary = "Get all payments (Admin)")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<List<PaymentHistoryResponse>>> getAllPayments() {
        List<PaymentHistoryResponse> response = paymentService.getAllPayments();
        return ResponseEntity.ok(ApiResponse.success(response, "All payments retrieved"));
    }
}
