# Product Stock Service

## Purpose
Inventory and stock management service with REST + SOAP APIs.

## Run
```bash
mvn spring-boot:run
```

## Port
- `8082`

## APIs
- `GET /api/products`
- `GET /api/products/{sku}`
- `POST /api/stock/reserve`
- SOAP endpoint: `/ws`
- SOAP WSDL: `/ws/stock.wsdl`
