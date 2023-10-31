package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.vctek.converter.Converter;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.MinimumAmountValidationStrategy;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.RuleEngineCalculationService;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.Currency;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.*;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.*;
import com.vctek.orderservice.promotionengine.ruleengineservice.util.RaoUtils;
import com.vctek.redis.ProductData;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DefaultRuleEngineCalculationService implements RuleEngineCalculationService {
    public static final String AMOUNT_MUST_NOT_BE_NULL = "amount must not be null";
    private Converter<AbstractOrderRAO, Order> abstractOrderRaoToOrderConverter;
    private Converter<ProductData, ProductRAO> productRAOConverter;
    private RaoUtils raoUtils;
    private MinimumAmountValidationStrategy minimumAmountValidationStrategy;

    public DefaultRuleEngineCalculationService(Converter<AbstractOrderRAO, Order> abstractOrderRaoToOrderConverter,
                                               RaoUtils raoUtils, MinimumAmountValidationStrategy minimumAmountValidationStrategy) {
        this.abstractOrderRaoToOrderConverter = abstractOrderRaoToOrderConverter;
        this.raoUtils = raoUtils;
        this.minimumAmountValidationStrategy = minimumAmountValidationStrategy;
    }

    @Override
    public void calculateTotals(AbstractOrderRAO rao) {
        Order order = this.abstractOrderRaoToOrderConverter.convert(rao);
        this.recalculateTotals(rao, order);
    }

    @Override
    public DiscountRAO addOrderLevelDiscount(AbstractOrderRAO orderRAO, boolean absolute, BigDecimal amount) {
        Order order = this.abstractOrderRaoToOrderConverter.convert(orderRAO);
        OrderDiscount discount = this.createOrderDiscount(order, absolute, amount);
        DiscountRAO discountRAO = this.createDiscountRAO(discount);
        this.raoUtils.addAction(orderRAO, discountRAO);
        this.recalculateTotals(orderRAO, order);
        return discountRAO;
    }

    @Override
    public int getProductAvailableQuantityInOrderEntry(OrderEntryRAO orderEntryRAO) {
        return orderEntryRAO.getQuantity() - this.getConsumedQuantityForOrderEntry(orderEntryRAO);
    }

    @Override
    public BigDecimal calculateSubTotals(CartRAO cartRao, Collection<ProductRAO> excludedProducts) {
        Preconditions.checkArgument(cartRao != null, "Cart must not be null.");
        if (org.apache.commons.collections.CollectionUtils.isEmpty(excludedProducts)) {
            return cartRao.getSubTotal();
        } else {
            CartRAO cloneCart = new CartRAO();
            cloneCart.setEntries(new HashSet());
            List<Long> productCodes = Lists.newArrayList();
            excludedProducts.forEach((p) -> {
                productCodes.add(p.getId());
            });
            cartRao.getEntries().stream()
                    .filter((entry) -> entry.getProduct() != null &&
                            !productCodes.contains(entry.getProduct().getId()))
                    .forEach((eRao) -> {
                cloneCart.getEntries().add(eRao);
            });
            cloneCart.setPaymentCost(cartRao.getPaymentCost());
            cloneCart.setDeliveryCost(cartRao.getDeliveryCost());
            cloneCart.setDiscountValue(cartRao.getDiscountValue());
            cloneCart.setCurrencyIsoCode(cartRao.getCurrencyIsoCode());
            cloneCart.setActions(cartRao.getActions());
            cloneCart.setOriginalTotal(cartRao.getOriginalTotal());
            this.calculateTotals(cloneCart);
            return cloneCart.getSubTotal();
        }
    }

    @Override
    public int getConsumedQuantityForOrderEntry(OrderEntryRAO orderEntryRao) {
        Set<OrderEntryRAO> entries = orderEntryRao.getOrder().getEntries();
        if (CollectionUtils.isNotEmpty(entries)) {
            Set<AbstractRuleActionRAO> allActions = entries.stream()
                    .filter((e) -> CollectionUtils.isNotEmpty(e.getActions()))
                    .flatMap((e) -> e.getActions().stream()).collect(Collectors.toSet());
            return this.getConsumedQuantityForOrderEntry(orderEntryRao, allActions);
        } else {
            Set<AbstractRuleActionRAO> actions = orderEntryRao.getActions();
            return CollectionUtils.isNotEmpty(actions) ? this.getConsumedQuantityForOrderEntry(orderEntryRao, actions) : 0;
        }
    }

    protected int getConsumedQuantityForOrderEntry(OrderEntryRAO orderEntryRao, Set<AbstractRuleActionRAO> actions) {
        int consumedQty = 0;
        Iterator var5 = actions.iterator();

        while(var5.hasNext()) {
            AbstractRuleActionRAO action = (AbstractRuleActionRAO)var5.next();
            if (action instanceof DiscountRAO) {
                DiscountRAO discountRAO = (DiscountRAO)action;
                Set<OrderEntryConsumedRAO> consumedEntries = discountRAO.getConsumedEntries();
                if (CollectionUtils.isNotEmpty(consumedEntries)) {
                    consumedQty += consumedEntries.stream()
                            .filter((e) -> e.getOrderEntry().equals(orderEntryRao))
                            .mapToInt(OrderEntryConsumedRAO::getQuantity)
                            .reduce(0, (q1, q2) -> q1 + q2);
                }
            }
        }

        return consumedQty;
    }

    private DiscountRAO createDiscountRAO(AbstractDiscount discount) {
        Preconditions.checkArgument(discount != null, "OrderDiscount must not be null.");
        DiscountRAO discountRAO = new DiscountRAO();
        if (discount.getAmount() instanceof Money) {
            Money money = (Money)discount.getAmount();
            discountRAO.setValue(money.getAmount());
            discountRAO.setCurrencyIsoCode(money.getCurrency().getIsoCode());
        } else {
            if (!(discount.getAmount() instanceof Percentage)) {
                throw new IllegalArgumentException("OrderDiscount must have Money or Percentage amount set.");
            }

            Percentage percentage = (Percentage)discount.getAmount();
            discountRAO.setValue(percentage.getRate());
        }

        if (discount instanceof LineItemDiscount) {
            LineItemDiscount lineItemDiscount = (LineItemDiscount)discount;
            discountRAO.setAppliedToQuantity((long)lineItemDiscount.getApplicableUnits());
            discountRAO.setPerUnit(true);
        }

        return discountRAO;
    }

    protected OrderDiscount createOrderDiscount(Order order, boolean absolute, BigDecimal amount) {
        Currency currency = order.getCurrency();
        AbstractAmount discountAmount = absolute ? new Money(amount, currency) : new Percentage(amount);
        OrderDiscount discount = new OrderDiscount(discountAmount);
        if (this.minimumAmountValidationStrategy.isOrderLowerLimitValid(order, discount)) {
            order.addDiscount(discount);
            return discount;
        } else {
            BigDecimal orderAmount = order.getSubTotal().subtract(order.getTotalDiscount()).getAmount();
            orderAmount = orderAmount.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : orderAmount;
            AbstractAmount zeroDiscountAmount = absolute ? new Money(orderAmount, currency) : Percentage.ZERO;
            OrderDiscount zeroDiscount = new OrderDiscount(zeroDiscountAmount);
            order.addDiscount(zeroDiscount);
            return zeroDiscount;
        }
    }

    protected void recalculateTotals(AbstractOrderRAO orderRao, Order order) {
        Money total = order.getTotal();
        Money subTotal = order.getSubTotal();
        Money deliveryCost = order.getTotalChargeOfType(ChargeType.SHIPPING);
        Money paymentCost = order.getTotalChargeOfType(ChargeType.PAYMENT);
        orderRao.setSubTotal(subTotal.getAmount());
        orderRao.setTotal(total.getAmount());
        orderRao.setDeliveryCost(deliveryCost.getAmount());
        orderRao.setPaymentCost(paymentCost.getAmount());
        if (!CollectionUtils.isEmpty(orderRao.getEntries())) {
            Iterator var8 = orderRao.getEntries().iterator();

            while(var8.hasNext()) {
                OrderEntryRAO entryRao = (OrderEntryRAO)var8.next();
                LineItem lineItem = this.findLineItem(order, entryRao);
                entryRao.setTotalPrice(lineItem.getTotal(order).getAmount());
            }
        }
    }

    protected NumberedLineItem findLineItem(Order cart, OrderEntryRAO entryRao) {
        Preconditions.checkArgument(cart != null, "cart must not be null");
        Preconditions.checkArgument(entryRao != null, "entry rao must not be null");
        Preconditions.checkArgument(entryRao.getEntryNumber() != null, "entry rao must have an entry number!");
        Iterator var4 = cart.getLineItems().iterator();

        LineItem item;
        do {
            if (!var4.hasNext()) {
                throw new IllegalArgumentException("can't find corresponding LineItem for the given orderEntryRao:" + entryRao);
            }

            item = (LineItem)var4.next();
        } while(!(item instanceof NumberedLineItem) || !entryRao.getEntryNumber().equals((item).getEntryNumber()));

        return (NumberedLineItem) item;
    }

    public DiscountRAO addOrderEntryLevelDiscount(OrderEntryRAO orderEntryRao, boolean absolute, BigDecimal amount) {
        Preconditions.checkArgument(orderEntryRao != null, "order entry rao must not be null");
        Preconditions.checkArgument(orderEntryRao.getOrder() != null, "corresponding entry cart rao must not be null");
        Preconditions.checkArgument(amount != null, AMOUNT_MUST_NOT_BE_NULL);
        return this.addOrderEntryLevelDiscount(orderEntryRao, absolute, amount, this.getConsumedQuantityForOrderEntry(orderEntryRao));
    }

    @Override
    public BigDecimal getAdjustedUnitPrice(int quantity, OrderEntryRAO orderEntryRao) {
        if(quantity == 0) return BigDecimal.ZERO;
        Order cart = this.abstractOrderRaoToOrderConverter.convert(orderEntryRao.getOrder());
        NumberedLineItem lineItem = this.findLineItem(cart, orderEntryRao);
        return lineItem.getTotalDiscount().getAmount().divide(BigDecimal.valueOf((long)quantity), RoundingMode.HALF_UP);
    }

    @Override
    public FreeProductRAO addFreeProductsToCart(CartRAO cartRao, ProductData product, Integer quantity) {
//        OrderEntryRAO orderEntryRao = cartRao.getEntries().stream()
//                .filter((e) -> e.getProduct().getId().equals(product.getId())).findFirst()
//                .orElseGet(OrderEntryRAO::new);
        OrderEntryRAO orderEntryRao = new OrderEntryRAO();
        orderEntryRao.setBasePrice(BigDecimal.ZERO);
        orderEntryRao.setCurrencyIsoCode(cartRao.getCurrencyIsoCode());
        orderEntryRao.setQuantity(quantity);
        orderEntryRao.setProduct(this.productRAOConverter.convert(product));
        orderEntryRao.setOrder(cartRao);
//        if (cartRao.getEntries() == null) {
//            cartRao.setEntries(new LinkedHashSet());
//        }
//
//        cartRao.getEntries().add(orderEntryRao);
//        this.ensureOrderEntryRAOEntryNumbers(cartRao);
        FreeProductRAO result = new FreeProductRAO();
        this.raoUtils.addAction(cartRao, result);
        result.setAddedOrderEntry(orderEntryRao);
        return result;
    }

    protected void ensureOrderEntryRAOEntryNumbers(AbstractOrderRAO abstractOrderRao) {
        if (abstractOrderRao != null && abstractOrderRao.getEntries() != null) {
            List<OrderEntryRAO> nullEntries = Lists.newArrayList();
            Set<OrderEntryRAO> abstractOrderRaoEntries = abstractOrderRao.getEntries();
            abstractOrderRaoEntries.stream().filter((e) -> Objects.isNull(e.getEntryNumber()))
                    .forEach(nullEntries::add);
            int max = abstractOrderRaoEntries.stream().filter((e) -> Objects.nonNull(e.getEntryNumber()))
                    .mapToInt(OrderEntryRAO::getEntryNumber).max().orElse(-1);
            if (CollectionUtils.isNotEmpty(nullEntries)) {
                Iterator var8 = nullEntries.iterator();
                while(var8.hasNext()) {
                    OrderEntryRAO orderEntryRAO = (OrderEntryRAO)var8.next();
                    max = max != -1 ? max + 1 : 1;
                    orderEntryRAO.setEntryNumber(max);
                }
            }
        }

    }

    @Override
    public DiscountRAO addOrderEntryLevelDiscountWithConsumableQty(OrderEntryRAO orderEntryRao, boolean absolute, BigDecimal amount, int qty) {
        Preconditions.checkArgument(orderEntryRao != null, "order entry rao must not be null");
        Preconditions.checkArgument(orderEntryRao.getOrder() != null, "corresponding entry cart rao must not be null");
        Preconditions.checkArgument(amount != null, AMOUNT_MUST_NOT_BE_NULL);
        int consumedQty = this.getConsumedQuantityForOrderEntry(orderEntryRao);
        Preconditions.checkArgument(consumedQty >= 0, "consumed quantity can't be negative");
        Order cart = this.abstractOrderRaoToOrderConverter.convert(orderEntryRao.getOrder());
        NumberedLineItem lineItem = this.findLineItem(cart, orderEntryRao);
        BigDecimal adjustedAmount = absolute ? amount.multiply(BigDecimal.valueOf((long)qty)) : this.convertPercentageDiscountToAbsoluteDiscount(amount, qty, lineItem);
        DiscountRAO discountRAO = this.createAbsoluteDiscountRAO(lineItem, adjustedAmount, qty, true);
        this.raoUtils.addAction(orderEntryRao, discountRAO);
        AbstractOrderRAO cartRao = orderEntryRao.getOrder();
        this.recalculateTotals(cartRao, cart);
        return discountRAO;
    }

    public DiscountRAO addOrderEntryLevelDiscount(OrderEntryRAO orderEntryRao, boolean absolute, BigDecimal amount, int consumedQty) {
        Preconditions.checkArgument(consumedQty >= 0, "consumed quantity can't be negative");
        Order cart = this.abstractOrderRaoToOrderConverter.convert(orderEntryRao.getOrder());
        NumberedLineItem lineItem = this.findLineItem(cart, orderEntryRao);
        int qty = orderEntryRao.getQuantity() - consumedQty;
        BigDecimal adjustedAmount = absolute ? amount.multiply(BigDecimal.valueOf((long)qty)) : this.convertPercentageDiscountToAbsoluteDiscount(amount, qty, lineItem);
        DiscountRAO discountRAO = this.createAbsoluteDiscountRAO(lineItem, adjustedAmount, qty, true);
        this.raoUtils.addAction(orderEntryRao, discountRAO);
        AbstractOrderRAO cartRao = orderEntryRao.getOrder();
        this.recalculateTotals(cartRao, cart);
        return discountRAO;
    }

    public List<DiscountRAO> addOrderEntryLevelDiscount(Map<Integer, Integer> selectedOrderEntryMap, Set<OrderEntryRAO> selectedOrderEntryRaos,
                                                        boolean absolute, BigDecimal amount) {
        Preconditions.checkArgument(selectedOrderEntryMap != null, "selectedOrderEntryMap must not be null");
        Preconditions.checkArgument(selectedOrderEntryRaos != null, "selectedOrderEntryRaos must not be null");
        Preconditions.checkArgument(amount != null, AMOUNT_MUST_NOT_BE_NULL);
        List<DiscountRAO> result = Lists.newArrayList();
        Iterator var7 = selectedOrderEntryRaos.iterator();

        while(var7.hasNext()) {
            OrderEntryRAO orderEntryRao = (OrderEntryRAO)var7.next();
            int qty = selectedOrderEntryMap.get(orderEntryRao.getEntryNumber());
            Order cart = this.abstractOrderRaoToOrderConverter.convert(orderEntryRao.getOrder());
            NumberedLineItem lineItem = this.findLineItem(cart, orderEntryRao);
            BigDecimal adjustedAmount = absolute ? amount.multiply(BigDecimal.valueOf((long)qty)) :
            this.convertPercentageDiscountToAbsoluteDiscount(amount, qty, lineItem);
            DiscountRAO discountRAO = this.createAbsoluteDiscountRAO(lineItem, adjustedAmount, qty, true);
            this.raoUtils.addAction(orderEntryRao, discountRAO);
            result.add(discountRAO);
            CartRAO cartRao = (CartRAO)orderEntryRao.getOrder();
            this.recalculateTotals(cartRao, cart);
        }

        return result;
    }

    public DiscountRAO addFixedPriceEntryDiscount(OrderEntryRAO orderEntryRao, BigDecimal fixedPrice) {
        Preconditions.checkArgument(orderEntryRao != null, "cart rao must not be null");
        Preconditions.checkArgument(fixedPrice != null, "fixedPrice must not be null");
        Preconditions.checkArgument(orderEntryRao.getOrder() != null, "Order must not be null");
        Preconditions.checkArgument(orderEntryRao.getBasePrice() != null, "Product base price is null");
        if (orderEntryRao.getBasePrice().compareTo(fixedPrice) > 0) {
            BigDecimal basePrice = orderEntryRao.getBasePrice();
            BigDecimal discountAmount = basePrice.subtract(fixedPrice);
            return this.addOrderEntryLevelDiscount(orderEntryRao, true, discountAmount);
        } else {
            return null;
        }
    }

    @Override
    public FixedPriceProductRAO addFixedPriceEntryDiscount(OrderEntryRAO orderEntryRao, BigDecimal fixedPrice, int qty) {
        Preconditions.checkArgument(orderEntryRao != null, "cart rao must not be null");
        Preconditions.checkArgument(fixedPrice != null, "fixedPrice must not be null");
        Preconditions.checkArgument(orderEntryRao.getOrder() != null, "Order must not be null");
        Preconditions.checkArgument(orderEntryRao.getBasePrice() != null, "Product base price is null");
        orderEntryRao.setBasePrice(fixedPrice);
        FixedPriceProductRAO fixedPriceProductRAO = new FixedPriceProductRAO();
        fixedPriceProductRAO.setFixedPrice(fixedPrice);
        fixedPriceProductRAO.setOrderEntryRAO(orderEntryRao);
        int consumedQty = this.getConsumedQuantityForOrderEntry(orderEntryRao);
        Preconditions.checkArgument(consumedQty >= 0, "consumed quantity can't be negative");
        Order cart = this.abstractOrderRaoToOrderConverter.convert(orderEntryRao.getOrder());
        NumberedLineItem lineItem = this.findLineItem(cart, orderEntryRao);

        Currency currency = lineItem.getBasePrice().getCurrency();
        AbstractAmount discountAmount = new Money(BigDecimal.ZERO, currency);
        LineItemDiscount discount = new LineItemDiscount(discountAmount, true, qty);
        discount = this.validateLineItemDiscount(lineItem, true, currency, discount);
        fixedPriceProductRAO.setPerUnit(true);
        fixedPriceProductRAO.setAppliedToQuantity((long)qty);
        Money money = (Money)discount.getAmount();
        fixedPriceProductRAO.setValue(BigDecimal.ZERO);
        fixedPriceProductRAO.setCurrencyIsoCode(money.getCurrency().getIsoCode());

        this.raoUtils.addAction(orderEntryRao, fixedPriceProductRAO);
        AbstractOrderRAO cartRao = orderEntryRao.getOrder();
        this.recalculateTotals(cartRao, cart);
        return fixedPriceProductRAO;
    }

    @Override
    public FixedPriceProductRAO addFixedPriceEntry(OrderEntryRAO orderEntryRao, BigDecimal fixedPrice) {
        Preconditions.checkArgument(orderEntryRao != null, "cart rao must not be null");
        Preconditions.checkArgument(fixedPrice != null, "fixedPrice must not be null");
        Preconditions.checkArgument(orderEntryRao.getOrder() != null, "Order must not be null");
        Preconditions.checkArgument(orderEntryRao.getBasePrice() != null, "Product base price is null");
        FixedPriceProductRAO fixedPriceProductRAO = new FixedPriceProductRAO();
        fixedPriceProductRAO.setFixedPrice(fixedPrice);
        fixedPriceProductRAO.setOrderEntryRAO(orderEntryRao);
        this.raoUtils.addAction(orderEntryRao, fixedPriceProductRAO);
        return fixedPriceProductRAO;
    }

    protected BigDecimal convertPercentageDiscountToAbsoluteDiscount(BigDecimal percentageAmount, int quantityToConsume, NumberedLineItem orderLineItem) {
        List<LineItemDiscount> lineItemDiscounts = orderLineItem.getDiscounts();
        long numItemsDiscounted = lineItemDiscounts.stream().mapToInt(LineItemDiscount::getApplicableUnits).sum();
        long availableItems = orderLineItem.getNumberOfUnits() - numItemsDiscounted;
        BigDecimal valueToDiscount;
        BigDecimal fraction;
        BigDecimal amount = orderLineItem.getBasePrice().getAmount();
        if (quantityToConsume <= availableItems) {
            valueToDiscount = amount.multiply(BigDecimal.valueOf((long)quantityToConsume));
        } else {
            fraction = amount.multiply(BigDecimal.valueOf(availableItems));
            BigDecimal lineItemAmount = orderLineItem.getTotalDiscount().getAmount();
            BigDecimal residualItemsValueToDiscount = amount.multiply(BigDecimal.valueOf(numItemsDiscounted))
                    .subtract(lineItemAmount).multiply(BigDecimal.valueOf((double)((long)quantityToConsume - availableItems) / (double)numItemsDiscounted));
            valueToDiscount = fraction.add(residualItemsValueToDiscount);
        }

        fraction = percentageAmount.divide(BigDecimal.valueOf(100.0D), 10, RoundingMode.DOWN);
        return valueToDiscount.multiply(fraction);
    }

    protected DiscountRAO createAbsoluteDiscountRAO(LineItem lineItem, BigDecimal amount, int applicableUnits, boolean perUnit) {
        int appliedToQuantity = perUnit ? applicableUnits : lineItem.getNumberOfUnits();
        Currency currency = lineItem.getBasePrice().getCurrency();
        BigDecimal adjustedAmount = appliedToQuantity > 0 ?
                amount.divide(BigDecimal.valueOf((long)appliedToQuantity), 10, 1) : BigDecimal.ZERO;
        AbstractAmount discountAmount = new Money(adjustedAmount, currency);
        LineItemDiscount discount = new LineItemDiscount(discountAmount, true, appliedToQuantity);
        discount = this.validateLineItemDiscount(lineItem, true, currency, discount);
        DiscountRAO discountRAO = new DiscountRAO();
        discountRAO.setPerUnit(perUnit);
        discountRAO.setAppliedToQuantity((long)appliedToQuantity);
        Money money = (Money)discount.getAmount();
        discountRAO.setValue(money.getAmount().equals(BigDecimal.ZERO) ? BigDecimal.ZERO : adjustedAmount);
        discountRAO.setCurrencyIsoCode(money.getCurrency().getIsoCode());
        return discountRAO;
    }

    protected LineItemDiscount validateLineItemDiscount(LineItem lineItem, boolean absolute, Currency currency, LineItemDiscount discount) {
        if (this.minimumAmountValidationStrategy.isLineItemLowerLimitValid(lineItem, discount)) {
            lineItem.addDiscount(discount);
            return discount;
        } else {
            AbstractAmount zeroDiscountAmount = absolute ? new Money(BigDecimal.ZERO, currency) : Percentage.ZERO;
            LineItemDiscount zeroDiscount = new LineItemDiscount(zeroDiscountAmount);
            lineItem.addDiscount(zeroDiscount);
            return zeroDiscount;
        }
    }

    @Autowired
    public void setProductRAOConverter(Converter<ProductData, ProductRAO> productRAOConverter) {
        this.productRAOConverter = productRAOConverter;
    }
}
