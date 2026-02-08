You are Codex 5.3 acting as a senior Java microservices architect + implementer.

GOAL:
Build a working Netflix OSS style microservices system using Java 17 + Spring Boot + Spring Cloud:
1) config-server
2) eureka-server
3) api-gateway (Spring Cloud Gateway)
4) shop-management-service
5) product-stock-service

All services must run locally via docker-compose OR direct mvn spring-boot:run. Prefer direct run, but provide docker-compose optionally.

REQUIREMENTS (NON-NEGOTIABLE):
A) Netflix OSS:
- Config Server provides centralized config for all services.
- Eureka Server for service discovery.
- API Gateway routes to both microservices via Eureka serviceId.
- Both microservices register with Eureka.
- Use Spring Cloud 2023.x compatible versions (Java 17).

B) Two Microservices:
- shop-management-service (business: shop & orders)
- product-stock-service (business: inventory & stock)

C) TOTAL 10 API calls across the system (not counting gateway health):
- REST: 6 endpoints (split across both services)
- SOAP: 2 operations (WSDL + endpoint)
- GraphQL: 2 operations (query/mutation)

D) Inter-service Communication:
- shop-management-service MUST call product-stock-service for stock validation & reservation.
- Use OpenFeign OR WebClient (choose one, but implement cleanly).
- Calls must go through Eureka service name (not hardcoded localhost).
- Calls may go through Gateway OR direct service discovery; choose direct service discovery for internal calls, gateway for external.

E) Logging (VERY IMPORTANT):
Implement strong request tracing logs with:
- Correlation ID (X-Correlation-Id) generated at gateway if missing, forwarded to downstream.
- Log incoming request at gateway AND at each service:
  - method, path, query params
  - headers (mask Authorization, Cookie)
  - request body (safe: limit to 5KB, mask password/secret)
- Log outgoing request (from shop-management -> product-stock):
  - destination service, url/path
  - headers (masked)
  - payload
- Log response summary:
  - status code, latency ms
- Include correlation id in ALL logs (MDC).
- Provide sample log lines in README for one end-to-end flow.

Use:
- Logback + MDC
- OncePerRequestFilter for REST
- SOAP endpoint interceptor for SOAP logs
- GraphQL interceptor for GraphQL logs
- For outgoing calls: Feign RequestInterceptor OR WebClient filter to log outgoing requests.

F) Data layer:
- Use H2 in-memory DB for both microservices with JPA.
- Provide sample schema + seed data via data.sql.
- Keep domain small but realistic.

G) API Design (10 total):
REST (6):
Shop Management (3):
1) POST /api/shops/{shopId}/orders  -> create order (calls stock reserve in product-stock)
2) GET  /api/orders/{orderId}       -> get order
3) GET  /api/shops/{shopId}/orders  -> list orders

Product Stock (3):
4) GET  /api/products              -> list products
5) GET  /api/products/{sku}        -> get product by sku
6) POST /api/stock/reserve         -> reserve stock (internal call from shop-management)

SOAP (2):
Product Stock SOAP service:
7) getStockLevel(sku) -> returns quantity
8) restockProduct(sku, qty) -> updates stock

GraphQL (2):
Shop Management GraphQL:
9) query orderById(id)
10) mutation createOrder(shopId, sku, qty)

H) Postman Collection:
Generate a Postman collection JSON file in /postman:
- Use environment variables:
  - baseUrl = http://localhost:8080 (gateway)
  - correlationId optional
- Include requests for all REST endpoints via gateway routes.
- For SOAP include raw XML body requests (POST) to SOAP endpoint via gateway.
- For GraphQL include POST to /graphql with query/mutation bodies.
- Include example payloads and tests:
  - test status code is 200/201
  - save created orderId into env variable for next call

I) Config:
- All service ports:
  - config-server: 8888
  - eureka-server: 8761
  - api-gateway: 8080
  - shop-management: 8081
  - product-stock: 8082
- Put centralized configs in config-repo folder (local file-based config for config-server).
- Each service reads config from config-server.
- Put routes in gateway config.
- Enable actuator health endpoints.
- Add resilience:
  - basic retry for stock reservation calls OR Resilience4j circuit breaker (simple default).

DELIVERABLES:
1) Monorepo folder structure:
   /config-repo
   /config-server
   /eureka-server
   /api-gateway
   /shop-management-service
   /product-stock-service
   /postman

2) Each module must have:
- pom.xml
- application.yml bootstrap/config-client setup
- Java code with clean packages
- README.md with run steps

3) Provide ONE root README.md explaining:
- architecture diagram (ASCII is fine)
- how to run in order
- how to test using Postman
- one “end-to-end flow” example: create order -> reserve stock -> logs show IN/OUT with correlation-id

IMPLEMENTATION DETAILS (IMPORTANT):
- Use Spring Cloud Config client dependency and bootstrap config.
- Eureka discovery client enabled.
- Gateway routes:
  - /shop/** -> lb://SHOP-MANAGEMENT-SERVICE
  - /stock/** -> lb://PRODUCT-STOCK-SERVICE
  - /soap/** -> route to SOAP endpoint in product-stock
  - /graphql/** -> route to shop-management graphql
- Make sure REST controllers align to above endpoints even if routed with prefixes.
- Prefer consistent response DTOs with fields: success, data, error, correlationId, timestamp.
- Add exception handling with @ControllerAdvice returning same response shape.

QUALITY BAR:
- Code must compile and run.
- No pseudo code. Provide real code for SOAP, GraphQL, REST, logging filters/interceptors, Feign/WebClient client.
- Keep it simple but production-like.
- Provide sample WSDL generation (Spring-WS) and schema XSD.
- Provide Postman collection ready to import.

OUTPUT FORMAT:
- First: show repository tree.
- Then: generate files with full content (use markdown code blocks per file).
- Keep secrets masked.
- Ensure instructions are correct.

Start now.