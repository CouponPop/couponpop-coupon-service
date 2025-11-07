package com.couponpop.couponservice.domain.coupon.service;

import com.couponpop.couponservice.common.config.RedisTestContainersConfig;
import com.couponpop.couponservice.domain.coupon.repository.db.CouponRepository;
import com.couponpop.couponservice.domain.couponevent.entity.CouponEvent;
import com.couponpop.couponservice.domain.couponevent.repository.CouponEventRepository;
import com.couponpop.couponservice.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Map;


@Import(RedisTestContainersConfig.class)
@ActiveProfiles("test-concurrency")
@SpringBootTest(properties = {
        "spring.rabbitmq.host=localhost",
        "spring.rabbitmq.port=5672",
        "spring.rabbitmq.username=guest",
        "spring.rabbitmq.password=guest"
})
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class CouponIssueConcurrencyTestSupport {

    @MockitoBean
    private RabbitTemplate rabbitTemplate; // 실제 RabbitMQ 호출 차단

    @Autowired
    protected CouponEventRepository couponEventRepository;

    @Autowired
    protected CouponRepository couponRepository;

    protected CouponEvent couponEvent;

    protected static final int THREAD_COUNT = 100;
    protected static final int TOTAL_COUPON_COUNT = 100;

    protected static final LocalDateTime issuedTime = LocalDateTime.of(2025, 10, 25, 12, 0);
    protected static final LocalDateTime eventStartAt = issuedTime.minusHours(1);
    protected static final LocalDateTime eventEndAt = issuedTime.plusDays(1);

    @BeforeEach
    void setUp() {
        couponEvent = TestUtils.createEntity(CouponEvent.class, Map.of(
                "name", "이벤트 제목",
                "eventStartAt", eventStartAt,
                "eventEndAt", eventEndAt,
                "totalCount", TOTAL_COUPON_COUNT,
                "storeId", 1L,
                "memberId", 12345L
        ));
        couponEventRepository.save(couponEvent);
    }

    @AfterEach
    void tearDown() {
        couponRepository.deleteAllInBatch();
        couponEventRepository.deleteAllInBatch();
    }
}
