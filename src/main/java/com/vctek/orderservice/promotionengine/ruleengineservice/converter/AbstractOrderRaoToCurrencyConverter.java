package com.vctek.orderservice.promotionengine.ruleengineservice.converter;

import com.google.common.base.Preconditions;
import com.vctek.converter.ConversionException;
import com.vctek.converter.Converter;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.Currency;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.AbstractOrderRAO;
import org.springframework.stereotype.Component;

@Component
public class AbstractOrderRaoToCurrencyConverter implements Converter<AbstractOrderRAO, Currency> {

    @Override
    public Currency convert(AbstractOrderRAO source) throws ConversionException {
        String currencyIso = source.getCurrencyIsoCode();
        Preconditions.checkArgument(currencyIso != null, "currencyIso must not be null");
        return new Currency(currencyIso, Currency.DEFAULT_DIGITS);
    }
}
