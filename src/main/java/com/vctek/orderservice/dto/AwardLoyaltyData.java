package com.vctek.orderservice.dto;

import java.util.HashMap;
import java.util.Map;

public class AwardLoyaltyData {
    private Map<Long, Double> entryPoints = new HashMap<>();
    private Map<Long, Double> toppingPoints = new HashMap<>();

    public void setEntryPoints(Map<Long, Double> entryPoints) {
        this.entryPoints = entryPoints;
    }

    public Map<Long, Double> getEntryPoints() {
        return entryPoints;
    }

    public Map<Long, Double> getToppingPoints() {
        return toppingPoints;
    }

    public void setToppingPoints(Map<Long, Double> toppingPoints) {
        this.toppingPoints = toppingPoints;
    }
}
