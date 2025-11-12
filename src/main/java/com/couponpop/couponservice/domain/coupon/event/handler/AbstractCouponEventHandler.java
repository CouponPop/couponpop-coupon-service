package com.couponpop.couponservice.domain.coupon.event.handler;

import com.couponpop.couponpopcoremodule.dto.coupon.event.model.CouponFcmMessage;
import com.couponpop.couponservice.domain.coupon.common.enums.CouponStatus;
import com.couponpop.couponservice.domain.coupon.event.CouponPublisher;
import com.couponpop.couponservice.domain.coupon.event.model.CouponEvent;
import com.couponpop.couponservice.domain.notification.service.NotificationInternalService;
import com.couponpop.couponservice.domain.store.service.StoreInternalService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@AllArgsConstructor
public abstract class AbstractCouponEventHandler<E extends CouponEvent, M extends CouponFcmMessage> {

    protected final StoreInternalService storeInternalService;
    protected final NotificationInternalService notificationInternalService;
    protected final CouponPublisher eventPublisher;

    protected void handleEvent(E event, Long memberId, Long storeId, Runnable retryableTask) {

        // Retryable 처리
        retryableTask.run();

        // 알림 토큰 조회
        List<String> tokens = getFcmTokensForMember(memberId);
        if (tokens.isEmpty()) {
            log.info("[CouponUsedEventHandler] memberId={} 알림 토큰 없음, 이벤트 발행 생략", memberId);
            return;
        }

        // 매장 이름 조회
        String storeName = getStoreName(storeId);

        // 4. 메시지 발행
        tokens.forEach(token -> {
            try {
                CouponFcmMessage message = buildMessage(event, token, storeName, LocalDateTime.now());
                eventPublisher.publish(getCouponStatus(), message);
                log.debug("[{}] 알림 발행 성공 memberId={}, storeId={}", getHandlerName(), memberId, storeId);
            } catch (Exception e) {
                log.error("[{}] 알림 발행 실패 memberId={}, storeId={}", getHandlerName(), memberId, storeId, e);
            }
        });
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

    protected abstract CouponStatus getCouponStatus();

    protected abstract M buildMessage(E event, String token, String storeName, LocalDateTime occurredAt);

    protected abstract String getHandlerName();
}
