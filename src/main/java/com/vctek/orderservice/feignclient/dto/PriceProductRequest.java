package com.vctek.orderservice.feignclient.dto;

import java.util.ArrayList;
import java.util.List;

public class PriceProductRequest {
    private List<PriceRequest> priceRequestList = new ArrayList<>();

    public List<PriceRequest> getPriceRequestList() {
        return priceRequestList;
    }

    public void setPriceRequestList(List<PriceRequest> priceRequestList) {
        this.priceRequestList = priceRequestList;
    }
}
