package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class DiscountsTest
{
	private Money zeroEuro;
	private Percentage zeroPercent;

	@Before
	public void setup()
	{
		final Currency euro = new Currency("EUR", 2);
		zeroEuro = new Money(0, euro);
		zeroPercent = new Percentage(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateLineItemDiscountWithNullOne()
	{
		new LineItemDiscount(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateLineItemDiscountWithNullTwo()
	{
		new LineItemDiscount(null, false, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateLineItemDiscountWithNegativeValue()
	{
		new LineItemDiscount(zeroEuro, true, -10);
	}

	public void testCreateLineItemDiscountWithNegativeValueButNotPErUnit()
	{
		final LineItemDiscount prodDisc = new LineItemDiscount(zeroEuro, false, -10);
		assertNotNull(prodDisc);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateOrderDiscountWithNull()
	{
		new OrderDiscount(null);
	}

	@Test
	public void testCreateOrderDiscount()
	{
		final OrderDiscount orderDiscount = new OrderDiscount(Percentage.EIGHTY);
		assertEquals("80%", orderDiscount.toString());
	}

	@Test
	public void testToStringByLineItemDiscount()
	{
		LineItemDiscount lineItemDiscount = new LineItemDiscount(Percentage.EIGHTY, true, 42);
		assertEquals("80% applicableUnits:42", lineItemDiscount.toString());
		lineItemDiscount = new LineItemDiscount(Percentage.EIGHTY, false, 42);
		assertEquals("80%", lineItemDiscount.toString());
	}

	@Test
	public void testDiscountAndLineItemAssigning()
	{
		final LineItem liONE = new LineItem(zeroEuro);
		final LineItem liTWO = new LineItem(zeroEuro);
		final LineItemDiscount discount = new LineItemDiscount(zeroPercent);

		//test one
		liONE.addDiscount(discount);
		assertTrue(liONE.getDiscounts().size() == 1);
		assertEquals(discount, liONE.getDiscounts().get(0));
		assertTrue(liTWO.getDiscounts().isEmpty());

	}

	@Test
	public void testClearDiscountbyLineItemAssigning()
	{
		final LineItem liONE = new LineItem(zeroEuro);
		final LineItem liTWO = new LineItem(zeroEuro);
		final LineItemDiscount discount = new LineItemDiscount(zeroPercent);

		liONE.addDiscount(discount);
		liONE.clearDiscounts();
		assertTrue(liONE.getDiscounts().isEmpty());
		assertTrue(liTWO.getDiscounts().isEmpty());


		liONE.addDiscount(discount);
		assertFalse(liONE.getDiscounts().isEmpty());
		assertTrue(liTWO.getDiscounts().isEmpty());
	}




}
