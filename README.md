# MobyApp API Gateway

El `api-gateway` es el punto de entrada único para todos los clientes que interactúan con el ecosistema de microservicios de MobyApp. Basado en Spring Cloud Gateway, este servicio se encarga de enrutar las solicitudes entrantes al microservicio correspondiente, además de gestionar la autenticación, la seguridad y la resiliencia de la comunicación.

## Tecnologías Principales

- **Java 17**: Versión del lenguaje de programación.
- **Spring Boot 3**: Framework principal para el desarrollo de la aplicación.
- **Spring Cloud Gateway**: Proporciona un enrutamiento dinámico y filtros para los microservicios.
- **Spring Cloud Netflix Eureka**: Utilizado para el registro y descubrimiento de servicios en la arquitectura.
- **Spring Data Redis**: Gestiona el almacenamiento de sesiones de usuario de forma reactiva.
- **Resilience4j**: Implementa patrones de resiliencia como Circuit Breaker para mejorar la tolerancia a fallos.
- **WebFlux**: Framework reactivo para construir aplicaciones eficientes y escalables.
- **Maven**: Herramienta para la gestión de dependencias y construcción del proyecto.

## Características

- **Enrutamiento Dinámico**: Redirige las peticiones a los microservicios internos basándose en prefijos de ruta (path-based routing).
- **Autenticación Centralizada**: Utiliza un filtro personalizado (`CustomAuth`) para validar los tokens de autenticación (OAuth2 con Google) antes de permitir el acceso a rutas protegidas.
- **Gestión de Sesiones**: Almacena y gestiona las sesiones de los usuarios en Redis para mantener un estado de autenticación consistente.
- **CORS (Cross-Origin Resource Sharing)**: Configuración centralizada para permitir peticiones desde dominios autorizados, facilitando la integración con aplicaciones frontend.
- **Resiliencia y Tolerancia a Fallos**: Integra Resilience4j para implementar patrones como Circuit Breaker, evitando que fallos en un microservicio afecten a todo el sistema.
- **Service Discovery**: Se registra en un servidor Eureka, permitiendo el descubrimiento dinámico de las instancias de los microservicios.

## Configuración

La aplicación se configura mediante variables de entorno. Es necesario proporcionar las siguientes variables para su correcto funcionamiento:

| Variable                  | Descripción                                               | Ejemplo                                     |
| ------------------------- | --------------------------------------------------------- | ------------------------------------------- |
| `GATEWAY_PORT`            | Puerto en el que se ejecutará el servicio.                | `8080`                                      |
| `URL_EUREKA`              | URL del servidor Eureka para el registro de servicios.    | `http://localhost:8761/eureka`              |
| `REDIS_URL`               | URL de conexión a la instancia de Redis.                  | `redis://user:password@host:port`           |
| `COOKIE_SECURE`           | `true` para HTTPS (producción), `false` para HTTP (desarrollo). | `false`                               |
| `COOKIE_SAME_SITE`        | Política Same-Site para cookies (`None`, `Lax`, `Strict`).| `Lax`                                       |
| `FALLBACK_REDIRECT_URL`   | URL a la que se redirige en caso de error de autenticación. | `http://localhost:9000`                   |
| `SERVICE_HOSTNAME`        | Hostname utilizado para registrarse en Eureka.            | `localhost`                                 |

Para un entorno de desarrollo local, puede crear un archivo `dev.properties` en `src/main/resources` para definir estas variables, basándose en el `dev.properties.example`.

## Cómo Ejecutar

1.  **Clonar el repositorio**:
    ```bash
    git clone <URL_DEL_REPOSITORIO>
    cd mobyapp-ms-gateway
    ```

2.  **Configurar las variables de entorno**:
    Cree el archivo `src/main/resources/dev.properties` y añada las configuraciones necesarias.

3.  **Ejecutar la aplicación**:
    Utilice el wrapper de Maven para compilar y ejecutar el proyecto:
    ```bash
    ./mvnw spring-boot:run
    ```

El API Gateway se iniciará y estará disponible en el puerto especificado por `GATEWAY_PORT`.

## Rutas de API

El gateway gestiona el enrutamiento hacia los siguientes microservicios:

| ID del Servicio      | Prefijo de la Ruta     | Microservicio de Destino |
| -------------------- | ---------------------- | ------------------------ |
| `login-service`      | `/api/auth/**`         | `MS-LOGIN`               |
| `calendar-service`   | `/api/calendar/**`     | `ms-calendar`            |
| `user`               | `/user/**`             | `user`                   |
| `project`            | `/project/**`          | `project`                |
| `georef`             | `/locations/**`        | `georef`                 |
| `airtable-service`   | `/api/airtable/**`     | `localhost:3000`         |
| `news-service`       | `/contentful/**`       | `ms-news`                |
| `websocket`          | `/ws/**`               | `websocket`              |
| `tasks-service`      | `/api/tasks/**`        | `ms-tasks`               |

Las rutas excluidas del filtro de autenticación (`CustomAuth`) incluyen puntos de acceso para el login, logout y servicios públicos como `georef` y `news-service`.