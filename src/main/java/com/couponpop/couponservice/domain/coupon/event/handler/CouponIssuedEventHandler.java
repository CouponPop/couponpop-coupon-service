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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Slf4j
@Component
public class CouponIssuedEventHandler extends AbstractCouponEventHandler<CouponIssuedEvent, CouponIssuedMessage> {

    private final CouponIssuedRetryableProcessor retryableProcessor;

    public CouponIssuedEventHandler(StoreInternalService storeInternalService, NotificationInternalService notificationInternalService, CouponPublisher eventPublisher, CouponIssuedRetryableProcessor retryableProcessor) {
        super(storeInternalService, notificationInternalService, eventPublisher);
        this.retryableProcessor = retryableProcessor;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCouponIssuedEvent(CouponIssuedEvent event) {
        handleEvent(event, event.memberId(), event.storeId(), () -> retryableProcessor.process(event));
    }

    @Override
    protected CouponStatus getCouponStatus() {
        return CouponStatus.ISSUED;
    }

    @Override
    protected CouponIssuedMessage buildMessage(CouponIssuedEvent event, String token, String storeName, LocalDateTime occurredAt) {
        String traceId = NotificationTraceIdGenerator.generate(event.couponId(), event.memberId(), event.storeId(), event.eventId(), token);
        return CouponIssuedMessage.of(
                traceId,
                token,
                event.couponId(),
                event.memberId(),
                event.storeId(),
                event.eventId(),
                storeName,
                event.eventName(),
                event.totalCount(),
                event.issuedCount(),
                CouponMessageType.ISSUED,
                occurredAt
        );
    }

    @Override
    protected String getHandlerName() {
        return "CouponIssuedEventHandler";
    }

}
