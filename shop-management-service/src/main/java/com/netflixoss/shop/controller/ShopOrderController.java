package com.netflixoss.shop.controller;

import java.util.List;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.netflixoss.shop.dto.ApiResponse;
import com.netflixoss.shop.dto.CreateOrderRequest;
import com.netflixoss.shop.dto.OrderResponse;
import com.netflixoss.shop.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Validated
public class ShopOrderController {

    private final OrderService orderService;

    public ShopOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/shops/{shopId}/orders")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@PathVariable Long shopId,
                                                                  @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(orderService.createOrder(shopId, request), MDC.get("correlationId")));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrder(orderId), MDC.get("correlationId")));
    }

    @GetMapping("/shops/{shopId}/orders")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> listOrdersByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrdersByShop(shopId), MDC.get("correlationId")));
    }
}
