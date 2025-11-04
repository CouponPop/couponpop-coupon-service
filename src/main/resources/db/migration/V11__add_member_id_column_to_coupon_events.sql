ALTER TABLE coupon_events
    ADD COLUMN member_id BIGINT NOT NULL COMMENT '쿠폰을 발급한 회원 ID';