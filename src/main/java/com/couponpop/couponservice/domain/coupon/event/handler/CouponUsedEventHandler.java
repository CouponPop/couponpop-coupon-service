package com.couponpop.couponservice.domain.coupon.event.handler;

import com.couponpop.couponpopcoremodule.dto.coupon.event.model.CouponUsedMessage;
import com.couponpop.couponpopcoremodule.dto.store.response.StoreResponse;
import com.couponpop.couponpopcoremodule.enums.coupon.CouponMessageType;
import com.couponpop.couponservice.domain.coupon.common.enums.CouponStatus;
import com.couponpop.couponservice.domain.coupon.event.CouponPublisher;
import com.couponpop.couponservice.domain.coupon.event.model.CouponUsedEvent;
import com.couponpop.couponservice.domain.couponhistory.service.CouponHistoryService;
import com.couponpop.couponservice.domain.store.service.StoreInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponUsedEventHandler {

    private final CouponHistoryService couponHistoryService;
    private final StoreInternalService storeInternalService;

    private final CouponPublisher eventPublisher;

    /**
     * 쿠폰 사용 이벤트 발생 후, 독립 트랜잭션에서 DB 적재
     */
    // TODO: 후추 알림 비동기 적용할 때 꼭 AsyncConfig 비동기 작업 전용 스레드 풀 설정하기
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCouponUsedEvent(CouponUsedEvent event) {
        couponHistoryService.saveCouponHistory(event.toCouponUsedDto());

        StoreResponse storeResponse = storeInternalService.findByIdOrElseThrow(event.storeId());
        CouponUsedMessage usedMessage = convertMessageFrom(event, storeResponse);
        eventPublisher.publish(CouponStatus.USED, usedMessage);
    }

    private CouponUsedMessage convertMessageFrom(CouponUsedEvent event, StoreResponse storeResponse) {
        return CouponUsedMessage.of(
                event.couponId(),
                event.memberId(),
                event.storeId(),
                storeResponse.name(),
                event.eventId(),
                event.eventName(),
                CouponMessageType.USED
        );
    }
}
