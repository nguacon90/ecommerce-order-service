package com.vctek.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vctek.orderservice.util.DateUtil;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderStatusImportSearchRequest {
    private Long companyId;

    @DateTimeFormat(pattern = DateUtil.ISO_DATE_PATTERN)
    private Date fromCreatedDate;

    @DateTimeFormat(pattern = DateUtil.ISO_DATE_PATTERN)
    private Date toCreatedDate;

    private String orderCode;
    private List<String> status;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Date getFromCreatedDate() {
        return fromCreatedDate;
    }

    public void setFromCreatedDate(Date fromCreatedDate) {
        this.fromCreatedDate = fromCreatedDate;
    }

    public Date getToCreatedDate() {
        return toCreatedDate;
    }

    public void setToCreatedDate(Date toCreatedDate) {
        this.toCreatedDate = toCreatedDate;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public List<String> getStatus() {
        return status;
    }

    public void setStatus(List<String> status) {
        this.status = status;
    }
}
