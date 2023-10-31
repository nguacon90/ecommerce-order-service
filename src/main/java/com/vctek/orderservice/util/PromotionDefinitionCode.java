package com.vctek.orderservice.util;

public enum PromotionDefinitionCode {
    ORDER_TYPES("vctek_order_types"), WAREHOUSE("vctek_warehouses"), PRICE_TYPES("vctek_price_types"),
    ORDER_TOTAL("vctek_order_total"), QUALIFIER_COUPONS("vctek_qualifying_coupons"),
    QUALIFIER_PRODUCTS("vctek_qualifying_products"),
    GROUP("vctek_group"), CONTAINER("vctek_container"),
    VCTEK_PRODUCT_PRICE_THRESHOLD("vctek_product_price_threshold"),
    PARTNER_ORDER_PERCENTAGE_DISCOUNT_ACTION("vctek_partner_order_entry_percentage_discount"),
    QUALIFIER_CATEGORIES("vctek_qualifying_categories"),
    ORDER_ENTRY_FIXED_DISCOUNT_ACTION("vctek_order_entry_fixed_discount"),
    ORDER_PERCENTAGE_DISCOUNT_ACTION("vctek_order_percentage_discount"),
    ORDER_FIXED_DISCOUNT_ACTION("vctek_order_fixed_discount"),
    VCTEK_FREE_GIFT_ACTION("vctek_free_gift"),
    EXCLUDE_ORDER_SOURCES("vctek_exclude_order_sources"),
    VCTEK_TARGET_CUSTOMERS("vctek_target_customers"),
    VCTEK_EMPLOYEE_ORDER_DISCOUNT_ACTION("vctek_employee_order_discount"),
    VCTEK_BUDGET_PER_CUSTOMER("vctek_budget_per_customer");
    private String code;
    PromotionDefinitionCode(String code) {
        this.code = code;
    }

    public String code() {
        return this.code;
    }

}
