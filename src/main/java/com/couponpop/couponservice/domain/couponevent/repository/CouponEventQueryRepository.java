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

    // TODO : 이벤트가 진행 중이지 않은 매장에 대한 대시보드도 보여줘야 하나? 현재는 이벤트 진행 중인 매장만 보임
    List<StoreCouponEventStatisticsProjection> fetchStoreCouponEventStatistics(List<Long> storeIds);
}
