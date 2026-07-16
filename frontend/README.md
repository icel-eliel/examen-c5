# CTI Frontend

Dashboard Angular para consultar el backend CTI y mostrar llamadas, agentes y extensiones en tiempo real.

## Requisitos

- Node.js.
- Backend corriendo en `http://localhost:8080`.

## Ejecutar localmente

```bash
npm start
```

La aplicacion queda disponible en:

```text
http://localhost:4200
```

Durante desarrollo, `proxy.conf.json` redirige `/api` al backend local para evitar configurar CORS a mano.

## Docker

```bash
docker build -t cti-frontend .
docker run --rm -p 4200:8080 -e BACKEND_URL=http://localhost:8080 cti-frontend
```

En Docker, Angular se sirve como contenido estatico usando Nginx. La variable `BACKEND_URL` define a que backend se conecta el dashboard.

## Estructura principal

- `src/app/types`: contratos TypeScript del backend.
- `src/app/services/cti-api.service.ts`: carga inicial por REST.
- `src/app/services/cti-stream.service.ts`: actualizacion en vivo por SSE.
- `src/app/app.*`: dashboard principal.
