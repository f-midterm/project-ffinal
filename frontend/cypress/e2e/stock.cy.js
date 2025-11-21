describe('Admin Stock - Add New Item', () => {

  it('can open Add Item modal, fill form, and click Create', () => {

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

    // ---- GO TO STOCK PAGE ----
    cy.contains('Stock')
      .scrollIntoView()
      .click({ force: true });

    cy.url().should('include', '/stock');
    cy.wait(800);

    // ---- MOCK API CREATE ----
    cy.intercept('POST', '**/api/maintenance/stocks', {
      statusCode: 201,
      body: { message: 'created' }
    }).as('createStock');

    // ---- OPEN ADD ITEM MODAL ----
    cy.contains('Add Item')
      .should('be.visible')
      .click({ force: true });

    cy.contains('Add New Item').should('be.visible');

    // ---- FILL FORM ----

    cy.get('input[placeholder="e.g., PVC Pipe 1/2 inch"]')
      .type('Test Item 01');

    cy.contains('label', 'Category')
      .parent()
      .find('select')
      .select('PLUMBING', { force: true });

    cy.contains('label', 'Unit')
      .parent()
      .find('input')
      .clear()
      .type('pieces');

    cy.contains('label', 'Quantity')
      .parent()
      .find('input')
      .clear()
      .type('10');

    cy.contains('label', 'Unit Price')
      .parent()
      .find('input')
      .clear()
      .type('150');

    cy.get('textarea[placeholder="Additional details..."]')
      .type('This is a Cypress test item.');

    // ---- CLICK CREATE ----
    cy.contains('Create')
      .should('be.visible')
      .click({ force: true });

    // ---- WAIT FOR MOCK API ----
    cy.wait('@createStock');

    // ---- ASSERT MODAL CLOSED ----
    cy.contains('Add New Item').should('not.exist');

  });

});
