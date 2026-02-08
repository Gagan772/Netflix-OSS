package com.netflixoss.shop.dto;

public class OrderResponse {

    private Long orderId;
    private Long shopId;
    private String sku;
    private Integer qty;
    private String status;
    private String createdAt;

    public OrderResponse() {
    }

    public OrderResponse(Long orderId, Long shopId, String sku, Integer qty, String status, String createdAt) {
        this.orderId = orderId;
        this.shopId = shopId;
        this.sku = sku;
        this.qty = qty;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
