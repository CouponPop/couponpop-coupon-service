package com.couponpop.couponservice.domain.couponevent.command.service;

import com.couponpop.couponpopcoremodule.dto.couponevent.response.StoreOwnershipResponse;
import com.couponpop.couponservice.common.exception.GlobalException;
import com.couponpop.couponservice.domain.couponevent.command.controller.dto.request.CreateCouponEventRequest;
import com.couponpop.couponservice.domain.couponevent.command.controller.dto.response.CreateCouponEventResponse;
import com.couponpop.couponservice.domain.couponevent.common.entity.CouponEvent;
import com.couponpop.couponservice.domain.couponevent.common.exception.CouponEventErrorCode;
import com.couponpop.couponservice.domain.couponevent.common.repository.CouponEventRepository;
import com.couponpop.couponservice.domain.store.exception.StoreErrorCode;
import com.couponpop.couponservice.domain.store.service.StoreInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class CouponEventCommandService {

    private final CouponEventRepository couponEventRepository;
    private final StoreInternalService storeInternalService;

    private static final long MAX_EVENT_HOURS = 48L; // 이벤트 최대 기간(시간)

    // TODO : 정확한 시간? 에 이벤트를 어떻게 시작할 수 있을까?
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
