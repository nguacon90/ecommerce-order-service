package com.vctek.orderservice.dto;

import java.util.ArrayList;
import java.util.List;

public class CommerceCartData extends AbstractOrderData {
    private boolean hasError;
    private List<String> orderErrorCodes = new ArrayList<>();
    private Long shippingCompanyId;
    private Long shippingFeeSettingId;
    private String cancelText;

    public boolean isHasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    public List<String> getOrderErrorCodes() {
        return orderErrorCodes;
    }

    public void setOrderErrorCodes(List<String> orderErrorCodes) {
        this.orderErrorCodes = orderErrorCodes;
    }

    public Long getShippingCompanyId() {
        return shippingCompanyId;
    }

    public void setShippingCompanyId(Long shippingCompanyId) {
        this.shippingCompanyId = shippingCompanyId;
    }

    public String getCancelText() {
        return cancelText;
    }

    public void setCancelText(String cancelText) {
        this.cancelText = cancelText;
    }

    public Long getShippingFeeSettingId() {
        return shippingFeeSettingId;
    }

    public void setShippingFeeSettingId(Long shippingFeeSettingId) {
        this.shippingFeeSettingId = shippingFeeSettingId;
    }
}
