alter table coupons
    MODIFY COLUMN used_at DATETIME NULL COMMENT '쿠폰 사용 시간'
;
