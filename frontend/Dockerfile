FROM node:24-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:1.27-alpine

WORKDIR /usr/share/nginx/html

COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist/frontend/browser/ /usr/share/nginx/html/

ENV PORT=8080
ENV BACKEND_URL=/api

EXPOSE 8080

CMD sh -c "envsubst '\$PORT' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf && printf 'window.__env = { apiBaseUrl: \"%s\" };' \"$BACKEND_URL\" > /usr/share/nginx/html/env.js && nginx -g 'daemon off;'"
