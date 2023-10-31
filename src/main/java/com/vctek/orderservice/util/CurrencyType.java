package com.vctek.orderservice.util;

public enum CurrencyType {
    PERCENT("%"), CASH("VND");
    private String code;

    CurrencyType(String code) {
        this.code = code;
    }

    public String code() {
        return this.code;
    }

    public static CurrencyType findByCode(String code) {
        for(CurrencyType currencyType : CurrencyType.values()) {
            if(currencyType.toString().equals(code)) {
                return currencyType;
            }
        }

        return null;
    }

    public static CurrencyType findCurrencyByCode(String code) {
        for(CurrencyType currencyType : CurrencyType.values()) {
            if(currencyType.code().equals(code)) {
                return currencyType;
            }
        }

        return null;
    }
}
