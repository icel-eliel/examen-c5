# Examen C5 - CTI AVAYA Fullstack

Solucion fullstack para consumir eventos CTI simulados por WebSocket, mantener el estado de llamadas en memoria y mostrarlo en un dashboard Angular en tiempo real.

## Estructura

```text
backend/   Spring Boot + Gradle
frontend/  Angular standalone
```

## Requisitos locales

- Java 17 o superior. Probado con Java 21.
- Node.js.
- No es necesario instalar Gradle globalmente; el backend usa Gradle Wrapper.

## Ejecutar backend

```bash
cd backend
.\gradlew.bat bootRun
```

Backend local:

```text
http://localhost:8080
```

Endpoints principales:

- `GET /health`
- `GET /calls/active`
- `GET /agents`
- `GET /extensions`
- `GET /stream/cti`

La URL del WebSocket CTI se puede cambiar con:

```bash
set CTI_WS_URL=wss://host-del-evaluador/
```

## Ejecutar frontend

```bash
cd frontend
npm start
```

Frontend local:

```text
http://localhost:4200
```

En desarrollo, Angular usa `proxy.conf.json` para redirigir `/api` hacia `http://localhost:8080`.

## Decisiones tecnicas

- Estado en memoria para respetar el alcance de la prueba.
- `ConcurrentHashMap` en backend para manejar eventos concurrentes.
- REST para carga inicial del dashboard.
- Server-Sent Events para actualizacion en vivo hacia Angular.
- Sin base de datos, autenticacion, Kafka ni Kubernetes.
