describe('Dashboard', () => {
  beforeEach(() => {
    cy.loginAsAdmin()
    cy.visit('/dashboard')
  })

  it('loads dashboard with key metrics', () => {
    cy.contains(/dashboard|overview|analytics/i).should('exist')
    cy.get('[class*="card"], [class*="widget"], [class*="metric"]').should('have.length.at.least', 1)
  })

  it('displays charts', () => {
    cy.get('canvas').should('have.length.at.least', 1)
  })

  it('shows latest posts', () => {
    cy.contains(/post/i).should('exist')
  })
})
