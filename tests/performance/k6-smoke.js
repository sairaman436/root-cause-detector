/*
 * Purpose: Provides a lightweight load-test template for production smoke validation.
 * Why it exists: Establishes repeatable performance evidence before larger load and stress tests.
 * Architecture fit: Supports Milestone 11 load tests, performance benchmarks, and release gates.
 */
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 10,
  duration: '1m',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500'],
  },
};

export default function () {
  const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
  const response = http.get(`${baseUrl}/actuator/health/readiness`);
  check(response, {
    'readiness is successful': (r) => r.status === 200,
  });
  sleep(1);
}
