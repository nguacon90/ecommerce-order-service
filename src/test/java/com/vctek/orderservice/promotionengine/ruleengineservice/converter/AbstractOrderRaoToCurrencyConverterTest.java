package com.vctek.orderservice.promotionengine.ruleengineservice.converter;

import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.AbstractRuleEngineTest;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.Currency;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import org.junit.Assert;
import org.junit.Test;


public class AbstractOrderRaoToCurrencyConverterTest extends AbstractRuleEngineTest
{
	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionOnNullCurrencyIso()
	{
		getAbstractOrderRaoToCurrencyConverter().convert(createCartRAO("cart_code", null));
	}

	@Test
	public void testCartToCurrencyConversionForUSD()
	{
		final CartRAO cart = createCartRAO("cart_code", VND);
		final Currency conversionResult = getAbstractOrderRaoToCurrencyConverter().convert(cart);
		Assert.assertEquals(VND, conversionResult.getIsoCode());
		Assert.assertEquals(2, conversionResult.getDigits());
	}

}
