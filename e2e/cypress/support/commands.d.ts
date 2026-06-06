declare namespace Cypress {
  interface Chainable {
    login(username: string, password: string): Chainable<void>
    loginAsAdmin(): Chainable<void>
    loginAsMarketing(): Chainable<void>
    dataCy(value: string): Chainable<JQuery<HTMLElement>>
  }
}
