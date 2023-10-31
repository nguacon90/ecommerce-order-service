package com.vctek.orderservice.util;

public enum ConditionDefinitionParameter {
    VALUE("value"),
    OPERATOR("operator"),
    SCHEDULE("schedule"),
    QUALIFYING_CONTAINERS("qualifying_containers"),
    TARGET_CONTAINERS("target_containers"),
    QUALIFYING_PRODUCTS("products"),
    FREE_PRODUCT("product"),
    QUANTITY("quantity"),
    CUSTOMER_GROUPS("customer_groups"),
    QUALIFYING_CATEGORIES("categories");

    private String code;
    ConditionDefinitionParameter(String code) {
        this.code = code;
    }

    public String code() {
        return this.code;
    }
}
