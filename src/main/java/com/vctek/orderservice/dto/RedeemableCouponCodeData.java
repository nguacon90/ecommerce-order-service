package com.vctek.orderservice.dto;

public class RedeemableCouponCodeData {
    private boolean canRedeem;
    private int remainRedeemQuantity;

    public boolean isCanRedeem() {
        return canRedeem;
    }

    public void setCanRedeem(boolean canRedeem) {
        this.canRedeem = canRedeem;
    }

    public int getRemainRedeemQuantity() {
        return remainRedeemQuantity;
    }

    public void setRemainRedeemQuantity(int remainRedeemQuantity) {
        this.remainRedeemQuantity = remainRedeemQuantity;
    }
}
