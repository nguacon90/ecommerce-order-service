package com.vctek.orderservice.promotionengine.ruleengineservice.util;

import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.Currency;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.Money;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.OrderCharge;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.Percentage;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class OrderUtilsTest
{
	private static final String USD = "USD";

	private OrderUtils orderUtils;
	private Currency currency;

	@Before
	public void setUp()
	{
		orderUtils = new OrderUtils();
		currency = new Currency(USD, 2);
	}

	@Test
	public void testCreateShippingChargeAbsolute()
	{
		final BigDecimal value = new BigDecimal("100");
		final OrderCharge orderCharge = orderUtils.createShippingCharge(currency, true, value);
		assertThat(orderCharge.getAmount()).isNotNull().isInstanceOf(Money.class);
		final Money money = (Money) orderCharge.getAmount();
		assertThat(money.getAmount()).isEqualByComparingTo(value);
		assertThat(money.getCurrency()).isEqualTo(currency);
	}

	@Test
	public void testCreateShippingChargePercentage()
	{
		final BigDecimal value = new BigDecimal("10");
		final OrderCharge orderCharge = orderUtils.createShippingCharge(currency, false, value);
		assertThat(orderCharge.getAmount()).isNotNull().isInstanceOf(Percentage.class);
		final Percentage percentage = (Percentage) orderCharge.getAmount();
		assertThat(percentage.getRate()).isEqualByComparingTo(value);
	}
}
