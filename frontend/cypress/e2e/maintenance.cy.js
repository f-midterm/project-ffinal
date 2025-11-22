describe('Admin Maintenance - Create Schedule Form', () => {

  it('can open Create Schedule modal and fill all fields', () => {

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

    // ---- GO TO MAINTENANCE ----
    cy.contains('Maintenance')
      .scrollIntoView()
      .click({ force: true });

    cy.url().should('include', '/maintenance');
    cy.wait(1000);

    // ---- OPEN MODAL ----
    cy.contains('Create Schedule')
      .should('be.visible')
      .click({ force: true });

    // ---- VERIFY MODAL ----
    cy.contains('Create Schedule')
      .should('be.visible');

    // ---- BASIC INFORMATION ----
    cy.get('input[placeholder="e.g., Monthly Air Conditioning Cleaning"]')
      .type('Air Conditioner Cleaning');

    cy.get('textarea[placeholder="Describe the maintenance task..."]')
      .type('General AC cleaning for all units.');

    // ---- Category dropdown ----
    cy.contains('label', 'Category')
      .next('select')
      .select('OTHER');

    // ---- Priority dropdown ----
    cy.contains('label', 'Priority')
      .next('select')
      .select('MEDIUM');

    // ---- RECURRENCE SETTINGS ----
    cy.contains('label', 'Recurrence Type')
      .next('select')
      .select('ONE_TIME');

    // ---- TARGET UNITS ----
    cy.contains('label', 'Target Type')
      .next('select')
      .select('ALL_UNITS');

    // ---- SCHEDULE DATES ----
    const today = new Date().toISOString().split('T')[0];

    cy.get('input[type="date"]').eq(0).type(today);  // Start Date
    cy.get('input[type="date"]').eq(1).type(today);  // Next Trigger Date

    // ---- BUTTONS ----
    cy.contains('Cancel').should('exist');
    cy.contains('Create Schedule').should('be.visible');

    // ---- CLICK CREATE BUTTON ----
    cy.contains('button', 'Create Schedule')
      .should('be.enabled')
      .click({ force: true });

    // ---- HANDLE SUCCESS ALERT ----
    cy.on('window:alert', (msg) => {
      expect(msg).to.include('Schedule created successfully');
    });

  });

});
