describe('Dashboard', () => {
  beforeEach(() => {
    cy.loginAsAdmin()
    cy.visit('/dashboard')
  })

  it('loads dashboard with key metric cards', () => {
    cy.contains('Dashboard').should('exist')
    cy.get('.kpi-card').should('have.length', 4)
    cy.get('.kpi-card .kpi-title').then(($titles) => {
      const texts = $titles.map((_, el) => el.textContent).get()
      expect(texts).to.include.members(['Total Posts', 'Published', 'Campaigns', 'Engagement'])
    })
  })

  it('displays chart canvas', () => {
    cy.get('canvas', { timeout: 10000 }).should('have.length.at.least', 1)
  })

  it('shows latest top posts with seeded data', () => {
    cy.contains('Top Posts').should('exist')
    cy.get('.post-row .post-name', { timeout: 10000 }).should('have.length.at.least', 1)
  })
})
