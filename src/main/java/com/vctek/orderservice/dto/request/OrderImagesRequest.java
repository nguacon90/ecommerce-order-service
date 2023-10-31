package com.vctek.orderservice.dto.request;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vctek.orderservice.dto.OrderImageData;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderImagesRequest {
    private List<OrderImageData> orderImages;
    private Long companyId;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public List<OrderImageData> getOrderImages() {
        return orderImages;
    }

    public void setOrderImages(List<OrderImageData> orderImages) {
        this.orderImages = orderImages;
    }
}
