import users from '../fixtures/users.json'

describe('Login', () => {
  beforeEach(() => {
    cy.visit('/login')
  })

  it('shows login form', () => {
    cy.get('#username').should('exist')
    cy.get('#password').should('exist')
    cy.get('.login-btn').should('exist')
  })

  it('logs in with valid credentials and stores token', () => {
    cy.get('#username').type(users.admin.username)
    cy.get('#password').type(users.admin.password)
    cy.get('.login-btn').click()
    cy.url().should('include', '/dashboard')
    cy.window().its('localStorage.token').should('exist')
    cy.window().its('localStorage.role').should('eq', 'ADMIN')
  })

  it('shows error on invalid credentials', () => {
    cy.get('#username').type(users.invalid.username)
    cy.get('#password').type(users.invalid.password)
    cy.get('.login-btn').click()
    cy.get('.error-message').should('be.visible')
  })

  it('loads dashboard without auth (no route guard)', () => {
    cy.visit('/dashboard')
    cy.contains('Dashboard').should('exist')
  })
})
