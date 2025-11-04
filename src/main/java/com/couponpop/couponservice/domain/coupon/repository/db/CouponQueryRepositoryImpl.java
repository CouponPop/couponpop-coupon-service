package com.couponpop.couponservice.domain.coupon.repository.db;

import com.couponpop.couponservice.domain.coupon.entity.QCoupon;
import com.couponpop.couponservice.domain.coupon.enums.CouponStatus;
import com.couponpop.couponservice.domain.coupon.repository.db.dto.CouponSummaryInfoProjection;
import com.couponpop.couponservice.domain.coupon.repository.db.dto.QCouponSummaryInfoProjection;
import com.couponpop.couponservice.domain.couponevent.entity.QCouponEvent;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class CouponQueryRepositoryImpl implements CouponQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<CouponSummaryInfoProjection> findAllByMemberIdWithEventAndStore(Long memberId, CouponStatus status, LocalDateTime lastEventEndAt, Long lastCouponId, int limit) {
        QCoupon coupon = QCoupon.coupon;
        QCouponEvent couponEvent = QCouponEvent.couponEvent;

        // TODO 정렬 조건은 couponEvent end가 아닌 쿠폰 만료 시간으로 할것!!
        return jpaQueryFactory
                .select(
                        new QCouponSummaryInfoProjection(
                                coupon.id,
                                coupon.couponStatus,
                                coupon.receivedAt,
                                coupon.expireAt,
                                coupon.usedAt,
                                couponEvent.id,
                                couponEvent.name,
                                couponEvent.eventStartAt,
                                couponEvent.eventEndAt,
                                couponEvent.storeId
                        )
                )
                .from(coupon)
                .leftJoin(coupon.couponEvent, couponEvent)
                .where(
                        coupon.memberId.eq(memberId),
                        coupon.couponStatus.eq(status),
                        nextCouponCursorCondition(coupon, lastEventEndAt, lastCouponId)
                )
                .orderBy(
                        coupon.expireAt.asc(),
                        coupon.id.asc()
                )
                .limit(limit)
                .fetch();
    }

    /**
     * Cursor 기반 다음 페이지 조회 조건
     *
     * <p>조건:</p>
     * <ol>
     *     <li>eventEndAt > lastEventEndAt → 다음 페이지</li>
     *     <li>eventEndAt = lastEventEndAt 이면 couponId > lastCouponId → 다음 페이지</li>
     * </ol>
     *
     * @param coupon         QCoupon 객체
     * @param lastEventEndAt 이전 페이지 마지막 쿠폰 이벤트 종료 시간
     * @param lastCouponId   이전 페이지 마지막 쿠폰 ID
     * @return BooleanExpression (QueryDSL where 조건)
     */
    private BooleanExpression nextCouponCursorCondition(QCoupon coupon, LocalDateTime lastEventEndAt, Long lastCouponId) {
        if (lastEventEndAt == null || lastCouponId == null) {
            return null; // 첫 페이지 조회
        }

        return coupon.couponEvent.eventEndAt.gt(lastEventEndAt)
                .or(
                        coupon.couponEvent.eventEndAt.eq(lastEventEndAt)
                                .and(coupon.id.gt(lastCouponId))
                );
    }

}
