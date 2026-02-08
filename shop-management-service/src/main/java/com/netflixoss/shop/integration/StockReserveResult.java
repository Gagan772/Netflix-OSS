package com.netflixoss.shop.integration;

public class StockReserveResult {

    private boolean reserved;
    private Integer remainingQuantity;
    private String message;

    public StockReserveResult() {
    }

    public StockReserveResult(boolean reserved, Integer remainingQuantity, String message) {
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
