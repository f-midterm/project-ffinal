// cypress/e2e/logout.cy.js

describe('Admin Logout Flow', () => {

  it('logs out correctly and redirects to login page', () => {

    // ---- LOGIN ----
    cy.visit('/login');

    cy.get('#username').type('apartment_admin');
    cy.get('#password').type('Admin@2024!Secure');

    cy.intercept('POST', '**/api/auth/login').as('login');
    cy.intercept('GET', '**/api/auth/me').as('me');

    cy.get('button[type="submit"]').click();

    cy.wait('@login');
    cy.wait('@me');

    // ---- OPEN SIDEBAR ----
    cy.get('button')
      .filter((i, el) => el.innerHTML.includes('svg'))
      .first()
      .click({ force: true });

    // ---- CLICK Log out ----
    cy.contains('Log out', { matchCase: false })
      .scrollIntoView()
      .should('be.visible')
      .click({ force: true });

    // ---- VERIFY REDIRECT ----
    cy.url().should('include', '/login');

    // ---- VERIFY STORAGE IS CLEARED ----
    cy.window().then(win => {
      expect(win.localStorage.getItem('token')).to.be.null;
    });

    // ---- CHECK LOGIN PAGE UI ----
    cy.contains('Login', { matchCase: false }).should('be.visible');
  });

});
