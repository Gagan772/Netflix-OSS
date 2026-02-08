package com.netflixoss.stock.soap;

import java.io.StringWriter;
import java.util.UUID;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpServletConnection;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class SoapLoggingInterceptor implements EndpointInterceptor {

    private static final Logger log = LoggerFactory.getLogger(SoapLoggingInterceptor.class);
    private static final String CORRELATION_HEADER = "X-Correlation-Id";
    private static final int MAX_BODY_LOG_SIZE = 5 * 1024;

    @Override
    public boolean handleRequest(MessageContext messageContext, Object endpoint) {
        String correlationId = resolveCorrelationId();
        MDC.put("correlationId", correlationId);
        log.info("SOAP IN payload={}", sanitizePayload(extractPayload(messageContext.getRequest())));
        return true;
    }

    @Override
    public boolean handleResponse(MessageContext messageContext, Object endpoint) {
        log.info("SOAP OUT payload={}", sanitizePayload(extractPayload(messageContext.getResponse())));
        return true;
    }

    @Override
    public boolean handleFault(MessageContext messageContext, Object endpoint) {
        log.error("SOAP FAULT payload={}", sanitizePayload(extractPayload(messageContext.getResponse())));
        return true;
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) {
        MDC.clear();
    }

    private String resolveCorrelationId() {
        TransportContext transportContext = TransportContextHolder.getTransportContext();
        if (transportContext != null && transportContext.getConnection() instanceof HttpServletConnection connection) {
            HttpServletRequest request = connection.getHttpServletRequest();
            String incoming = request.getHeader(CORRELATION_HEADER);
            if (StringUtils.hasText(incoming)) {
                return incoming;
            }
        }
        return UUID.randomUUID().toString();
    }

    private String extractPayload(WebServiceMessage message) {
        if (!(message instanceof SoapMessage soapMessage)) {
            return "";
        }

        try {
            Source source = soapMessage.getPayloadSource();
            if (source == null) {
                return "";
            }
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(source, new StreamResult(writer));
            return writer.toString();
        } catch (Exception ex) {
            return "[payload-unavailable:" + ex.getMessage() + "]";
        }
    }

    private String sanitizePayload(String payload) {
        if (!StringUtils.hasText(payload)) {
            return "";
        }
        String trimmed = payload.length() > MAX_BODY_LOG_SIZE ? payload.substring(0, MAX_BODY_LOG_SIZE) + "...[truncated]" : payload;
        return trimmed.replaceAll("(?i)<(password|secret)>.*?</(password|secret)>", "<$1>***</$2>");
    }
}
