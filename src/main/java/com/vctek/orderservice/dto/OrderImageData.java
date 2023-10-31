package com.vctek.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderImageData {
    private String url;
    private boolean finishedProduct = false;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isFinishedProduct() {
        return finishedProduct;
    }

    public void setFinishedProduct(boolean finishedProduct) {
        this.finishedProduct = finishedProduct;
    }
}
