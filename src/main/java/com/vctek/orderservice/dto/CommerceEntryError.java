package com.vctek.orderservice.dto;

import java.util.HashMap;
import java.util.Map;

public class CommerceEntryError {
    private boolean hasError;
    private String errorCode;
    private Integer availableStock;
    private Map<Long, CommerceEntryError> subEntryErrors = new HashMap<>();

    public Map<Long, CommerceEntryError> getSubEntryErrors() {
        return subEntryErrors;
    }

    public void setSubEntryErrors(Map<Long, CommerceEntryError> subEntryErrors) {
        this.subEntryErrors = subEntryErrors;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isHasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }
}
