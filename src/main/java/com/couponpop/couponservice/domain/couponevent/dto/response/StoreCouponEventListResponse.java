package com.couponpop.couponservice.domain.couponevent.dto.response;


import com.couponpop.couponservice.domain.couponevent.dto.cursor.StoreCouponEventsCursor;

import java.util.ArrayList;
import java.util.List;

public record StoreCouponEventListResponse(
        Long storeId,
        List<CouponEventDetailResponse> events,
        StoreCouponEventsCursor nextCursor,
        int size,
        boolean hasNext
) {
    public static StoreCouponEventListResponse of(
            Long storeId,
            List<CouponEventDetailResponse> originalEvents,
            int pageSize
    ) {
        boolean hasNext = originalEvents.size() > pageSize;
        List<CouponEventDetailResponse> events = trimToPageSize(originalEvents, pageSize);
        StoreCouponEventsCursor cursor = hasNext ? buildNextCursor(events) : null;

        return new StoreCouponEventListResponse(storeId, events, cursor, events.size(), hasNext);
    }

    private static List<CouponEventDetailResponse> trimToPageSize(List<CouponEventDetailResponse> events, int pageSize) {
        if (events.size() <= pageSize) {
            return events;
        }
        // 원본 리스트 변형 방지를 위해 복사 후 제거
        List<CouponEventDetailResponse> trimmed = new ArrayList<>(events);
        trimmed.remove(trimmed.size() - 1);
        return trimmed;
    }

    private static StoreCouponEventsCursor buildNextCursor(List<CouponEventDetailResponse> events) {
        if (events.isEmpty()) {
            return null;
        }

        CouponEventDetailResponse lastEvent = events.get(events.size() - 1);
        return new StoreCouponEventsCursor(
                lastEvent.eventPeriod().start(),
                lastEvent.eventPeriod().end(),
                lastEvent.id()
        );
    }
}
