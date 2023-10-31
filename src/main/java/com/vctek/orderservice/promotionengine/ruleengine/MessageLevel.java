package com.vctek.orderservice.promotionengine.ruleengine;

public enum MessageLevel {
    ERROR, WARNING, INFO;

    public static MessageLevel get(String level) {
        if(MessageLevel.ERROR.toString().equalsIgnoreCase(level)) {
            return MessageLevel.ERROR;
        }

        if(MessageLevel.WARNING.toString().equalsIgnoreCase(level)) {
            return MessageLevel.WARNING;
        }

        return MessageLevel.INFO;
    }
}
