package com.couponpop.couponservice.domain.coupon.dto.response;

import com.couponpop.couponservice.domain.coupon.dto.request.MemberIssuedCouponCursor;

import java.util.ArrayList;
import java.util.List;

public record IssuedCouponListResponse(
        List<CouponDetailResponse> coupons,
        MemberIssuedCouponCursor nextCursor,
        int size,
        boolean hasNext
) {
    public static IssuedCouponListResponse of(List<CouponDetailResponse> originalCoupons, int pageSize) {
        if (originalCoupons == null || originalCoupons.isEmpty()) {
            return new IssuedCouponListResponse(List.of(), null, 0, false);
        }

        boolean hasNext = originalCoupons.size() > pageSize;
        List<CouponDetailResponse> coupons = trimToPageSize(originalCoupons, pageSize);
        MemberIssuedCouponCursor nextCursor = hasNext ? buildNextCursor(coupons) : null;

        return new IssuedCouponListResponse(List.copyOf(coupons), nextCursor, coupons.size(), hasNext);
    }

    private static List<CouponDetailResponse> trimToPageSize(List<CouponDetailResponse> coupons, int pageSize) {
        if (coupons.size() <= pageSize) {
            return coupons;
        }
        // 원본 리스트 변형 방지를 위해 복사 후 제거
        List<CouponDetailResponse> trimmed = new ArrayList<>(coupons);
        trimmed.remove(trimmed.size() - 1);
        return trimmed;
    }

    private static MemberIssuedCouponCursor buildNextCursor(List<CouponDetailResponse> coupons) {
        if (coupons.isEmpty()) {
            return null;
        }

        CouponDetailResponse lastCoupon = coupons.get(coupons.size() - 1);
        return new MemberIssuedCouponCursor(
                lastCoupon.event().period().endAt(),
                lastCoupon.id()
        );
    }

}
