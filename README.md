# Examen C5 - CTI AVAYA Fullstack

Solucion fullstack para una prueba tecnica de integracion CTI. El backend se conecta a un mock tipo AVAYA por WebSocket, procesa eventos de llamadas en tiempo real y expone el estado actual para que el frontend lo muestre en un dashboard.

La idea fue mantener el proyecto simple y directo: sin base de datos, sin autenticacion y sin infraestructura extra que no fuera necesaria para resolver el ejercicio.

## Estructura

```text
backend/   API Spring Boot con Gradle
frontend/  Dashboard Angular standalone
```

## Requisitos

- Java 17 o superior. Fue probado con Java 21.
- Node.js.
- No hace falta instalar Gradle globalmente; el backend incluye Gradle Wrapper.

## Levantar el backend

```bash
cd backend
.\gradlew.bat bootRun
```

Por defecto queda disponible en:

```text
http://localhost:8080
```

Endpoints principales:

- `GET /health`
- `GET /calls/active`
- `GET /agents`
- `GET /extensions`
- `GET /cti/connection`
- `GET /stream/cti`

La URL del mock CTI se puede cambiar con:

```bash
set CTI_WS_URL=wss://host-del-evaluador/
```

## Levantar el frontend

```bash
cd frontend
npm start
```

La aplicacion queda disponible en:

```text
http://localhost:4200
```

En desarrollo, Angular usa `proxy.conf.json` para mandar las llamadas `/api` al backend local.

## Notas tecnicas

- El backend corre como una aplicacion Spring Boot con Tomcat embebido.
- El frontend se compila como archivos estaticos de Angular; en Docker se sirve con Nginx.
- El estado se mantiene en memoria usando estructuras concurrentes.
- La carga inicial del dashboard se hace por REST.
- Las actualizaciones en vivo se reciben por Server-Sent Events.
- Los eventos duplicados se filtran por tipo, llamada y timestamp.
