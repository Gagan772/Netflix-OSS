package com.netflixoss.stock.controller;

import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.netflixoss.stock.dto.ApiResponse;
import com.netflixoss.stock.dto.ReserveStockRequest;
import com.netflixoss.stock.dto.ReserveStockResponse;
import com.netflixoss.stock.service.ProductStockService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/stock")
@Validated
public class StockController {

    private final ProductStockService productStockService;

    public StockController(ProductStockService productStockService) {
        this.productStockService = productStockService;
    }

    @PostMapping("/reserve")
    public ResponseEntity<ApiResponse<ReserveStockResponse>> reserveStock(@Valid @RequestBody ReserveStockRequest request) {
        ReserveStockResponse response = productStockService.reserveStock(request);
        return ResponseEntity.ok(ApiResponse.success(response, MDC.get("correlationId")));
    }
}
