package com.couponpop.couponservice.domain.store.service;

import com.couponpop.couponpopcoremodule.dto.couponevent.response.StoreOwnershipResponse;
import com.couponpop.couponpopcoremodule.dto.store.request.cursor.StoreCouponEventsStatisticsCursor;
import com.couponpop.couponpopcoremodule.dto.store.response.StoreResponse;
import com.couponpop.couponpopcoremodule.enums.StoreCategory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreInternalServiceImpl implements StoreInternalService {

    // TODO : 설명
    @Override
    public StoreOwnershipResponse checkOwnership(Long storeId, Long memberId) {
        /*
        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매장입니다."));

        if (!store.getMember().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 매장은 로그인한 회원 소유가 아닙니다.");
        }
         */
        //Mock 데이터 Return
        return StoreOwnershipResponse.from(true);
    }

    // TODO : memberId에 해당하는 cursor 기반 매장 목록 조회(매장 ID 내림차순)
    @Override
    public List<StoreResponse> findStoresByOwner(Long memberId, StoreCouponEventsStatisticsCursor cursor, int pageSize) {
        /*
           @Override
    public List<StoreCouponEventStatisticsProjection> fetchStoreCouponEventStatistics(Long memberId, StoreCouponEventsStatisticsCursor cursor, int limit) {
        QStore store = QStore.store;
        QCouponEvent couponEvent = QCouponEvent.couponEvent;
        QCoupon coupon = QCoupon.coupon;

        NumberExpression<Integer> usedCount = Expressions.numberTemplate(Integer.class,
                "sum(case when {0}.usedAt is not null then 1 else 0 end)", coupon);
        return jpaQueryFactory
                .select(
                        new QStoreCouponEventStatisticsProjection(
                                store.id,
                                store.name,
                                new QStoreCouponEventStatisticsProjection_CouponStats(
                                        couponEvent.totalCount.sum().coalesce(0),
                                        couponEvent.issuedCount.sum().coalesce(0),
                                        usedCount.coalesce(0)
                                ),
                                couponEvent.eventEndAt.max()
                        )
                )
                .from(store)
                .leftJoin(couponEvent).on(couponEvent.storeId.eq(store))
                .leftJoin(coupon).on(coupon.couponEvent.eq(couponEvent))
                .where(
                        store.member.id.eq(memberId),
                        nextStatisticCondition(store, cursor.lastStoreId())
                )
                .groupBy(store.id)
                .orderBy(store.id.desc())
                .limit(limit)
                .fetch();
    }
         */
        return List.of(
                StoreResponse.of(1L, "매장1", StoreCategory.CAFE, 3.14, 3.14, "imageUrl"),
                StoreResponse.of(2L, "매장2", StoreCategory.CAFE, 3.14, 3.14, "imageUrl")
        );
    }

    // TODO : 매장 ID 에 해당하는 DTO 반환, 일단 필요한 값이 ID와 name 정도. 매장에 대한 추가 정보가 필요하면 ID 를 통해서 UI 쪽에서 API 조회를 하지 않을까?
    @Override
    public StoreResponse findByIdOrElseThrow(Long storeId) {
        /*
          Store store = storeRepository.findById(request.storeId())
                          .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매장입니다."));
         */
        return StoreResponse.of(1L, "매장1", StoreCategory.CAFE, 3.14, 3.14, "imageUrl");
    }

    @Override
    public List<StoreResponse> findAllByIds(List<Long> storeIds) {
        return List.of(
                StoreResponse.of(1L, "매장1", StoreCategory.CAFE, 3.14, 3.14, "imageUrl"),
                StoreResponse.of(2L, "매장2", StoreCategory.CAFE, 3.14, 3.14, "imageUrl")
        );
    }
}
