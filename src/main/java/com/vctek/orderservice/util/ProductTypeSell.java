package com.vctek.orderservice.util;

public enum ProductTypeSell {
    SELLING, STOP_SELLING, NEW;

    public static ProductTypeSell getByName(String name) {
        for(ProductTypeSell typeSell : ProductTypeSell.values()) {
            if(typeSell.toString().equals(name)) {
                return typeSell;
            }
        }

        return null;
    }
}
