describe('Scraped Posts', () => {
  beforeEach(() => {
    cy.loginAsAdmin()
    cy.visit('/scraped-posts')
  })

  it('displays list of scraped posts', () => {
    cy.contains(/scraped/i).should('exist')
  })

  it('shows platform indicators', () => {
    cy.contains(/facebook|instagram|linkedin|twitter|x/i).should('exist')
  })
})
