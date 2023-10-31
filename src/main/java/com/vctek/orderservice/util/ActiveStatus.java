package com.vctek.orderservice.util;

public enum ActiveStatus {
    ACTIVE, INACTIVE;

    public static ActiveStatus findByCode(String code) {
        for(ActiveStatus activeStatus : ActiveStatus.values()) {
            if(activeStatus.toString().equals(code)) {
                return activeStatus;
            }
        }

        return null;
    }
}
