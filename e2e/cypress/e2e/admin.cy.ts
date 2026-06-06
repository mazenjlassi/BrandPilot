describe('Admin', () => {
  beforeEach(() => {
    cy.loginAsAdmin()
    cy.visit('/admin')
  })

  it('shows user management for admin', () => {
    cy.contains(/user|admin|manage/i).should('exist')
  })

  it('allows searching users', () => {
    cy.get('input[type="text"], input[type="search"]').first().type('admin')
  })
})
