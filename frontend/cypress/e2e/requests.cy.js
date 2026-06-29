describe('[E2E] Gestión de solicitudes médicas — proceso de negocio crítico', () => {

  beforeEach(() => {
    cy.clearLocalStorage();
    cy.loginAsPaciente();
  });

  it('Crear una nueva solicitud médica completa el flujo y la muestra en la tabla', () => {
    let requestsInBackend = [];

    cy.intercept('GET', '/bff/requests/user/1', (req) => {
      req.reply({ statusCode: 200, body: requestsInBackend });
    }).as('getRequests');

    cy.intercept('GET', '/bff/users/medicos', {
      statusCode: 200,
      body: [
        { id: 5, name: 'Ana', lastname: 'López', especialidad: 'Cardiología' },
      ],
    }).as('getMedicos');

    cy.intercept('POST', '/bff/requests', (req) => {
      const created = { id: 99, ...req.body, fechaSolicitud: new Date().toISOString() };
      requestsInBackend = [...requestsInBackend, created];
      req.reply({ statusCode: 201, body: created });
    }).as('createRequest');

    cy.visit('/requests');
    cy.wait(['@getRequests', '@getMedicos']);
    cy.contains('No tienes solicitudes registradas.').should('be.visible');

    cy.contains('button', 'Nueva solicitud').click();
    cy.get('select[name="especialidad"]').select('Cardiología');
    cy.get('select[name="medicoId"]').select('5');
    cy.get('textarea[name="descripcion"]').type('Dolor en el pecho intermitente.');
    cy.contains('button', 'Crear solicitud').click();

    cy.wait('@createRequest');
    cy.wait('@getRequests');

    cy.contains('Solicitud creada correctamente.').should('be.visible');
    cy.contains('td', 'Cardiología').should('be.visible');
    cy.contains('td', 'PENDIENTE').should('be.visible');
  });

  it('Intentar crear una solicitud sin completar los campos obligatorios muestra errores de validación', () => {
    cy.intercept('GET', '/bff/requests/user/1', { statusCode: 200, body: [] }).as('getRequests');
    cy.intercept('GET', '/bff/users/medicos', { statusCode: 200, body: [] }).as('getMedicos');

    cy.visit('/requests');
    cy.wait(['@getRequests', '@getMedicos']);

    cy.contains('button', 'Nueva solicitud').click();
    cy.contains('button', 'Crear solicitud').click();

    cy.contains('Selecciona una especialidad.').should('be.visible');
    cy.contains('La descripción es obligatoria.').should('be.visible');
  });
});