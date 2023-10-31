package com.vctek.orderservice.util;

public enum HistoryOrderType {
    CHANGE_ORDER_STATUS("CHANGE_ORDER_STATUS", "Thay đổi trạng thái đơn hàng"),
    TRACKING_ORDER_UPDATE("TRACKING_ORDER_UPDATE", "Thay đổi thông tin đơn hàng"),
    ;

    private String code;
    private String description;
    HistoryOrderType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String code() {
        return this.code;
    }
    public String description() {
        return this.description;
    }

}
