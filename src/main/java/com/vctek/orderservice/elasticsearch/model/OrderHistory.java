package com.vctek.orderservice.elasticsearch.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderHistory implements Serializable {
    private String previousStatus;
    private String currentStatus;
    @Field(type = FieldType.Date)
    private Date modifiedTimeStatus;
    private String extraData;

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public Date getModifiedTimeStatus() {
        return modifiedTimeStatus;
    }

    public void setModifiedTimeStatus(Date modifiedTimeStatus) {
        this.modifiedTimeStatus = modifiedTimeStatus;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }
}
