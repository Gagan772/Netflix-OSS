package com.netflixoss.stock.dto;

public class ReserveStockResponse {

    private boolean reserved;
    private Integer remainingQuantity;
    private String message;

    public ReserveStockResponse() {
    }

    public ReserveStockResponse(boolean reserved, Integer remainingQuantity, String message) {
        this.reserved = reserved;
        this.remainingQuantity = remainingQuantity;
        this.message = message;
    }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    public Integer getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(Integer remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
