package com.netflixoss.shop.graphql;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class GraphqlLoggingInterceptor implements WebGraphQlInterceptor {

    private static final Logger log = LoggerFactory.getLogger(GraphqlLoggingInterceptor.class);
    private static final Set<String> SENSITIVE_HEADERS = Set.of("authorization", "cookie");
    private static final int MAX_BODY_LOG_SIZE = 5 * 1024;

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        String correlationId = resolveCorrelationId(request.getHeaders());
        MDC.put("correlationId", correlationId);

        log.info("GQL IN operation={} query={} variables={} headers={}",
                request.getOperationName(),
                sanitizeText(request.getDocument()),
                sanitizeText(String.valueOf(request.getVariables())),
                maskHeaders(request.getHeaders()));

        return chain.next(request)
                .doOnNext(response -> log.info("GQL OUT errors={} dataPresent={}", response.getErrors().size(), response.isValid()))
                .doFinally(signalType -> MDC.clear());
    }

    private String resolveCorrelationId(HttpHeaders headers) {
        String header = headers.getFirst("X-Correlation-Id");
        if (header == null || header.isBlank()) {
            String mdcValue = MDC.get("correlationId");
            return mdcValue == null || mdcValue.isBlank() ? UUID.randomUUID().toString() : mdcValue;
        }
        return header;
    }

    private Map<String, String> maskHeaders(HttpHeaders headers) {
        return headers.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> {
            if (SENSITIVE_HEADERS.contains(entry.getKey().toLowerCase(Locale.ROOT))) {
                return "***";
            }
            List<String> values = entry.getValue();
            return String.join(",", values);
        }));
    }

    private String sanitizeText(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String trimmed = input.length() > MAX_BODY_LOG_SIZE ? input.substring(0, MAX_BODY_LOG_SIZE) + "...[truncated]" : input;
        return trimmed.replaceAll("(?i)\\\"(password|secret)\\\"\\s*:\\s*\\\".*?\\\"", "\"$1\":\"***\"");
    }
}
