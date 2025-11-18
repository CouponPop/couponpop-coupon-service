package com.couponpop.couponservice.domain.couponevent.common.repository.dto;

import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

public record CouponEventWithUsedCountProjection(
        Long id,
        String eventName,
        LocalDateTime start,
        LocalDateTime end,
        int totalCount,
        int issuedCount,
        int usedCouponCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    @QueryProjection
    public CouponEventWithUsedCountProjection {
    }
}
