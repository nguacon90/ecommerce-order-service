package com.vctek.orderservice.util;

public enum ProductDType {
    PRODUCT_MODEL("ProductModel"),
    VARIANT_PRODUCT_MODEL("VariantProductModel"),
    COMBO_MODEL("ComboModel");

    public String code() {
        return code;
    }

    private String code;
    ProductDType(String code) {
        this.code = code;
    }

    public static ProductDType findByCode(String code) {
        ProductDType[] var1 = values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            ProductDType productDType = var1[var3];
            if (productDType.code.equals(code)) {
                return productDType;
            }
        }

        return null;
    }
}
