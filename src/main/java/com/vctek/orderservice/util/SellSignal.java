package com.vctek.orderservice.util;

public enum SellSignal {
    WEB, MOBILE, EXTENSION, ECOMMERCE_WEB;

    public static SellSignal findByName(String signal) {
        for(SellSignal sellSignal : SellSignal.values()) {
            if(sellSignal.toString().equalsIgnoreCase(signal)) {
                return sellSignal;
            }
        }

        return null;
    }
}
