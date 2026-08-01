/*
 * Purpose: Provides a stress-test template for production capacity validation.
 * Why it exists: Defines controlled overload behavior and regression evidence.
 * Architecture fit: Supports Milestone 11 stress tests and scaling validation.
 */
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '2m', target: 50 },
    { duration: '5m', target: 100 },
    { duration: '2m', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(99)<2000'],
  },
};

export default function () {
  const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
  const response = http.get(`${baseUrl}/api/v1/platform/deployment-status`);
  check(response, {
    'deployment status is reachable': (r) => r.status === 200 || r.status === 401,
  });
  sleep(1);
}
