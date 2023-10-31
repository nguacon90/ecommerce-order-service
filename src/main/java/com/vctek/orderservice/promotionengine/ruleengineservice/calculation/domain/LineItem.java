package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain;


import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.MissingCalculationDataException;

import java.math.BigDecimal;
import java.util.*;

public class LineItem implements Taxable {
    public static final String COULD_NOT_CALCULATE_DISCOUNT_FOR_LINE_ITEM = "Could not calculate discount for LineItem ";
    private Integer entryNumber;
    private int giveAwayCount;
    private int numberOfUnits;
    private final List<LineItemDiscount> discounts;
    private final List<LineItemCharge> charges;
    private Order order;
    private final Money basePrice;
    private Money totalToppingPrice;
    private Money totalToppingFixedDiscount;

    public LineItem(Money basePrice) {
        if (basePrice == null) {
            throw new IllegalArgumentException("The basePrice for the LineItem is null!");
        } else {
            this.basePrice = basePrice;
            this.giveAwayCount = 0;
            this.numberOfUnits = 1;
            this.discounts = new ArrayList();
            this.charges = new ArrayList();
        }
    }

    public LineItem(Money basePrice, int numberOfUnits) {
        this(basePrice);
        this.setNumberOfUnits(numberOfUnits);
    }

    public int getGiveAwayCount() {
        return giveAwayCount;
    }

    public void setGiveAwayCount(int giveAwayCount) {
        this.giveAwayCount = giveAwayCount;
    }

    public int getNumberOfUnits() {
        return numberOfUnits;
    }

    public void setNumberOfUnits(int numberOfUnits) {
        if (numberOfUnits < 0) {
            throw new IllegalArgumentException("The numberOfUnits cannot be negative!");
        } else {
            this.numberOfUnits = numberOfUnits;
        }
    }

    public Order getOrder() {
        if (this.order == null) {
            throw new MissingCalculationDataException("Order for LineItem [" + this + "] was not set.");
        } else {
            return this.order;
        }
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Money getBasePrice() {
        return basePrice;
    }

    public List<LineItemDiscount> getDiscounts() {
        return discounts;
    }

    public List<LineItemCharge> getCharges() {
        return charges;
    }

    public void setEntryNumber(Integer entryNumber) {
        this.entryNumber = entryNumber;
    }

    public Integer getEntryNumber() {
        return entryNumber;
    }

    public Money getTotalToppingPrice() {
        return totalToppingPrice;
    }

    public void setTotalToppingPrice(Money totalToppingPrice) {
        this.totalToppingPrice = totalToppingPrice;
    }

    public Money getTotalToppingFixedDiscount() {
        return totalToppingFixedDiscount;
    }

    public void setTotalToppingFixedDiscount(Money totalToppingFixedDiscount) {
        this.totalToppingFixedDiscount = totalToppingFixedDiscount;
    }

    public void addDiscounts(List<LineItemDiscount> discounts) {
        Iterator var3 = discounts.iterator();

        while(var3.hasNext()) {
            LineItemDiscount prodD = (LineItemDiscount)var3.next();
            if (!this.discounts.contains(prodD)) {
                this.discounts.add(prodD);
            }
        }
    }

    public Money getTotal(Order context) {
        return this.getSubTotal().subtract(this.getTotalDiscount()).add(this.getTotalCharge());
    }

    public Money getTotalCharge() {
        Map<LineItemCharge, Money> chargeValues = this.getTotalCharges();
        return chargeValues.isEmpty() ? Money.zero(this.getOrder().getCurrency()) : Money.sum(chargeValues.values());
    }

    public Money getSubTotal() {
        Money subTotal = this.getOrder().getCalculationStrategies().getRoundingStrategy()
                .multiply(this.getBasePrice(), BigDecimal.valueOf((long) this.getNumberOfUnitsForCalculation()));
        if(this.totalToppingPrice != null) {
            subTotal = subTotal.add(this.totalToppingPrice);
        }
        if(this.totalToppingFixedDiscount != null) {
            subTotal = subTotal.subtract(this.totalToppingFixedDiscount);
        }
        return subTotal;
    }

    public int getNumberOfUnitsForCalculation() {
        return Math.max(0, this.getNumberOfUnits() - this.getGiveAwayUnits());
    }

    public int getGiveAwayUnits() {
        return this.giveAwayCount;
    }


    public Money getTotalDiscount() {
        Map<LineItemDiscount, Money> discountValues = this.getTotalDiscounts();
        return discountValues.isEmpty() ? Money.zero(this.getOrder().getCurrency()) :
                Money.sum(discountValues.values());
    }

    public Map<LineItemDiscount, Money> getTotalDiscounts() {
        if (this.discounts.isEmpty()) {
            return Collections.emptyMap();
        } else {
            Map<LineItemDiscount, Money> resultmap = new LinkedHashMap(this.discounts.size());
            Money currentValue = this.getSubTotal();
            Iterator var5 = this.discounts.iterator();

            while(var5.hasNext()) {
                LineItemDiscount lid = (LineItemDiscount)var5.next();
                Money calculatedDiscount = this.calculateDiscount(currentValue, lid);
                currentValue = currentValue.subtract(calculatedDiscount);
                resultmap.put(lid, calculatedDiscount);
            }

            return resultmap;
        }
    }

    public Map<LineItemCharge, Money> getTotalCharges() {
        if (this.charges.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<LineItemCharge, Money> resultmap = new LinkedHashMap(this.charges.size());
        Money currentValue = this.getSubTotal();
        Iterator var5 = this.charges.iterator();

        while(var5.hasNext()) {
            LineItemCharge lic = (LineItemCharge)var5.next();
            Money calculatedAddCharge = this.calculateCharge(currentValue, lic);
            currentValue = currentValue.add(calculatedAddCharge);
            resultmap.put(lic, calculatedAddCharge);
        }

        return resultmap;
    }

    protected Money calculateCharge(Money currentValue, LineItemCharge charge) {
        Money zeroMoney = Money.zero(this.getBasePrice().getCurrency());
        if (charge.isDisabled()) {
            return zeroMoney;
        } else if (charge.isPerUnit()) {
            int applyForThisItemCount = Math.min(charge.getApplicableUnits(), this.getNumberOfUnits());
            if (charge.getAmount() instanceof Money) {
                return this.getOrder().getCalculationStrategies().getRoundingStrategy().multiply((Money)charge.getAmount(), BigDecimal.valueOf((long)applyForThisItemCount));
            } else if (charge.getAmount() instanceof Percentage) {
                Percentage percent = (Percentage)charge.getAmount();
                return this.getOrder().getCalculationStrategies().getRoundingStrategy().roundToMoney(this.getBasePrice().getAmount().multiply(percent.getRate()).movePointLeft(2).multiply(BigDecimal.valueOf((long)applyForThisItemCount)), this.getOrder().getCurrency());
            } else {
                throw new MissingCalculationDataException(COULD_NOT_CALCULATE_DISCOUNT_FOR_LINE_ITEM + this);
            }
        } else if (charge.getAmount() instanceof Money) {
            return (Money)charge.getAmount();
        } else if (charge.getAmount() instanceof Percentage) {
            return this.getOrder().getCalculationStrategies().getRoundingStrategy().getPercentValue(currentValue, (Percentage)charge.getAmount());
        } else {
            throw new MissingCalculationDataException(COULD_NOT_CALCULATE_DISCOUNT_FOR_LINE_ITEM + this);
        }
    }

    public void clearDiscounts() {
        this.discounts.clear();
    }

    protected Money calculateDiscount(Money currentValue, LineItemDiscount discount) {
        Money zeroMoney = new Money(this.getOrder().getCurrency());
        if (zeroMoney.equals(currentValue)) {
            return zeroMoney;
        }

        if (discount.isPerUnit()) {
            int numberDiscountUnit = Math.min(discount.getApplicableUnits(), this.getNumberOfUnits());
            if (discount.getAmount() instanceof Money) {
                return this.getOrder().getCalculationStrategies().getRoundingStrategy().multiply((Money)discount.getAmount(), BigDecimal.valueOf((long)numberDiscountUnit));
            } else if (discount.getAmount() instanceof Percentage) {
                Percentage percent = (Percentage)discount.getAmount();
                BigDecimal basePriceAmount = this.getBasePrice().getAmount();
                return this.getOrder().getCalculationStrategies().getRoundingStrategy().roundToMoney(basePriceAmount.multiply(percent.getRate()).movePointLeft(2).multiply(BigDecimal.valueOf((long)numberDiscountUnit)), this.getOrder().getCurrency());
            } else {
                throw new MissingCalculationDataException(COULD_NOT_CALCULATE_DISCOUNT_FOR_LINE_ITEM + this);
            }
        }

        if (discount.getAmount() instanceof Money) {
            return (Money)discount.getAmount();
        }

        if (discount.getAmount() instanceof Percentage) {
            return this.getOrder().getCalculationStrategies().getRoundingStrategy().getPercentValue(currentValue, (Percentage)discount.getAmount());
        } else {
            throw new MissingCalculationDataException(COULD_NOT_CALCULATE_DISCOUNT_FOR_LINE_ITEM + this);
        }
    }

    public void addDiscount(LineItemDiscount discount) {
        this.addDiscount(this.discounts.size(), discount);
    }

    public void addDiscount(int index, LineItemDiscount discount) {
        if (!this.discounts.contains(discount)) {
            this.assertCurrency(discount.getAmount());
            this.discounts.add(index, discount);
        }

    }

    public void addCharges(LineItemCharge... lineItemCharges) {
        this.addCharges(Arrays.asList(lineItemCharges));
    }

    public void addCharges(List<LineItemCharge> lineItemCharges) {
        Iterator var3 = lineItemCharges.iterator();

        while(var3.hasNext()) {
            LineItemCharge apc = (LineItemCharge)var3.next();
            if (!this.charges.contains(apc)) {
                this.assertCurrency(apc.getAmount());
                this.charges.add(apc);
            }
        }

    }

    public void addCharge(LineItemCharge lineItemCharge) {
        this.addCharge(this.charges.size(), lineItemCharge);
    }

    public void addCharge(int index, LineItemCharge lineItemCharge) {
        if (!this.charges.contains(lineItemCharge)) {
            this.assertCurrency(lineItemCharge.getAmount());
            this.charges.add(index, lineItemCharge);
        }

    }

    public void clearCharges() {
        this.charges.clear();
    }

    protected void assertCurrency(AbstractAmount amount) {
        if (amount instanceof Money) {
            ((Money)amount).assertCurrenciesAreEqual(this.getBasePrice().getCurrency());
        }

    }

    public void removeCharge(LineItemCharge charge) {
        if (!this.charges.remove(charge)) {
            throw new IllegalArgumentException("Charge " + charge + " doesnt belong to line item " + this + " - cannot remove.");
        }
    }

    public void removeDiscount(LineItemDiscount discount) {
        if (!this.discounts.remove(discount)) {
            throw new IllegalArgumentException("Discount " + discount
                    + " doesnt belong to line item " + this + " - cannot remove.");
        }
    }

    public final void setGiveAwayUnits(int giveAwayCount) {
        if (giveAwayCount < 0) {
            throw new IllegalArgumentException("The give away count cannot be negative");
        } else {
            this.giveAwayCount = giveAwayCount;
        }
    }

    public String toString() {
        return this.getNumberOfUnits() + "x " + this.getBasePrice() + (this.getGiveAwayUnits() > 0 ?
                "(free:" + this.getGiveAwayUnits() + ")" : "") +
                (this.discounts.isEmpty() ? "" : " discounts:" + this.discounts) + (this.charges.isEmpty() ? ""
                : " charges:" + this.charges);
    }
}
