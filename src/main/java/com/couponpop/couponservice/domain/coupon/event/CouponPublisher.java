package com.couponpop.couponservice.domain.coupon.event;

import com.couponpop.couponservice.common.constants.RabbitMqConstants;
import com.couponpop.couponservice.domain.coupon.common.enums.CouponStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.couponpop.couponservice.common.constants.RabbitMqConstants.COUPON_EVENT_ROUTING_KEY_PREFIX;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(CouponStatus status, Object message) {
        rabbitTemplate.convertAndSend(
                RabbitMqConstants.COUPON_EXCHANGE,
                COUPON_EVENT_ROUTING_KEY_PREFIX + status.asRoutingKey(),
                message);
    }
}
