package com.netflixoss.stock.dto;

import java.math.BigDecimal;

public class ProductResponse {

    private String sku;
    private String name;
    private Integer availableQuantity;
    private BigDecimal price;

    public ProductResponse() {
    }

    public ProductResponse(String sku, String name, Integer availableQuantity, BigDecimal price) {
        this.sku = sku;
        this.name = name;
        this.availableQuantity = availableQuantity;
        this.price = price;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
