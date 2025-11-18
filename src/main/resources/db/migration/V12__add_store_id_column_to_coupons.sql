ALTER TABLE coupons
    ADD COLUMN store_id BIGINT NOT NULL COMMENT '쿠폰을 발급한 매장 ID';
