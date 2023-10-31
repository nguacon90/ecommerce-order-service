package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.*;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.strategy.impl.CalculationStrategies;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.strategy.impl.DefaultRoundingStrategy;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.strategy.impl.DefaultTaxRoundingStrategy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class DefaultMinimumAmountValidationStrategyTest
{
	private DefaultMinimumAmountValidationStrategy minimumAmountValidationStrategy;
	private Currency currency;

	@Before
	public void setUp()
	{
		minimumAmountValidationStrategy = new DefaultMinimumAmountValidationStrategy();
		currency = new Currency("USD", 2);
	}

	protected Order createOrder()
	{
		final CalculationStrategies strategies = new CalculationStrategies(new DefaultRoundingStrategy(),
                new DefaultTaxRoundingStrategy());
		final Order order = new Order(currency, strategies);

		final OrderCharge shippingCharge = new OrderCharge(new Money(new BigDecimal("5.00"), currency));
		order.addCharge(shippingCharge);
		final OrderCharge paymentCharge = new OrderCharge(new Money(new BigDecimal("5.00"), currency));
		order.addCharge(paymentCharge);

		final List<LineItem> lineItems = new ArrayList<>();
		final NumberedLineItem lineItem = new NumberedLineItem(new Money(new BigDecimal("10.00"), currency), 1);
		lineItems.add(lineItem);
		order.addLineItems(lineItems);

		return order;
	}

	@Test
	public void testIsOrderLowerLimitValid()
	{
		Assert.assertTrue(minimumAmountValidationStrategy.isOrderLowerLimitValid(createOrder(), new OrderDiscount(new Money(
				new BigDecimal("5.00"), currency))));
	}

	@Test
	public void testIsOrderLowerLimitNotValid()
	{
		Assert.assertFalse(minimumAmountValidationStrategy.isOrderLowerLimitValid(createOrder(), new OrderDiscount(new Money(
				new BigDecimal("11.00"), currency))));
	}

	@Test
	public void testIsLineItemLowerLimitValid()
	{
		Assert.assertTrue(minimumAmountValidationStrategy.isLineItemLowerLimitValid(createOrder().getLineItems().get(0),
				new LineItemDiscount(new Money(new BigDecimal("5.00"), currency))));
	}

	@Test
	public void testIsLineItemLowerLimitNotValid()
	{
		Assert.assertFalse(minimumAmountValidationStrategy.isLineItemLowerLimitValid(createOrder().getLineItems().get(0),
				new LineItemDiscount(new Money(new BigDecimal("11.00"), currency))));
	}

	@Test
	public void testIsLineItemLowerLimitNotValidAfterOrderDiscountApplied()
	{
		final Order createOrder = createOrder();
		createOrder.addDiscount(new OrderDiscount(new Money(new BigDecimal("7.00"), currency)));
		Assert.assertFalse(minimumAmountValidationStrategy.isLineItemLowerLimitValid(createOrder.getLineItems().get(0),
				new LineItemDiscount(new Money(new BigDecimal("5.00"), currency))));
	}
}
