package com.couponpop.couponservice.domain.couponevent.dto.response;

import com.couponpop.couponservice.domain.couponevent.entity.CouponEvent;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CreateCouponEventResponse(
        Long eventId,
        String name,
        LocalDateTime eventStartAt,
        LocalDateTime eventEndAt,
        int totalCount,
        LocalDateTime createdAt
) {
    public static CreateCouponEventResponse from(CouponEvent couponEvent) {
        return CreateCouponEventResponse.builder()
                .eventId(couponEvent.getId())
                .name(couponEvent.getName())
                .eventStartAt(couponEvent.getEventStartAt())
                .eventEndAt(couponEvent.getEventEndAt())
                .totalCount(couponEvent.getTotalCount())
                .createdAt(couponEvent.getCreatedAt())
                .build();
    }
}
