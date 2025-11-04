package com.couponpop.couponservice.domain.coupon.service.coupon_issue;

import java.time.LocalDateTime;

public interface CouponIssueFacade {
    void issueCoupon(Long memberId, Long eventId, LocalDateTime currentDateTime) throws InterruptedException;
}
