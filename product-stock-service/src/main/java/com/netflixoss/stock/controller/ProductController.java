package com.netflixoss.stock.controller;

import java.util.List;

import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.netflixoss.stock.dto.ApiResponse;
import com.netflixoss.stock.dto.ProductResponse;
import com.netflixoss.stock.service.ProductStockService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductStockService productStockService;

    public ProductController(ProductStockService productStockService) {
        this.productStockService = productStockService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProducts() {
        return ResponseEntity.ok(ApiResponse.success(productStockService.getAllProducts(), MDC.get("correlationId")));
    }

    @GetMapping("/{sku}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductBySku(@PathVariable String sku) {
        return ResponseEntity.ok(ApiResponse.success(productStockService.getProductBySku(sku), MDC.get("correlationId")));
    }
}
