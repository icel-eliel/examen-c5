# CTI Frontend

Dashboard Angular para mostrar llamadas, agentes y extensiones en tiempo real.

## Requisitos

- Node.js instalado.
- Backend ejecutandose en `http://localhost:8080`.

## Ejecutar

```bash
npm start
```

La aplicacion abre en:

```text
http://localhost:4200
```

El proyecto usa `proxy.conf.json` para enviar las llamadas `/api` al backend local y evitar problemas de CORS durante desarrollo.

## Docker

```bash
docker build -t cti-frontend .
docker run --rm -p 4200:8080 -e BACKEND_URL=http://localhost:8080 cti-frontend
```

En Railway, configurar:

```text
BACKEND_URL=https://URL_PUBLICA_DEL_BACKEND
```

## Estructura

- `src/app/types`: contratos TypeScript del backend.
- `src/app/services/cti-api.service.ts`: carga inicial por REST.
- `src/app/services/cti-stream.service.ts`: actualizacion en vivo por SSE.
- `src/app/app.*`: dashboard principal.
