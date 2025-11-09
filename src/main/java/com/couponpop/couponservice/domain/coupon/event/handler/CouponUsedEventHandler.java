package com.couponpop.couponservice.domain.coupon.event.handler;

import com.couponpop.couponpopcoremodule.dto.coupon.event.model.CouponUsedMessage;
import com.couponpop.couponpopcoremodule.dto.store.response.StoreResponse;
import com.couponpop.couponpopcoremodule.enums.coupon.CouponMessageType;
import com.couponpop.couponservice.domain.coupon.common.enums.CouponStatus;
import com.couponpop.couponservice.domain.coupon.event.CouponPublisher;
import com.couponpop.couponservice.domain.coupon.event.model.CouponUsedEvent;
import com.couponpop.couponservice.domain.coupon.event.retry.CouponUsedRetryableProcessor;
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

    private final StoreInternalService storeInternalService;

    private final CouponUsedRetryableProcessor retryableProcessor;
    private final CouponPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCouponUsedEvent(CouponUsedEvent event) {
        retryableProcessor.process(event); // 쿠폰 사용 이력 적재 (Retry + Recover 적용)

        // 알림은 누락되고 늦게 요청되어도 상관없음
        // TODO : FCM 토큰 개별적으로 포함해서 요청
        StoreResponse storeResponse = storeInternalService.findByIdOrElseThrow(event.storeId());

        CouponUsedMessage usedMessage = CouponUsedMessage.of(
                event.couponId(),
                event.memberId(),
                event.storeId(),
                storeResponse.name(),
                event.eventId(),
                event.eventName(),
                CouponMessageType.USED
        );

        eventPublisher.publish(CouponStatus.USED, usedMessage);
    }

}
