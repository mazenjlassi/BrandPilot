Cypress.Commands.add('login', (username: string, password: string) => {
  cy.session([username, password], () => {
    cy.request({
      method: 'POST',
      url: `${Cypress.env('apiUrl')}/auth/login`,
      body: { username, password },
    }).then((res) => {
      window.localStorage.setItem('token', res.body.token)
      window.localStorage.setItem('role', res.body.role)
      window.localStorage.setItem('name', res.body.name)
    })
  })
})

Cypress.Commands.add('loginAsAdmin', () => {
  cy.login('admin', 'admin123')
})

Cypress.Commands.add('loginAsMarketing', () => {
  cy.login('marketing', 'marketing123')
})

Cypress.Commands.add('dataCy', (value: string) => {
  return cy.get(`[data-cy="${value}"]`)
})
