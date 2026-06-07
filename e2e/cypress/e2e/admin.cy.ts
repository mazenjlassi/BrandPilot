describe('Admin', () => {
  beforeEach(() => {
    cy.loginAsAdmin()
    cy.visit('/admin/users')
  })

  it('shows user management for admin', () => {
    cy.contains(/user|admin|manage/i).should('exist')
  })

  it('displays user table', () => {
    cy.contains(/user|admin|manage/i).should('exist')
    cy.get('.users-table').should('exist')
    cy.get('.table-row', { timeout: 15000 }).should('have.length.at.least', 1)
  })
})
