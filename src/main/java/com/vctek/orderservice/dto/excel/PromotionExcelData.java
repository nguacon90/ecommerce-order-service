package com.vctek.orderservice.dto.excel;

import com.vctek.dto.promotion.PromotionSourceRuleDTO;

public class PromotionExcelData extends PromotionSourceRuleDTO {
    private String appliedWarehouses;
    private String appliedOrderTypes;
    private String startDateStr;
    private String endDateStr;
    private String activeStatus;
    private String publishStatusStr;
    private String conditionsStr;
    private String actionStr;

    public String getAppliedWarehouses() {
        return appliedWarehouses;
    }

    public void setAppliedWarehouses(String appliedWarehouses) {
        this.appliedWarehouses = appliedWarehouses;
    }

    public String getAppliedOrderTypes() {
        return appliedOrderTypes;
    }

    public void setAppliedOrderTypes(String appliedOrderTypes) {
        this.appliedOrderTypes = appliedOrderTypes;
    }

    public String getStartDateStr() {
        return startDateStr;
    }

    public void setStartDateStr(String startDateStr) {
        this.startDateStr = startDateStr;
    }

    public String getEndDateStr() {
        return endDateStr;
    }

    public void setEndDateStr(String endDateStr) {
        this.endDateStr = endDateStr;
    }

    public String getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(String activeStatus) {
        this.activeStatus = activeStatus;
    }

    public String getPublishStatusStr() {
        return publishStatusStr;
    }

    public void setPublishStatusStr(String publishStatusStr) {
        this.publishStatusStr = publishStatusStr;
    }

    public String getConditionsStr() {
        return conditionsStr;
    }

    public void setConditionsStr(String conditionsStr) {
        this.conditionsStr = conditionsStr;
    }

    public String getActionStr() {
        return actionStr;
    }

    public void setActionStr(String actionStr) {
        this.actionStr = actionStr;
    }
}
