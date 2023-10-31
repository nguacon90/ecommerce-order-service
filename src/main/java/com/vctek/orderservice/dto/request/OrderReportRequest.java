package com.vctek.orderservice.dto.request;

import com.vctek.orderservice.util.DateUtil;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

public class OrderReportRequest {
    private Long companyId;
    @DateTimeFormat(pattern = DateUtil.ISO_DATE_TIME_PATTERN)
    private Date fromDate;
    private List<String> orderCodes;
    private String orderType;
    private Long productId;
    private String kafkaMessageType;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getKafkaMessageType() {
        return kafkaMessageType;
    }

    public void setKafkaMessageType(String kafkaMessageType) {
        this.kafkaMessageType = kafkaMessageType;
    }

    public List<String> getOrderCodes() {
        return orderCodes;
    }

    public void setOrderCodes(List<String> orderCodes) {
        this.orderCodes = orderCodes;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }
}
