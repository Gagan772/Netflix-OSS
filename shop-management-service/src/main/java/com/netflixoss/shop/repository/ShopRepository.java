package com.netflixoss.shop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.netflixoss.shop.entity.ShopEntity;

public interface ShopRepository extends JpaRepository<ShopEntity, Long> {
}
