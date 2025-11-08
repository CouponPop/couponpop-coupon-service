package com.couponpop.couponservice.domain.couponevent.common.repository;

import com.couponpop.couponservice.domain.coupon.common.entity.QCoupon;
import com.couponpop.couponservice.domain.coupon.common.enums.CouponStatus;
import com.couponpop.couponservice.domain.couponevent.common.entity.QCouponEvent;
import com.couponpop.couponservice.domain.couponevent.common.repository.dto.*;
import com.couponpop.couponservice.domain.couponevent.command.controller.dto.cursor.StoreCouponEventsCursor;
import com.couponpop.couponservice.domain.couponevent.common.enums.CouponEventStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class CouponEventQueryRepositoryImpl implements CouponEventQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<CouponEventWithUsedCountProjection> fetchCouponEventsByStore(
            Long storeId,
            CouponEventStatus eventStatus,
            LocalDateTime now,
            StoreCouponEventsCursor cursor,
            int limit
    ) {

        QCouponEvent couponEvent = QCouponEvent.couponEvent;
        QCoupon coupon = QCoupon.coupon;

        return jpaQueryFactory
                .select(
                        new QCouponEventWithUsedCountProjection(
                                couponEvent.id,
                                couponEvent.name,
                                couponEvent.eventStartAt,
                                couponEvent.eventEndAt,
                                couponEvent.totalCount,
                                couponEvent.issuedCount,
                                coupon.count().intValue(),
                                couponEvent.createdAt,
                                couponEvent.updatedAt
                        )
                )
                .from(couponEvent)
                .leftJoin(coupon).on(
                        coupon.couponEvent.eq(couponEvent)
                                .and(coupon.couponStatus.eq(CouponStatus.USED))
                )
                .where(
                        storeIdEq(couponEvent, storeId),
                        eventPeriodCondition(couponEvent, now, eventStatus),
                        nextEventCondition(couponEvent, cursor.lastStartAt(), cursor.lastEndAt(), cursor.lastEventId())
                )
                .groupBy(couponEvent.id)
                .orderBy(
                        couponEvent.eventStartAt.asc(),
                        couponEvent.eventEndAt.asc(),
                        couponEvent.id.asc()
                )
                .limit(limit)
                .fetch();
    }

    @Override
    public List<StoreCouponEventStatisticsProjection> fetchStoreCouponEventStatistics(List<Long> storeIds) {
        QCouponEvent couponEvent = QCouponEvent.couponEvent;
        QCoupon coupon = QCoupon.coupon;

        NumberExpression<Integer> usedCount = Expressions.numberTemplate(Integer.class,
                "sum(case when {0}.usedAt is not null then 1 else 0 end)", coupon);

        return jpaQueryFactory
                .select(
                        new QStoreCouponEventStatisticsProjection(
                                couponEvent.storeId,
                                new QStoreCouponEventStatisticsProjection_CouponStats(
                                        couponEvent.totalCount.sum().coalesce(0),
                                        couponEvent.issuedCount.sum().coalesce(0),
                                        usedCount.coalesce(0)
                                ),
                                couponEvent.eventEndAt.max()
                        )
                )
                .from(couponEvent)
                .leftJoin(coupon).on(coupon.couponEvent.eq(couponEvent))
                .where(couponEvent.storeId.in(storeIds))
                .groupBy(couponEvent.storeId)
                .orderBy(couponEvent.storeId.desc())
                .fetch();
    }

    private BooleanExpression storeIdEq(QCouponEvent couponEvent, Long storeId) {
        if (ObjectUtils.isEmpty(storeId)) {
            return null;
        }
        return couponEvent.storeId.eq(storeId);
    }

    private BooleanExpression eventPeriodCondition(QCouponEvent event, LocalDateTime now, CouponEventStatus eventStatus) {
        if (eventStatus == null) {
            return null;
        }

        return switch (eventStatus) {
            case IN_PROGRESS -> event.eventStartAt.loe(now).and(event.eventEndAt.goe(now));
            case COMPLETED -> event.eventEndAt.lt(now);
            default -> throw new IllegalStateException("Unexpected value: " + eventStatus);
        };
    }

    private BooleanExpression eventStatusEq(QCouponEvent couponEvent, CouponEventStatus eventStatus) {
        if (ObjectUtils.isEmpty(eventStatus)) {
            return null;
        }
        return couponEvent.couponEventStatus.eq(eventStatus);
    }

    /**
     * no-offset 페이징 조건:
     * 1) eventStartAt > lastStartAt 이면 다음 페이지
     * 2) eventStartAt 같으면 eventEndAt > lastEndAt
     * 3) 둘 다 같으면 id > lastEventId
     */
    private BooleanExpression nextEventCondition(QCouponEvent couponEvent, LocalDateTime lastStartAt, LocalDateTime lastEndAt, Long lastEventId) {
        if (lastStartAt == null || lastEndAt == null || lastEventId == null) {
            return null;
        }

        return couponEvent.eventStartAt.gt(lastStartAt)
                .or(
                        couponEvent.eventStartAt.eq(lastStartAt)
                                .and(couponEvent.eventEndAt.gt(lastEndAt))
                )
                .or(
                        couponEvent.eventStartAt.eq(lastStartAt)
                                .and(couponEvent.eventEndAt.eq(lastEndAt))
                                .and(couponEvent.id.gt(lastEventId))
                );
    }

}
