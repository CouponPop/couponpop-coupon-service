ALTER TABLE coupon_events
    MODIFY COLUMN coupon_event_status ENUM('CANCELED', 'COMPLETED', 'IN_PROGRESS', 'SCHEDULED') NULL COMMENT '쿠폰 이벤트 상태';