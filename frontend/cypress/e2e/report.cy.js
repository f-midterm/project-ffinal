describe('Admin Report Page', () => {

  it('can open Report page and display correct UI', () => {

    // ---- LOGIN ----
    cy.visit('/login');

    cy.get('#username').type('apartment_admin');
    cy.get('#password').type('Admin@2024!Secure');

    cy.intercept('POST', '/api/auth/login').as('login');
    cy.intercept('GET', '/api/auth/me').as('me');

    cy.get('button[type="submit"]').click();

    cy.wait('@login');
    cy.wait('@me');


    // ---- OPEN SIDEBAR ----
    cy.get('button')
      .filter((i, el) => el.innerHTML.includes('svg'))
      .first()
      .click({ force: true });

    // ---- CLICK REPORT ----
    cy.contains('Report')
      .scrollIntoView()
      .click({ force: true });

    cy.url().should('include', '/report');
    cy.wait(1000);



    // ---- SUMMARY CARDS ----
    cy.contains('Total Profit').should('exist');
    cy.contains('Total Revenue').should('exist');
    cy.contains('Total Expense').should('exist');


    // ---- FINANCIAL SUMMARY ----
    cy.contains('Financial Summary').should('exist');

    // chart container (รองรับทุก lib)
    cy.get('canvas, svg, .recharts-wrapper, .chart-container')
      .should('exist');


    // ---- DROPDOWNS ----
    cy.contains('All Revenue').should('exist');
    cy.contains('Monthly').should('exist');


    // ---- LOWER SECTIONS ----
    cy.contains('Booking Unit Types').should('exist');
    cy.contains('Booking Summary').should('exist');

  });

});