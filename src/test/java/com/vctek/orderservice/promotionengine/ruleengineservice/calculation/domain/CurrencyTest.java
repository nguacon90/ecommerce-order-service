package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain;

import org.junit.Test;

import static org.junit.Assert.*;


public class CurrencyTest
{
	@Test(expected = IllegalArgumentException.class)
	public void testNullCurrency()
	{
		new Currency(null, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testEmptyCurrency()
	{
		new Currency("", 0);
	}

	@Test
	public void testMinusZeroCurrency()
	{
		final Currency curr = new Currency("x", -0); //fine
		assertEquals("x", curr.toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNegativeCurrency()
	{
		new Currency("EUR", -10);
	}

	@Test
	public void testEquality()
	{
		final Currency curr1 = new Currency("A", 1);
		final Currency curr2 = new Currency("A", 2);
		final Currency curr3 = new Currency("B", 1);
		final Currency curr4 = new Currency("B", 1);

		final Currency eur1 = new Currency("eur", 1);
		final Currency eur2 = new Currency("EUR", 1);

		assertTrue(curr3.equals(curr4));
		assertTrue(curr2.equals(curr2));
		assertTrue(eur1.equals(eur2));

		assertFalse(curr1.equals(curr2));
		assertFalse(curr1.equals(curr3));
		assertFalse(curr2.equals(curr3));
		assertFalse(curr2.equals(new Object()));


	}

	@Test
	public void testHashCode()
	{
		final Currency curr1 = new Currency("A", 1);
		final Currency curr2 = new Currency("A", 2);
		final Currency curr3 = new Currency("B", 1);
		final Currency curr4 = new Currency("B", 1);

		assertTrue(curr3.hashCode() == curr4.hashCode());
		assertFalse(curr1.hashCode() == curr2.hashCode());
		assertFalse(curr1.hashCode() == curr3.hashCode());
		assertFalse(curr2.hashCode() == curr3.hashCode());
		assertTrue(curr2.hashCode() == curr2.hashCode());

		final Currency eur1 = new Currency("eur", 1);
		final Currency eur2 = new Currency("EUR", 1);
		assertTrue(eur1.hashCode() == eur2.hashCode());
	}

	@Test
	public void testAnotherHashCodeProblem()
	{
		final Currency curr1 = new Currency("A", 0);
		final Currency curr2 = new Currency("B", 0);
		assertFalse(curr1.hashCode() == curr2.hashCode());
	}
}
