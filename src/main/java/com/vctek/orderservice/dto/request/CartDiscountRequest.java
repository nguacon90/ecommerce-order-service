package com.vctek.orderservice.dto.request;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Calendar;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CartDiscountRequest {
    private Long companyId;
    private String code;
    private Double discount;
    private String discountType;
    private Long timeRequest;

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getTimeRequest() {
        return timeRequest;
    }

    public void setTimeRequest(Long timeRequest) {
        if(timeRequest == null) {
            this.timeRequest = Calendar.getInstance().getTimeInMillis();
            return;
        }

        this.timeRequest = timeRequest;
    }
}
