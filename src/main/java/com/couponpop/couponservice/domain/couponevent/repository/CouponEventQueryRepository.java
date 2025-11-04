package com.couponpop.couponservice.domain.couponevent.repository;

import com.couponpop.couponservice.domain.couponevent.dto.cursor.StoreCouponEventsCursor;
import com.couponpop.couponservice.domain.couponevent.enums.CouponEventStatus;
import com.couponpop.couponservice.domain.couponevent.repository.dto.CouponEventWithUsedCountProjection;
import com.couponpop.couponservice.domain.couponevent.repository.dto.StoreCouponEventStatisticsProjection;

import java.time.LocalDateTime;
import java.util.List;

public interface CouponEventQueryRepository {

    List<CouponEventWithUsedCountProjection> fetchCouponEventsByStore(
            Long storeId,
            CouponEventStatus eventStatus,
            LocalDateTime now,
            StoreCouponEventsCursor cursor,
            int limit
    );

    List<StoreCouponEventStatisticsProjection> fetchStoreCouponEventStatistics(List<Long> storeIds);
}
