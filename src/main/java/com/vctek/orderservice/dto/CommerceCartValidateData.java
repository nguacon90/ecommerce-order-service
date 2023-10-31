package com.vctek.orderservice.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommerceCartValidateData {
    private boolean hasError;
    private List<String> orderErrorCodes = new ArrayList<>();

    private Map<Long, CommerceEntryError> entryErrors = new HashMap<>();

    public Map<Long, CommerceEntryError> getEntryErrors() {
        return entryErrors;
    }

    public void setEntryErrors(Map<Long, CommerceEntryError> entryErrors) {
        this.entryErrors = entryErrors;
    }

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
}
