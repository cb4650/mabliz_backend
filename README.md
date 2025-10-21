# DZTech Microservices Starter

This starter contains two Spring Boot microservices (`auth-service` and `rayder-service`) and a MySQL database orchestrated with Docker Compose.

## Services

- **auth-service**: Manages user registration and lookup. Exposes REST endpoints under `/api/auth/users`.
- **rayder-service**: Powers the Raydar chauffeur booking experience (profiles, vehicle management, and future booking APIs). Current endpoints live under `/api/profile` and `/api/vehicles`.

Both services use Spring Data JPA with MySQL and follow a `controller -> service -> repository -> model` layering.

## Requirements

- Docker and Docker Compose
- Java 17+
- Maven 3.9+

## Running Locally

```bash
docker compose up --build
```

The services will be available at:

- `http://localhost:8081/api/auth/users`
- `http://localhost:8082/api/profile`
- `http://localhost:8082/api/vehicles`
- `http://localhost/auth` (proxied through Nginx)
- `http://localhost/core` (proxied through Nginx)
- `http://localhost/rcore` (legacy alias for Rayder)
- `https://localhost/auth` and `https://localhost/core` using a generated self-signed certificate located at `nginx/certs/server.crt` (replace for production use)

A MySQL container is started on port `3306` with the root password set to `secret`.

### Development Override (custom host ports)

To run the stack with the Nginx HTTP endpoint exposed on port `5100` instead of `80` (and HTTPS on `5443`), use the additional compose file:

```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```

You can then reach the proxied services at:

- `http://localhost:5100/auth`
- `http://localhost:5100/core`
- `http://localhost:5100/rcore`

## Local Development without Docker

You can run each service independently:

```bash
cd auth-service
mvn spring-boot:run
```

```bash
cd rayder-service
mvn spring-boot:run
```

Make sure you have a running MySQL instance and update the `spring.datasource.*` properties in `src/main/resources/application.properties` if needed.
