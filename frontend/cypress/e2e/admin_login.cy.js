describe('Admin full flow', () => {

  it('Full admin flow including Add Floor and Room actions', () => {

    // ============================================
    // 🔵 LOGIN
    // ============================================
    cy.visit('/login');
    cy.wait(1000); // Wait for page to fully load

    cy.get('#username').should('be.visible').type('apartment_admin');
    cy.get('#password').should('be.visible').type('Admin@2024!Secure');

    cy.intercept('POST', '**/api/auth/login').as('loginRequest');
    cy.intercept('GET', '**/api/auth/me').as('me');
    cy.intercept('GET', '**/api/units').as('units');
    cy.intercept('GET', '**/api/rental-requests').as('rental');
    cy.intercept('GET', '**/api/maintenance-requests').as('maint');
    cy.intercept('GET', '**/api/invoices/waiting-verification').as('invoice');

    cy.get('button[type="submit"]').click();

    cy.wait('@loginRequest', { timeout: 20000 });
    cy.wait('@me', { timeout: 20000 });
    cy.wait('@units', { timeout: 20000 });


    // ============================================
    // 🔵 RENTAL REQUESTS
    // ============================================
    cy.contains('Rental Requests').click({ force: true });
    cy.url().should('include', '/admin/rental-requests');
    cy.contains('No Rental Requests').should('be.visible');

    cy.visit('/admin');
    cy.wait('@units');


    // ============================================
    // 🔵 MAINTENANCE REQUESTS
    // ============================================
    cy.contains('Maintenane Requests').click({ force: true });
    cy.url().should('include', '/admin/maintenance-requests');

    const statusValues = [
      'all','SUBMITTED','WAITING_FOR_REPAIR',
      'APPROVED','IN_PROGRESS','COMPLETED','CANCELLED'
    ];
    statusValues.forEach(v => cy.get('select').eq(0).select(v, { force: true }));

    const priorityValues = ['all','URGENT','HIGH','MEDIUM','LOW'];
    priorityValues.forEach(v => cy.get('select').eq(1).select(v, { force: true }));

    const sortValues = ['newest','oldest','priority'];
    sortValues.forEach(v => cy.get('select').eq(2).select(v, { force: true }));

    cy.contains('Show Calendar').click({ force: true });
    cy.wait(600);
    cy.contains('Maintenance Schedule Calendar').should('be.visible');
    cy.contains('Hide Calendar').click({ force: true });


    // ============================================
    // 🔵 PAYMENT REQUESTS
    // ============================================
    cy.visit('/admin');
    cy.wait('@invoice');

    cy.contains('Payment Requests').scrollIntoView().click({ force: true });
    cy.url().should('include', '/admin/payment-requests');

    ['newest', 'oldest'].forEach(v => {
      cy.get('select').first().select(v, { force: true });
    });


    // ============================================
    // 🔵 QUICK IMPORT CSV
    // ============================================
    cy.visit('/admin');
    cy.wait('@units');

    cy.contains('Quick Import from CSV').click({ force: true });
    cy.contains('Bulk Invoice Import').should('be.visible');

    cy.contains('Import CSV').click({ force: true });

    cy.get('input[type="file"]').selectFile(
      {
        contents: Cypress.Buffer.from('unit,water,electric\n101,10,50'),
        fileName: 'test.csv',
        mimeType: 'text/csv'
      },
      { force: true }
    );

    cy.wait(500);

    cy.contains('Download Template')
      .scrollIntoView()
      .should('be.visible')
      .click({ force: true });

    cy.wait(300);


    // ============================================
    // 🔵 ADD FLOOR → 1 ROOM ONLY
    // ============================================
    cy.get('body').then($body => {
      if ($body.find('h3:contains("Add New Room")').length > 0) {
        cy.contains('Cancel').click({ force: true });
      }
    });

    cy.visit('/admin');
    cy.wait('@units');

    cy.contains('Add Floor').click({ force: true });
    cy.contains('Add New Floor').should('be.visible');

    cy.get('input[placeholder="Enter floor number"]').type('5');

    cy.get('input[placeholder="e.g., 101"]').eq(0).type('501');
    cy.get('input[placeholder="e.g., 25.5"]').eq(0).type('30');
    cy.get('input[placeholder="e.g., 5000"]').eq(0).type('6500');

    cy.intercept('POST', '**/api/units').as('createUnit');

    cy.contains('Create Floor').click({ force: true });

    cy.wait('@createUnit');

    cy.on('window:alert', msg => {
      expect(msg).to.include('created successfully');
    });


    // ============================================
    // 🔵 GO TO UNIT 501 → TEST EDIT + DELETE
    // ============================================
    cy.visit('/admin');
    cy.wait('@units');

    cy.contains('501').scrollIntoView().click({ force: true });

    cy.url().should('include', '/admin/unit/');
    cy.contains('Vacant - No Tenant').should('be.visible');

    // --- Edit Unit ---
    cy.contains('Edit Unit').should('be.visible').click({ force: true });
    cy.contains('Edit Unit').should('be.visible');
    cy.contains('Cancel').click({ force: true });

    // --- Delete Unit ---
cy.intercept('DELETE', '**/api/units/*').as('deleteUnit');

// 2) click ปุ่ม delete
cy.contains('Delete').click({ force: true });

// 3) handle confirm popup
cy.on('window:confirm', (msg) => {
  expect(msg).to.include('delete unit');
  return true; // simulate clicking OK
});

// 4) รอ DELETE request เกิดขึ้น
cy.wait('@deleteUnit');

// 5) handle alert success
cy.on('window:alert', (msg) => {
  expect(msg).to.include('deleted successfully');
});
  });

});
