package com.couponpop.couponservice.domain.coupon.event.handler;

import com.couponpop.couponpopcoremodule.dto.coupon.event.model.CouponIssuedMessage;
import com.couponpop.couponpopcoremodule.enums.coupon.CouponMessageType;
import com.couponpop.couponpopcoremodule.utils.NotificationTraceIdGenerator;
import com.couponpop.couponservice.domain.coupon.common.enums.CouponStatus;
import com.couponpop.couponservice.domain.coupon.event.CouponPublisher;
import com.couponpop.couponservice.domain.coupon.event.model.CouponIssuedEvent;
import com.couponpop.couponservice.domain.coupon.event.retry.CouponIssuedRetryableProcessor;
import com.couponpop.couponservice.domain.notification.service.NotificationInternalService;
import com.couponpop.couponservice.domain.store.service.StoreInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssuedEventHandler {

    private final StoreInternalService storeInternalService;
    private final NotificationInternalService notificationInternalService;

    private final CouponIssuedRetryableProcessor retryableProcessor;
    private final CouponPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCouponIssuedEvent(CouponIssuedEvent event) {
        retryableProcessor.process(event); // 쿠폰 사용 이력 적재 (Retry + Recover 적용)
        log.info("[쿠폰 발급 이벤트]");

        // 알림은 누락되고 늦게 요청되어도 상관없음
        List<String> tokens = getFcmTokensForMember(event.memberId());
        if (tokens.isEmpty()) {
            log.info("[CouponUsedEventHandler] memberId={} 알림 토큰 없음, 이벤트 발행 생략", event.memberId());
            return;
        }
        String storeName = getStoreName(event.storeId());

        publishCouponUsedNotifications(event, tokens, storeName);
    }

    /**
     * FCM 토큰 조회
     */
    private List<String> getFcmTokensForMember(Long memberId) {
        return notificationInternalService
                .fetchFcmTokensByMemberId(memberId)
                .fcmTokens();
    }

    /**
     * 매장 이름 조회
     */
    private String getStoreName(Long storeId) {
        return storeInternalService
                .findByIdOrElseThrow(storeId)
                .name();
    }

    /**
     * 쿠폰 발행 알림 메시지 발행
     */
    private void publishCouponUsedNotifications(CouponIssuedEvent event, List<String> tokens, String storeName) {
        Long couponId = event.couponId();
        Long memberId = event.memberId();
        Long storeId = event.storeId();
        Long eventId = event.eventId();

        tokens.forEach(token -> {
            String traceId = NotificationTraceIdGenerator.generate(couponId, memberId, storeId, eventId, token);

            CouponIssuedMessage message = CouponIssuedMessage.of(
                    traceId,
                    token,
                    couponId,
                    memberId,
                    storeId,
                    storeName,
                    eventId,
                    event.totalCount(),
                    event.issuedCount(),
                    event.eventName(),
                    CouponMessageType.ISSUED
            );

            eventPublisher.publish(CouponStatus.ISSUED, message);
            log.debug("[CouponUsedEventHandler] 쿠폰 발행 알림 traceId={}, memberId={}, storeId={}", traceId, memberId, storeId);
        });
    }

}
