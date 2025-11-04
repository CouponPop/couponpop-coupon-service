package com.couponpop.couponservice.domain.coupon.dto.response;


import com.couponpop.couponpopcoremodule.dto.store.response.StoreResponse;
import com.couponpop.couponpopcoremodule.enums.StoreCategory;

public record StoreInfoResponse(
        Long id,
        String name,
        StoreCategory storeCategory,
        double latitude,
        double longitude,
        String imageUrl
) {

    public static StoreInfoResponse from(StoreResponse store) {
        return new StoreInfoResponse(
                store.id(),
                store.name(),
                store.storeCategory(),
                store.latitude(),
                store.longitude(),
                store.imageUrl()
        );
    }
}
