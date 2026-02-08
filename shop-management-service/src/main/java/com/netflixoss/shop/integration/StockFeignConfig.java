package com.netflixoss.shop.integration;

import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@Configuration
public class StockFeignConfig {

    private static final Logger log = LoggerFactory.getLogger(StockFeignConfig.class);
    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final int MAX_BODY_LOG_SIZE = 5 * 1024;

    @Bean
    public RequestInterceptor correlationRequestInterceptor() {
        return this::addCorrelationId;
    }

    private void addCorrelationId(RequestTemplate template) {
        String correlationId = MDC.get("correlationId");
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
            MDC.put("correlationId", correlationId);
        }
        template.header(CORRELATION_HEADER, correlationId);

        log.info("SVC OUT REQ destinationService=product-stock-service method={} path={} headers={} payload={}",
                template.method(),
                template.path(),
                template.headers().entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> e.getKey(),
                                e -> String.join(",", e.getValue()))),
                sanitizeBody(template.body() == null ? "" : new String(template.body())));
    }

    private String sanitizeBody(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        String trimmed = body.length() > MAX_BODY_LOG_SIZE ? body.substring(0, MAX_BODY_LOG_SIZE) + "...[truncated]" : body;
        return trimmed.replaceAll("(?i)\\\"(password|secret)\\\"\\s*:\\s*\\\".*?\\\"", "\"$1\":\"***\"");
    }
}
