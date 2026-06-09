import users from '../fixtures/users.json'

Cypress.on('fail', (err) => {
  console.error(err)
  throw err
})

describe('Full E2E Workflow', () => {
  it('completes the 18-step workflow', () => {
    // ============================================================
    // PHASE A: Admin Exploration (Steps 1-5)
    // ============================================================

    // Step 1: Login
    cy.visit('/login')
    cy.get('#username').type(users.admin.username)
    cy.get('#password').type(users.admin.password)
    cy.log('BEFORE CLICK: line 19')
    cy.get('.login-btn').click()
    cy.url().should('include', '/dashboard')
    cy.window().its('localStorage.token').should('exist')

    // Step 2: Dashboard
    cy.contains('Dashboard').should('exist')
    cy.get('.kpi-card').should('have.length.at.least', 1)

    // Step 3: Campaigns (view list)
    cy.visit('/campaign-list')
    cy.contains('All Campaigns').should('exist')
    cy.get('.campaign-card').should('have.length.at.least', 1)

    // Step 4: Posts (view list)
    cy.visit('/posts')
    cy.contains('Posts').should('exist')
    cy.get('.post-card').should('have.length.at.least', 1)

    // Step 5: Admin (view)
    cy.visit('/admin/users')
    cy.contains('User Management').should('exist')
    cy.get('.table-row').should('have.length.at.least', 1)

    // ============================================================
    // PHASE B: Create Campaign + Facebook Post (Step 6)
    // ============================================================

    const campaignName = 'E2E Test Campaign'
    const postTitle = 'Test Facebook Post'
    cy.wrap(campaignName).as('campaignName')
    cy.wrap(postTitle).as('postTitle')

    cy.visit('/campaigns')
    cy.log('BEFORE CLICK: line 53')
    cy.contains('+ New Campaign').click()
    cy.get('#campaign-name').type(campaignName)
    cy.get('#campaign-topic').type('Automated Testing')
    cy.log('BEFORE CLICK: line 57')
    cy.contains('Manual Post').click()
    cy.get('#post-platform').select('FACEBOOK')
    cy.get('#post-title').type(postTitle)
    cy.get('#post-content').type('This is a test post created by Cypress E2E')
    cy.get('#post-hashtags').type('#E2E #Testing')
    cy.get('input[name="approved"]').uncheck()
    cy.log('BEFORE CLICK: line 64')
    cy.contains('Create Post').click()
    cy.get('.post-card', { timeout: 10000 }).should('have.length.at.least', 1)

    // ============================================================
    // PHASE C: Verify Campaign (Steps 7-9)
    // ============================================================

    // Step 7: Verify campaign appears in list
    cy.visit('/campaign-list')
    cy.contains(campaignName).should('exist')

    // Step 8: Open campaign (save campaignId from URL)
    cy.log('BEFORE CLICK: line 77')
    cy.contains('.campaign-card', campaignName).find('button').contains('Open').click()
    cy.url().should('match', /\/campaigns\/\d+/)
    cy.url().then((url) => {
      const match = url.match(/\/campaigns\/(\d+)/)
      if (match) cy.wrap(match[1]).as('campaignId')
    })

    // Step 9: Verify details
    cy.contains(campaignName).should('exist')
    cy.contains('FACEBOOK').should('exist')

    // ============================================================
    // PHASE D: Edit Post (Steps 10-12)
    // ============================================================

    // Step 10: Open post (save postId from URL)
    cy.log('BEFORE CLICK: line 94')
    cy.contains('.post-card', postTitle).click()
    cy.url().should('match', /\/posts\/\d+/)
    cy.url().then((url) => {
      const match = url.match(/\/posts\/(\d+)/)
      if (match) cy.wrap(match[1]).as('postId')
    })

    // Step 11: Update post
    cy.log('BEFORE CLICK: line 103')
    cy.contains('Edit Post').click()
    const newContent = 'Updated via Cypress E2E - ' + Date.now()
    cy.wrap(newContent).as('newContent')
    cy.get('textarea[rows="6"]').clear().type(newContent)
    cy.log('BEFORE CLICK: line 108')
    cy.contains('Save Changes').click()

    // Step 12: Verify update persisted
    cy.contains(newContent).should('exist')

    // ============================================================
    // PHASE E: Admin Ban + Logout (Steps 13-15)
    // ============================================================

    // Step 13: Admin bans marketer user
    cy.visit('/admin/users')
    cy.get('.table-row', { timeout: 15000 }).should('have.length.at.least', 2)
    cy.log('BEFORE CLICK: line 121')
    cy.get('.btn-icon.ban').should('be.visible').click()
    cy.contains('.table-row', 'marketer').should('contain.text', 'Banned')

    // Step 14: Logout
    cy.log('BEFORE CLICK: line 126')
    cy.get('.logout-btn').click()
    cy.url().should('include', '/login')
    cy.window().its('localStorage.token').should('not.exist')

    // Step 15: Banned user login fails
    cy.get('#username').type('marketer')
    cy.get('#password').type('pass123')
    cy.log('BEFORE CLICK: line 134')
    cy.get('.login-btn').click()
    cy.get('.error-message').should('be.visible')

    // ============================================================
    // PHASE F: Approve + Publish (Steps 16-18)
    // ============================================================

    // Step 16: Approve Post
    cy.get('#username').clear().type(users.admin.username)
    cy.get('#password').clear().type(users.admin.password)
    cy.log('BEFORE CLICK: line 145')
    cy.get('.login-btn').click()
    cy.url().should('include', '/dashboard')

    cy.get('@postId').then((postId) => {
      cy.visit(`/posts/${postId}`)
    })
    cy.log('BEFORE CLICK: line 152')
    cy.contains('Edit Post').click()
    cy.get('input[type="checkbox"]').check()
    cy.log('BEFORE CLICK: line 155')
    cy.contains('Save Changes').click()

    // Step 17: Publish Post
    cy.get('@campaignId').then((campaignId) => {
      cy.visit(`/campaigns/${campaignId}`)
    })
    cy.contains('.post-card', postTitle).within(() => {
      cy.log('BEFORE CLICK: line 163')
      cy.get('button[aria-label="Publish post"]').click()
    })

    // Step 18: Verify Status
    cy.contains('.post-card', postTitle).within(() => {
      cy.get('[data-status="published"]', { timeout: 10000 }).should('exist')
    })
  })
})
