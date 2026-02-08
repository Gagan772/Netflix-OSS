package com.netflixoss.shop.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CorrelationLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(CorrelationLoggingFilter.class);
    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final int MAX_BODY_LOG_SIZE = 5 * 1024;
    private static final Set<String> SENSITIVE_HEADERS = Set.of("authorization", "cookie");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        String correlationId = resolveCorrelationId(request);
        MDC.put("correlationId", correlationId);
        wrappedResponse.setHeader(CORRELATION_HEADER, correlationId);

        long startNanos = System.nanoTime();
        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            String query = request.getQueryString() == null ? "" : "?" + request.getQueryString();
            String path = request.getRequestURI() + query;
            String body = sanitizeBody(readRequestBody(wrappedRequest));
            Map<String, String> headers = maskHeaders(request);
            long latency = Duration.ofNanos(System.nanoTime() - startNanos).toMillis();

            log.info("SVC IN method={} path={} headers={} body={}", request.getMethod(), path, headers, body);
            log.info("SVC OUT status={} latencyMs={}", wrappedResponse.getStatus(), latency);

            wrappedResponse.copyBodyToResponse();
            MDC.clear();
        }
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String incoming = request.getHeader(CORRELATION_HEADER);
        if (!StringUtils.hasText(incoming)) {
            return UUID.randomUUID().toString();
        }
        return incoming;
    }

    private Map<String, String> maskHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames == null) {
            return Collections.emptyMap();
        }

        Map<String, String> result = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            String value = request.getHeader(name);
            if (SENSITIVE_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                result.put(name, "***");
            } else {
                result.put(name, value);
            }
        }
        return result;
    }

    private String readRequestBody(ContentCachingRequestWrapper request) {
        byte[] buffer = request.getContentAsByteArray();
        if (buffer.length == 0) {
            return "";
        }
        return new String(buffer, StandardCharsets.UTF_8);
    }

    private String sanitizeBody(String body) {
        if (!StringUtils.hasText(body)) {
            return "";
        }
        String trimmed = body.length() > MAX_BODY_LOG_SIZE ? body.substring(0, MAX_BODY_LOG_SIZE) + "...[truncated]" : body;
        return trimmed.replaceAll("(?i)\\\"(password|secret)\\\"\\s*:\\s*\\\".*?\\\"", "\"$1\":\"***\"");
    }
}
