package com.couponpop.couponservice.domain.couponevent.query.controller;

import com.couponpop.couponservice.domain.couponevent.command.controller.dto.response.*;
import com.couponpop.couponservice.domain.couponevent.common.enums.CouponEventStatus;
import com.couponpop.couponservice.domain.couponevent.common.repository.dto.StoreCouponEventStatisticsProjection;
import com.couponpop.couponservice.domain.couponevent.query.service.CouponEventQueryService;
import com.couponpop.security.dto.AuthMember;
import com.couponpop.security.token.JwtAuthFilter;
import com.couponpop.security.token.JwtAuthenticationToken;
import com.couponpop.security.token.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
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
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CouponEventQueryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CouponEventQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private CouponEventQueryService couponEventQueryService;

    @BeforeEach
    void setUp() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        AuthMember authMember = AuthMember.of(123L, "testUser", "CUSTOMER");
        Authentication authenticationToken = new JwtAuthenticationToken(authMember);
        context.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(context);
    }


    @Test
    @DisplayName("쿠폰 이벤트 정보 상세 조회 - 성공")
    void getCouponEvent_success() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.of(2025, 10, 14, 17, 0);

        LocalDateTime eventStartAt = now.minusDays(1);
        LocalDateTime eventEndAt = now.plusDays(1);
        Long eventId = 1L;

        CouponEventDetailResponse response = new CouponEventDetailResponse(
                eventId,
                "아이스 아메리카노 1+1",
                EventPeriod.of(eventStartAt, eventEndAt),
                new EventStatisticSummary(30, 30, 0, 0, 0),
                LocalDateTime.of(2025, 10, 14, 15, 0),
                LocalDateTime.of(2025, 10, 14, 15, 0)
        );

        given(couponEventQueryService.getCouponEvent(anyLong(), anyLong()))
                .willReturn(response);

        // when & then
        mockMvc.perform(
                        get("/api/v1/owner/coupons/events/{eventId}", eventId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventName").value("아이스 아메리카노 1+1"))
        ;
    }

    @Test
    @DisplayName("Cursor 기반 페이지 조회 - 성공")
    void getCouponEventsByStore_withCursor() throws Exception {
        // given
        Long storeId = 1L;

        // Mock 15개의 이벤트
        List<CouponEventDetailResponse> allEvents = new ArrayList<>();
        for (int i = 1; i <= 15; i++) {
            CouponEventDetailResponse event = new CouponEventDetailResponse(
                    (long) i,
                    "이벤트 " + i,
                    new EventPeriod(
                            LocalDateTime.of(2025, 10, 22, 4 + i, 0),
                            LocalDateTime.of(2025, 10, 22, 5 + i, 0)
                    ),
                    new EventStatisticSummary(100, 50, 50, 25, 25),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );
            allEvents.add(event);
        }

        // 페이지 1
        StoreCouponEventListResponse page1 = StoreCouponEventListResponse.of(
                storeId,
                allEvents.subList(0, 11),
                10
        );

        // 페이지 2
        StoreCouponEventListResponse page2 = StoreCouponEventListResponse.of(
                storeId,
                allEvents.subList(10, 15),
                10
        );

        BDDMockito.given(couponEventQueryService.getCouponEventsByStore(anyLong(), anyLong(), any(CouponEventStatus.class), any(LocalDateTime.class), any(), any(Integer.class)))
                .willReturn(page1)
                .willReturn(page2);

        // when & then
        // 첫 페이지 호출
        mockMvc.perform(
                        get("/api/v1/owner/coupons/events")
                                .param("storeId", "1")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.events.length()").value(10))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor.lastEventId").value(10));

        // 두 번째 페이지 호출
        mockMvc.perform(
                        get("/api/v1/owner/coupons/events")
                                .param("storeId", "1")
                                .param("size", "10")
                                .param("lastStartAt", page1.nextCursor().lastStartAt().toString())
                                .param("lastEndAt", page1.nextCursor().lastEndAt().toString())
                                .param("lastEventId", page1.nextCursor().lastEventId().toString())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.events.length()").value(5))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.nextCursor").doesNotExist());
    }

    @Test
    @DisplayName("매장 별 대시보드 조회 - 성공")
    void getStoreCouponEventStatistics_withCursor() throws Exception {
        // given
        Long lastStoreId = 100L;
        int size = 2;

        var couponStats = new StoreCouponEventStatisticsProjection.CouponStats(2300, 1840, 1000);
        var storeStat = new StoreCouponEventStatisticsProjection(
                101L,
                couponStats,
                LocalDateTime.of(2025, 10, 21, 23, 59, 59)
        );

        var responseDto = StoreCouponEventStatisticsResponse.of(
                List.of(storeStat),
                size
        );

        given(couponEventQueryService.getStoreCouponEventStatistics(anyLong(), any(), anyInt()))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/owner/coupons/events/statistics")
                        .param("lastStoreId", lastStoreId.toString())
                        .param("size", String.valueOf(size))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.statistics[0].storeId").value(101))
                .andExpect(jsonPath("$.data.statistics[0].couponStats.total").value(2300))
                .andExpect(jsonPath("$.data.statistics[0].couponStats.unclaimed").value(460))
                .andExpect(jsonPath("$.data.statistics[0].couponStats.used").value(1000))
                .andExpect(jsonPath("$.data.statistics[0].couponStats.unused").value(840))
                .andExpect(jsonPath("$.data.nextCursor").isEmpty())
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }
}