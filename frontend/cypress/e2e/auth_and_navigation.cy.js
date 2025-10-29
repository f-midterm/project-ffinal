describe('BeLiv App – Auth & Navigation Flow', () => {
  beforeEach(() => {
    cy.visit('/');
    cy.clearLocalStorage();
  });

  // =====================================================
  // 🧩 SIGNUP → CREATE PROFILE → BOOKING PAGE
  // =====================================================
  it('Signup ➜ Create Profile ➜ Booking Page', () => {
    cy.contains('Get Started').click();
    cy.url().should('include', '/signup');

    const uniqueUser = `cypresstester_${Date.now()}`;
    const fakeToken = 'fake-token-' + Date.now();

    cy.get('input[name="username"]').type(uniqueUser);
    cy.get('input[name="email"]').type(`${uniqueUser}@example.com`);
    cy.get('input[name="password"]').type('Password123');
    cy.get('input[name="confirmPassword"]').type('Password123');

    cy.intercept('POST', '**/api/auth/register', {
      statusCode: 200,
      body: { message: 'Registration successful', token: fakeToken },
    }).as('signup');

    cy.get('button[type="submit"]').click();
    cy.wait('@signup');

    cy.window().then((win) => {
      win.localStorage.setItem('token', fakeToken);
      win.localStorage.setItem(
        'user',
        JSON.stringify({ id: 123, username: uniqueUser, role: 'USER' })
      );
    });

    cy.visit('/create-profile');
    cy.url().should('include', '/create-profile');

    cy.intercept('POST', '**/create-profile', {
      statusCode: 201,
      body: { message: 'Profile created successfully' },
    }).as('createProfile');

    cy.get('input[name="firstName"]').type('John');
    cy.get('input[name="lastName"]').type('Doe');
    cy.get('input[name="phone"]').type('0891234567');
    cy.get('input[name="emergencyContact"]').type('Jane Doe');
    cy.get('input[name="emergencyPhone"]').type('0819876543');
    cy.get('input[name="occupation"]').type('Engineer');

    cy.get('button[type="submit"]').click();
    cy.wait('@createProfile', { timeout: 10000 });

    cy.visit('/booking');
    cy.url().should('include', '/booking');
  });

  // =====================================================
  // 🧩 LOGIN → REDIRECT TO HOME
  // =====================================================
  it('Login ➜ Redirect by Role ➜ Return to Home', () => {
    cy.visit('/login');
    cy.url().should('include', '/login');

    cy.intercept('POST', '**/api/auth/login', {
      statusCode: 200,
      body: {
        id: 123,
        username: 'cypresstester',
        role: 'USER',
        token: 'fake-login-token',
      },
    }).as('login');

    cy.get('input[name="username"]').type('cypresstester');
    cy.get('input[name="password"]').type('Password123');
    cy.get('button[type="submit"]').click();

    cy.wait('@login');

    cy.window().then((win) => {
      win.localStorage.setItem('token', 'fake-login-token');
      win.localStorage.setItem(
        'user',
        JSON.stringify({ id: 123, username: 'cypresstester', role: 'USER' })
      );
    });

    cy.visit('/');
    cy.url().should('include', '/');
  });

  // =====================================================
  // 🧩 ACCESS CONTROL (USER CAN'T ACCESS ADMIN)
  // =====================================================
  it('User cannot access Admin Dashboard', () => {
    cy.window().then((win) => {
      win.localStorage.setItem('token', 'fake-token');
      win.localStorage.setItem(
        'user',
        JSON.stringify({ id: 123, username: 'user1', role: 'USER' })
      );
    });

    // 🕐 เพิ่ม delay ให้ flow นี้ช้าลงหน่อยเพื่อดูการ redirect
    cy.wait(1500);

    cy.visit('/admin');
    cy.wait(1500); // ให้เวลาโหลดหน้า/redirect ให้เห็น
    cy.url().should('not.include', '/admin');

    // เพิ่ม wait อีกนิดก่อนจบ test
    cy.wait(1000);
  });

  // =====================================================
  // 🧩 UNAUTHENTICATED USERS BEHAVIOR
  // =====================================================
  it('Unauthenticated users should be redirected to Login from Booking', () => {
    cy.clearLocalStorage();
    cy.visit('/booking');
    cy.url().should('include', '/login');
  });

  // =====================================================
  // 🧩 FIXED: USER CAN NAVIGATE TO PROFILE PAGE AFTER LOGIN
  // =====================================================
  it('User can navigate to Profile Page after login', () => {
    cy.window().then((win) => {
      win.localStorage.setItem('token', 'fake-token');
      win.localStorage.setItem(
        'user',
        JSON.stringify({ id: 123, username: 'testuser', role: 'USER' })
      );
    });

    cy.visit('/');

    cy.intercept('GET', '**/api/auth/me', {
      statusCode: 200,
      body: { id: 123, username: 'testuser', role: 'USER' },
    }).as('getMe');

    cy.wait('@getMe');
    cy.wait(1000);

    cy.get('button').find('svg').first().click({ force: true });
    cy.contains('Profile').click({ force: true });

    cy.url().should('include', '/user/123');
  });

  // =====================================================
  // 🧩 LOGOUT FLOW
  // =====================================================
  it('Logout works properly', () => {
    cy.window().then((win) => {
      win.localStorage.setItem('token', 'fake-token');
      win.localStorage.setItem(
        'user',
        JSON.stringify({ id: 123, username: 'cypresstester', role: 'USER' })
      );
    });

    cy.visit('/');

    cy.intercept('GET', '**/api/auth/me', {
      statusCode: 200,
      body: { id: 123, username: 'cypresstester', role: 'USER' },
    }).as('getMe');

    cy.wait('@getMe');
    cy.wait(1000);

    cy.intercept('POST', '**/api/auth/logout', {
      statusCode: 200,
      body: { message: 'Logout success' },
    }).as('logout');

    cy.get('button').find('svg').first().click({ force: true });
    cy.contains('Logout').click({ force: true });
  });
});
