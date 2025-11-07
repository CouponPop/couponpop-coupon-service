alter table coupons
    ADD COLUMN coupon_event_id BIGINT                                                             NOT NULL COMMENT '쿠폰 이벤트 ID',
    ADD COLUMN member_id       BIGINT                                                             NOT NULL COMMENT '회원 ID',
    ADD COLUMN coupon_status   ENUM ('ISSUED','CANCELED','EXPIRED','USED') DEFAULT 'ISSUED' NOT NULL COMMENT '쿠폰 상태',
    ADD COLUMN coupon_code     VARCHAR(255)                                                       NOT NULL COMMENT '쿠폰 식별 코드',
    ADD COLUMN received_at     DATETIME                                                           NOT NULL COMMENT '쿠폰 발급 시간',
    ADD COLUMN used_at         DATETIME                                                           NOT NULL COMMENT '쿠폰 사용 시간',
    ADD COLUMN created_at      DATETIME                                                           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    ADD COLUMN updated_at      DATETIME                                                           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일'
;
