package com.vctek.orderservice.feignclient.dto;

import java.util.List;

public class UpdateInventoryStatusRequest {
    private Long companyId;
    private Long warehouseId;
    private List<UpdateProductInventoryDetailData> detailDataList;
    private String statusCode;

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

    public List<UpdateProductInventoryDetailData> getDetailDataList() {
        return detailDataList;
    }

    public void setDetailDataList(List<UpdateProductInventoryDetailData> detailDataList) {
        this.detailDataList = detailDataList;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }
}
