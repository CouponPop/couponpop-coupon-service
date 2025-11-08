package com.couponpop.couponservice.domain.couponevent.common.repository.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.time.LocalDateTime;

public record StoreCouponEventStatisticsProjection(
        Long storeId,
        CouponStats couponStats,
        LocalDateTime lastEventEndAt
) {
    @QueryProjection
    public StoreCouponEventStatisticsProjection {
    }

    @Getter
    public static class CouponStats {
        private final int total;
        private final int unclaimed;
        private final int used;
        private final int unused;

        @QueryProjection
        public CouponStats(int total, int issuedCount, int usedCount) {
            this.total = total;
            this.unclaimed = total - issuedCount;
            this.used = usedCount;
            this.unused = issuedCount - usedCount;
        }
    }

}
