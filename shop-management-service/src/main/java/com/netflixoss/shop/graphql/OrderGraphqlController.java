package com.netflixoss.shop.graphql;

import org.slf4j.MDC;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.netflixoss.shop.dto.ApiResponse;
import com.netflixoss.shop.dto.CreateOrderRequest;
import com.netflixoss.shop.dto.OrderResponse;
import com.netflixoss.shop.service.OrderService;

@Controller
public class OrderGraphqlController {

    private final OrderService orderService;

    public OrderGraphqlController(OrderService orderService) {
        this.orderService = orderService;
    }

    @QueryMapping
    public GraphqlOrderPayload orderById(@Argument Long id) {
        ApiResponse<OrderResponse> response = ApiResponse.success(orderService.getOrder(id), MDC.get("correlationId"));
        return GraphqlOrderPayload.from(response);
    }

    @MutationMapping
    public GraphqlOrderPayload createOrder(@Argument Long shopId, @Argument String sku, @Argument Integer qty) {
        OrderResponse created = orderService.createOrder(shopId, new CreateOrderRequest(sku, qty));
        return GraphqlOrderPayload.from(ApiResponse.success(created, MDC.get("correlationId")));
    }
}
