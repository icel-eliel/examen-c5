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

## Despliegue en Railway

Usar un solo repositorio con dos servicios.

### Servicio backend

Crear un servicio desde este repo con:

```text
Root Directory: /backend
```

Variables recomendadas:

```text
CTI_WS_URL=wss://precook-overtone-syndrome.ngrok-free.dev/
CTI_ALLOWED_ORIGINS=https://URL_PUBLICA_DEL_FRONTEND
```

Railway inyecta `PORT`; el backend lo lee desde `application.yml`.

### Servicio frontend

Crear otro servicio desde el mismo repo con:

```text
Root Directory: /frontend
```

Variables recomendadas:

```text
BACKEND_URL=https://URL_PUBLICA_DEL_BACKEND
```

El contenedor del frontend genera `env.js` al iniciar para apuntar al backend desplegado.

### Orden sugerido

1. Desplegar backend.
2. Generar dominio publico del backend.
3. Desplegar frontend con `BACKEND_URL`.
4. Generar dominio publico del frontend.
5. Volver al backend y configurar `CTI_ALLOWED_ORIGINS` con la URL publica del frontend.
6. Redeploy del backend.

## Decisiones tecnicas

- Estado en memoria para respetar el alcance de la prueba.
- `ConcurrentHashMap` en backend para manejar eventos concurrentes.
- REST para carga inicial del dashboard.
- Server-Sent Events para actualizacion en vivo hacia Angular.
- Sin base de datos, autenticacion, Kafka ni Kubernetes.
