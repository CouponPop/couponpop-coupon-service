CREATE TABLE coupon_histories
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '쿠폰 히스토리 고유 ID',
    member_id       BIGINT       NOT NULL COMMENT '쿠폰 사용 회원 ID',
    store_id        BIGINT       NOT NULL COMMENT '쿠폰 발급 매장 ID',
    coupon_event_id BIGINT       NOT NULL COMMENT '쿠폰 이벤트 ID',
    total_count     INT          NOT NULL COMMENT '쿠폰 이벤트 총 발급 가능 수',
    event_name      VARCHAR(255) NOT NULL COMMENT '이벤트 이름',
    member_name     VARCHAR(255) NOT NULL COMMENT '회원 이름',
    store_name      VARCHAR(255) NOT NULL COMMENT '매장 이름',
    store_address   VARCHAR(255) NOT NULL COMMENT '매장 주소',
    event_start_at  DATETIME     NOT NULL COMMENT '이벤트 시작 시간',
    event_end_at    DATETIME     NOT NULL COMMENT '이벤트 종료 시간',
    issued_at       DATETIME     NOT NULL COMMENT '쿠폰 발급 시각',
    used_at         DATETIME NULL COMMENT '쿠폰 사용 시각',
    expire_at       DATETIME     NOT NULL COMMENT '쿠폰 만료 시각',
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '히스토리 생성 시각',

    -- 인덱스
    INDEX           idx_member_id (member_id),
    INDEX           idx_store_id (store_id),
    INDEX           idx_coupon_event_id (coupon_event_id),
    INDEX           idx_used_at (used_at)
) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE = utf8mb4_unicode_ci COMMENT='쿠폰 사용 히스토리 테이블';