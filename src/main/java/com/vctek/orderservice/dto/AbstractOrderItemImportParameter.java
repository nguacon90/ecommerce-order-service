package com.vctek.orderservice.dto;

import com.vctek.orderservice.dto.excel.OrderItemDTO;

import java.util.List;

public class AbstractOrderItemImportParameter {
    private List<OrderItemDTO> orderItems;

    public AbstractOrderItemImportParameter(List<OrderItemDTO> orderItems) {
        this.orderItems = orderItems;
    }

    public List<OrderItemDTO> getOrderItems() {
        return orderItems;
    }
}
