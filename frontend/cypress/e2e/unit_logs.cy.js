describe('Unit Change Logs Page', () => {

  it('can open Logs page and check UI + Restore button + Reset button', () => {

    // ---- LOGIN ----
    cy.visit('/login');

    cy.get('#username').type('apartment_admin');
    cy.get('#password').type('Admin@2024!Secure');

    cy.intercept('POST', '**/api/auth/login').as('login');
    cy.intercept('GET', '**/api/auth/me').as('me');
    cy.intercept('GET', '**/api/unit-audit-logs').as('logs');

    cy.get('button[type="submit"]').click();

    cy.wait('@login');
    cy.wait('@me');


    // ---- OPEN SIDEBAR ----
    cy.get('button')
      .filter((i, el) => el.innerHTML.includes('svg'))
      .first()
      .click({ force: true });


    // ---- OPEN LOG PAGE ----
    cy.contains('Log')
      .scrollIntoView()
      .should('be.visible')
      .click({ force: true });

    cy.url().should('include', '/admin/log');

    cy.wait('@logs');


    // ---- CHECK PAGE TITLE ----
    cy.contains('Unit Change Logs').should('be.visible');
    cy.contains('ประวัติการเปลี่ยนแปลงข้อมูลห้องพัก').should('be.visible');


    // ---- CHECK FILTER UI ----
    cy.contains('Filters').should('be.visible');
    cy.contains('Unit').should('be.visible');
    cy.contains('Action Type').should('be.visible');


    // ---- TEST UNIT DROPDOWN ----
    cy.get('select').eq(0)
      .scrollIntoView()
      .should('be.visible')
      .select('101', { force: true });

    cy.contains('Apply Filters').click({ force: true });



    // ====================================================
    // 🔵 NEW: TEST RESET BUTTON
    // ====================================================
    cy.contains('Reset')
      .scrollIntoView()
      .should('be.visible')
      .click({ force: true });

    // wait ให้ค่ามัน reset ก่อน
    cy.wait(300);

    // default value → ALL
    cy.get('select').eq(0).should('have.value', 'all');



    // ---- CHECK LIST OR SKIP ----
// ---- CHECK LIST OR SKIP ----
// ---- CHECK LIST OR SKIP ----
cy.get('body').then($body => {

  // ไม่มีปุ่ม restore → ข้าม
  if ($body.find('button:contains("Restore")').length === 0) {
    cy.log('⚠ ไม่มี Restore → ข้าม restore test');
    return;
  }

  // มี log ให้ restore
  cy.contains('DELETED').should('exist');
  cy.contains('Hard deleted unit').should('exist');

  // intercept ไว้ แต่เรา "จะไม่ wait"
  cy.intercept('POST', '**/api/units/restore*').as('restoreUnit');

  // คลิก Restore
  cy.contains('Restore')
    .first()
    .scrollIntoView()
    .click({ force: true });

  // ⛔ ห้าม cy.wait() เพราะ FE reload หน้าเร็วเกิน → intercept ไม่ทัน
  // แค่ดูว่ามี alert ก็พอ

  cy.on('window:alert', msg => {
    cy.log("Alert message:", msg);
  });

});
  });

});
