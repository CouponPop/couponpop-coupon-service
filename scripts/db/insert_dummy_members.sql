-- ==================================================
-- ✅ 대량/다양한 케이스 쿠폰 테스트 데이터 생성
-- ==================================================
-- 작성일: 2025-10-22
-- 용도: 매장별 쿠폰 이벤트 목록 조회 성능 + 케이스 다양성 테스트
-- ==================================================

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE coupons;
TRUNCATE TABLE coupon_events;
TRUNCATE TABLE stores;
TRUNCATE TABLE members;
SET FOREIGN_KEY_CHECKS = 1;

-- 1️⃣ Members (OWNER 10명 + CUSTOMER 90명)
INSERT INTO members (member_type, email, username, password, phone_number, created_at, updated_at)
SELECT 'CUSTOMER',
       CONCAT('user', i, '@test.com'),
       CONCAT('user', i),
       '$2a$10$NQ.yFI.B1o86zY2g8M7sTOpacGG6zaveHTbwu59ieT44oRBRitoNK',
       CONCAT('010', LPAD(i, 8, '0')),
       now(),
       now()
FROM (SELECT @rownum := @rownum + 1 AS i
      FROM information_schema.tables t1,
          information_schema.tables t2,
          (SELECT @rownum := 0) r
          LIMIT 1000) tmp;