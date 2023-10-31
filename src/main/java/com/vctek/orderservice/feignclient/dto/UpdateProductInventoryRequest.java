package com.vctek.orderservice.feignclient.dto;

import java.util.List;

public class UpdateProductInventoryRequest {
    private Long companyId;
    private Long warehouseId;
    private List<UpdateProductInventoryDetailData> detailDataList;
    private String from;
    private String to;

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

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
