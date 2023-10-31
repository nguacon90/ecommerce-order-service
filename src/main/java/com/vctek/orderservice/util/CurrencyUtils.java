package com.vctek.orderservice.util;

public final class CurrencyUtils {

    public static final int PERCENT_100 = 100;

    private CurrencyUtils() {
        //NOSONAR
    }

    public static double computeValue(Double value, String type, Double amount) {
        if (value == null) {
            return 0d;
        }

        if (CurrencyType.CASH.toString().equals(type)) {
            return value.doubleValue() > amount && amount > 0 ? amount : value.doubleValue();
        }

        if (value >= PERCENT_100) {
            return amount;
        }

        return amount * value / PERCENT_100;
    }
}
