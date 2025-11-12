package com.couponpop.couponservice.domain.coupon.event.model;

import com.couponpop.couponservice.domain.coupon.common.enums.CouponStatus;

public interface CouponEvent {

    Long couponId();

    Long memberId();

    Long storeId();

    Long eventId();

    CouponStatus couponStatus();
}
