package com.vctek.orderservice.util;

public enum  SugarAndIce {
    ZERO_PERCENT("ZERO_PERCENT",0 ),
    THIRTY_PERCENT("THIRTY_PERCENT",30 ),
    FIFTY_PERCENT("FIFTY_PERCENT",50),
    SEVENTY_PERCENT("SEVENTY_PERCENT",70),
    ONEHUNDRED_PERCENT("ONEHUNDRED_PERCENT",100);

    private String code;
    private int value;

    SugarAndIce(String code, int value) {
        this.code = code;
        this.value = value;
    }


    public static SugarAndIce findByValue(int value) {
        SugarAndIce[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            SugarAndIce sugarAndIce = var1[var3];
            if (sugarAndIce.value == value) {
                return sugarAndIce;
            }
        }

        return null;
    }

    public String code() {
        return this.code;
    }

    public int value() {
        return this.value;
    }
}
