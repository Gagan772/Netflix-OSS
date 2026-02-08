# Shop Management Service

## Purpose
Shop and order domain service with REST + GraphQL APIs.

## Run
```bash
mvn spring-boot:run
```

## Port
- `8081`

## APIs
- `POST /api/shops/{shopId}/orders`
- `GET /api/orders/{orderId}`
- `GET /api/shops/{shopId}/orders`
- `POST /graphql` (`orderById`, `createOrder`)

## Integration
- Calls `product-stock-service` using OpenFeign with Eureka service discovery.
