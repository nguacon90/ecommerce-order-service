package com.vctek.orderservice.util;

public enum TrackingHistoryOrderType {
    DISCOUNT("discount", "Thay đổi chiết khấu đơn"),
    VAT("vat", "Thay đổi VAT đơn hàng"),
    DELIVERY_COST("deliveryCost", "Thay đổi phí vận chuyển"),
    COMPANY_SHIPPING_FEE("companyShippingFee", "Thay đổi phí vận chuyển doanh nghiệp"),
    COLLABORATOR_SHIPPING_FEE("collaboratorShippingFee", "Thay đổi phí vận chuyển cộng tác viên"),
    ORDER_SOURCE("orderSourceId", "Thay đổi kênh bán hàng"),
    CUSTOMER_NOTE("customerNote", "Thay đổi ghi chú khách hàng"),
    CUSTOMER_SUPPORT_NOTE("customerSupportNote", "Thay đổi ghi chú - CSKH"),
    DELIVERY_DATE("deliveryDate", "Thay đổi ngày giao hàng dự kiến"),
    SHIPPING_COMPANY_ID("shippingCompanyId", "Thay đổi đơn vị vận chuyển"),
    CUSTOMER_ID("customerId", "Thay đổi khách hàng"),
    SHIPPING_ADDRESS("shippingAddress", "Thay đổi địa chỉ giao hàng"),
    ADD_PRODUCT("addProduct", "Thêm sản phẩm"),
    DELETED_PRODUCT("deletedProduct", "Xóa sản phẩm"),
    CHANGE_QUANTITY_PRODUCT("changeQuantityProduct", "Cập nhật số lượng sản phẩm"),
    CHANGE_PRICE_PRODUCT("changePriceProduct", "Cập nhật giá bán sản phẩm"),
    CHANGE_DISCOUNT_PRODUCT("changeDiscountProduct", "Cập nhật chiết khấu sản phẩm"),
    ;

    private String code;
    private String description;
    TrackingHistoryOrderType(String code, String description) {
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
