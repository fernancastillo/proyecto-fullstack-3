describe('[E2E] Autenticación — flujo crítico de acceso al sistema', () => {

  beforeEach(() => {
    cy.clearLocalStorage();
  });

  it('Login con credenciales válidas redirige al Dashboard y persiste la sesión', () => {
    cy.loginAsPaciente();

    cy.contains('h1', 'Dashboard').should('be.visible');
    cy.window().its('localStorage.auth_token').should('exist');
    cy.window().its('localStorage.auth_user').should('exist');
  });

  it('Login con credenciales inválidas muestra el error del servidor y no redirige', () => {
    cy.intercept('POST', '/bff/auth/login', {
      statusCode: 401,
      body: { message: 'Correo o contraseña incorrectos.' },
    }).as('loginFallido');

    cy.visit('/login');
    cy.get('#email').type('paciente@rednorte.cl');
    cy.get('#password').type('claveIncorrecta');
    cy.contains('button', 'Iniciar sesión').click();

    cy.wait('@loginFallido');
    cy.contains('Correo o contraseña incorrectos.').should('be.visible');
    cy.url().should('include', '/login');
  });

  it('Acceder a una ruta protegida sin sesión redirige automáticamente a /login', () => {
    cy.visit('/requests');
    cy.url().should('include', '/login');
  });

  it('Cerrar sesión limpia el almacenamiento local y redirige a /login', () => {
    cy.loginAsPaciente();

    cy.contains('button', 'Cerrar sesión').click();

    cy.url().should('include', '/login');
    cy.window().its('localStorage.auth_token').should('not.exist');
  });
});