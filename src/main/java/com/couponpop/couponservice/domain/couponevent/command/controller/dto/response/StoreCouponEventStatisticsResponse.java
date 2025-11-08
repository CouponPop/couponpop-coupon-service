package com.couponpop.couponservice.domain.couponevent.command.controller.dto.response;

import com.couponpop.couponpopcoremodule.dto.store.request.cursor.StoreCouponEventsStatisticsCursor;
import com.couponpop.couponservice.domain.couponevent.common.repository.dto.StoreCouponEventStatisticsProjection;

import java.util.ArrayList;
import java.util.List;

public record StoreCouponEventStatisticsResponse(
        List<StoreCouponEventStatisticsProjection> statistics,
        StoreCouponEventsStatisticsCursor nextCursor,
        int size,
        boolean hasNext
) {

    public static StoreCouponEventStatisticsResponse of(List<StoreCouponEventStatisticsProjection> originalStatistics, int pageSize) {
        boolean hasNext = originalStatistics.size() > pageSize;
        List<StoreCouponEventStatisticsProjection> statistics = trimToPageSize(originalStatistics, pageSize);
        StoreCouponEventsStatisticsCursor cursor = hasNext ? buildNextCursor(statistics) : null;
        return new StoreCouponEventStatisticsResponse(statistics, cursor, statistics.size(), hasNext);
    }

    private static List<StoreCouponEventStatisticsProjection> trimToPageSize(List<StoreCouponEventStatisticsProjection> statistics, int pageSize) {
        if (statistics.size() <= pageSize) {
            return statistics;
        }

        List<StoreCouponEventStatisticsProjection> trimmed = new ArrayList<>(statistics);
        trimmed.remove(trimmed.size() - 1);
        return trimmed;
    }

    private static StoreCouponEventsStatisticsCursor buildNextCursor(List<StoreCouponEventStatisticsProjection> statistics) {
        if (statistics.isEmpty()) {
            return null;
        }

        StoreCouponEventStatisticsProjection last = statistics.get(statistics.size() - 1);
        return new StoreCouponEventsStatisticsCursor(
                last.storeId()
        );
    }
}
