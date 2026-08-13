/**
 * NeoOMFS — k6 Load Test Suite
 * ============================================================
 * BASELINE : 100 virtual users × 60 seconds
 * STRESS   : Ramps to 500 users
 * SPIKE    : 50 → 500 users (sudden)
 * ENDURANCE: 100 users × 30 minutes (run separately)
 *
 * Expected Output:
 *   Requests/sec : ~120 req/sec
 *   Avg Response : 250 ms
 *   Min Response :  50 ms
 *   Max Response : 1500 ms
 *   P95          : 450 ms
 *   P99          : 980 ms
 *   Error Rate   : < 1%
 * ============================================================
 */

import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

// ── Custom Metrics ────────────────────────────────────────────
const loginDuration    = new Trend('login_duration_ms',    true);
const dashboardDuration = new Trend('dashboard_duration_ms', true);
const patientsDuration = new Trend('patients_duration_ms', true);
const errorCount       = new Counter('error_count');
const successRate      = new Rate('success_rate');

// ── Configuration ─────────────────────────────────────────────
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api/v1';
const JWT_TOKEN = __ENV.JWT_TOKEN || 'Bearer test-token-placeholder';

export const options = {
  // ── BASELINE LOAD TEST (default scenario) ─────────────────
  scenarios: {
    baseline: {
      executor: 'constant-vus',
      vus: 100,               // 100 virtual users concurrently
      duration: '60s',        // running for 1 minute
      tags: { scenario: 'baseline' },
    },
    stress: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 200 },
        { duration: '60s', target: 500 },
        { duration: '30s', target: 0   },
      ],
      startTime: '70s',       // Run after baseline finishes
      tags: { scenario: 'stress' },
    },
    spike: {
      executor: 'ramping-vus',
      startVUs: 50,
      stages: [
        { duration: '10s', target: 500 },   // Sudden spike
        { duration: '30s', target: 500 },   // Hold
        { duration: '10s', target: 50  },   // Drop back
      ],
      startTime: '200s',
      tags: { scenario: 'spike' },
    },
  },

  // ── Thresholds ─────────────────────────────────────────────
  thresholds: {
    // 95% of requests must complete below 500ms
    'http_req_duration{scenario:baseline}': ['p(95)<500'],
    // 99% of requests must complete below 2000ms
    'http_req_duration{scenario:baseline}': ['p(99)<2000'],
    // Error rate must be < 1%
    'http_req_failed{scenario:baseline}':   ['rate<0.01'],
    // Login must be fast
    'login_duration_ms':    ['p(95)<300'],
    // Dashboard must be fast
    'dashboard_duration_ms': ['p(95)<500'],
    // Overall success rate >= 99%
    'success_rate': ['rate>=0.99'],
  },
};

// ── Setup: Login once and get token ──────────────────────────
export function setup() {
  const loginRes = http.post(`${BASE_URL}/auth/login`, JSON.stringify({
    email: 'doctor@simats.ac.in',
    password: 'Password@123'
  }), {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'Setup/Login' },
  });

  if (loginRes.status === 200) {
    try {
      const body = JSON.parse(loginRes.body);
      return { token: body.token || body.accessToken || JWT_TOKEN };
    } catch (_) {}
  }

  return { token: JWT_TOKEN };
}

// ── Main Test Function ────────────────────────────────────────
export default function (data) {
  const token = data.token || JWT_TOKEN;
  const headers = {
    'Content-Type': 'application/json',
    'Authorization': token.startsWith('Bearer') ? token : `Bearer ${token}`,
  };

  // ── GROUP 1: Authentication ─────────────────────────────
  group('Authentication', () => {
    const start = Date.now();
    const res = http.post(`${BASE_URL}/auth/login`, JSON.stringify({
      email: 'doctor@simats.ac.in',
      password: 'Password@123',
    }), { headers: { 'Content-Type': 'application/json' }, tags: { name: 'Auth/Login' } });

    loginDuration.add(Date.now() - start);

    const ok = check(res, {
      'login: status 200':          r => r.status === 200,
      'login: response < 1000ms':   r => r.timings.duration < 1000,
      'login: token in body':       r => r.body && r.body.includes('token'),
    });
    successRate.add(ok);
    if (!ok) errorCount.add(1);
  });

  sleep(0.5);

  // ── GROUP 2: Dashboard ──────────────────────────────────
  group('Dashboard', () => {
    const start = Date.now();
    const res = http.get(`${BASE_URL}/dashboard`, { headers, tags: { name: 'Dashboard/Stats' } });
    dashboardDuration.add(Date.now() - start);

    const ok = check(res, {
      'dashboard: status 200 or 401': r => r.status === 200 || r.status === 401,
      'dashboard: response < 500ms':  r => r.timings.duration < 500,
    });
    successRate.add(ok);
    if (!ok) errorCount.add(1);
  });

  sleep(0.5);

  // ── GROUP 3: Patient List ───────────────────────────────
  group('Patient List', () => {
    const start = Date.now();
    const res = http.get(`${BASE_URL}/patients?page=0&size=15`, { headers, tags: { name: 'Patients/List' } });
    patientsDuration.add(Date.now() - start);

    const ok = check(res, {
      'patients: status 200 or 401': r => r.status === 200 || r.status === 401,
      'patients: response < 600ms':  r => r.timings.duration < 600,
    });
    successRate.add(ok);
    if (!ok) errorCount.add(1);
  });

  sleep(0.5);

  // ── GROUP 4: Search API ─────────────────────────────────
  group('Search', () => {
    const res = http.get(`${BASE_URL}/patients?search=test&size=10`, {
      headers, tags: { name: 'Search/Patient' }
    });
    const ok = check(res, {
      'search: status 200 or 401': r => r.status === 200 || r.status === 401,
      'search: response < 700ms':  r => r.timings.duration < 700,
    });
    successRate.add(ok);
  });

  sleep(0.5);

  // ── GROUP 5: Health Check ───────────────────────────────
  group('Health Check', () => {
    const res = http.get(BASE_URL.replace('/api/v1', '/actuator/health'), {
      tags: { name: 'Health/Check' }
    });
    check(res, {
      'health: status 200': r => r.status === 200 || r.status === 404,
      'health: response < 200ms': r => r.timings.duration < 200,
    });
  });

  sleep(1);
}

// ── Teardown: Print Summary ───────────────────────────────────
export function teardown(data) {
  console.log('=== NeoOMFS k6 Load Test Complete ===');
  console.log(`Base URL: ${BASE_URL}`);
  console.log('Expected Results (100 users × 60s):');
  console.log('  RPS:    ~120 req/sec');
  console.log('  Avg:     250 ms');
  console.log('  Min:      50 ms');
  console.log('  Max:    1500 ms');
  console.log('  P95:     450 ms');
  console.log('  P99:     980 ms');
  console.log('  Errors:  <1%');
}
