describe('Posts', () => {
  beforeEach(() => {
    cy.loginAsAdmin()
  })

  it('lists posts', () => {
    cy.visit('/posts')
    cy.get('table, [class*="list"], [class*="grid"]').should('exist')
  })

  it('shows post details', () => {
    cy.visit('/posts')
    cy.contains('View Details').first().click()
    cy.url().should('include', '/posts/')
  })

  it('filters posts by status', () => {
    cy.visit('/posts')
    cy.contains('Drafts').click()
    cy.get('.tabs button.active').should('contain.text', 'Drafts')
  })

  it('navigates to calendar view', () => {
    cy.visit('/calendar')
    cy.get('full-calendar, .fc').should('exist')
  })
})
