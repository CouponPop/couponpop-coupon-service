-- 인덱스 삭제
ALTER TABLE coupon_histories
    DROP INDEX idx_member_id,
    DROP INDEX idx_store_id,
    DROP INDEX idx_coupon_event_id,
    DROP INDEX idx_used_at;

-- 불필요한 컬럼 삭제
ALTER TABLE coupon_histories
    DROP COLUMN total_count,
    DROP COLUMN event_name,
    DROP COLUMN member_name,
    DROP COLUMN store_name,
    DROP COLUMN store_address,
    DROP COLUMN event_start_at,
    DROP COLUMN event_end_at,
    DROP COLUMN issued_at,
    DROP COLUMN used_at,
    DROP COLUMN expire_at;

-- 컬럼 추가
ALTER TABLE coupon_histories
    ADD COLUMN coupon_id     BIGINT                              NOT NULL COMMENT '쿠폰 ID',
    ADD COLUMN coupon_status ENUM ('ISSUED', 'USED', 'CANCELED') NOT NULL COMMENT '쿠폰 상태';

-- 테이블 설명 변경
ALTER TABLE coupon_histories
    COMMENT ='쿠폰 히스토리 테이블'