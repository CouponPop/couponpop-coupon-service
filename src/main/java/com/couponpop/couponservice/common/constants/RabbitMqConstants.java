package com.couponpop.couponservice.common.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RabbitMqConstants {

    // Exchange
    public static final String COUPON_EXCHANGE = "coupon.exchange";
    public static final String COUPON_DLX_EXCHANGE = "coupon.exchange.dlx";

    // Queue
    public static final String COUPON_USED_QUEUE = "coupon.queue.used";
    public static final String COUPON_USED_DLQ_QUEUE = "coupon.queue.used.dlq";
    public static final String COUPON_ISSUED_QUEUE = "coupon.queue.issued";
    public static final String COUPON_ISSUED_DLQ_QUEUE = "coupon.queue.issued.dlq";

    // Routing Key
    public static final String COUPON_EVENT_ROUTING_KEY_PREFIX = "coupon.";
    public static final String COUPON_USED_ROUTING_KEY = "coupon.used";
    public static final String COUPON_USED_DLQ_ROUTING_KEY = "coupon.used.dlq";

    public static final String COUPON_ISSUED_ROUTING_KEY = "coupon.issued";
    public static final String COUPON_ISSUED_DLQ_ROUTING_KEY = "coupon.issued.dlq";

}
