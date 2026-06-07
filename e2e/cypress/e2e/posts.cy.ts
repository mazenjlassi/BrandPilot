describe('Posts', () => {
  beforeEach(() => {
    cy.loginAsAdmin()
  })

  it('lists seeded posts with content visible', () => {
    cy.visit('/posts')
    cy.get('.post-card', { timeout: 10000 }).should('have.length.at.least', 1)

    cy.contains('Published').click()
    cy.contains('new product launch this summer', { timeout: 10000 }).should('be.visible')

    cy.contains('Drafts').click()
    cy.contains('sneak peek', { timeout: 10000 }).should('be.visible')
  })

  it('shows post details when clicking View Details', () => {
    cy.visit('/posts')
    cy.get('.post-card', { timeout: 10000 }).should('have.length.at.least', 1)
    cy.contains('View Details').first().click()
    cy.url().should('match', /\/posts\/\d+/)
    cy.get('h1').should('exist')
  })

  it('filters posts by status tabs', () => {
    cy.visit('/posts')
    cy.get('.post-card', { timeout: 10000 }).should('have.length.at.least', 1)

    cy.contains('Drafts').click()
    cy.get('button.active').should('contain.text', 'Drafts')

    cy.contains('Published').click()
    cy.get('button.active').should('contain.text', 'Published')
  })

  it('navigates to calendar view', () => {
    cy.visit('/calendar')
    cy.get('full-calendar, .fc').should('exist')
  })
})
