package com.vctek.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vctek.orderservice.feignclient.dto.OrderBillRequest;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ComboOrToppingOrderRequest {
    private Long comboId;
    private Long companyId;
    private Integer quantity;
    private Long billId;
    private String orderCode;
    private Long orderEntryId;
    private List<OrderBillRequest> orderRequestList;

    public Long getComboId() {
        return comboId;
    }

    public void setComboId(Long comboId) {
        this.comboId = comboId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public List<OrderBillRequest> getOrderRequestList() {
        return orderRequestList;
    }

    public void setOrderRequestList(List<OrderBillRequest> orderRequestList) {
        this.orderRequestList = orderRequestList;
    }

    public Long getOrderEntryId() {
        return orderEntryId;
    }

    public void setOrderEntryId(Long orderEntryId) {
        this.orderEntryId = orderEntryId;
    }
}
