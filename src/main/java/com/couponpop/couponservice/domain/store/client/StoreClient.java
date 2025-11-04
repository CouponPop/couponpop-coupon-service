package com.couponpop.couponservice.domain.store.client;

import com.couponpop.couponpopcoremodule.dto.couponevent.response.StoreOwnershipResponse;
import com.couponpop.couponpopcoremodule.dto.store.response.StoreResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "${client.store-service.name}", url = "${client.store-service.url}")
public interface StoreClient {

    @GetMapping("/v1/stores/ownership")
    StoreOwnershipResponse checkOwnership(@RequestParam Long storeId, @RequestParam Long memberId);

    @GetMapping("/v1/stores/owner/{memberId}")
    List<StoreResponse> findStoresByOwner(@PathVariable Long memberId, @RequestParam Long lastStoreId, @RequestParam int pageSize);

    @GetMapping("/v1/stores/{storeId}")
    StoreResponse findByIdOrElseThrow(@PathVariable Long storeId);

    @GetMapping("/v1/stores")
    List<StoreResponse> findAllByIds(@RequestBody List<Long> storeIds);
}
