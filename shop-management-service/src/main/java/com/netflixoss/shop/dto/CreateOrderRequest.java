package com.netflixoss.shop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class CreateOrderRequest {

    @NotBlank
    private String sku;

    @Min(1)
    private Integer qty;

    public CreateOrderRequest() {
    }

    public CreateOrderRequest(String sku, Integer qty) {
        this.sku = sku;
        this.qty = qty;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }
}
