alter table coupons
    ADD COLUMN expire_at DATETIME NOT NULL COMMENT '쿠폰 만료 시간'
;
