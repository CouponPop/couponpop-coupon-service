package com.couponpop.couponservice.domain.coupon.event.handler;

import com.couponpop.couponpopcoremodule.dto.coupon.event.model.CouponUsedMessage;
import com.couponpop.couponpopcoremodule.enums.coupon.CouponMessageType;
import com.couponpop.couponpopcoremodule.utils.NotificationTraceIdGenerator;
import com.couponpop.couponservice.domain.coupon.common.enums.CouponStatus;
import com.couponpop.couponservice.domain.coupon.event.CouponPublisher;
import com.couponpop.couponservice.domain.coupon.event.model.CouponUsedEvent;
import com.couponpop.couponservice.domain.coupon.event.retry.CouponUsedRetryableProcessor;
import com.couponpop.couponservice.domain.notification.service.NotificationInternalService;
import com.couponpop.couponservice.domain.store.service.StoreInternalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Slf4j
@Component
public class CouponUsedEventHandler extends AbstractCouponEventHandler<CouponUsedEvent, CouponUsedMessage> {

    private final CouponUsedRetryableProcessor retryableProcessor;

    public CouponUsedEventHandler(StoreInternalService storeInternalService, NotificationInternalService notificationInternalService, CouponPublisher eventPublisher, CouponUsedRetryableProcessor retryableProcessor) {
        super(storeInternalService, notificationInternalService, eventPublisher);
        this.retryableProcessor = retryableProcessor;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCouponUsedEvent(CouponUsedEvent event) {
        handleEvent(event, event.memberId(), event.storeId(), () -> retryableProcessor.process(event));
    }

    @Override
    protected CouponStatus getCouponStatus() {
        return CouponStatus.USED;
    }

    @Override
    protected CouponUsedMessage buildMessage(CouponUsedEvent event, String token, String storeName, LocalDateTime occurredAt) {
        String traceId = NotificationTraceIdGenerator.generate(event.couponId(), event.memberId(), event.storeId(), event.eventId(), token);
        return CouponUsedMessage.of(
                traceId,
                token,
                event.couponId(),
                event.memberId(),
                event.storeId(),
                storeName,
                event.eventId(),
                event.eventName(),
                CouponMessageType.USED,
                occurredAt
        );
    }

    @Override
    protected String getHandlerName() {
        return "CouponUsedEventHandler";
    }

}
