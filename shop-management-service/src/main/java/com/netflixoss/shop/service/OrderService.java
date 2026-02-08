package com.netflixoss.shop.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.netflixoss.shop.dto.CreateOrderRequest;
import com.netflixoss.shop.dto.OrderResponse;
import com.netflixoss.shop.entity.OrderEntity;
import com.netflixoss.shop.exception.BusinessException;
import com.netflixoss.shop.exception.ResourceNotFoundException;
import com.netflixoss.shop.integration.StockReservationGateway;
import com.netflixoss.shop.integration.StockReserveResult;
import com.netflixoss.shop.repository.OrderRepository;
import com.netflixoss.shop.repository.ShopRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ShopRepository shopRepository;
    private final StockReservationGateway stockReservationGateway;

    public OrderService(OrderRepository orderRepository,
                        ShopRepository shopRepository,
                        StockReservationGateway stockReservationGateway) {
        this.orderRepository = orderRepository;
        this.shopRepository = shopRepository;
        this.stockReservationGateway = stockReservationGateway;
    }

    @Transactional
    public OrderResponse createOrder(Long shopId, CreateOrderRequest request) {
        if (!shopRepository.existsById(shopId)) {
            throw new ResourceNotFoundException("Shop not found for id: " + shopId);
        }

        StockReserveResult reservation = stockReservationGateway.reserve(request.getSku(), request.getQty());
        if (!reservation.isReserved()) {
            throw new BusinessException("Order rejected, stock reservation failed: " + reservation.getMessage());
        }

        OrderEntity order = new OrderEntity();
        order.setShopId(shopId);
        order.setSku(request.getSku());
        order.setQuantity(request.getQty());
        order.setStatus("CONFIRMED");
        order.setCreatedAt(Instant.now());

        OrderEntity saved = orderRepository.save(order);
        return map(saved);
    }

    public OrderResponse getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .map(this::map)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for id: " + orderId));
    }

    public List<OrderResponse> getOrdersByShop(Long shopId) {
        if (!shopRepository.existsById(shopId)) {
            throw new ResourceNotFoundException("Shop not found for id: " + shopId);
        }
        return orderRepository.findByShopId(shopId).stream().map(this::map).toList();
    }

    private OrderResponse map(OrderEntity order) {
        return new OrderResponse(order.getId(), order.getShopId(), order.getSku(), order.getQuantity(), order.getStatus(),
                order.getCreatedAt().toString());
    }
}
