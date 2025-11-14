import http from 'k6/http';
import {check} from 'k6';

export let options = {
    scenarios: {
        spike: { // 시나리오 이름. 여기선 'spike'라고 이름 붙임
            executor: 'constant-vus', // onstant VUs = 지정한 가상 사용자 수(VUs)가 지속적으로 동시에 요청. 즉, 50명의 VUs가 15초 동안 계속 루프하며 API 호출
            vus: 50,         // 동시에 50명 요청
            duration: '15s', // 15초 유지
        },
    },
};

const BASE_URL = 'http://localhost:8083';
const EVENT_ID = 1;
const params = {headers: {'Content-Type': 'application/json'}};

export default function () {
    // const memberId = Math.floor(Math.random() * 1000) + 1; // 랜덤 회원
    const memberId = (__VU - 1) * 100 + __ITER + 1; // VU별, 반복별로 고유한 memberId
    const payload = JSON.stringify({eventId: EVENT_ID});

    // URL 고정 + tags 사용 → high-cardinality 방지
    const res = http.post(`${BASE_URL}/api-test/v1/coupons/issue?memberId=${memberId}`, payload, {
        ...params,
        tags: {name: 'issue_coupon'},
    });

    check(res, {
        'HTTP 204 정상 응답': (r) => r.status === 204,
        'HTTP 400 중복 방지': (r) => r.status === 400,
        '응답시간 < 200ms': (r) => r.timings.duration < 200,
    });
}