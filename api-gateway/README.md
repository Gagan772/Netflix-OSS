# API Gateway

## Purpose
Spring Cloud Gateway routes external traffic to internal services and injects/propagates `X-Correlation-Id`.

## Run
```bash
mvn spring-boot:run
```

## Port
- `8080`

## Routes
- `/shop/** -> lb://SHOP-MANAGEMENT-SERVICE`
- `/stock/** -> lb://PRODUCT-STOCK-SERVICE`
- `/soap/** -> lb://PRODUCT-STOCK-SERVICE`
- `/graphql/** -> lb://SHOP-MANAGEMENT-SERVICE`
