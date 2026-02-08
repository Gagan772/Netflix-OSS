package com.netflixoss.stock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ProductStockApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductStockApplication.class, args);
    }
}
