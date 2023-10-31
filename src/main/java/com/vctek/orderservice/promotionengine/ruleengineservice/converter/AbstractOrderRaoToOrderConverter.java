package com.vctek.orderservice.promotionengine.ruleengineservice.converter;

import com.vctek.converter.ConversionException;
import com.vctek.converter.Converter;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.*;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.strategy.impl.CalculationStrategies;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.AbstractOrderRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.DiscountRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.OrderEntryRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.ShipmentRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.util.OrderUtils;
import com.vctek.orderservice.promotionengine.ruleengineservice.util.RaoUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Component
public class AbstractOrderRaoToOrderConverter implements Converter<AbstractOrderRAO, Order> {
    private RaoUtils raoUtils;
    private OrderUtils orderUtils;
    private Converter<OrderEntryRAO, NumberedLineItem> lineItemConverter;
    private CalculationStrategies calculationStrategies;
    private Converter<AbstractOrderRAO, Currency> abstractOrderRaoToCurrencyConverter;

    public AbstractOrderRaoToOrderConverter(RaoUtils raoUtils, CalculationStrategies calculationStrategies,
                                            Converter<OrderEntryRAO, NumberedLineItem> lineItemConverter,
                                            Converter<AbstractOrderRAO, Currency> abstractOrderRaoToCurrencyConverter) {
        this.raoUtils = raoUtils;
        this.lineItemConverter = lineItemConverter;
        this.calculationStrategies = calculationStrategies;
        this.abstractOrderRaoToCurrencyConverter = abstractOrderRaoToCurrencyConverter;
    }

    @Override
    public Order convert(AbstractOrderRAO cartRao) throws ConversionException {
        Order order = new Order(new Currency(cartRao.getCurrencyIsoCode()), calculationStrategies);
//        OrderCharge shippingCharge = this.convertToShippingOrderCharge(cartRao);
//        if (shippingCharge != null) {
//            order.addCharge(shippingCharge);
//        }

//        OrderCharge paymentCharge = this.convertToPaymentOrderCharge(cartRao);
//        if (paymentCharge != null) {
//            order.addCharge(paymentCharge);
//        }
        populateOrderFixedDiscount(cartRao, order);


        if (CollectionUtils.isNotEmpty(cartRao.getActions())) {
            List<OrderDiscount> orderDiscounts = new ArrayList<>();
            this.raoUtils.getDiscounts(cartRao).forEach((action) -> {
                orderDiscounts.add(this.convertToOrderDiscount(action, cartRao));
            });
            order.addDiscounts(orderDiscounts);
        }

        if (CollectionUtils.isNotEmpty(cartRao.getEntries())) {
            Iterator var8 = cartRao.getEntries().iterator();
            List<LineItem> lineItems = new ArrayList();
            while (var8.hasNext()) {
                OrderEntryRAO entryRao = (OrderEntryRAO) var8.next();
                NumberedLineItem lineItem = this.lineItemConverter.convert(entryRao);
                lineItems.add(lineItem);
                if (CollectionUtils.isNotEmpty(entryRao.getActions())) {
                    List<LineItemDiscount> lineItemDiscounts = new ArrayList();
                    entryRao.getActions().stream()
                            .filter((action) -> action instanceof DiscountRAO)
                            .filter((a) -> this.isDiscountNotOrderLevel(cartRao, (DiscountRAO) a))
                            .forEach((action) -> lineItemDiscounts.add(this.convertToLineItemDiscount((DiscountRAO) action, cartRao)));
                    if (CollectionUtils.isNotEmpty(lineItemDiscounts)) {
                        lineItem.addDiscounts(lineItemDiscounts);
                    }
                }

                populateEntryFixedDiscount(cartRao, entryRao, lineItem);
            }

            order.addLineItems(lineItems);
        }

        return order;
    }

    private void populateOrderFixedDiscount(AbstractOrderRAO cartRao, Order order) {
        if(cartRao.getFixedOrderDiscount() != null &&
                cartRao.getFixedOrderDiscount().doubleValue() > 0) {
            Money money = new Money(cartRao.getFixedOrderDiscount(), this.abstractOrderRaoToCurrencyConverter.convert(cartRao));
            order.addDiscount(new OrderDiscount(money));
        }
    }

    private void populateEntryFixedDiscount(AbstractOrderRAO cartRao, OrderEntryRAO entryRao, NumberedLineItem lineItem) {
        if(entryRao.getFixedDiscount() != null && entryRao.getFixedDiscount().doubleValue() > 0) {
            Money money = new Money(entryRao.getFixedDiscount(), this.abstractOrderRaoToCurrencyConverter.convert(cartRao));
            lineItem.addDiscount(new LineItemDiscount(money));
        }
    }

    protected LineItemDiscount convertToLineItemDiscount(DiscountRAO discountRao, AbstractOrderRAO cartRao) {
        AbstractAmount amount;
        if (StringUtils.isEmpty(discountRao.getCurrencyIsoCode())) {
            amount = new Percentage(discountRao.getValue());
        } else {
            amount = new Money(discountRao.getValue(), this.abstractOrderRaoToCurrencyConverter.convert(cartRao));
        }

        boolean perUnit = discountRao.isPerUnit() || this.raoUtils.isAbsolute(discountRao);
        return discountRao.getAppliedToQuantity() > 0L ? new LineItemDiscount(amount, perUnit, (int) discountRao.getAppliedToQuantity()) :
                new LineItemDiscount(amount, perUnit);
    }

    protected boolean isDiscountNotOrderLevel(AbstractOrderRAO orderRAO, DiscountRAO discount) {
        return CollectionUtils.isNotEmpty(orderRAO.getActions()) ? orderRAO.getActions().stream()
                .filter((a) -> a instanceof DiscountRAO).noneMatch(discount::equals) : true;
    }

    protected OrderDiscount convertToOrderDiscount(DiscountRAO discountRao, AbstractOrderRAO cartRao) {
        AbstractAmount amount;
        if (StringUtils.isEmpty(discountRao.getCurrencyIsoCode())) {
            amount = new Percentage(discountRao.getValue());
        } else {
            amount = new Money(discountRao.getValue(), this.abstractOrderRaoToCurrencyConverter.convert(cartRao));
        }
        return new OrderDiscount(amount);
    }

    protected OrderCharge convertToShippingOrderCharge(AbstractOrderRAO cartRao) {
        Optional<ShipmentRAO> shipment = this.raoUtils.getShipment(cartRao);
        Currency currency = this.abstractOrderRaoToCurrencyConverter.convert(cartRao);
        if (shipment.isPresent() && shipment.get().getMode() != null) {
            return this.orderUtils.createShippingCharge(currency, true, shipment.get().getMode().getCost());
        }

        return cartRao.getDeliveryCost() != null ? new OrderCharge(new Money(cartRao.getDeliveryCost(), currency), ChargeType.SHIPPING) : null;
    }

    private OrderCharge convertToPaymentOrderCharge(AbstractOrderRAO cartRao) {
        Currency currency = this.abstractOrderRaoToCurrencyConverter.convert(cartRao);
        return cartRao.getPaymentCost() != null ? new OrderCharge(new Money(cartRao.getPaymentCost(), currency), ChargeType.PAYMENT) : null;
    }


    @Autowired
    public void setOrderUtils(OrderUtils orderUtils) {
        this.orderUtils = orderUtils;
    }
}
