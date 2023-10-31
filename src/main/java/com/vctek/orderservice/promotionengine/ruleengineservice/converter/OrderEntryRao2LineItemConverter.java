package com.vctek.orderservice.promotionengine.ruleengineservice.converter;

import com.vctek.converter.ConversionException;
import com.vctek.converter.Converter;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.Currency;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.Money;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.NumberedLineItem;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.AbstractOrderRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.OrderEntryRAO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OrderEntryRao2LineItemConverter implements Converter<OrderEntryRAO, NumberedLineItem> {
    private Converter<AbstractOrderRAO, Currency> abstractOrderRaoToCurrencyConverter;

    public OrderEntryRao2LineItemConverter(Converter<AbstractOrderRAO, Currency> abstractOrderRaoToCurrencyConverter) {
        this.abstractOrderRaoToCurrencyConverter = abstractOrderRaoToCurrencyConverter;
    }

    @Override
    public NumberedLineItem convert(OrderEntryRAO entryRao) throws ConversionException {
        Currency currency = abstractOrderRaoToCurrencyConverter.convert(entryRao.getOrder());
        Money money = new Money(entryRao.getBasePrice(), currency);
        BigDecimal toppingPriceAmount = entryRao.getTotalToppingPrice() == null ? BigDecimal.ZERO : entryRao.getTotalToppingPrice();
        BigDecimal toppingFixedDiscountAmount = entryRao.getTotalToppingFixedDiscount() == null ? BigDecimal.ZERO :
                entryRao.getTotalToppingFixedDiscount();
        Money totalToppingPrice = new Money(toppingPriceAmount, currency);
        Money totalToppingFixedDiscount = new Money(toppingFixedDiscountAmount, currency);
        NumberedLineItem lineItem = new NumberedLineItem(money, entryRao.getQuantity());
        lineItem.setTotalToppingFixedDiscount(totalToppingFixedDiscount);
        lineItem.setTotalToppingPrice(totalToppingPrice);
        lineItem.setEntryNumber(entryRao.getEntryNumber());
        return lineItem;
    }
}
