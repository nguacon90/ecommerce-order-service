package com.vctek.orderservice.dto;

import com.vctek.orderservice.dto.excel.OrderItemDTO;

import java.util.List;

public class OrderImportData extends AbstractOrderData {
    private boolean hasError;
    private List<OrderItemDTO> itemDTO;

    public boolean isHasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    public List<OrderItemDTO> getItemDTO() {
        return itemDTO;
    }

    public void setItemDTO(List<OrderItemDTO> itemDTO) {
        this.itemDTO = itemDTO;
    }
}
