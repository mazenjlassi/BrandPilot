describe('Campaigns', () => {
  beforeEach(() => {
    cy.loginAsAdmin()
    cy.visit('/campaign-list')
  })

  it('lists campaigns', () => {
    cy.contains(/campaign/i).should('exist')
  })

  it('opens campaign details', () => {
    cy.contains('Open').first().click()
    cy.url().should('include', '/campaigns/')
  })

  it('shows posts within a campaign', () => {
    cy.contains('Open').first().click()
    cy.contains(/post/i).should('exist')
  })
})
