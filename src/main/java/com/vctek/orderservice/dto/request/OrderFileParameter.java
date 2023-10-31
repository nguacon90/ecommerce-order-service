package com.vctek.orderservice.dto.request;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vctek.orderservice.util.ExportExcelType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderFileParameter {
    private ExportExcelType exportExcelType;
    private Integer fileNum;
    private Long userId;
    private String orderType;

    public ExportExcelType getExportExcelType() {
        return exportExcelType;
    }

    public void setExportExcelType(ExportExcelType exportExcelType) {
        this.exportExcelType = exportExcelType;
    }

    public Integer getFileNum() {
        return fileNum;
    }

    public void setFileNum(Integer fileNum) {
        this.fileNum = fileNum;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }
}
