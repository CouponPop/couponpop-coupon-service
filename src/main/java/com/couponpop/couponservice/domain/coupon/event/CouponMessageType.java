package com.couponpop.couponservice.domain.coupon.event;

import lombok.AllArgsConstructor;

@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public enum CouponMessageType {

    ISSUED("issued"),
    USED("used");

    private final String routingKey;

    public String asRoutingKey() {
        return routingKey;
    }
}
