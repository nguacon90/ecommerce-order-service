package com.vctek.orderservice.dto;

public class ReturnRewardRedeemData {
    private Double revertPoint;
    private Double remainRedeemPoint;
    private Double refundPoint;
    private Double remainMoney;
    private Double conversionRate;
    private Double availablePoint;
    private Double pendingPoint;
    private Double newAvailablePoint;

    public Double getRevertPoint() {
        return revertPoint;
    }

    public void setRevertPoint(Double revertPoint) {
        this.revertPoint = revertPoint;
    }

    public Double getRemainRedeemPoint() {
        return remainRedeemPoint;
    }

    public void setRemainRedeemPoint(Double remainRedeemPoint) {
        this.remainRedeemPoint = remainRedeemPoint;
    }

    public Double getRefundPoint() {
        return refundPoint;
    }

    public void setRefundPoint(Double refundPoint) {
        this.refundPoint = refundPoint;
    }

    public Double getRemainMoney() {
        return remainMoney;
    }

    public void setRemainMoney(Double remainMoney) {
        this.remainMoney = remainMoney;
    }

    public Double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(Double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public Double getAvailablePoint() {
        return availablePoint;
    }

    public void setAvailablePoint(Double availablePoint) {
        this.availablePoint = availablePoint;
    }

    public Double getPendingPoint() {
        return pendingPoint;
    }

    public void setPendingPoint(Double pendingPoint) {
        this.pendingPoint = pendingPoint;
    }

    public Double getNewAvailablePoint() {
        return newAvailablePoint;
    }

    public void setNewAvailablePoint(Double newAvailablePoint) {
        this.newAvailablePoint = newAvailablePoint;
    }
}
