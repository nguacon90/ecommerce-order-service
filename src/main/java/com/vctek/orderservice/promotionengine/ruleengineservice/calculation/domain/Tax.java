package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain;

import java.util.*;

public class Tax {
    private final AbstractAmount amount;
    private List<Taxable> targets = new ArrayList();

    public Tax(AbstractAmount amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Given amount is null!");
        } else {
            this.amount = amount;
        }
    }

    public AbstractAmount getAmount() {
        return this.amount;
    }

    public void addTarget(Taxable target) {
        if (!this.targets.contains(target)) {
            this.targets.add(target);
        }

    }

    public void addTargets(List<? extends Taxable> targets) {
        this.targets = new ArrayList(targets);
    }

    public void addTargets(Taxable... targets) {
        this.addTargets(Arrays.asList(targets));
    }

    public Collection<Taxable> getTargets() {
        return Collections.unmodifiableList(this.targets);
    }

    public void removeTarget(Taxable target) {
        if (!this.targets.remove(target)) {
            throw new IllegalArgumentException("Tax target " + target + " doesnt belong to tax " + this + " - cannot remove.");
        }
    }

    public void clearTargets() {
        this.targets.clear();
    }

    public String toString() {
        return "Tax:" + this.amount.toString() + " Items:" + this.targets;
    }
}
