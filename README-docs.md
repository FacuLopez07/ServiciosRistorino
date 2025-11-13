# Documentación de Ristorino API

Este proyecto es una API Spring Boot que expone información de promociones y detalles de restaurantes y permite registrar clics y notificar clics a una API externa.

## Endpoints principales

- GET `/api/promotions/{nroRestaurante}?soloVigentes&nroSucursal` — Devuelve `RestaurantResponse` con la lista de contenidos.
- POST `/api/promotions/{nroRestaurante}/{nroIdioma}/{nroContenido}/click` — Registra un click anónimo.
- GET `/api/restaurants/{nroRestaurante}` — Devuelve el detalle anidado del restaurante.
- POST `/api/manual/notify-clicks` — Dispara la notificación manual de clics pendientes.

## Notificación manual de clics
- Servicio: `ClickNotificationService`
- URL externa: `http://localhost:8085/api/v1/clicks`
- Autenticación: JWT HS256 con secreto compartido. Payload `{ "registrador": "ristorino", "iat": <epoch>, "exp": <epoch> }`

## CORS
- Configurado mediante `WebConfig` para permitir origen `http://localhost:4200` (Angular).

## Base de datos
- Los repositorios ejecutan procedimientos almacenados en SQL Server y parsean el JSON resultante (FOR JSON PATH).



