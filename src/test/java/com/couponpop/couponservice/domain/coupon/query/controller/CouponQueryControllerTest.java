package com.couponpop.couponservice.domain.coupon.query.controller;

import com.couponpop.couponpopcoremodule.enums.StoreCategory;
import com.couponpop.couponservice.common.exception.GlobalException;
import com.couponpop.couponservice.domain.coupon.command.service.coupon_issue.CouponIssueFacade;
import com.couponpop.couponservice.domain.coupon.common.enums.CouponStatus;
import com.couponpop.couponservice.domain.coupon.common.exception.CouponErrorCode;
import com.couponpop.couponservice.domain.coupon.query.controller.dto.request.MemberIssuedCouponCursor;
import com.couponpop.couponservice.domain.coupon.query.controller.dto.response.*;
import com.couponpop.couponservice.domain.coupon.query.service.CouponQueryService;
import com.couponpop.security.dto.AuthMember;
import com.couponpop.security.token.JwtAuthFilter;
import com.couponpop.security.token.JwtAuthenticationToken;
import com.couponpop.security.token.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CouponQueryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CouponQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private CouponQueryService couponQueryService;

    @MockitoBean
    private CouponIssueFacade couponIssueFacade;

    @BeforeEach
    void setUp() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        AuthMember authMember = AuthMember.of(123L, "testUser", "CUSTOMER");
        Authentication authenticationToken = new JwtAuthenticationToken(authMember);
        context.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(context);
    }

    @Nested
    @DisplayName("쿠폰 정보 상세 조회 요청")
    class GetCouponDetailTests {

        public static CouponDetailResponse createSample(Long storeId, Long eventId, Long couponId) {
            // QR 코드
            CouponDetailResponse.QrCode qrCode = new CouponDetailResponse.QrCode(
                    "https://couponpop.com/q/ABC123XYZ",
                    "사장님께 QR코드를 보여주세요"
            );

            // 이벤트 기간
            EventPeriodResponse period = new EventPeriodResponse(
                    LocalDateTime.of(2025, 10, 21, 0, 0),
                    LocalDateTime.of(2025, 10, 31, 23, 59)
            );

            //  이벤트 정보
            EventInfoResponse eventInfo = new EventInfoResponse(
                    eventId,
                    "아메리카노 1+1 이벤트",
                    period
            );

            // 매장 정보
            StoreInfoResponse storeInfo = new StoreInfoResponse(
                    storeId,
                    "스타벅스 강남점",
                    StoreCategory.CAFE,
                    3.14,
                    4.1534,
                    "imageUrl"
            );

            // CouponDetailResponse 생성
            return new CouponDetailResponse(
                    couponId,
                    CouponStatus.ISSUED,
                    LocalDateTime.of(2025, 10, 21, 12, 0),
                    LocalDateTime.of(2025, 11, 21, 23, 59),
                    null,
                    qrCode,
                    eventInfo,
                    storeInfo
            );
        }

        @Test
        @DisplayName("쿠폰 정보 상세 조회 요청 - 200 반환")
        void getCouponDetail_Success() throws Exception {
            // given
            Long couponId = 1L;
            CouponDetailResponse response = createSample(100L, 1L, couponId);

            given(couponQueryService.getCouponDetail(anyLong(), anyLong())).willReturn(response);

            // when & then
            mockMvc.perform(
                            get("/api/v1/coupons/{couponId}", couponId)
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1L))
                    .andExpect(jsonPath("$.data.status").value(CouponStatus.ISSUED.name()))
                    .andExpect(jsonPath("$.data.event.id").value(1L))
                    .andExpect(jsonPath("$.data.store.id").value(100L));
        }

        @Test
        @DisplayName("쿠폰 정보 상세 조회 실패 - 쿠폰 없음")
        void getCouponDetail_NotFound() throws Exception {
            // given
            willThrow(new GlobalException(CouponErrorCode.COUPON_NOT_FOUND))
                    .given(couponQueryService)
                    .getCouponDetail(anyLong(), anyLong());

            // when & then
            mockMvc.perform(
                            get("/api/v1/coupons/{couponId}", 999L)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value(CouponErrorCode.COUPON_NOT_FOUND.getMessage()));
        }

        @Test
        @DisplayName("쿠폰 정보 상세 조회 실패 - 쿠폰 접근 권한 없음")
        void getCouponDetail_AccessDenied() throws Exception {
            // given
            willThrow(new GlobalException(CouponErrorCode.COUPON_ACCESS_DENIED))
                    .given(couponQueryService)
                    .getCouponDetail(anyLong(), anyLong());

            // when & then
            mockMvc.perform(
                            get("/api/v1/coupons/{couponId}", 999L)
                    )
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.message").value(CouponErrorCode.COUPON_ACCESS_DENIED.getMessage()));
        }
    }

    @Nested
    @DisplayName("쿠폰 리스트 조회")
    class GetIssuedCoupons {

        private static final LocalDateTime now = LocalDateTime.now();

        @Test
        @DisplayName("첫 페이지 조회 - hasNext true")
        void getIssuedCoupons_firstPage_hasNextTrue() throws Exception {
            // given
            EventInfoResponse eventInfoResponse = new EventInfoResponse(1L, "이벤트1", new EventPeriodResponse(now.minusDays(2), now.plusDays(1)));
            StoreInfoResponse storeInfoResponse = new StoreInfoResponse(1L, "매장", StoreCategory.CAFE, 3.14, 3.14, "imageUrl");
            List<CouponDetailResponse> responses = List.of(
                    new CouponDetailResponse(1L, CouponStatus.ISSUED, now.minusDays(1), now.plusDays(1), null, null, eventInfoResponse, storeInfoResponse),
                    new CouponDetailResponse(2L, CouponStatus.ISSUED, now.minusDays(1), now.plusDays(1), null, null, eventInfoResponse, storeInfoResponse),
                    new CouponDetailResponse(3L, CouponStatus.ISSUED, now.minusDays(1), now.plusDays(1), null, null, eventInfoResponse, storeInfoResponse)
            );
            IssuedCouponListResponse mockResponse = IssuedCouponListResponse.of(responses, 2);

            given(couponQueryService.getIssuedCoupons(anyLong(), eq(CouponStatus.ISSUED), any(MemberIssuedCouponCursor.class), eq(2)))
                    .willReturn(mockResponse);

            // when & then
            mockMvc.perform(
                            get("/api/v1/coupons")
                                    .param("type", CouponStatus.ISSUED.name())
                                    .param("size", "2")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.coupons.length()").value(2))
                    .andExpect(jsonPath("$.data.hasNext").value(true))
                    .andExpect(jsonPath("$.data.nextCursor").isNotEmpty());
        }

        @Test
        @DisplayName("다음 페이지 조회 - 마지막 페이지 (hasNext false)")
        void getIssuedCoupons_nextPage_lastPage() throws Exception {
            // given
            EventInfoResponse eventInfoResponse = new EventInfoResponse(1L, "이벤트1", new EventPeriodResponse(now.minusDays(2), now.plusDays(1)));
            StoreInfoResponse storeInfoResponse = new StoreInfoResponse(1L, "매장", StoreCategory.CAFE, 3.14, 3.14, "imageUrl");
            List<CouponDetailResponse> responses = List.of(
                    new CouponDetailResponse(3L, CouponStatus.ISSUED, now.minusDays(1), now.plusDays(1), null, null, eventInfoResponse, storeInfoResponse)
            );
            IssuedCouponListResponse mockResponse = IssuedCouponListResponse.of(responses, 2);

            given(couponQueryService.getIssuedCoupons(anyLong(), eq(CouponStatus.ISSUED), any(MemberIssuedCouponCursor.class), eq(2)))
                    .willReturn(mockResponse);

            // when & then
            mockMvc.perform(
                            get("/api/v1/coupons")
                                    .param("type", CouponStatus.ISSUED.name())
                                    .param("lastEventEndAt", now.minusDays(1).toString())
                                    .param("lastCouponId", "2")
                                    .param("size", "2")
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.coupons.length()").value(1))
                    .andExpect(jsonPath("$.data.hasNext").value(false))
                    .andExpect(jsonPath("$.data.nextCursor").isEmpty());
        }
    }
}