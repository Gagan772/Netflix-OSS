package com.netflixoss.shop.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.netflixoss.shop.dto.ApiResponse;

@FeignClient(name = "product-stock-service", path = "/api/stock", configuration = StockFeignConfig.class)
public interface StockClient {

    @PostMapping("/reserve")
    ApiResponse<StockReserveResult> reserveStock(@RequestBody StockReserveRequest request);
}
