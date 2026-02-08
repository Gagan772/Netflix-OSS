package com.netflixoss.gateway.filter;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class CorrelationLoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(CorrelationLoggingGlobalFilter.class);
    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final int MAX_BODY_LOG_SIZE = 5 * 1024;
    private static final Set<String> SENSITIVE_HEADERS = new HashSet<>(Arrays.asList("authorization", "cookie"));

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startNanos = System.nanoTime();
        String correlationId = resolveCorrelationId(exchange.getRequest().getHeaders());

        ServerHttpRequest requestWithCorrelation = exchange.getRequest().mutate()
                .header(CORRELATION_HEADER, correlationId)
                .build();
        exchange.getResponse().getHeaders().set(CORRELATION_HEADER, correlationId);

        ServerWebExchange mutatedExchange = exchange.mutate().request(requestWithCorrelation).build();
        DataBufferFactory bufferFactory = mutatedExchange.getResponse().bufferFactory();

        Mono<DataBuffer> joined = DataBufferUtils.join(requestWithCorrelation.getBody())
                .defaultIfEmpty(bufferFactory.wrap(new byte[0]));

        return joined.flatMap(dataBuffer -> {
            byte[] bytes = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(bytes);
            DataBufferUtils.release(dataBuffer);

            String requestBody = sanitizeBody(new String(bytes, StandardCharsets.UTF_8));
            String method = requestWithCorrelation.getMethod() == null ? "UNKNOWN" : requestWithCorrelation.getMethod().name();
            String query = requestWithCorrelation.getURI().getRawQuery();
            String queryText = query == null ? "" : "?" + query;
            String path = requestWithCorrelation.getURI().getRawPath() + queryText;

            MDC.put("correlationId", correlationId);
            log.info("GW IN method={} path={} headers={} body={}", method, path,
                    maskHeaders(requestWithCorrelation.getHeaders()), requestBody);
            MDC.clear();

            ServerHttpRequest decoratedRequest = new ServerHttpRequestDecorator(requestWithCorrelation) {
                @Override
                public Flux<DataBuffer> getBody() {
                    return Flux.defer(() -> Mono.just(bufferFactory.wrap(bytes)));
                }
            };

            ServerWebExchange decoratedExchange = mutatedExchange.mutate().request(decoratedRequest).build();

            return chain.filter(decoratedExchange)
                    .doFinally(signalType -> {
                        long latency = Duration.ofNanos(System.nanoTime() - startNanos).toMillis();
                        HttpStatusCode statusCode = decoratedExchange.getResponse().getStatusCode();
                        int status = statusCode == null ? 200 : statusCode.value();
                        MDC.put("correlationId", correlationId);
                        log.info("GW OUT status={} latencyMs={}", status, latency);
                        MDC.clear();
                    });
        });
    }

    private String resolveCorrelationId(HttpHeaders headers) {
        String incoming = headers.getFirst(CORRELATION_HEADER);
        if (incoming == null || incoming.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return incoming;
    }

    private String sanitizeBody(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        String trimmed = body.length() > MAX_BODY_LOG_SIZE ? body.substring(0, MAX_BODY_LOG_SIZE) + "...[truncated]" : body;
        return trimmed.replaceAll("(?i)\\\"(password|secret)\\\"\\s*:\\s*\\\".*?\\\"", "\"$1\":\"***\"");
    }

    private Map<String, String> maskHeaders(HttpHeaders headers) {
        return headers.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> maskHeaderValue(e.getKey(), e.getValue())));
    }

    private String maskHeaderValue(String key, List<String> values) {
        if (key == null) {
            return String.join(",", values);
        }
        String normalized = key.toLowerCase(Locale.ROOT);
        if (SENSITIVE_HEADERS.contains(normalized)) {
            return "***";
        }
        return String.join(",", values);
    }
}
