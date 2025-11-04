import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const pageLoadTime = new Trend('page_load_time');
const totalRequests = new Counter('total_requests');

// Test configuration - 100 users, each refreshes admin page 2 times
export const options = {
  stages: [
    { duration: '30s', target: 100 },  // Ramp up to 100 users in 30 seconds
    { duration: '2m', target: 100 },   // Stay at 100 users for 2 minutes (F5 twice)
    { duration: '30s', target: 0 },    // Ramp down to 0
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'],  // 95% of requests should be below 2s
    http_req_failed: ['rate<0.1'],      // Error rate should be less than 10%
    errors: ['rate<0.1'],
  },
};

// Configuration
const BASE_URL = __ENV.BASE_URL || 'http://localhost';

// Test user credentials for admin access
const ADMIN_USER = {
  username: __ENV.ADMIN_USERNAME || 'admin',
  password: __ENV.ADMIN_PASSWORD || 'admin123',
};

// Step 1: Load login page
function loadLoginPage() {
  const startTime = Date.now();
  const loginPageResponse = http.get(`${BASE_URL}/login`);
  const loadTime = Date.now() - startTime;
  
  pageLoadTime.add(loadTime);
  totalRequests.add(1);
  
  const pageSuccess = check(loginPageResponse, {
    'login page status is 200': (r) => r.status === 200,
    'login page loaded': () => loadTime < 2000,
  });
  
  if (!pageSuccess) {
    errorRate.add(1);
  }
  
  return pageSuccess;
}

// Step 2: Login via API to get JWT token
function login() {
  const loginPayload = JSON.stringify({
    username: ADMIN_USER.username,
    password: ADMIN_USER.password,
  });

  const loginParams = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const loginResponse = http.post(`${BASE_URL}/api/auth/login`, loginPayload, loginParams);
  
  const loginSuccess = check(loginResponse, {
    'login API status is 200': (r) => r.status === 200,
    'login has token': (r) => r.json('token') !== undefined,
  });

  if (!loginSuccess) {
    errorRate.add(1);
    console.error(`Login failed: ${loginResponse.status} - ${loginResponse.body}`);
    return null;
  }

  return loginResponse.json('token');
}

// Step 3: Load admin page after login (simulates F5 refresh)
function loadAdminPage(token) {
  const authHeaders = {
    headers: {
      'Authorization': `Bearer ${token}`,
      'Accept': 'application/json, text/html',
    },
  };

  const startTime = Date.now();

  // Load admin page - this simulates accessing /admin route
  const adminResponse = http.get(`${BASE_URL}/admin`, authHeaders);
  
  const loadTime = Date.now() - startTime;
  pageLoadTime.add(loadTime);
  totalRequests.add(1);

  const pageSuccess = check(adminResponse, {
    'admin page status is 200 or 304': (r) => r.status === 200 || r.status === 304,
    'admin page loaded in < 2s': () => loadTime < 2000,
  });

  if (!pageSuccess) {
    errorRate.add(1);
  }

  // Load admin-related API endpoints (simulating what the page would load)
  const apiEndpoints = [
    '/api/apartments',
    '/api/users',
    '/api/bookings',
  ];

  apiEndpoints.forEach(endpoint => {
    const apiResponse = http.get(`${BASE_URL}${endpoint}`, authHeaders);
    totalRequests.add(1);
    
    check(apiResponse, {
      [`${endpoint} responded`]: (r) => r.status === 200 || r.status === 403,
    });
  });
}

// Main test scenario
export default function () {
  // Step 1: Load login page (like opening the browser to login)
  console.log('Loading login page...');
  const loginPageLoaded = loadLoginPage();
  
  if (!loginPageLoaded) {
    console.error('Failed to load login page');
    sleep(1);
    return;
  }
  
  sleep(1); // User looks at login form

  // Step 2: Submit login (user enters credentials and clicks Login)
  console.log('Submitting login...');
  const token = login();

  if (!token) {
    console.error('Failed to login, skipping user simulation');
    sleep(1);
    return;
  }

  sleep(1); // Wait after successful login, browser redirects to /admin

  // Step 3: Load admin page - FIRST TIME (after login, browser shows /admin)
  console.log('Loading admin page after login - First time');
  loadAdminPage(token);
  
  sleep(2); // User looks at the admin dashboard for 2 seconds

  // Step 4: Refresh admin page - F5 #1 (First refresh)
  console.log('User presses F5 - Refresh #1');
  loadAdminPage(token);
  
  sleep(3); // User looks at refreshed page for 3 seconds

  // Step 5: Refresh admin page - F5 #2 (Second refresh)
  console.log('User presses F5 - Refresh #2');
  loadAdminPage(token);
  
  sleep(2); // Short pause before iteration ends
}

// Setup function - runs once before the test
export function setup() {
  console.log('========================================');
  console.log('k6 Admin Page Load Test');
  console.log('========================================');
  console.log(`Base URL: ${BASE_URL}`);
  console.log(`Target: /admin page`);
  console.log(`Users: 100 concurrent users`);
  console.log(`Refreshes per user: 2 times (F5 twice)`);
  console.log('========================================');
  
  // Verify backend is accessible
  console.log('Checking backend health...');
  const healthCheck = http.get(`${BASE_URL}/api/actuator/health`);
  
  if (healthCheck.status !== 200) {
    console.error('Backend health check failed!');
    console.error(`Status: ${healthCheck.status}`);
    console.error(`Response: ${healthCheck.body}`);
    throw new Error('Backend is not accessible. Please start your application.');
  }
  
  console.log('✓ Backend health check passed');
  console.log('========================================');
  console.log('Starting test in 3 seconds...');
  console.log('========================================');
  
  return { startTime: Date.now() };
}

// Teardown function - runs once after the test
export function teardown(data) {
  const duration = (Date.now() - data.startTime) / 1000;
  
  console.log('');
  console.log('========================================');
  console.log('Test Completed!');
  console.log('========================================');
  console.log(`Total duration: ${duration.toFixed(2)} seconds`);
  console.log('');
  console.log('Summary:');
  console.log('- 100 concurrent users tested');
  console.log('- Each user refreshed /admin page 2 times');
  console.log('- Check Grafana for CPU and memory metrics');
  console.log('========================================');
}
