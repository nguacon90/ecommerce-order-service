package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain;

import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.MissingCalculationDataException;
import org.junit.Test;

import static org.junit.Assert.*;

public class ChargeTest
{
	@Test(expected = IllegalArgumentException.class)
	public void testNullLineItemChargeOne()
	{
		new LineItemCharge(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullLineItemChargeTwo()
	{
		new LineItemCharge(null, false, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullOrderChargeOne()
	{
		new OrderCharge(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullOrderChargeTwo()
	{
		new OrderCharge(null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNegativeApplicableUnit()
	{
		new LineItemCharge(Percentage.SEVENTY, true, -10);
	}

	@Test
	public void testNegativeApplicableUnitWhichIsNotPerUnit()
	{
		final LineItemCharge apc = new LineItemCharge(Percentage.THIRTY, false, -10);
		assertNotNull(apc);
		assertFalse(apc.isPerUnit());
		assertTrue(apc.getApplicableUnits() == 0);
		assertEquals("30% dontCharge:false", apc.toString());
	}

	@Test
	public void testPositiveApplicableUnitWhichIsNotPerUnit()
	{
		final LineItemCharge apc = new LineItemCharge(Percentage.THIRTY, true, 10);
		assertNotNull(apc);
		assertTrue(apc.isPerUnit());
		assertTrue(apc.getApplicableUnits() == 10);
		assertEquals("30% dontCharge:false applicableUnit:10", apc.toString());
	}

	@Test
	public void testWithAdditionalChargeType()
	{
		final OrderCharge apc = new OrderCharge(Percentage.THIRTY, null);
		assertNull(apc.getChargeType());
		apc.setChargeType(ChargeType.PAYMENT);
		assertEquals(ChargeType.PAYMENT, apc.getChargeType());
		assertEquals("30% dontCharge:false type:PAYMENT", apc.toString());
	}

	@Test
	public void testDontCharge()
	{
		LineItemCharge apd = new LineItemCharge(Percentage.FIFTY);
		assertFalse(apd.isDisabled());
		apd = new LineItemCharge(Percentage.FIFTY, false, 0);
		apd.setDisabled(true);
		assertTrue(apd.isDisabled());

		OrderCharge aoc = new OrderCharge(Percentage.FIFTY);
		aoc.setDisabled(true);
		assertTrue(aoc.isDisabled());
		aoc = new OrderCharge(Percentage.FIFTY);
		assertFalse(aoc.isDisabled());
	}

	@Test
	public void testToStringComplete()
	{
		final Currency curr = new Currency("EUR", 2);
		final LineItemCharge apd = new LineItemCharge(new Money(curr), true, 23);
		apd.setDisabled(true);
		apd.setChargeType(ChargeType.SHIPPING);
		assertEquals("0 EUR dontCharge:true applicableUnit:23 type:SHIPPING", apd.toString());
	}

	@Test
	public void getOrderTotalCalculationException()
	{
		final Currency curr = new Currency("xxx", 1);
		final OrderCharge orderCharge = new OrderCharge(new Money("0", curr));
		orderCharge.setDisabled(true);
		try
		{
			orderCharge.getTotal(null);
			fail("Expected MissingCalculationDataException");
		}
		catch (final MissingCalculationDataException e)
		{
			// fine
		}
		catch (final Exception e)
		{
			fail("unexpected exception: " + e);
		}
	}

	@Test
	public void testOrderCharge()
	{
		final Currency curr = new Currency("xxx", 1);
		OrderCharge orderCharge = new OrderCharge(new Money("0.000", curr), ChargeType.PAYMENT);
		assertEquals("0.0 xxx dontCharge:false type:PAYMENT", orderCharge.toString());

		orderCharge = new OrderCharge(new Money("0.000", curr), null);
		assertEquals("0.0 xxx dontCharge:false", orderCharge.toString());
	}
}
