package com.vctek.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vctek.orderservice.util.DateUtil;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Calendar;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VatRequest {
    private Long companyId;
    private String code;
    private Double vat;
    private String vatType;
    private String vatNumber;
    private Long timeRequest;

    @DateTimeFormat(pattern = DateUtil.ISO_DATE_PATTERN)
    private Date vatDate;

    public Double getVat() {
        return vat;
    }

    public void setVat(Double vat) {
        this.vat = vat;
    }

    public String getVatType() {
        return vatType;
    }

    public void setVatType(String vatType) {
        this.vatType = vatType;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public Date getVatDate() {
        return vatDate;
    }

    public void setVatDate(Date vatDate) {
        this.vatDate = vatDate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getTimeRequest() {
        return timeRequest;
    }

    public void setTimeRequest(Long timeRequest) {
        if(timeRequest == null) {
            this.timeRequest = Calendar.getInstance().getTimeInMillis();
            return;
        }

        this.timeRequest = timeRequest;
    }
}
