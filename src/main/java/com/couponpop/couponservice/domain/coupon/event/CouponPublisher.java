package com.couponpop.couponservice.domain.coupon.event;

import com.couponpop.couponservice.domain.coupon.common.enums.CouponStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.couponpop.couponpopcoremodule.constants.RabbitMqExchanges.COUPON_EXCHANGE;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponPublisher {

    public static final String COUPON_EVENT_ROUTING_KEY_PREFIX = "coupon.";

    private final RabbitTemplate rabbitTemplate;

    public <T> void publish(CouponStatus status, T message) {
        String routingKey = buildRoutingKey(status);

        try {
            rabbitTemplate.convertAndSend(COUPON_EXCHANGE, routingKey, message);
            log.info("[쿠폰 이벤트 발행 성공] routingKey={}, message={}", routingKey, message);
        } catch (AmqpException e) {
            log.error("쿠폰 사용 FCM 발송 요청 전송에 실패했습니다. message: {}", message, e);
            throw e;
        }
    }

    private String buildRoutingKey(CouponStatus status) {
        return COUPON_EVENT_ROUTING_KEY_PREFIX + status.asRoutingKey();
    }
}
