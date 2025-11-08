package com.couponpop.couponservice.domain.couponevent.command.controller;

import com.couponpop.couponservice.domain.couponevent.command.controller.dto.request.CreateCouponEventRequest;
import com.couponpop.couponservice.domain.couponevent.command.controller.dto.response.*;
import com.couponpop.couponservice.domain.couponevent.command.service.CouponEventCommandService;
import com.couponpop.security.dto.AuthMember;
import com.couponpop.security.token.JwtAuthFilter;
import com.couponpop.security.token.JwtAuthenticationToken;
import com.couponpop.security.token.JwtProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CouponEventCommandController.class)
@AutoConfigureMockMvc(addFilters = false)
class CouponEventCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private CouponEventCommandService couponEventCommandService;

    @BeforeEach
    void setUp() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        AuthMember authMember = AuthMember.of(123L, "testUser", "CUSTOMER");
        Authentication authenticationToken = new JwtAuthenticationToken(authMember);
        context.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("Owner 쿠폰 이벤트 생성 - 성공")
    void createCouponEvent_success() throws Exception {
        // given
        LocalDateTime eventStartAt = LocalDateTime.of(2025, 10, 14, 17, 0);
        LocalDateTime eventEndAt = LocalDateTime.of(2025, 10, 15, 12, 0);

        CreateCouponEventRequest request = CreateCouponEventRequest.builder()
                .storeId(1L)
                .name("아이스 아메리카노 1+1")
                .eventStartAt(eventStartAt)
                .eventEndAt(eventEndAt)
                .totalCount(30)
                .build();

        CreateCouponEventResponse response = CreateCouponEventResponse.builder()
                .eventId(1L)
                .name("아이스 아메리카노 1+1")
                .eventStartAt(eventStartAt)
                .eventEndAt(eventEndAt)
                .totalCount(30)
                .createdAt(LocalDateTime.of(2025, 10, 14, 15, 0))
                .build();


        given(couponEventCommandService.createCouponEvent(any(CreateCouponEventRequest.class), anyLong()))
                .willReturn(response);

        // when & then
        mockMvc.perform(
                        post("/api/v1/owner/coupons/events")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("아이스 아메리카노 1+1"))
        ;
    }

}