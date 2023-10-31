package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Currency {
    public static final int DEFAULT_DIGITS = 2;
    private final String isocodelowercase;
    private final String isocode;
    private final int digits;
    private static final ConcurrentMap<String, Currency> commonCurrencyCache = new ConcurrentHashMap();
    public Currency(String isocode) {
        this(isocode, DEFAULT_DIGITS);
    }
    public Currency(String isocode, int digits) {
        if (StringUtils.isEmpty(isocode)) {
            throw new IllegalArgumentException("Iso code cannot be empty");
        } else if (digits < 0) {
            throw new IllegalArgumentException("Digits cannot be less than zero");
        } else {
            this.isocodelowercase = isocode.toLowerCase();
            this.isocode = isocode;
            this.digits = digits;
        }
    }

    public static Currency valueOf(String code, int digits) {
        String key = code.toLowerCase() + digits;
        Currency ret = commonCurrencyCache.get(key);
        if (ret == null) {
            ret = new Currency(code, digits);
            Currency previous = commonCurrencyCache.putIfAbsent(key, ret);
            if (previous != null) {
                ret = previous;
            }
        }

        return ret;
    }

    public boolean equals(Object obj) {
        return obj instanceof Currency && this.digits == ((Currency)obj).getDigits() &&
                this.isocodelowercase.equalsIgnoreCase(((Currency)obj).getIsoCode());
    }

    public int hashCode() {
        return this.isocodelowercase.hashCode() * (this.digits + 1);
    }

    public String toString() {
        return this.isocode;
    }

    public String getIsoCode() {
        return this.isocode;
    }

    public int getDigits() {
        return this.digits;
    }
}
