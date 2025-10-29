// cypress/support/commands.js

// ===== AUTH HELPERS =====
Cypress.Commands.add('setUser', (overrides = {}) => {
  const user = {
    id: 'u1',
    username: 'testuser',
    email: 'testuser@example.com',
    role: 'USER',
    ...overrides,
  };
  window.localStorage.setItem('token', 'fake-token');
  window.localStorage.setItem('user', JSON.stringify(user));
  return user;
});

Cypress.Commands.add('clearAuth', () => {
  window.localStorage.removeItem('token');
  window.localStorage.removeItem('user');
});

// ===== FORM FILLERS =====
Cypress.Commands.add('fillSignupForm', (data = {}) => {
  const {
    username = 'testuser',
    email = 'testuser@example.com',
    password = 'Password!23',
    confirm = 'Password!23',
  } = data;

  cy.get('[data-cy=signup-username]').clear().type(username);
  cy.get('[data-cy=signup-email]').clear().type(email);
  cy.get('[data-cy=signup-password]').clear().type(password);
  cy.get('[data-cy=signup-confirm]').clear().type(confirm);
  cy.get('[data-cy=signup-submit]').click();
});

Cypress.Commands.add('fillLoginForm', (data = {}) => {
  const {
    identity = 'testuser@example.com', // หรือ username ก็ได้
    password = 'Password!23',
  } = data;

  cy.get('[data-cy=login-identity]').clear().type(identity);
  cy.get('[data-cy=login-password]').clear().type(password);
  cy.get('[data-cy=login-submit]').click();
});

Cypress.Commands.add('fillCreateProfileForm', (data = {}) => {
  const {
    firstName = 'Test',
    lastName = 'User',
    phone = '0891234567',
  } = data;

  cy.get('[data-cy=profile-firstName]').clear().type(firstName);
  cy.get('[data-cy=profile-lastName]').clear().type(lastName);
  cy.get('[data-cy=profile-phone]').clear().type(phone);
  cy.get('[data-cy=profile-submit]').click();
});
