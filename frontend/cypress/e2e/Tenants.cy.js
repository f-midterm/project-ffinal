describe('Admin Tenants Page', () => {

  it('opens Tenants page from Dashboard', () => {

    cy.visit('/login');

    // รอเฉพาะ input – ชัวร์สุด
    cy.get('#username', { timeout: 10000 }).should('be.visible');

    cy.get('#username').type('apartment_admin');
    cy.get('#password').type('Admin@2024!Secure');

    cy.intercept('POST', '/api/auth/login').as('login');
    cy.intercept('GET', '/api/auth/me').as('me');

    cy.get('button[type="submit"]').click();

    cy.wait('@login');
    cy.wait('@me');

    // เปิด Sidebar
    cy.get('button')
      .filter((i, el) => el.innerHTML.includes('svg'))
      .first()
      .click({ force: true });

    // ไปหน้า Tenants
    cy.contains('Tenants')
      .scrollIntoView()
      .should('be.visible')
      .click({ force: true });

    cy.url().should('include', '/admin/tenants');
  });

});