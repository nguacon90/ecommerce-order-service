package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain;

import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.CurrenciesAreNotEqualException;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.MissingCalculationDataException;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.strategy.impl.CalculationStrategies;

import java.math.BigDecimal;
import java.util.*;

public class Order {
    public static final String DOES_NOT_BELONG_TO_ORDER = " does not belong to order ";
    private Currency currency;
    private static final Money.MoneyExtractor<LineItem> LINE_ITEM_TOTAL_EXTRACTOR = lineItem -> lineItem.getTotal(null);
    private final boolean netMode;
    private final CalculationStrategies calculationStrategies;
    private final List<LineItem> lineItems;
    private final List<Tax> taxes;
    private final List<OrderDiscount> discounts;
    private final List<OrderCharge> charges;

    public Order(Currency currency, CalculationStrategies calculationStrategies) {
        this(currency, false, calculationStrategies);
    }

    public Order(Currency currency, boolean isNet, CalculationStrategies calculationStrategies) {
        this.currency = currency;
        this.netMode = isNet;
        this.calculationStrategies = calculationStrategies;
        this.lineItems = new ArrayList();
        this.taxes = new ArrayList();
        this.discounts = new ArrayList();
        this.charges = new ArrayList();
    }

    public void addDiscounts(List<OrderDiscount> discounts) {
        Iterator var3 = discounts.iterator();

        while (var3.hasNext()) {
            OrderDiscount d = (OrderDiscount) var3.next();
            if (!this.discounts.contains(d)) {
                this.discounts.add(d);
            }
        }

    }

    public void addLineItem(LineItem lineitem) {
        this.addLineItem(this.lineItems.size(), lineitem);
    }

    public void addLineItem(int index, LineItem lineitem) {
        lineitem.getBasePrice().assertCurrenciesAreEqual(this.getCurrency());
        lineitem.setOrder(this);
        this.lineItems.add(index, lineitem);
    }

    public void addLineItems(LineItem... lineItems) {
        this.addLineItems(Arrays.asList(lineItems));
    }

    public void addLineItems(List<LineItem> lineItems) {
        Iterator var3 = lineItems.iterator();

        while (var3.hasNext()) {
            LineItem lineItem = (LineItem) var3.next();
            if (!this.lineItems.contains(lineItem)) {
                lineItem.setOrder(this);
                this.lineItems.add(lineItem);
            }
        }

    }

    public void clearLineItems() {
        Iterator var2 = this.lineItems.iterator();

        while(var2.hasNext()) {
            LineItem lineitem = (LineItem)var2.next();
            lineitem.setOrder(null);
        }

        this.lineItems.clear();
    }

    public void removeLineItem(LineItem lineitem) {
        if (this.lineItems.remove(lineitem)) {
            lineitem.setOrder(null);
        } else {
            throw new IllegalArgumentException("Line item " + lineitem + DOES_NOT_BELONG_TO_ORDER + this + " - cannot remove it.");
        }
    }

    public void addTaxes(Tax... taxes) {
        this.addTaxes(Arrays.asList(taxes));
    }

    public void addTaxes(Collection<Tax> taxes) {
        Iterator var3 = taxes.iterator();

        while(var3.hasNext()) {
            Tax tax = (Tax)var3.next();
            if (!this.taxes.contains(tax)) {
                if (tax.getAmount() instanceof Money) {
                    ((Money)tax.getAmount()).assertCurrenciesAreEqual(this.getCurrency());
                }

                this.taxes.add(tax);
            }
        }

    }

    public void addTax(Tax tax) {
        if (!this.taxes.contains(tax)) {
            if (tax.getAmount() instanceof Money) {
                ((Money)tax.getAmount()).assertCurrenciesAreEqual(this.getCurrency());
            }

            this.taxes.add(tax);
        }

    }

    public void clearTaxes() {
        this.taxes.clear();
    }

    public void removeTax(Tax tax) {
        if (!this.taxes.remove(tax)) {
            throw new IllegalArgumentException("Tax " + tax + DOES_NOT_BELONG_TO_ORDER + this + " - cannot remove.");
        }
    }

    public List<OrderDiscount> getDiscounts() {
        return Collections.unmodifiableList(this.discounts);
    }

    public Money getTotal() {
        return this.getSubTotal().subtract(this.getTotalDiscount()).add(this.getTotalCharge());
    }

    public Money getSubTotal() {
        return this.hasLineItems() ? Money.sum(this.getLineItems(), LINE_ITEM_TOTAL_EXTRACTOR) :
                new Money(BigDecimal.ZERO, currency);
    }

    public List<LineItem> getLineItems() {
        return Collections.unmodifiableList(this.lineItems);
    }

    public Money getTotalCharge() {
        Map<OrderCharge, Money> map = this.getTotalCharges();
        return map.isEmpty() ? new Money(this.getCurrency()) : Money.sum(map.values());
    }

    public boolean hasLineItems() {
        return !this.lineItems.isEmpty();
    }

    public Money getTotalDiscount() {
        Map<OrderDiscount, Money> orderDiscountValues = this.getTotalDiscounts();
        return orderDiscountValues.isEmpty() ? new Money(this.getCurrency()) :
                Money.sum(orderDiscountValues.values());
    }

    public Map<OrderCharge, Money> getTotalCharges() {
        if (this.charges.isEmpty()) {
            return Collections.emptyMap();
        } else {
            Map<OrderCharge, Money> resultmap = new LinkedHashMap(this.charges.size());
            Money currentValue = this.getSubTotal();
            Iterator var5 = this.charges.iterator();

            while(var5.hasNext()) {
                OrderCharge orderCharge = (OrderCharge)var5.next();
                Money calculatedAddCharge = this.calculateOrderCharge(currentValue, orderCharge);
                currentValue = currentValue.add(calculatedAddCharge);
                resultmap.put(orderCharge, calculatedAddCharge);
            }

            return resultmap;
        }
    }

    public Map<OrderDiscount, Money> getTotalDiscounts() {
        if (this.discounts.isEmpty()) {
            return Collections.emptyMap();
        } else {
            Map<OrderDiscount, Money> resultmap = new LinkedHashMap(this.discounts.size());
            Money currentValue = this.getSubTotal();
            Iterator var5 = this.discounts.iterator();

            while (var5.hasNext()) {
                OrderDiscount orderDisc = (OrderDiscount) var5.next();
                Money calculatedDiscount = this.calculateOrderDiscount(currentValue, orderDisc);
                currentValue = currentValue.subtract(calculatedDiscount);
                resultmap.put(orderDisc, calculatedDiscount);
            }

            return resultmap;
        }
    }

    protected Money calculateOrderCharge(Money currentValue, OrderCharge addCharge) {
        if (addCharge.isDisabled()) {
            return new Money(this.getCurrency());
        } else if (addCharge.getAmount() instanceof Money) {
            return (Money)addCharge.getAmount();
        } else if (addCharge.getAmount() instanceof Percentage) {
            Percentage percent = (Percentage)addCharge.getAmount();
            return this.getCalculationStrategies().getRoundingStrategy().multiply(currentValue, percent.getRate().movePointLeft(2));
        } else {
            throw new MissingCalculationDataException("Could not calculate order charge for Order");
        }
    }

    protected Money calculateOrderDiscount(Money currentValue, OrderDiscount discount) {
        if (discount.getAmount() instanceof Money) {
            return (Money) discount.getAmount();
        } else if (discount.getAmount() instanceof Percentage) {
            Percentage percent = (Percentage) discount.getAmount();
            return this.getCalculationStrategies().getRoundingStrategy().multiply(currentValue, percent.getRate().movePointLeft(2));
        } else {
            throw new MissingCalculationDataException("Could not calculate order discount for Order");
        }
    }

    public Currency getCurrency() {
        return this.currency;
    }

    public CalculationStrategies getCalculationStrategies() {
        return calculationStrategies;
    }

    public void addDiscount(OrderDiscount discount) {
        this.addDiscount(this.discounts.size(), discount);
    }

    public void addDiscount(int index, OrderDiscount discount) {
        if (!this.discounts.contains(discount)) {
            if (discount.getAmount() instanceof Money) {
                ((Money)discount.getAmount()).assertCurrenciesAreEqual(this.getCurrency());
            }

            this.discounts.add(index, discount);
        }
    }

    public void clearDiscounts() {
        this.discounts.clear();
    }

    public void assertCurreniesAreEqual(Currency curr) {
        if (curr == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }

        if (!this.getCurrency().equals(curr)) {
            throw new CurrenciesAreNotEqualException("The Currencies are not the same. "
                    + this.getCurrency() + " <-> " + curr);
        }
    }

    public void removeDiscount(OrderDiscount orderDiscount) {
        if (!this.discounts.remove(orderDiscount)) {
            throw new IllegalArgumentException("Discount " + orderDiscount +
                    DOES_NOT_BELONG_TO_ORDER + this + " - cannot remove");
        }
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public boolean isNetMode() {
        return netMode;
    }

    public Collection<Tax> getTaxes() {
        return Collections.unmodifiableList(this.taxes);
    }

    public List<OrderCharge> getCharges() {
        return charges;
    }

    public Money getTotalIncludingTaxes() {
        return this.isNet() ? this.getTotal().add(this.getTotalTax()) : this.getTotal();
    }

    public boolean isNet() {
        return this.netMode;
    }

    public Money getTotalTax() {
        Map<Tax, Money> taxValues = this.getTotalTaxes();
        return taxValues.isEmpty() ? Money.zero(this.getCurrency()) : Money.sum(taxValues.values());
    }

    public Map<Tax, Money> getTotalTaxes() {
        Map<Tax, Money> result = new LinkedHashMap(this.taxes.size());
        double taxCorrectionFactor = this.getAutomaticTaxCorrectionFactor();
        Iterator var5 = this.getTaxes().iterator();

        while(var5.hasNext()) {
            Tax tax = (Tax)var5.next();
            result.put(tax, this.calculateTaxTotal(tax, taxCorrectionFactor));
        }

        return result;
    }

    protected double getAutomaticTaxCorrectionFactor() {
        if (BigDecimal.ZERO.compareTo(this.getSubTotal().getAmount()) == 0) {
            return 1.0D;
        } else {
            BigDecimal totalWoFixedTaxedCharges = this.getTotal().subtract(this.getFixedTaxedAdditionalCharges(this.getTotalCharges())).getAmount();
            return totalWoFixedTaxedCharges.doubleValue() / this.getSubTotal().getAmount().doubleValue();
        }
    }

    protected Money getFixedTaxedAdditionalCharges(Map<OrderCharge, Money> aocValues) {
        Money sum = new Money(this.getCurrency());
        Iterator var4 = aocValues.entrySet().iterator();

        while(var4.hasNext()) {
            Map.Entry<OrderCharge, Money> e = (Map.Entry)var4.next();
            if (this.hasAssignedTaxes(e.getKey())) {
                sum = sum.add(e.getValue());
            }
        }

        return sum;
    }

    protected Money calculateTaxTotal(Tax tax, double autoTaxCorrectionFactor) {
        if (tax.getAmount() instanceof Money) {
            return (Money)tax.getAmount();
        } else if (!(tax.getAmount() instanceof Percentage)) {
            throw new IllegalStateException();
        } else {
            Money taxedChargeSum = Money.zero(this.getCurrency());

            Taxable taxcharge;
            for(Iterator var6 = tax.getTargets().iterator(); var6.hasNext(); taxedChargeSum = taxedChargeSum.add(taxcharge.getTotal(this))) {
                taxcharge = (Taxable)var6.next();
            }

            BigDecimal taxRate = ((Percentage)tax.getAmount()).getRate();
            BigDecimal costRate = this.isNet() ? new BigDecimal(100) : taxRate.add(new BigDecimal(100));
            double overallFactor = taxRate.doubleValue() * autoTaxCorrectionFactor / costRate.doubleValue();
            return this.getCalculationStrategies().getTaxRoundingStrategry().multiply(taxedChargeSum, BigDecimal.valueOf(overallFactor));
        }
    }

    public boolean hasAssignedTaxes(Taxable object) {
        Iterator var3 = this.getTaxes().iterator();

        while(var3.hasNext()) {
            Tax t = (Tax)var3.next();
            if (t.getTargets().contains(object)) {
                return true;
            }
        }

        return false;
    }

    public Money getTotalChargeOfType(final ChargeType chargeType) {
        final Money zero = Money.zero(this.getCurrency());
        return this.hasCharges() ? Money.sum(this.getTotalCharges().entrySet(), moneyEntry ->
                chargeType.equals(moneyEntry.getKey().getChargeType()) ? moneyEntry.getValue() : zero) : zero;
    }

    public boolean hasCharges() {
        return !this.charges.isEmpty();
    }

    public Collection<Tax> getTaxesFor(Taxable object) {
        Collection<Tax> ret = null;
        Iterator var4 = this.getTaxes().iterator();

        while(var4.hasNext()) {
            Tax t = (Tax)var4.next();
            if (t.getTargets().contains(object)) {
                if (ret == null) {
                    ret = new LinkedHashSet();
                }

                ret.add(t);
            }
        }

        return (Collection)(ret == null ? Collections.emptySet() : ret);
    }

    public void addCharges(OrderCharge... charges) {
        this.addCharges(Arrays.asList(charges));
    }

    public void addCharges(List<OrderCharge> charges) {
        Iterator var3 = charges.iterator();

        while(var3.hasNext()) {
            OrderCharge aoc = (OrderCharge)var3.next();
            if (!this.charges.contains(aoc)) {
                if (aoc.getAmount() instanceof Money) {
                    ((Money)aoc.getAmount()).assertCurrenciesAreEqual(this.getCurrency());
                }

                this.charges.add(aoc);
            }
        }

    }

    public void addCharge(OrderCharge aoc) {
        this.addCharge(this.charges.size(), aoc);
    }

    public void addCharge(int index, OrderCharge aoc) {
        if (!this.charges.contains(aoc)) {
            if (aoc.getAmount() instanceof Money) {
                ((Money)aoc.getAmount()).assertCurrenciesAreEqual(this.getCurrency());
            }

            this.charges.add(index, aoc);
        }
    }

    public void clearCharges() {
        this.charges.clear();
    }

    public void removeCharge(OrderCharge aoc) {
        if (!this.charges.remove(aoc)) {
            throw new IllegalArgumentException("Charge " + aoc + DOES_NOT_BELONG_TO_ORDER + this + " - cannot remove.");
        }
    }
}
