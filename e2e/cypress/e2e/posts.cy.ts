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
    cy.get('a, button, [class*="clickable"], tr')
      .first()
      .click()
    cy.url().should('include', '/posts/')
  })

  it('filters posts by status', () => {
    cy.visit('/posts')
    cy.contains(/draft|published|scheduled/i).click()
    cy.url().should('match', /draft|published|scheduled/i)
  })

  it('navigates to calendar view', () => {
    cy.visit('/posts')
    cy.contains(/calendar|schedule/i).click()
    cy.url().should('include', '/calendar')
  })
})
