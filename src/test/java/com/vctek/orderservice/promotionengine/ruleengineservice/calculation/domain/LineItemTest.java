package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain;

import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.MissingCalculationDataException;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;


public class LineItemTest
{
	private final Currency curr = new Currency("euro", 2);

	@Test(expected = IllegalArgumentException.class)
	public void testWithNull()
	{
		new LineItem(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWithNegativeValue()
	{
		new LineItem(new Money(BigDecimal.ZERO, curr), -3);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGiveAwayCount()
	{
		final LineItem lineItem = new LineItem(new Money(BigDecimal.ZERO, curr), 3);
		lineItem.setGiveAwayUnits(-3);
	}

	@Test
	public void testEmptyLineItem()
	{
		final LineItem lineItem = new LineItem(new Money(BigDecimal.ZERO, curr), 3);
		assertEquals(Collections.EMPTY_LIST, lineItem.getCharges());
		assertEquals(Collections.EMPTY_MAP, lineItem.getTotalCharges());
		assertEquals(Collections.EMPTY_LIST, lineItem.getDiscounts());
		assertEquals(Collections.EMPTY_MAP, lineItem.getTotalDiscounts());
	}

	@Test
	public void testToString()
	{
		final LineItem lineItem1 = new LineItem(new Money(BigDecimal.ZERO, curr));
		assertEquals("1x 0.00 euro", lineItem1.toString());
		final LineItemDiscount lid = new LineItemDiscount(Percentage.SEVENTYFIVE);
		lineItem1.addDiscount(lid);
		assertEquals("1x 0.00 euro discounts:[75%]", lineItem1.toString());
		lineItem1.setGiveAwayUnits(1);
		assertEquals("1x 0.00 euro(free:1) discounts:[75%]", lineItem1.toString());
		final LineItemCharge lic = new LineItemCharge(Percentage.FIFTY);
		lineItem1.addCharge(lic);
		assertEquals("1x 0.00 euro(free:1) discounts:[75%] charges:[50% dontCharge:false]", lineItem1.toString());
		final LineItem lineItem2 = new LineItem(new Money(BigDecimal.ZERO, curr));
		lineItem2.addCharge(lic);
		assertEquals("1x 0.00 euro charges:[50% dontCharge:false]", lineItem2.toString());
	}

	@Test
	public void testCalculatedUnitCount()
	{
		final LineItem lineItem1 = new LineItem(new Money(BigDecimal.ZERO, curr), 10);
		assertTrue(lineItem1.getNumberOfUnitsForCalculation() == 10);
		lineItem1.setGiveAwayUnits(3);
		assertTrue(lineItem1.getNumberOfUnitsForCalculation() == 7);
		lineItem1.setGiveAwayUnits(4);
		assertTrue(lineItem1.getNumberOfUnitsForCalculation() == 6);
		lineItem1.setGiveAwayUnits(10);
		assertTrue(lineItem1.getNumberOfUnitsForCalculation() == 0);
		lineItem1.setGiveAwayUnits(40);
		assertTrue(lineItem1.getNumberOfUnitsForCalculation() == 0);
	}

	@Test
	public void testLineItemChargeGetters()
	{
		final LineItem lineitem = new LineItem(new Money(curr), 3);
		assertNotNull(lineitem.getTotalCharges());
		assertTrue(lineitem.getTotalCharges().isEmpty());
		assertNotNull(lineitem.getCharges());
		assertTrue(lineitem.getCharges().isEmpty());
		assertNotNull(lineitem.getDiscounts());
		assertTrue(lineitem.getDiscounts().isEmpty());
		assertNotNull(lineitem.getTotalDiscounts());
		assertTrue(lineitem.getTotalDiscounts().isEmpty());
	}

	@Test
	public void testLineItemChargeWithoutOrder()
	{
		final LineItem lineitem = new LineItem(new Money(BigDecimal.ZERO, curr), 3);
		try
		{
			lineitem.getTotal(null);
			fail("expected MissingCalculationDataException");
		}
		catch (final MissingCalculationDataException e)
		{
			assertTrue(e.getMessage().contains("Order for LineItem"));
		}
		catch (final Exception e)
		{
			fail("unexpected exception: " + e);
		}
		//-----
		try
		{
			lineitem.getSubTotal();
			fail("expected MissingCalculationDataException");
		}
		catch (final MissingCalculationDataException e)
		{
			assertTrue(e.getMessage().contains("Order for LineItem"));
		}
		catch (final Exception e)
		{
			fail("unexpected exception: " + e);
		}
		//-----
		try
		{
			lineitem.getTotalCharge();
			fail("expected MissingCalculationDataException");
		}
		catch (final MissingCalculationDataException e)
		{
			assertTrue(e.getMessage().contains("Order for LineItem"));
		}
		catch (final Exception e)
		{
			fail("unexpected exception: " + e);
		}
		//-----
		try
		{
			lineitem.getTotalDiscount();
			fail("expected MissingCalculationDataException");
		}
		catch (final MissingCalculationDataException e)
		{
			assertTrue(e.getMessage().contains("Order for LineItem"));
		}
		catch (final Exception e)
		{
			fail("unexpected exception: " + e);
		}
	}

	@Test
	public void testSetAndModifyDiscounts()
	{
		//create testdata
		final LineItem testobject = new LineItem(new Money("22.45", curr), 10);
		final LineItemDiscount disc1 = new LineItemDiscount(new Money("10", curr));
		final LineItemDiscount disc2 = new LineItemDiscount(Percentage.TEN);
		final LineItemDiscount disc3 = new LineItemDiscount(Percentage.FIFTY);


		//test different settings

		//normal order
		testobject.addDiscount(disc2);
		testobject.addDiscount(disc1);
		assertTrue(testobject.getDiscounts().size() == 2);
		assertEquals(disc2, testobject.getDiscounts().get(0));
		assertEquals(disc1, testobject.getDiscounts().get(1));


		//clear discounts
		try
		{
			testobject.getDiscounts().clear();
		}
		catch (final UnsupportedOperationException e) //NOPMD
		{
			//fine!
		}
		testobject.clearDiscounts();
		assertTrue(testobject.getDiscounts().isEmpty());

		//some other operations
		final List<LineItemDiscount> list = new ArrayList<LineItemDiscount>();
		list.add(disc3);
		list.add(disc1);
		testobject.addDiscounts(list);

		assertEquals(disc1, testobject.getDiscounts().get(1));
		testobject.addDiscount(1, disc2);
		assertTrue(testobject.getDiscounts().size() == 3);
		assertEquals(disc2, testobject.getDiscounts().get(1));
		assertEquals(disc1, testobject.getDiscounts().get(2));

		//removing
		testobject.removeDiscount(disc3);
		try
		{
			testobject.removeDiscount(disc3);
			fail("IllegalArgumentException expected");
		}
		catch (final IllegalArgumentException e)
		{
			//
		}
	}

	@Test
	public void testSetAndModifyAdditionalCharges()
	{
		//create testdata
		final LineItem testobject = new LineItem(new Money("22.45", curr), 10);
		final LineItemCharge apc1 = new LineItemCharge(new Money("10", curr));
		final LineItemCharge apc2 = new LineItemCharge(Percentage.TEN);
		final LineItemCharge apc3 = new LineItemCharge(Percentage.FIFTY);


		//test different settings

		//normal order
		testobject.addCharge(apc2);
		testobject.addCharge(apc1);
		assertTrue(testobject.getCharges().size() == 2);
		assertEquals(apc2, testobject.getCharges().get(0));
		assertEquals(apc1, testobject.getCharges().get(1));


		//clear discounts
		try
		{
			testobject.getCharges().clear();
		}
		catch (final UnsupportedOperationException e) //NOPMD
		{
			//fine!
		}
		testobject.clearCharges();
		assertTrue(testobject.getCharges().isEmpty());

		//some other operations
		final List<LineItemCharge> list = new ArrayList<LineItemCharge>();
		list.add(apc3);
		list.add(apc1);
		testobject.addCharges(list);

		assertEquals(apc1, testobject.getCharges().get(1));
		testobject.addCharge(1, apc2);
		assertTrue(testobject.getCharges().size() == 3);
		assertEquals(apc2, testobject.getCharges().get(1));
		assertEquals(apc1, testobject.getCharges().get(2));

		//removing
		testobject.removeCharge(apc3);
		try
		{
			testobject.removeCharge(apc3);
			fail("IllegalArgumentException expected");
		}
		catch (final IllegalArgumentException e)
		{
			// fine
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void setIllegalGiveAwayCount()
	{
		final LineItem prodc = new LineItem(new Money("100", curr), 10);
		prodc.setGiveAwayUnits(-10);
	}

}
