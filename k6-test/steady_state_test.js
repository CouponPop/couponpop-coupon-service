import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

// 사용자 정의 메트릭: 응답시간
let reqDuration = new Trend('request_duration');

// --- 시나리오 옵션 ---
export let options = {
    scenarios: {
        ramp_up_steady: {
            executor: 'ramping-arrival-rate',
            startRate: 5,           // 초당 5 RPS로 시작
            timeUnit: '1s',
            preAllocatedVUs: 50,    // 최소 VU 확보
            maxVUs: 300,            // 목표 RPS를 맞출 수 있는 충분한 VU
            stages: [
                { target: 10, duration: '2m' },  // Ramp-up 1
                { target: 20, duration: '2m' },  // Ramp-up 2
                { target: 30, duration: '2m' },  // Ramp-up 3
                { target: 40, duration: '2m' },  // 목표 RPS 도달
                { target: 40, duration: '6m' },  // Steady-State
                { target: 0, duration: '2m' },   // Ramp-down
            ],
            gracefulStop: '30s',      // 종료 시 남은 요청 처리
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.05'],      // 실패율 5% 이하
        request_duration: ['p(90)<500', 'p(95)<1000'], // p90 < 500ms, p95 < 1s
    },
};

// --- 테스트 대상 정보 ---
const BASE_URL = 'http://localhost:8083';
const EVENT_ID = 1;
const params = { headers: { 'Content-Type': 'application/json' } };

export default function () {
    // 랜덤 memberId: 1명당 1 쿠폰 정책 회피
    const memberId = Math.floor(Math.random() * 1_000_000) + 1;
    const payload = JSON.stringify({ eventId: EVENT_ID });

    // URL 고정 + tags 사용 → high-cardinality 방지
    const res = http.post(`${BASE_URL}/api-test/v1/coupons/issue?memberId=${memberId}`, payload, {
        ...params,
        tags: { name: 'issue_coupon' },
    });

    // 응답시간 기록
    reqDuration.add(res.timings.duration);

    // 체크
    check(res, {
        'HTTP 204 정상 응답': (r) => r.status === 204,
        'HTTP 4xx/5xx 없음': (r) => r.status < 400,
        '응답시간 < 1s': (r) => r.timings.duration < 1000,
    });

    // 서버 과부하 방지용 optional sleep (조정 가능)
    // sleep(0.05);
}