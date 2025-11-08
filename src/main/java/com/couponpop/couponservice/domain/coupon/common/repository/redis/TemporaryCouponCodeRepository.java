package com.couponpop.couponservice.domain.coupon.common.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class TemporaryCouponCodeRepository {

    private static final String TEMP_COUPON_PREFIX = "TEMP_COUPON:";
    private static final String DELIMITER = ":";

    private final RedisTemplate<String, Object> redisTemplate;

    // 임시 쿠폰 코드 저장 (TTL 적용)
    public void setTemporaryCoupon(Long couponId, String tempCode, String couponCode, long ttlSeconds) {
        String key = generateTempCouponKey(couponId, tempCode);
        redisTemplate.opsForValue().set(key, couponCode, Duration.ofSeconds(ttlSeconds));
    }

    // 임시 코드 검증
    public boolean validateTemporaryCoupon(Long couponId, String tempCode) {
        String key = generateTempCouponKey(couponId, tempCode);
        return redisTemplate.hasKey(key);
    }

    // 사용 후 삭제
    public void deleteTemporaryCoupon(Long couponId, String tempCode) {
        String key = generateTempCouponKey(couponId, tempCode);
        redisTemplate.delete(key);
    }

    public boolean validateAndDeleteTemporaryCoupon(Long couponId, String tempCode) {
        String key = generateTempCouponKey(couponId, tempCode);
        String value = (String) redisTemplate.opsForValue().getAndDelete(key); // 원자적 조회+삭제
        return value != null;
    }

    private static String generateTempCouponKey(Long couponId, String tempCode) {
        return TEMP_COUPON_PREFIX + couponId + DELIMITER + tempCode;
    }
}
