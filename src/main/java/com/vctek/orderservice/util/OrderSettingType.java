package com.vctek.orderservice.util;

public enum OrderSettingType {
    COMBO_PRICE_SETTING("COMBO_PRICE_SETTING", "Cài đặt giá combo"),
    MAXIMUM_DISCOUNT_SETTING("MAXIMUM_DISCOUNT_SETTING", "Cài đặt chiết khấu tối đa sản phẩm"),
    CREATE_NOTIFICATION_CHANGE_ORDER_STATUS("CREATE_NOTIFICATION_CHANGE_ORDER_STATUS", "Cài đặt tạo thông báo cho khách khi chuyển trạng thái đơn online");
    private String code;
    private String description;

    OrderSettingType(String code, String description) {
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
