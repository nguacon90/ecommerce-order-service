package com.vctek.orderservice.dto.excel;


import java.util.ArrayList;
import java.util.List;

public class OrderSettingDiscountErrorDTO {
    private List<OrderSettingDiscountDTO> errors = new ArrayList<>();

    public List<OrderSettingDiscountDTO> getErrors() {
        return errors;
    }

    public void setErrors(List<OrderSettingDiscountDTO> errors) {
        this.errors = errors;
    }
}