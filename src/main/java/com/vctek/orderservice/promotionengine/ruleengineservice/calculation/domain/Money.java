package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain;

import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.AmountException;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.CurrenciesAreNotEqualException;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Money extends AbstractAmount {
    private static final ConcurrentMap<String, Money> commonMoneyCacheMap = new ConcurrentHashMap();
    public static final String CURRENCY_CANNOT_BE_NULL = "Currency cannot be null";
    public static final String CANNOT_SUM_NOTHING = "Cannot sum nothing";
    private final Currency currency;
    private final BigDecimal amount;

    public Money(BigDecimal amount, Currency currency) {
        if (currency == null) {
            throw new IllegalArgumentException(CURRENCY_CANNOT_BE_NULL);
        } else {
            this.amount = amount.setScale(currency.getDigits(), 1);
            this.currency = currency;
        }
    }

    public Money(Currency currency) {
        if (currency == null) {
            throw new IllegalArgumentException(CURRENCY_CANNOT_BE_NULL);
        } else {
            this.amount = BigDecimal.ZERO;
            this.currency = currency;
        }
    }
    public Money(long amountInSmallestPieces, Currency currency) {
        if (currency == null) {
            throw new IllegalArgumentException(CURRENCY_CANNOT_BE_NULL);
        } else {
            this.amount = new BigDecimal(BigInteger.valueOf(amountInSmallestPieces), currency.getDigits());
            this.currency = currency;
        }
    }

    public Money(String amount, Currency currency) {
        this(new BigDecimal(amount), currency);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Money subtract(Money money) {
        this.assertCurrenciesAreEqual(money);
        return new Money(this.getAmount().subtract(money.getAmount()), this.currency);
    }

    public void assertCurrenciesAreEqual(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Money cannot be null");
        } else if (!this.getCurrency().equals(other.getCurrency())) {
            throw new CurrenciesAreNotEqualException("The Currencies are not the same. "
                    + this.getCurrency() + " <-> " + other.getCurrency());
        }
    }

    public void assertCurrenciesAreEqual(Currency curr) {
        if (curr == null) {
            throw new IllegalArgumentException(CURRENCY_CANNOT_BE_NULL);
        } else if (!this.getCurrency().equals(curr)) {
            throw new CurrenciesAreNotEqualException("The Currencies are not the same. " + this.getCurrency() + " <-> " + curr);
        }
    }

    public Currency getCurrency() {
        return this.currency;
    }

    public static final Money sum(Money... money) {
        if (money != null && money.length != 0) {
            return sum(Arrays.asList(money));
        } else {
            throw new AmountException(CANNOT_SUM_NOTHING);
        }
    }

    public static final Money sum(Collection<Money> elements) {
        return new Money(sumUnscaled(elements), elements.iterator().next().getCurrency());
    }

    public static final <T> Money sum(Collection<T> elements, Money.MoneyExtractor<T> extractor) {
        if (CollectionUtils.isEmpty(elements)) {
            throw new AmountException(CANNOT_SUM_NOTHING);
        } else {
            return sumUnscaled(elements, extractor);
        }
    }

    protected static final long sumUnscaled(Collection<Money> elements) {
        if (elements != null && !elements.isEmpty()) {
            long res = 0L;
            Currency curr = null;

            Money x;
            for(Iterator var5 = elements.iterator(); var5.hasNext(); res += x.getAmount().unscaledValue().longValue()) {
                x = (Money)var5.next();
                if (curr == null) {
                    curr = x.getCurrency();
                } else if (!curr.equals(x.getCurrency())) {
                    throw new CurrenciesAreNotEqualException("Cannot sum up Money with different currencies");
                }
            }

            return res;
        } else {
            throw new AmountException(CANNOT_SUM_NOTHING);
        }
    }

    protected static final <T> Money sumUnscaled(Collection<T> elements, Money.MoneyExtractor<T> extractor) {
        if (elements != null && !elements.isEmpty()) {
            long res = 0L;
            Currency curr = null;

            Money money;
            for(Iterator var6 = elements.iterator(); var6.hasNext(); res += money.getAmount().unscaledValue().longValue()) {
                T t = (T)var6.next();
                money = extractor.extractMoney(t);
                if (curr == null) {
                    curr = money.getCurrency();
                } else if (!curr.equals(money.getCurrency())) {
                    throw new CurrenciesAreNotEqualException("Cannot sum up Money with different currencies");
                }
            }

            return new Money(res, curr);
        } else {
            throw new AmountException(CANNOT_SUM_NOTHING);
        }
    }

    public static Money zero(Currency curr) {
        String cacheKey = "Z" + curr.getIsoCode().toLowerCase() + curr.getDigits();
        Money ret = commonMoneyCacheMap.get(cacheKey);
        if (ret == null) {
            ret = new Money(curr);
            Money previous = commonMoneyCacheMap.putIfAbsent(cacheKey, ret);
            if (previous != null) {
                ret = previous;
            }
        }

        return ret;
    }

    public Money add(Money money) {
        this.assertCurrenciesAreEqual(money);
        return new Money(this.amount.add(money.getAmount()), this.currency);
    }

    public interface MoneyExtractor<T> {
        Money extractMoney(T var1);
    }

    public int hashCode() {
        return this.currency.hashCode() + this.amount.hashCode() + super.hashCode();
    }

    public boolean equals(Object obj) {
        return obj instanceof Money && ((Money)obj).getCurrency().equals(this.currency) &&
                ((Money)obj).getAmount().compareTo(this.amount) == 0;
    }

    public String toString() {
        return this.amount.toPlainString() + " " + this.currency.toString();
    }
}
