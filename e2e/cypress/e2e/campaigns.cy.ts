describe('Campaigns', () => {
  beforeEach(() => {
    cy.loginAsAdmin()
    cy.visit('/campaigns')
  })

  it('lists campaigns', () => {
    cy.contains(/campaign/i).should('exist')
  })

  it('opens campaign details', () => {
    cy.get('a, button, tr, [class*="card"]')
      .first()
      .click()
    cy.url().should('include', '/campaigns/')
  })

  it('shows posts within a campaign', () => {
    cy.get('a, button, tr, [class*="card"]')
      .first()
      .click()
    cy.contains(/post/i).should('exist')
  })
})
