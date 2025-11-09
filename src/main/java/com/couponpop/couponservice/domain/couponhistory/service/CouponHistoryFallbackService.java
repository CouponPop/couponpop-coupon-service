package com.couponpop.couponservice.domain.couponhistory.service;

import com.couponpop.couponservice.domain.couponhistory.service.dto.CouponUsedDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponHistoryFallbackService {

    private final CouponHistoryService couponHistoryService;

    public void saveToBackupStorage(CouponUsedDto dto, Exception e) {
        try {
            // 예시 ①: 보상 테이블에 저장
            log.warn("""
                            [보상 저장 실행]
                            - 쿠폰ID: {}
                            - 회원ID: {}
                            - 매장ID: {}
                            - 이벤트ID: {}
                            - 원인: {}
                            """,
                    dto.couponId(),
                    dto.memberId(),
                    dto.storeId(),
                    dto.eventId(),
                    e.getMessage()
            );

            couponHistoryService.saveCouponHistory(dto);
        } catch (Exception ex) {
            log.error("[보상 저장 실패 - 수동 점검 필요]", ex);
        }
    }
}
