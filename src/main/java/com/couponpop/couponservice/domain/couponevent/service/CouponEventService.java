package com.couponpop.couponservice.domain.couponevent.service;

import com.couponpop.couponpopcoremodule.dto.couponevent.response.StoreOwnershipResponse;
import com.couponpop.couponpopcoremodule.dto.store.request.cursor.StoreCouponEventsStatisticsCursor;
import com.couponpop.couponpopcoremodule.dto.store.response.StoreResponse;
import com.couponpop.couponservice.common.exception.GlobalException;
import com.couponpop.couponservice.domain.coupon.enums.CouponStatus;
import com.couponpop.couponservice.domain.coupon.repository.db.CouponRepository;
import com.couponpop.couponservice.domain.couponevent.dto.cursor.StoreCouponEventsCursor;
import com.couponpop.couponservice.domain.couponevent.dto.request.CreateCouponEventRequest;
import com.couponpop.couponservice.domain.couponevent.dto.response.CouponEventDetailResponse;
import com.couponpop.couponservice.domain.couponevent.dto.response.CreateCouponEventResponse;
import com.couponpop.couponservice.domain.couponevent.dto.response.StoreCouponEventListResponse;
import com.couponpop.couponservice.domain.couponevent.dto.response.StoreCouponEventStatisticsResponse;
import com.couponpop.couponservice.domain.couponevent.entity.CouponEvent;
import com.couponpop.couponservice.domain.couponevent.enums.CouponEventStatus;
import com.couponpop.couponservice.domain.couponevent.exception.CouponEventErrorCode;
import com.couponpop.couponservice.domain.couponevent.repository.CouponEventRepository;
import com.couponpop.couponservice.domain.couponevent.repository.dto.StoreCouponEventStatisticsProjection;
import com.couponpop.couponservice.domain.store.exception.StoreErrorCode;
import com.couponpop.couponservice.domain.store.service.StoreInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponEventService {

    private final CouponEventRepository couponEventRepository;
    private final CouponRepository couponRepository;
    private final StoreInternalService storeInternalService;

    private static final long MAX_EVENT_HOURS = 48L; // 이벤트 최대 기간(시간)

    // TODO : 정확한 시간? 에 이벤트를 어떻게 시작할 수 있을까?
    @Transactional
    public CreateCouponEventResponse createCouponEvent(CreateCouponEventRequest request, Long memberId) {
        /*
        TODO: StoreErrorCode 정의되면 변경하기. 사실 이 부분은 couponEvent 입장에선 매장 도메인에 요청을 해서 검증이 끝난 매장 entity 를 받아야 할듯
         */
        // store 소유 여부 검증
        StoreOwnershipResponse storeOwnership = storeInternalService.checkOwnership(request.storeId(), memberId);
        if (!storeOwnership.isOwner()) {
            throw new GlobalException(StoreErrorCode.STORE_ACCESS_PERMISSION_DENIED);
        }

        // 이벤트 기간 검증
        validateEventDuration(request.eventStartAt(), request.eventEndAt());

        CouponEvent couponEvent = couponEventRepository.save(request.toEntity(request.storeId(), memberId));
        return CreateCouponEventResponse.from(couponEvent);
    }

    public CouponEventDetailResponse getCouponEvent(Long eventId, Long memberId) {
        CouponEvent couponEvent = couponEventRepository.findById(eventId)
                .orElseThrow(() -> new GlobalException(CouponEventErrorCode.EVENT_NOT_FOUND));
        couponEvent.validateOwner(memberId); // CouponEvent 소유 여부 검증
        int usedCouponCount = couponRepository.countByEventIdAndStatus(eventId, CouponStatus.USED);
        return CouponEventDetailResponse.of(couponEvent, usedCouponCount);
    }

    /**
     * 매장별 쿠폰 이벤트 목록 조회 (Cursor 기반 페이징)
     *
     * @param memberId    요청한 회원 ID
     * @param storeId     조회할 매장 ID
     * @param eventStatus 조회할 이벤트 상태 (예: IN_PROGRESS)
     * @param cursor      이전 페이지 마지막 이벤트 정보를 담은 커서
     * @param pageSize    한 페이지당 조회할 이벤트 수
     * @return StoreCouponEventListResponse 페이징된 이벤트 목록 및 다음 커서 정보
     * @throws GlobalException 매장을 찾을 수 없거나, 회원이 매장 소유자가 아닌 경우 발생
     */
    public StoreCouponEventListResponse getCouponEventsByStore(Long memberId, Long storeId, CouponEventStatus eventStatus, LocalDateTime now, StoreCouponEventsCursor cursor, int pageSize) {
        // 매장 조회 및 소유자 검증
        StoreOwnershipResponse storeOwnership = storeInternalService.checkOwnership(storeId, memberId);
        if (!storeOwnership.isOwner()) {
            throw new IllegalArgumentException("해당 매장은 로그인한 회원 소유가 아닙니다.");
        }

        // Store 의 eventStatus 이벤트 목록 조회
        List<CouponEventDetailResponse> couponEventDetailResponses = couponEventRepository.fetchCouponEventsByStore(storeId, eventStatus, now, cursor, pageSize + 1).stream()
                .map(CouponEventDetailResponse::of)
                .toList();

        return StoreCouponEventListResponse.of(storeId, couponEventDetailResponses, pageSize);
    }

    /**
     * 매장별 쿠폰 이벤트 통계 정보를 조회합니다.
     *
     * <p>커서 기반 페이지네이션을 지원하며, 주어진 memberId에 해당하는 점주의
     * 매장 통계를 pageSize 단위로 조회합니다.</p>
     *
     * <p>조회 로직:
     * <ol>
     *     <li>Repository에서 pageSize + 1 만큼 데이터를 조회하여 다음 페이지 존재 여부 판단</li>
     *     <li>조회 결과를 {@link StoreCouponEventStatisticsResponse}로 변환</li>
     * </ol>
     * </p>
     *
     * @param memberId 조회할 점주의 회원 ID
     * @param cursor   다음 페이지 조회를 위한 커서 정보 (마지막 조회된 storeId 기준)
     * @param pageSize 한 페이지에 조회할 매장 수
     * @return {@link StoreCouponEventStatisticsResponse} - 매장별 통계 데이터와 다음 페이지 커서 포함
     */
    public StoreCouponEventStatisticsResponse getStoreCouponEventStatistics(Long memberId, StoreCouponEventsStatisticsCursor cursor, int pageSize) {
        List<StoreResponse> stores = storeInternalService.findStoresByOwner(memberId, cursor, pageSize + 1);
        List<Long> storeIds = stores.stream().map(StoreResponse::id).toList();

        List<StoreCouponEventStatisticsProjection> statistics = couponEventRepository.fetchStoreCouponEventStatistics(storeIds);
        return StoreCouponEventStatisticsResponse.of(statistics, pageSize);
    }

    private void validateEventDuration(LocalDateTime start, LocalDateTime end) {
        // "이벤트 종료 시간은 시작 시간보다 이후여야 합니다."
        if (end.isBefore(start)) {
            throw new GlobalException(CouponEventErrorCode.EVENT_END_BEFORE_START);
        }

        // "쿠폰 이벤트는 최대 48시간까지 생성 가능합니다."
        long hours = Duration.between(start, end).toHours();
        if (hours > MAX_EVENT_HOURS) {
            throw new GlobalException(CouponEventErrorCode.EVENT_DURATION_EXCEEDED);
        }

    }


}
