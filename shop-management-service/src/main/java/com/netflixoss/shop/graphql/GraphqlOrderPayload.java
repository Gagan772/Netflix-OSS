package com.netflixoss.shop.graphql;

import com.netflixoss.shop.dto.ApiResponse;
import com.netflixoss.shop.dto.OrderResponse;

public class GraphqlOrderPayload {

    private boolean success;
    private OrderResponse data;
    private String error;
    private String correlationId;
    private String timestamp;

    public static GraphqlOrderPayload from(ApiResponse<OrderResponse> response) {
        GraphqlOrderPayload payload = new GraphqlOrderPayload();
        payload.success = response.isSuccess();
        payload.data = response.getData();
        payload.error = response.getError();
        payload.correlationId = response.getCorrelationId();
        payload.timestamp = response.getTimestamp() == null ? null : response.getTimestamp().toString();
        return payload;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public OrderResponse getData() {
        return data;
    }

    public void setData(OrderResponse data) {
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
