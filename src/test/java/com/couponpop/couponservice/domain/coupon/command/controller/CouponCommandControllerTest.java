package com.couponpop.couponservice.domain.coupon.command.controller;

import com.couponpop.couponservice.common.exception.GlobalException;
import com.couponpop.couponservice.domain.coupon.command.service.CouponCommandService;
import com.couponpop.couponservice.domain.coupon.common.exception.CouponErrorCode;
import com.couponpop.couponservice.domain.coupon.command.controller.dto.request.CouponIssueRequest;
import com.couponpop.couponservice.domain.coupon.command.controller.dto.request.UseCouponRequest;
import com.couponpop.couponservice.domain.coupon.command.service.coupon_issue.CouponIssueFacade;
import com.couponpop.couponservice.domain.couponevent.exception.CouponEventErrorCode;
import com.couponpop.security.dto.AuthMember;
import com.couponpop.security.token.JwtAuthFilter;
import com.couponpop.security.token.JwtAuthenticationToken;
import com.couponpop.security.token.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CouponCommandController.class)
@AutoConfigureMockMvc(addFilters = false)
class CouponCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private CouponCommandService couponCommandService;

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
    @DisplayName("쿠폰 발급 요청")
    class IssueCoupon {
        @Test
        @DisplayName("쿠폰 발급 정상 요청 - 204 반환")
        void issueCoupon_success() throws Exception {
            // given
            CouponIssueRequest request = new CouponIssueRequest(2L);

            // when
            mockMvc.perform(
                            post("/api/v1/coupons/issue")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // then
            verify(couponIssueFacade, times(1))
                    .issueCoupon(anyLong(), anyLong(), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("쿠폰 발급 요청 실패 - 이벤트 없는 경우 예외")
        void issueCoupon_EventNotFound() throws Exception {
            // given
            CouponIssueRequest request = new CouponIssueRequest(999L);

            willThrow(new GlobalException(CouponEventErrorCode.EVENT_NOT_FOUND))
                    .given(couponIssueFacade)
                    .issueCoupon(anyLong(), anyLong(), any(LocalDateTime.class));

            // when & then
            mockMvc.perform(
                            post("/api/v1/coupons/issue")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.message").value(CouponEventErrorCode.EVENT_NOT_FOUND.getMessage()));
        }

        @Test
        @DisplayName("쿠폰 발급 요청 실패 - 중복 발급 예외")
        void issueCoupon_AlreadyIssued() throws Exception {
            // given
            CouponIssueRequest request = new CouponIssueRequest(10L);

            willThrow(new GlobalException(CouponErrorCode.COUPON_ALREADY_ISSUED))
                    .given(couponIssueFacade)
                    .issueCoupon(anyLong(), anyLong(), any(LocalDateTime.class));

            // when & then
            mockMvc.perform(
                            post("/api/v1/coupons/issue")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value(CouponErrorCode.COUPON_ALREADY_ISSUED.getMessage()));
        }

        @Test
        @DisplayName("쿠폰 발급 요청 실패 - 이벤트 기간 외 예외")
        void issueCoupon_EventNotInProgressTime() throws Exception {
            // given
            CouponIssueRequest request = new CouponIssueRequest(10L);

            willThrow(new GlobalException(CouponEventErrorCode.EVENT_NOT_IN_PROGRESS_TIME))
                    .given(couponIssueFacade)
                    .issueCoupon(anyLong(), anyLong(), any(LocalDateTime.class));

            // when & then
            mockMvc.perform(
                            post("/api/v1/coupons/issue")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value(CouponEventErrorCode.EVENT_NOT_IN_PROGRESS_TIME.getMessage()));
        }

        @Test
        @DisplayName("쿠폰 발급 요청 실패 - 이벤트 쿠폰 모두 소진")
        void issueCoupon_CouponSoldOut() throws Exception {
            // given
            CouponIssueRequest request = new CouponIssueRequest(10L);

            willThrow(new GlobalException(CouponEventErrorCode.EVENT_COUPON_SOLD_OUT))
                    .given(couponIssueFacade)
                    .issueCoupon(anyLong(), anyLong(), any(LocalDateTime.class));

            // when & then
            mockMvc.perform(
                            post("/api/v1/coupons/issue")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value(CouponEventErrorCode.EVENT_COUPON_SOLD_OUT.getMessage()));
        }

        @Test
        @DisplayName("쿠폰 발급 요청 실패 - eventId 누락")
        void issueCoupon_fail_missingEventId() throws Exception {
            String requestBody = """
                    {
                        "storeId": 1
                    }
                    """;

            mockMvc.perform(
                            post("/api/v1/coupons/issue")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("쿠폰 수령을 위한 이벤트 ID 는 필수입니다."));
        }
    }

    @Nested
    @DisplayName("쿠폰 사용 요청")
    class UseCouponTests {

        @Test
        @DisplayName("쿠폰 사용 성공 - 204 No Content")
        void useCoupon_Success() throws Exception {
            // given
            UseCouponRequest request = new UseCouponRequest("TEMP123", 1L, 1L);


            // when
            mockMvc.perform(
                            post("/api/v1/coupons/use")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // then
            verify(couponCommandService, times(1))
                    .useCoupon(anyLong(), anyLong(), anyString(), anyLong(), any(LocalDateTime.class));
        }

        @Test
        @DisplayName("쿠폰 사용 요청 실패 - 임시 코드 유효하지 않음")
        void useCoupon_InvalidTempCode() throws Exception {
            // given
            UseCouponRequest request = new UseCouponRequest("INVALID", 1L, 1L);

            Mockito.doThrow(new GlobalException(CouponErrorCode.COUPON_INVALID_TEMP_CODE))
                    .when(couponCommandService).useCoupon(anyLong(), Mockito.anyLong(), Mockito.anyString(), Mockito.anyLong(), Mockito.any(LocalDateTime.class));

            willThrow(new GlobalException(CouponErrorCode.COUPON_INVALID_TEMP_CODE))
                    .given(couponCommandService)
                    .useCoupon(anyLong(), anyLong(), anyString(), anyLong(), any(LocalDateTime.class));

            // when & then
            mockMvc.perform(
                            post("/api/v1/coupons/use")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request))
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value(CouponErrorCode.COUPON_INVALID_TEMP_CODE.getMessage()));
        }

        @Test
        @DisplayName("쿠폰 사용 요청 실패 - qrCode 누락")
        void useCoupon_fail_missingQrCode() throws Exception {
            String requestBody = """
                    {
                        "couponId": 1,
                        "storeId" : 1
                    }
                    """;

            mockMvc.perform(
                            post("/api/v1/coupons/use")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody)
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("QR 코드는 필수입니다."));
        }

        @Test
        @DisplayName("쿠폰 사용 요청 실패 - couponId 누락")
        void useCoupon_fail_missingCouponId() throws Exception {
            String requestBody = """
                    {
                        "qrCode": "foejsnvp",
                        "storeId" : 1
                    }
                    """;

            mockMvc.perform(
                            post("/api/v1/coupons/use")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(requestBody)
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.message").value("사용하려는 쿠폰 ID는 필수입니다."));
        }
    }

}