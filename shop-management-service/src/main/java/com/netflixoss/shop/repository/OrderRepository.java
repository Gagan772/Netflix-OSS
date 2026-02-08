package com.netflixoss.shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.netflixoss.shop.entity.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    List<OrderEntity> findByShopId(Long shopId);
}
