# CTI Backend

Backend Spring Boot para consumir eventos CTI simulados por WebSocket y exponer el estado actual por REST/SSE.

## Requisitos

- Java 17 o superior. Probado con Java 21.
- No necesitas instalar Gradle: el proyecto incluye Gradle Wrapper.

## Ejecutar

```bash
.\gradlew.bat bootRun
```

Por defecto se conecta a:

```text
wss://precook-overtone-syndrome.ngrok-free.dev/
```

Puedes cambiar la URL con una variable de entorno:

```bash
set CTI_WS_URL=wss://host-del-evaluador/
.\gradlew.bat bootRun
```

## Endpoints

- `GET /health`
- `GET /calls/active`
- `GET /agents`
- `GET /extensions`
- `GET /cti/connection`
- `GET /stream/cti` para actualizaciones SSE del dashboard

## Notas de diseño

- El estado se mantiene en memoria con estructuras concurrentes.
- Los eventos duplicados se ignoran por tipo, llamada y timestamp.
- Si el WebSocket se desconecta, el cliente intenta reconectar con backoff simple.
- No usa base de datos ni autenticación, siguiendo el alcance de la prueba.
