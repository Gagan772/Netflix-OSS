package com.netflixoss.shop.integration;

import org.springframework.stereotype.Component;

import com.netflixoss.shop.dto.ApiResponse;
import com.netflixoss.shop.exception.BusinessException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Component
public class StockReservationGateway {

    private final StockClient stockClient;

    public StockReservationGateway(StockClient stockClient) {
        this.stockClient = stockClient;
    }

    @CircuitBreaker(name = "stockReservation", fallbackMethod = "reserveFallback")
    public StockReserveResult reserve(String sku, Integer quantity) {
        ApiResponse<StockReserveResult> response = stockClient.reserveStock(new StockReserveRequest(sku, quantity));
        if (response == null || !response.isSuccess() || response.getData() == null) {
            throw new BusinessException("Failed to reserve stock from product-stock-service");
        }
        return response.getData();
    }

    public StockReserveResult reserveFallback(String sku, Integer quantity, Throwable throwable) {
        return new StockReserveResult(false, null, "Stock service fallback: " + throwable.getMessage());
    }
}
