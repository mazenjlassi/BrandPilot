import users from '../fixtures/users.json'

describe('Login', () => {
  beforeEach(() => {
    cy.visit('/login')
  })

  it('shows login form', () => {
    cy.get('input[type="text"]').should('exist')
    cy.get('input[type="password"]').should('exist')
    cy.contains('button', /login|sign/i).should('exist')
  })

  it('logs in with valid credentials and redirects to dashboard', () => {
    cy.get('input[type="text"]').type(users.admin.username)
    cy.get('input[type="password"]').type(users.admin.password)
    cy.contains('button', /login|sign/i).click()
    cy.url().should('not.include', '/login')
    cy.window().its('localStorage.token').should('exist')
  })

  it('shows error on invalid credentials', () => {
    cy.get('input[type="text"]').type(users.invalid.username)
    cy.get('input[type="password"]').type(users.invalid.password)
    cy.contains('button', /login|sign/i).click()
    cy.contains(/error|invalid|failed/i).should('exist')
  })

  it('loads dashboard without auth (no route guard)', () => {
    cy.visit('/dashboard')
    cy.contains('Dashboard').should('exist')
  })
})
