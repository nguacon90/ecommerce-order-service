package com.vctek.orderservice.event;

public class AbstractApplicationEvent {
    private ReturnOrderEventType eventType;

    public AbstractApplicationEvent(ReturnOrderEventType eventType) {
        this.eventType = eventType;
    }

    public ReturnOrderEventType getEventType() {
        return eventType;
    }
}
