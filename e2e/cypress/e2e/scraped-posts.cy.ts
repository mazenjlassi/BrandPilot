describe('Scraped Posts', () => {
  const ts = Date.now()
  const postText = `E2E scraped post ${ts}`

  beforeEach(() => {
    cy.loginAsAdmin()
    cy.visit('/scraped-posts')
  })

  it('displays company pills and scraped posts with platform badges', () => {
    cy.get('.company-pill', { timeout: 10000 }).should('have.length.at.least', 1)
    cy.contains('.company-pill', 'MetaTry').click()
    cy.get('.platform-badge', { timeout: 10000 }).should('exist')
    cy.get('table tbody tr').should('have.length.at.least', 1)
  })

  it('adds a new scraped post and verifies it appears in the table', () => {
    cy.get('.posts-section', { timeout: 10000 }).should('be.visible')
    cy.get('.section-header', { timeout: 10000 }).should('be.visible')
    cy.contains('+ Add Post').should('be.visible').click()
    cy.get('.modal-overlay', { timeout: 5000 }).should('be.visible')
    cy.get('select[name="company"]', { timeout: 5000 }).should('exist')
    cy.get('select[name="company"]').select('MetaTry')
    cy.get('select[name="platform"]').select('FACEBOOK')
    cy.get('textarea[name="postText"]').type(postText)
    cy.contains('Create Post').click()
    cy.contains('table tbody tr', postText).should('exist')
    cy.contains('table tbody tr', postText).contains('.platform-badge', 'FACEBOOK').should('exist')
  })
})
