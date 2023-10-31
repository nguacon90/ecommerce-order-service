package com.vctek.orderservice.feignclient.dto;

import java.util.List;

public class AddProductBillRequest {
    private Long billId;
    private Long companyId;
    private Long warehouseId;
    private String orderCode;
    private List<BillDetailRequest> billDetails;

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public List<BillDetailRequest> getBillDetails() {
        return billDetails;
    }

    public void setBillDetails(List<BillDetailRequest> billDetails) {
        this.billDetails = billDetails;
    }
}
