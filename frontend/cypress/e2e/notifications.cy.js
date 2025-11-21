describe('Admin Notification Page', () => {

  it('can open Notifications from sidebar bottom section', () => {

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


    // ---- SCROLL ลงหา Notifications ----
    cy.get('aside nav').scrollTo('bottom', { ensureScrollable: false });


    // รองรับทั้งชื่อผิดและชื่อถูก
    cy.contains(/Notfications|Notifications/)
      .scrollIntoView()
      .should('be.visible')
      .click({ force: true });


    // ---- VERIFY PAGE ----
    cy.url().should('include', '/admin/notifications');
  });

});
