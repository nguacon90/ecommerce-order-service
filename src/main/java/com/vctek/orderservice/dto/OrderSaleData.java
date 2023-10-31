package com.vctek.orderservice.dto;

public class OrderSaleData {
    private long online;
    private long retail;
    private long wholesale;

    public long getOnline() {
        return online;
    }

    public void setOnline(long online) {
        this.online = online;
    }

    public long getRetail() {
        return retail;
    }

    public void setRetail(long retail) {
        this.retail = retail;
    }

    public long getWholesale() {
        return wholesale;
    }

    public void setWholesale(long wholesale) {
        this.wholesale = wholesale;
    }
}
