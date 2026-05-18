# RedNorte – Plataforma de Gestión Médica

Sistema fullstack de gestión de solicitudes médicas construido con microservicios Spring Boot y frontend React + Vite.

---

## Requisitos Previos

* Java 17 o superior
* Node.js 18 o superior y npm
* MySQL 8.0
* Maven 3.8+
* Git

---

## Arquitectura
frontend/                          → React + Vite (puerto 5173)
rednorte-parent/
infrastructure/
eureka-server/                 → Registro de servicios (puerto 8761)
config-server/                 → Configuración centralizada (puerto 8888)
bff-service/                   → Backend for Frontend / Auth JWT (puerto 8080)
api-gateway/                   → Gateway de entrada (puerto 8090)
businessdomain/
user-service/                  → Gestión de usuarios (puerto 8081)
request-service/               → Gestión de solicitudes (puerto 8082)
waiting-list-service/          → Lista de espera (puerto 8083)

---

## Configuración de Bases de Datos

Antes de ejecutar los microservicios, crea las bases de datos en MySQL:

```sql
CREATE DATABASE user_db;
CREATE DATABASE request_db;
CREATE DATABASE waiting_list_db;
```

---

## Orden de Ejecución

### 1. Eureka Server
```bash
cd rednorte-parent/infrastructure/eureka-server
mvn spring-boot:run
```
Disponible en: `http://localhost:8761`

### 2. Config Server
> Requiere conexión a GitHub para clonar las configuraciones del repositorio remoto.
```bash
cd rednorte-parent/infrastructure/config-server
mvn spring-boot:run
```

### 3. Microservicios de Negocio (en paralelo)
```bash
cd rednorte-parent/businessdomain/user-service
mvn spring-boot:run

cd rednorte-parent/businessdomain/request-service
mvn spring-boot:run

cd rednorte-parent/businessdomain/waiting-list-service
mvn spring-boot:run
```

### 4. BFF Service
```bash
cd rednorte-parent/infrastructure/bff-service
mvn spring-boot:run
```

### 5. API Gateway
```bash
cd rednorte-parent/infrastructure/api-gateway
mvn spring-boot:run
```

### 6. Frontend
```bash
cd frontend
npm install
npm run dev
```
Disponible en: `http://localhost:5173`

---

## Roles de Usuario

| Rol               | Descripción                                           |
|-------------------|-------------------------------------------------------|
| `PACIENTE`        | Puede crear solicitudes y gestionar su perfil         |
| `MEDICO`          | Puede gestionar consultas asignadas                   |
| `MEDICO_INACTIVO` | Médico deshabilitado, no recibe nuevas solicitudes    |
| `ADMIN`           | Acceso completo al panel de administración            |
| `Deshabilitado`   | Usuario bloqueado, no puede iniciar sesión            |

---

## Ejecutar Tests (user-service)

```bash
cd rednorte-parent/businessdomain/user-service
mvn test
```