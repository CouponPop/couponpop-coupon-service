package com.couponpop.couponservice.domain.coupon.common.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public enum CouponStatus {

    ISSUED("issued"),
    USED("used"),
    EXPIRED("expired"),
    CANCELED("canceled");

    private final String routingKey;

    public String asRoutingKey() {
        return routingKey;
    }
}
