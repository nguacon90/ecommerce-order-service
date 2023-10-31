package com.vctek.orderservice.util;

public enum PriceType {
    RETAIL_PRICE, WHOLESALE_PRICE, DISTRIBUTOR_PRICE,
    ;

    public static PriceType findByCode(String code) {
        for (PriceType type : PriceType.values()) {
            if (type.toString().equals(code)) {
                return type;
            }
        }

        return null;
    }
}
