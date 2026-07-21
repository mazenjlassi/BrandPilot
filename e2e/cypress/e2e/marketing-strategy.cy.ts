describe('Marketing Strategy', () => {
  beforeEach(() => {
    cy.loginAsMarketing()
    cy.intercept('GET', '/posts/top?limit=5', { body: [] }).as('getTopPosts')
    cy.intercept('GET', '/posts/stats', { body: {} }).as('getStats')
    cy.intercept('GET', '/campaigns', { body: [] }).as('getCampaigns')
    cy.intercept('GET', '/posts/timing-analysis', { body: {} }).as('getTiming')
    cy.intercept('GET', '/posts/weekly-comparison', { body: {} }).as('getWeekly')
    cy.intercept('GET', '/posts/upcoming-scheduled?limit=3', { body: [] }).as('getUpcoming')
    cy.intercept('GET', '/marketing-strategies', { body: [] }).as('getStrategies')
    cy.intercept('GET', '/notifications', { body: [] }).as('getNotifications')
    cy.intercept('GET', '/notifications/unread-count', { body: { count: 0 } }).as('getUnreadCount')
    cy.intercept('GET', '/posts/latestPublished?limit=20', { body: [] }).as('getLatestPosts')
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

    cy.intercept('GET', '/marketing-strategies', {
      body: [{ id: 1, title: 'E2E Strategy', status: 'PENDING', autoGenerate: false }]
    })

    cy.get('[data-testid="generate-strategy-btn"]').click()
    cy.wait('@generateStrategy')

    cy.get('[data-testid="generate-strategy-btn"]').should('be.disabled')
    cy.get('[data-testid="auto-generate-toggle"]').should('exist')
  })

  it('toggles auto-generate', () => {
    cy.intercept('GET', '/marketing-strategies', {
      body: [{ id: 1, title: 'E2E Strategy', status: 'PENDING', autoGenerate: false }]
    })

    cy.visit('/dashboard')

    cy.intercept('PUT', '/marketing-strategies/1/auto-generate', {
      statusCode: 200, body: { autoGenerate: true }
    }).as('toggleAutoGenerate')

    cy.get('[data-testid="auto-generate-toggle"]').should('exist').click()
    cy.wait('@toggleAutoGenerate')
  })

  it('shows notification panel', () => {
    cy.intercept('GET', '/notifications', {
      body: [
        { id: 1, type: 'SUCCESS', message: 'Strategy generated', createdAt: new Date().toISOString(), read: false }
      ]
    })
    cy.intercept('GET', '/notifications/unread-count', { body: { count: 1 } })

    cy.visit('/dashboard')
    cy.get('[data-testid="notification-bell"]').click()
    cy.contains('Strategy generated').should('exist')
  })

  it('does NOT show strategy button for admin user', () => {
    cy.loginAsAdmin()
    cy.visit('/dashboard')
    cy.get('[data-testid="generate-strategy-btn"]').should('not.exist')
  })
})
