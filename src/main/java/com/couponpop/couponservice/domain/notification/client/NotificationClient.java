package com.couponpop.couponservice.domain.notification.client;

import com.couponpop.couponpopcoremodule.dto.fcmtoken.response.FcmTokensResponse;
import com.couponpop.couponservice.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "${client.notification-service.name}", url = "${client.notification-service.url}")
public interface NotificationClient {

    @GetMapping("/internal/v1/fcm-tokens/members/{memberId}")
    ApiResponse<FcmTokensResponse> fetchFcmTokensByMemberId(@PathVariable Long memberId);
}
