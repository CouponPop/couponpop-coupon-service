CREATE TABLE `coupon_usage_stats`
(
    `id`            bigint                                  NOT NULL AUTO_INCREMENT,
    `member_id`     bigint                                  NOT NULL COMMENT '손님 ID',
    `top_dong`      varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '쿠폰 사용 상위 동 정보',
    `top_hour`      tinyint                                 NOT NULL COMMENT '쿠폰 사용 상위 시간대(0~23)',
    `created_at`    datetime                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일',
    `aggregated_at` date                                    NOT NULL COMMENT '집계 날짜',

    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='손님별 쿠폰 사용 집계 테이블'