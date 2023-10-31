package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain;

public class LineItemDiscount extends AbstractDiscount {
    private final boolean perUnit;
    private int applicableUnits;

    public LineItemDiscount(AbstractAmount amount) {
        this(amount, false, 0);
    }

    public LineItemDiscount(AbstractAmount amount, boolean perUnit, int applicableUnits) {
        super(amount);
        this.perUnit = perUnit;
        if (perUnit && applicableUnits < 0) {
            throw new IllegalArgumentException("This LineItemDiscount is perUnit and therefore " +
                    "applicableUnits cannot be negative");
        }
        this.applicableUnits = applicableUnits;
    }

    public LineItemDiscount(AbstractAmount amount, boolean perUnit) {
        this(amount, perUnit, 0);
    }

    public boolean isPerUnit() {
        return perUnit;
    }

    public int getApplicableUnits() {
        return applicableUnits;
    }

    public String toString() {
        return this.getAmount() + (this.isPerUnit() ? " applicableUnits:" + this.getApplicableUnits() : "");
    }
}
