package com.netflixoss.shop.dto;

import java.time.Instant;

public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String error;
    private String correlationId;
    private Instant timestamp;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, T data, String error, String correlationId, Instant timestamp) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.correlationId = correlationId;
        this.timestamp = timestamp;
    }

    public static <T> ApiResponse<T> success(T data, String correlationId) {
        return new ApiResponse<>(true, data, null, correlationId, Instant.now());
    }

    public static <T> ApiResponse<T> error(String error, String correlationId) {
        return new ApiResponse<>(false, null, error, correlationId, Instant.now());
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
