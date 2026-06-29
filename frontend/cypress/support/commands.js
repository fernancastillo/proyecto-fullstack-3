Cypress.Commands.add('loginAsPaciente', () => {
  cy.intercept('POST', '/bff/auth/login', {
    statusCode: 200,
    body: {
      token: 'fake-jwt-token-test',
      id: 1,
      email: 'paciente@rednorte.cl',
      role: 'PACIENTE',
      name: 'Juan',
      lastname: 'Pérez',
      especialidad: null,
    },
  }).as('loginRequest');

  cy.visit('/login');
  cy.get('#email').type('paciente@rednorte.cl');
  cy.get('#password').type('Clave123!');
  cy.contains('button', 'Iniciar sesión').click();
  cy.wait('@loginRequest');
  cy.url().should('include', '/dashboard');
});