package com.vctek.orderservice.util;

public enum BillStatus {
    UNVERIFIED("UNVERIFIED"), VERIFIED("VERIFIED"), PENDING_FOR_VERIFIED("PENDING_FOR_VERIFIED"),
    VERIFIED_WITH_BALANCE_BILLS("VERIFIED_WITH_BALANCE_BILLS");

    private String code;
    BillStatus(String code) {
        this.code = code;
    }

    public String code() {
        return this.code;
    }

}
