package com.commercex.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CouponAppliedEvent {
    private String couponCode;
    private String userEmail;
}
