# CTI Backend

Backend Spring Boot para conectarse al mock CTI por WebSocket, procesar eventos de llamadas y exponer el estado actual por REST y SSE.

## Requisitos

- Java 17 o superior. Probado con Java 21.
- No es necesario instalar Gradle; el proyecto incluye Gradle Wrapper.

## Ejecutar localmente

```bash
.\gradlew.bat bootRun
```

Por defecto se conecta a:

```text
wss://precook-overtone-syndrome.ngrok-free.dev/
```

Para usar otra URL:

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
- `GET /stream/cti`

## Docker

```bash
docker build -t cti-backend .
docker run --rm -p 8080:8080 cti-backend
```

Variables utiles:

```text
PORT=8080
CTI_WS_URL=wss://precook-overtone-syndrome.ngrok-free.dev/
CTI_ALLOWED_ORIGINS=http://localhost:4200
```

## Notas

- Corre como JAR de Spring Boot sobre Tomcat embebido.
- Mantiene el estado en memoria, sin base de datos.
- Usa estructuras concurrentes para soportar eventos simultaneos.
- Intenta reconectar automaticamente si el WebSocket se desconecta.
- Ignora eventos duplicados usando tipo de evento, llamada y timestamp.
