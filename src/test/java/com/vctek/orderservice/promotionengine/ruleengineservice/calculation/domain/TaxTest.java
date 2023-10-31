package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain;

import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.MissingCalculationDataException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;


public class TaxTest
{
	private final Currency curr = new Currency("xxx", 2);

	@Test(expected = IllegalArgumentException.class)
	public void testWithNull()
	{
		new Tax(null);
	}

	@Test
	public void testTaxGetters()
	{
		final Tax tax = new Tax(Percentage.FIFTY);
		assertEquals(Percentage.FIFTY, tax.getAmount());
		assertNotNull(tax.getTargets());
		assertTrue(tax.getTargets().isEmpty());
	}

	@Test
	public void testSetAndModifyOrderDiscounts()
	{
		//create testdata
		final Tax testobject = new Tax(Percentage.TEN);
		final LineItem pc1 = new LineItem(new Money("10", curr));
		final LineItem pc2 = new LineItem(new Money("10", curr), 2);
		final LineItem pc3 = new LineItem(new Money("10", curr), 3);

		//normal order
		testobject.addTarget(pc2);
		testobject.addTarget(pc1);
		assertTrue(testobject.getTargets().size() == 2);
		assertTrue(testobject.getTargets().contains(pc2));
		assertTrue(testobject.getTargets().contains(pc1));


		//clear discounts
		try
		{
			testobject.getTargets().clear();
		}
		catch (final UnsupportedOperationException e) //NOPMD
		{
			//fine!
		}
		testobject.clearTargets();
		assertTrue(testobject.getTargets().isEmpty());

		//some other operations
		final List<Taxable> list = new ArrayList<Taxable>();
		list.add(pc3);
		list.add(pc1);
		testobject.addTargets(list);

		assertTrue(testobject.getTargets().contains(pc1));
		try
		{
			assertNull(pc2.getOrder());
		}
		catch (final MissingCalculationDataException e)
		{//fine 
		}

		testobject.addTarget(pc2);
		assertTrue(testobject.getTargets().contains(pc2));
		assertTrue(testobject.getTargets().size() == 3);
		assertTrue(testobject.getTargets().contains(pc2));
		assertTrue(testobject.getTargets().contains(pc1));

		//removing
		assertTrue(testobject.getTargets().contains(pc3));
		testobject.removeTarget(pc3);
		assertFalse(testobject.getTargets().contains(pc3));
		try
		{
			testobject.removeTarget(pc3);
			fail("IllegalArgumentException expecte");
		}
		catch (final IllegalArgumentException e)
		{
			// fine
		}
		assertFalse(testobject.getTargets().contains(pc3));
	}

}
