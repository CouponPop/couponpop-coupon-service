import http from 'k6/http';
import { check, sleep } from 'k6';

// === 옵션 ===
export let options = {
    vus: 1,          // 동시에 1명 (RPS=1 정도)
    duration: '1m'   // 테스트 지속 시간
};

const BASE_URL = 'http://localhost:8083';
const EVENT_ID = 1;
const MEMBER_IDS = [1, 2, 3, 4, 5];

// === 헤더 ===
const params = {
    headers: { 'Content-Type': 'application/json' }
};

// === 테스트 함수 ===
export default function () {
    const memberId = MEMBER_IDS[Math.floor(Math.random() * MEMBER_IDS.length)];
    const payload = JSON.stringify({ eventId: EVENT_ID });

    const res = http.post(`${BASE_URL}/api-test/v1/coupons/issue?memberId=${memberId}`, payload, params);

    check(res, {
        'HTTP 204 정상 응답': (r) => r.status === 204,
        'HTTP 400 (중복 방지) 발생 가능': (r) => r.status === 400,
        '응답 시간 < 200ms': (r) => r.timings.duration < 200,
    });

    sleep(1); // 1초 간격
}