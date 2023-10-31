package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain;

import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.AmountException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class Percentage extends AbstractAmount {
    public static final Percentage ZERO = new Percentage(0);
    public static final Percentage TEN = new Percentage(10);
    public static final Percentage TWENTY = new Percentage(20);
    public static final Percentage TWENTYFIVE = new Percentage(25);
    public static final Percentage THIRTY = new Percentage(30);
    public static final Percentage FOURTY = new Percentage(40);
    public static final Percentage FIFTY = new Percentage(50);
    public static final Percentage SIXTY = new Percentage(60);
    public static final Percentage SEVENTY = new Percentage(70);
    public static final Percentage SEVENTYFIVE = new Percentage(75);
    public static final Percentage EIGHTY = new Percentage(80);
    public static final Percentage NINETY = new Percentage(90);
    public static final Percentage HUNDRED = new Percentage(100);
    private final BigDecimal rate;

    public Percentage(String rate) {
        if (rate == null) {
            throw new IllegalArgumentException("Parameter 'rate' is null!");
        } else {
            this.rate = new BigDecimal(rate);
        }
    }
    public Percentage(int rate) {
        this.rate = new BigDecimal(rate);
    }

    public Percentage(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public Percentage add(Percentage percentage) {
        return new Percentage(this.rate.add(percentage.getRate()));
    }

    public Percentage subtract(Percentage percentage) {
        return new Percentage(this.rate.subtract(percentage.getRate()));
    }

    public int hashCode() {
        return this.rate.hashCode();
    }

    public boolean equals(Object obj) {
        return obj instanceof Percentage && ((Percentage)obj).getRate().compareTo(this.rate) == 0;
    }

    public String toString() {
        return this.rate + "%";
    }

    public static final Percentage sum(Percentage... percent) {
        if (percent != null && percent.length != 0) {
            return sum((Collection)Arrays.asList(percent));
        } else {
            throw new AmountException("Cannot sum nothing");
        }
    }

    public static final Percentage sum(Collection<Percentage> elements) {
        if (elements == null) {
            throw new IllegalArgumentException("Cannot sum null");
        } else {
            BigDecimal res = BigDecimal.ZERO;

            Percentage x;
            for(Iterator var3 = elements.iterator(); var3.hasNext(); res = res.add(x.getRate())) {
                x = (Percentage)var3.next();
            }

            return new Percentage(res);
        }
    }

}
