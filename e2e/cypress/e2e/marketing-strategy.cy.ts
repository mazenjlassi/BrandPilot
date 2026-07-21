describe('Marketing Strategy', () => {
  beforeEach(() => {
    cy.loginAsMarketing()
    cy.visit('/dashboard')
  })

  it('shows strategy elements for marketing user', () => {
    cy.contains('Generate AI Strategy').should('exist')
    cy.get('[data-testid="notification-bell"]').should('exist')
  })

  it('generates strategy', () => {
    cy.intercept('POST', '/marketing-strategies/generate-auto', {
      statusCode: 200,
      body: { id: 1, title: 'E2E Strategy', status: 'PENDING', autoGenerate: false }
    }).as('generateStrategy')

    cy.get('[data-testid="generate-strategy-btn"]').click()
    cy.wait('@generateStrategy')

    cy.get('[data-testid="generate-strategy-btn"]').should('be.disabled')
    cy.get('[data-testid="auto-generate-toggle"]').should('exist')
  })

  it('toggles auto-generate', () => {
    cy.intercept('POST', '/marketing-strategies/generate-auto', {
      statusCode: 200,
      body: { id: 1, title: 'E2E Strategy', status: 'PENDING', autoGenerate: false }
    }).as('generateStrategy')

    cy.get('[data-testid="generate-strategy-btn"]').click()
    cy.wait('@generateStrategy')

    cy.intercept('PUT', '/marketing-strategies/1/auto-generate', {
      statusCode: 200, body: { autoGenerate: true }
    }).as('toggleAutoGenerate')

    cy.get('[data-testid="auto-generate-toggle"]').click()
    cy.wait('@toggleAutoGenerate')
  })

  it('shows notification panel', () => {
    cy.intercept('GET', '/notifications', {
      statusCode: 200,
      body: [
        { id: 1, type: 'SUCCESS', message: 'Strategy generated', createdAt: new Date().toISOString(), read: false }
      ]
    }).as('getNotifications')

    cy.intercept('GET', '/notifications/unread-count', {
      statusCode: 200, body: { count: 1 }
    }).as('getUnreadCount')

    cy.get('[data-testid="notification-bell"]').click()
    cy.contains('Strategy generated').should('exist')
  })

  it('does NOT show strategy button for admin user', () => {
    cy.loginAsAdmin()
    cy.visit('/dashboard')
    cy.get('[data-testid="generate-strategy-btn"]').should('not.exist')
  })
})
