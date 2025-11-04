alter table coupons
    ADD CONSTRAINT fk_coupons_coupon_event FOREIGN KEY (coupon_event_id) REFERENCES coupon_events (id),
    ADD CONSTRAINT uk_coupons_member_coupon_event UNIQUE (member_id, coupon_event_id),
    ADD CONSTRAINT uk_coupons_coupon_code UNIQUE (coupon_code)
;
