package com.vctek.orderservice.promotionengine.ruleengine.enums;

public enum KIESessionType {
    STATEFUL, STATELESS;

    public static KIESessionType getValueOf(String type) {
        if(KIESessionType.STATEFUL.toString().equalsIgnoreCase(type)) {
            return KIESessionType.STATEFUL;
        }

        return KIESessionType.STATELESS;
    }
}
