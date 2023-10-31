package com.vctek.orderservice.promotionengine.promotionservice.util;

public enum PromotionCertainty {
    FIRED(1.0F),
    POTENTIAL(0.5F);

    private Float certainty;
    private float epsilon = 1.0E-9F;

    PromotionCertainty(float certainty) {
        this.certainty = certainty;
    }

    public boolean around(Float value) {
        return Math.abs(value - this.certainty) < this.epsilon;
    }

    public Float value() {
        return this.certainty;
    }

}
