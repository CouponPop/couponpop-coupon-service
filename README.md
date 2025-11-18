# couponpop-coupon-service

CouponPop 마이크로서비스 아키텍처의 **쿠폰 관리(Coupon)** 도메인을 담당하는 서비스입니다.

---

## 1. 주요 역할

- **쿠폰 관리(CRUD)**: 쿠폰 발급, 조회, 사용, 소멸 관리
- **회원 기반 발급/사용**: 회원(Member) 기준 쿠폰 발급 및 사용 내역 관리
- **알림 연동**: 쿠폰 발급, 사용, 만료 시 `notification-service`를 통한 FCM 알림
- **이벤트 처리**: RabbitMQ를 활용한 쿠폰 이벤트 비동기 처리 및 트랜잭션 이후 이벤트 발행 보장
- **데이터 관리**: Flyway 기반 DB 스키마 관리 및 Master/Slave DB 이중화 (읽기/쓰기 분리)

---

## 2. 기술 스택

- **Language**: Java 17
- **Framework**: Spring Boot 3.x, Spring Data JPA, Spring Security
- **Database**: MySQL (Master/Slave), Flyway, H2(테스트용)
- **Messaging**: RabbitMQ, Spring AMQP
- **CI/CD**: Jenkins, Docker, SonarQube, Jacoco
- **Monitoring**: Micrometer (Prometheus)
- **Common Modules**: `couponpop-core-module`, `couponpop-security-module`
- **Internal Service Client**: Spring Cloud OpenFeign

---

## 3. API 엔드포인트

### Coupon API (`/api/v1/coupons`)

- `POST /issue`: 쿠폰 발급 (회원/매장 기준)
- `POST /use`: 쿠폰 사용
- `GET /{couponId}`: 특정 쿠폰 조회
- `GET /`: 쿠폰 리스트 조회

### Owner API (`/api/v1/owner/coupons/events`)

- `POST /`: 쿠폰 이벤트 생성
- `GET /{eventId}`: 쿠폰 이벤트 상세 조회
- `GET /`: 쿠폰 이벤트 목록 조회
- `GET /statistics`: 쿠폰 통계 정보 조회

---

## 4. 외부 마이크로서비스 및 리소스

- **Downstream Services**:
    - `notification-service`: 쿠폰 발급, 사용, 만료 시 FCM 알림 전송
- **Database**:
    - MySQL (Master/Slave): 쿠폰 및 쿠폰 사용 기록 저장
    - Redis: 발급 제한, 멱등성 체크 등 일부 캐시 용도
- **Messaging Queue**:
    - RabbitMQ: 쿠폰 이벤트(발급, 사용, 만료) 비동기 처리

---

## 5. 환경 변수 및 설정

| 변수명 | 설명 | 예시 |
| :--- | :--- | :--- |
| DB_MASTER_URL | Master DB (쓰기) JDBC URL | `(AWS Parameter Store)` |
| DB_SLAVE_URL | Slave DB (읽기) JDBC URL | `(AWS Parameter Store)` |
| DB_USERNAME | DB 사용자명 | `(AWS Parameter Store)` |
| DB_PASSWORD | DB 비밀번호 | `(AWS Parameter Store)` |
| REDIS_HOST | Redis 호스트 | `(AWS Parameter Store)` |
| REDIS_PORT | Redis 포트 | `(AWS Parameter Store)` |
| RABBITMQ_HOST | RabbitMQ 호스트 | `(AWS Parameter Store)` |
| RABBITMQ_PORT | RabbitMQ 포트 | `(AWS Parameter Store)` |
| GITHUB_ACTOR | GitHub Packages Read용 ID | `(Jenkins Credential)` |
| GITHUB_TOKEN | GitHub Packages Read용 PAT | `(Jenkins Credential)` |
| client.notification-service.url | 알림 서비스 내부 DNS | `http://notification.couponpop.internal:8080` |

---

## 6. 로컬 개발 실행 방법

1. **GitHub PAT 발급**
    - GitHub에서 `read:packages` 스코프를 가진 PAT 발급

2. **인프라 실행**
    - 로컬에 MySQL(Master/Slave), Redis, RabbitMQ 실행
    - `local-docker-infra/docker-compose.coupon.yml` 참고

3. **`.env` 파일 생성**

```dotenv
GITHUB_ACTOR=your-github-username
GITHUB_TOKEN=your-github-pat-token

DB_MASTER_URL=jdbc:mysql://localhost:3307/coupon_db
DB_SLAVE_URL=jdbc:mysql://localhost:3317/coupon_db
DB_USERNAME=root
DB_PASSWORD=1234
REDIS_HOST=localhost
REDIS_PORT=6379
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
```

4.  **(필수) DB 마이그레이션**:
    * `application-local.yml`에 `spring.flyway.enabled=true`가 설정되어 있는지 확인합니다.
    * 애플리케이션을 실행하면 Flyway가 자동으로 `V1`, `V2`, `V3` 스크립트를 실행하여 `members` 테이블을 Master DB에 생성합니다.

5.  **애플리케이션 실행**:
    * IDE의 실행 설정(Run Configuration)에서 Active Profile을 `local`로 설정하여 실행합니다.
    * (또는) 터미널에서 Gradle로 직접 실행합니다 (Port: 8081):
    ```bash
    ./gradlew bootRun --args='--spring.profiles.active=local'
    ```

6.  **(선택) 연동 테스트**:
    * `api-gateway`, `notification-service` (Port 8084)를 함께 실행하면, 로그아웃/회원탈퇴 시 OpenFeign을 통한 FCM 토큰 만료 요청이 정상적으로 동작하는지 확인할 수 있습니다.

## 7. 운영 시 참고 사항

* **[AWS Parameter Store]** 운영(`prod`) 환경의 모든 민감 정보(DB, Redis, JWT Key 등)는 `build.gradle`의 `spring-cloud-aws-starter-parameter-store` 의존성을 통해 AWS Parameter Store에서 주입받습니다.
* **[DB Replication]** `DataSourceConfig`에 따라 Master/Slave DB가 분리되어 있습니다. `@Transactional(readOnly = true)`가 붙은 서비스 로직은 **Slave DB**로, 쓰기 트랜잭션은 **Master DB**로 자동 라우팅됩니다.
* **[Redis 의존성]** 이 서비스는 `couponpop-security-module`을 통해 Redis에 강하게 의존합니다. Redis는 로그아웃/회원탈퇴 시 JWT를 무효화하는 **블랙리스트 저장소**로 사용됩니다. Redis 장애 시 토큰 무효화가 지연될 수 있습니다.
* **[모니터링]** `/actuator/prometheus` 엔드포인트를 통해 서비스의 상세 메트릭(JVM, DB Pool, API Latency 등)이 노출됩니다.
