describe('Campaigns', () => {
  const ts = Date.now()
  const campaignName = `E2E Campaign ${ts}`
  const postTitle = `E2E Post ${ts}`

  beforeEach(() => {
    cy.loginAsAdmin()
  })

  it('creates a campaign and a Facebook post', () => {
    cy.visit('/campaigns')
    cy.contains('+ New Campaign').click()
    cy.get('#campaign-name').type(campaignName)
    cy.get('#campaign-topic').type('E2E Testing')
    cy.contains('Manual Post').click()
    cy.get('#post-platform').select('FACEBOOK')
    cy.get('#post-title').type(postTitle)
    cy.get('#post-content').type('Created by Cypress E2E campaign test')
    cy.get('#post-hashtags').type('#E2E #Test')
    cy.get('input[name="approved"]').uncheck()
    cy.contains('Create Post').click()
    cy.get('.post-card', { timeout: 10000 }).should('have.length.at.least', 1)
    cy.contains('.post-card', postTitle).should('exist')
  })

  it('verifies created campaign appears in campaign list and opens details', () => {
    cy.visit('/campaigns')
    cy.contains('+ New Campaign').click()
    cy.get('#campaign-name').type(campaignName)
    cy.get('#campaign-topic').type('E2E Testing')
    cy.contains('Manual Post').click()
    cy.get('#post-platform').select('FACEBOOK')
    cy.get('#post-title').type(postTitle)
    cy.get('#post-content').type('Created by Cypress E2E campaign test')
    cy.get('#post-hashtags').type('#E2E #Test')
    cy.get('input[name="approved"]').uncheck()
    cy.contains('Create Post').click()
    cy.get('.post-card', { timeout: 10000 }).should('have.length.at.least', 1)

    cy.visit('/campaign-list')
    cy.contains(campaignName).should('exist')
    cy.contains('.campaign-card', campaignName).contains('Open').click()
    cy.url().should('match', /\/campaigns\/\d+/)
    cy.contains(campaignName).should('exist')
  })

  it('opens a seeded campaign and shows its posts', () => {
    cy.visit('/campaign-list')
    cy.get('.campaign-card', { timeout: 10000 }).should('have.length.at.least', 1)
    cy.contains('Summer Launch 2025').should('exist')
    cy.contains('.campaign-card', 'Summer Launch 2025').contains('Open').click()
    cy.url().should('match', /\/campaigns\/\d+/)
    cy.contains('Summer Launch 2025').should('exist')
    cy.get('.post-card', { timeout: 10000 }).should('have.length.at.least', 1)
  })
})
