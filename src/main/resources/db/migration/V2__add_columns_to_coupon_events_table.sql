alter table coupon_events
    ADD COLUMN store_id BIGINT NOT NULL COMMENT '쿠폰 발급 매장 ID',
    ADD COLUMN coupon_event_status  ENUM ('CANCELED', 'COMPLETED', 'IN_PROGRESS', 'SCHEDULED') DEFAULT 'SCHEDULED' NOT NULL COMMENT '쿠폰 이벤트 상태',
    ADD COLUMN name                 VARCHAR (100)       NOT NULL COMMENT '쿠폰 이벤트 이름',
    ADD COLUMN event_start_at       DATETIME            NOT NULL COMMENT '이벤트 시작 시간',
    ADD COLUMN event_end_at         DATETIME            NOT NULL COMMENT '이벤트 종료 시간',
    ADD COLUMN total_count          INT                 NOT NULL COMMENT '발급 가능한 쿠폰 총 수량',
    ADD COLUMN issued_count         INT                 NOT NULL DEFAULT 0 COMMENT '발금된 쿠폰 수량',
    ADD COLUMN created_at           DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
    ADD COLUMN updated_at           DATETIME            NOT NULL DEFAULT CURRENT_TIMESTAMP ON
UPDATE CURRENT_TIMESTAMP COMMENT '수정일'
;
