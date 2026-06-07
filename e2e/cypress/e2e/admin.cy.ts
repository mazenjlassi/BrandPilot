describe('Admin', () => {
  beforeEach(() => {
    cy.loginAsAdmin()
    cy.visit('/admin/users')
    cy.get('.table-row', { timeout: 15000 }).should('have.length.at.least', 1)
  })

  it('creates a new user and verifies it appears in the table', () => {
    const name = `e2e-create-${Date.now()}`

    cy.contains('Add User').click()
    cy.get('input[placeholder="Enter username"]').type(name)
    cy.get('input[placeholder="Enter email"]').type(`${name}@test.com`)
    cy.get('input[placeholder="Enter password"]').type('test123')
    cy.get('.create-form select').select('Marketing')
    cy.contains('Create User').click()
    cy.contains('.table-row', name).should('contain.text', 'MARKETING')
  })

  it('bans a user and verifies status changes to Banned', () => {
    const name = `e2e-ban-${Date.now()}`

    cy.contains('Add User').click()
    cy.get('input[placeholder="Enter username"]').type(name)
    cy.get('input[placeholder="Enter email"]').type(`${name}@test.com`)
    cy.get('input[placeholder="Enter password"]').type('test123')
    cy.get('.create-form select').select('Marketing')
    cy.contains('Create User').click()
    cy.contains('.table-row', name).should('contain.text', 'MARKETING')

    cy.contains('.table-row', name).within(() => {
      cy.get('.btn-icon.ban').click()
    })
    cy.contains('.table-row', name).should('contain.text', 'Banned')
  })

  it('deletes a user and verifies removal from the table', () => {
    const name = `e2e-delete-${Date.now()}`

    cy.contains('Add User').click()
    cy.get('input[placeholder="Enter username"]').type(name)
    cy.get('input[placeholder="Enter email"]').type(`${name}@test.com`)
    cy.get('input[placeholder="Enter password"]').type('test123')
    cy.get('.create-form select').select('Marketing')
    cy.contains('Create User').click()
    cy.contains('.table-row', name).should('contain.text', 'MARKETING')

    cy.contains('.table-row', name).within(() => {
      cy.get('.btn-icon.delete').click()
    })
    cy.get('.confirm-ok').click()
    cy.contains('.table-row', name).should('not.exist')
  })
})
