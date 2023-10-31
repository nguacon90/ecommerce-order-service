package com.vctek.orderservice.promotionengine.ruleengineservice.calculation.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.AbstractRuleEngineTest;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.*;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.strategy.impl.CalculationStrategies;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.strategy.impl.DefaultRoundingStrategy;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.strategy.impl.DefaultTaxRoundingStrategy;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.OrderEntryRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.ProductRAO;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class DefaultRuleEngineCalculationServiceTest extends AbstractRuleEngineTest
{
	@Test
	public void testSimpleCalculateTotals()
	{

		// simple cart with 1 entry of quantity 2
		final CartRAO simple01 = createCartRAO("simple01", VND);
		simple01.setEntries(Collections.singleton(createOrderEntryRAO(simple01, "12.34", // NOSONAR
                VND, 2, 0)));
		getRuleEngineCalculationService().calculateTotals(simple01);
		assertEquals(new BigDecimal("24.68"), simple01.getTotal());
		assertEquals(new BigDecimal("24.68"), simple01.getSubTotal());

		// simple cart with 2 entries and delivery cost
		final CartRAO simple02 = createCartRAO("simple02", VND);
		simple02.setEntries(set(createOrderEntryRAO(simple02, "12.34", VND, 2, 0),
				createOrderEntryRAO(simple02, "23.45", VND, 3, 1))); // NOSONAR
		simple02.setDeliveryCost(new BigDecimal("5.00"));
		getRuleEngineCalculationService().calculateTotals(simple02);
		assertEquals(new BigDecimal("95.03"), simple02.getTotal()); // NOSONAR
		assertEquals(new BigDecimal("95.03"), simple02.getSubTotal()); // NOSONAR
		assertEquals(new BigDecimal("0"), simple02.getDeliveryCost());
	}

	@Test
	public void testAddOrderLevelDiscountPercentage()
	{
		// simple cart with 2 entries and delivery cost
		final CartRAO simple03 = createCartRAO("simple03", VND);
		simple03.setEntries(set(createOrderEntryRAO(simple03, "12.34", VND, 2, 0),
				createOrderEntryRAO(simple03, "23.45", VND, 3, 1)));
		simple03.setDeliveryCost(new BigDecimal("5.00"));
		getRuleEngineCalculationService().calculateTotals(simple03);
		assertEquals(new BigDecimal("95.03"), simple03.getTotal());
		assertEquals(new BigDecimal("95.03"), simple03.getSubTotal());
		assertEquals(new BigDecimal("0"), simple03.getDeliveryCost());

		// now add a 10% oder-level discount
		// 10% of sub total = 9.50
		// total is 100.03 - 9.50 = 90.53
		getRuleEngineCalculationService().addOrderLevelDiscount(simple03, false, new BigDecimal("10.00")); // NOSONAR
		assertEquals(new BigDecimal("0"), simple03.getDeliveryCost());
		assertEquals(new BigDecimal("95.03"), simple03.getSubTotal());
		assertEquals(new BigDecimal("85.53"), simple03.getTotal());
	}

	@Test
	public void testAddOrderLevelDiscountAbsolute()
	{
		// simple cart with 2 entries and delivery cost
		final CartRAO simple03 = createCartRAO("simple03", VND);
		simple03.setEntries(set(createOrderEntryRAO(simple03, "12.34", VND, 2, 0),
				createOrderEntryRAO(simple03, "23.45", VND, 3, 1)));
		simple03.setDeliveryCost(new BigDecimal("5.00"));
		getRuleEngineCalculationService().calculateTotals(simple03);
		assertEquals(new BigDecimal("95.03"), simple03.getTotal());
		assertEquals(new BigDecimal("95.03"), simple03.getSubTotal());
		assertEquals(new BigDecimal("0"), simple03.getDeliveryCost());

		// now add a 10USD oder-level discount
		// total is 100.03 - 10 = 90.03
		getRuleEngineCalculationService().addOrderLevelDiscount(simple03, true, new BigDecimal("10.00"));
		assertEquals(new BigDecimal("0"), simple03.getDeliveryCost());
		assertEquals(new BigDecimal("95.03"), simple03.getSubTotal());
		assertEquals(new BigDecimal("85.03"), simple03.getTotal());
	}

	@Test
	public void testAddOrderEntryLevelDiscountPercentage()
	{
		// simple cart with 2 entries and delivery cost
		final CartRAO simple04 = createCartRAO("simple04", VND);
		final OrderEntryRAO orderEntry1 = createOrderEntryRAO(simple04, "12.34", VND, 2, 0);
		simple04.setEntries(set(orderEntry1, createOrderEntryRAO(simple04, "23.45", VND, 3, 1)));
		simple04.setDeliveryCost(new BigDecimal("5.00"));
		getRuleEngineCalculationService().calculateTotals(simple04);
		assertEquals(new BigDecimal("95.03"), simple04.getTotal());
		assertEquals(new BigDecimal("95.03"), simple04.getSubTotal());
		assertEquals(new BigDecimal("0"), simple04.getDeliveryCost());

		// now add a 10% oder-entry-level discount
		// subtotal is: 12.34*2-rounded_down(12.34*2*0.1) + 23.45*3 = 24.68 - rounded_down(2.468) + 70.35 = 24.68-2.46+70.35=92.57
		// total is: 12.34*2-rounded_down(12.34*2*0.1) + 23.45*3 + 5 = 24.68 - rounded_down(2.468) + 70.35 + 5 = 24.68-2.46+70.35+5 = 97.57
		getRuleEngineCalculationService().addOrderEntryLevelDiscount(orderEntry1, false, new BigDecimal("10.00"));
		assertEquals(new BigDecimal("0"), simple04.getDeliveryCost());
		assertEquals(new BigDecimal("92.57"), simple04.getSubTotal());
		assertEquals(new BigDecimal("92.57"), simple04.getTotal());
	}


	@Test
	public void testAddOrderEntryLevelDiscountAbsolute()
	{
		// simple cart with 2 entries and delivery cost
		final CartRAO simple05 = createCartRAO("simple05", VND);
		final OrderEntryRAO orderEntry1 = createOrderEntryRAO(simple05, "12.34", VND, 2, 0);
		final OrderEntryRAO orderEntry2 = createOrderEntryRAO(simple05, "23.45", VND, 3, 1);

		simple05.setEntries(set(orderEntry1, orderEntry2));
		simple05.setDeliveryCost(new BigDecimal("5.00"));
		getRuleEngineCalculationService().calculateTotals(simple05);
		assertEquals(new BigDecimal("95.03"), simple05.getTotal());
		assertEquals(new BigDecimal("95.03"), simple05.getSubTotal());
		assertEquals(new BigDecimal("0"), simple05.getDeliveryCost());

		// now add a 10USD oder-entry-level discount
		// subtotal is: (12.34-10)*2 + 23.45*3 = 75.03
		// total is: (12.34-10)*2 + 23.45*3 + 5 = 80.03
		getRuleEngineCalculationService().addOrderEntryLevelDiscount(orderEntry1, true, new BigDecimal("10.00"));
		assertEquals(new BigDecimal("0"), simple05.getDeliveryCost());
		assertEquals(new BigDecimal("75.03"), simple05.getSubTotal());
		assertEquals(new BigDecimal("75.03"), simple05.getTotal());
	}


	@Test
	public void testAddOrderEntryLevelFixedPrice()
	{
		// simple cart with 2 entries and delivery cost
		final CartRAO cartRao1 = createCartRAO("cart01", VND); // NOSONAR
		final OrderEntryRAO orderEntry1 = createOrderEntryRAO(cartRao1, "12.50", VND, 1, 0); //NOSONAR
		cartRao1.setEntries(set(orderEntry1, createOrderEntryRAO(cartRao1, "20.25", VND, 2, 1)));
		cartRao1.setDeliveryCost(new BigDecimal("5.00"));

		getRuleEngineCalculationService().calculateTotals(cartRao1);
		assertEquals(new BigDecimal("53.00"), cartRao1.getTotal()); // NOSONAR
		assertEquals(new BigDecimal("53.00"), cartRao1.getSubTotal()); // NOSONAR
		assertEquals(new BigDecimal("0"), cartRao1.getDeliveryCost());

		getRuleEngineCalculationService().addFixedPriceEntryDiscount(orderEntry1, new BigDecimal("10.00"));
		assertEquals(new BigDecimal("50.50"), cartRao1.getTotal());
		assertEquals(new BigDecimal("50.50"), cartRao1.getSubTotal());
		assertEquals(new BigDecimal("0"), cartRao1.getDeliveryCost());
	}


	@Test
	public void testAddOrderEntryLevelFixedPriceError()
	{
		// fixed price amount is greater than the base price for this item so no discount should be created
		final CartRAO cartRao1 = createCartRAO("cart01", VND);
		final OrderEntryRAO orderEntry1 = createOrderEntryRAO(cartRao1, "12.50", VND, 1, 0);
		cartRao1.setEntries(set(orderEntry1, createOrderEntryRAO(cartRao1, "20.25", VND, 2, 1)));
		cartRao1.setDeliveryCost(new BigDecimal("5.00"));

		getRuleEngineCalculationService().calculateTotals(cartRao1);
		assertEquals(new BigDecimal("53.00"), cartRao1.getTotal());
		assertEquals(new BigDecimal("53.00"), cartRao1.getSubTotal());
		assertEquals(new BigDecimal("0"), cartRao1.getDeliveryCost());

		getRuleEngineCalculationService().addFixedPriceEntryDiscount(orderEntry1, new BigDecimal("50"));

		//no discount should be applied because fixed price was higher that original price
		assertEquals(new BigDecimal("53.00"), cartRao1.getTotal());
		assertEquals(new BigDecimal("53.00"), cartRao1.getSubTotal());
		assertEquals(new BigDecimal("0"), cartRao1.getDeliveryCost());
	}

	@Test
	public void testExcludedProducts()
	{
		final List<ProductRAO> excludedProducts = new ArrayList();
		final ProductRAO prodRao1 = new ProductRAO();
		prodRao1.setId(13579l );
		excludedProducts.add(prodRao1);

		final CartRAO cartRao1 = createCartRAO("cart01", VND);
		final OrderEntryRAO orderEntry1 = createOrderEntryRAO(cartRao1, "12.50", VND, 1, 0);
		orderEntry1.getProduct().setId(13579l);
		final OrderEntryRAO orderEntry2 = createOrderEntryRAO(cartRao1, "24.00", VND, 1, 1);
		orderEntry2.getProduct().setId(24680l);
		cartRao1.setEntries(set(orderEntry1, orderEntry2));
		cartRao1.setDeliveryCost(new BigDecimal("5.00"));

		getRuleEngineCalculationService().calculateTotals(cartRao1);
		assertEquals(new BigDecimal("36.50"), cartRao1.getTotal());
		assertEquals(new BigDecimal("36.50"), cartRao1.getSubTotal());
		assertEquals(new BigDecimal("0"), cartRao1.getDeliveryCost());

		final BigDecimal result = getRuleEngineCalculationService().calculateSubTotals(cartRao1, excludedProducts);

		//sub total without the excluded products
		assertEquals(new BigDecimal("24.00"), result);

		//Original cartRao is unchanged
		assertEquals(new BigDecimal("36.50"), cartRao1.getTotal());
		assertEquals(new BigDecimal("36.50"), cartRao1.getSubTotal());
		assertEquals(new BigDecimal("0"), cartRao1.getDeliveryCost());

	}

	@Test
	public void testConvertPercentageDiscountToAbsoluteDiscountDiscountAvailableItems()
	{
		BigDecimal percentageAmount = BigDecimal.valueOf(0.0);
		final Currency usdCurrency = Currency.valueOf("USD", 2);
		NumberedLineItem orderLineItem = new NumberedLineItem(new Money("12.50", usdCurrency), 2);
		BigDecimal adjustedAbsoluteAmount = getRuleEngineCalculationService().convertPercentageDiscountToAbsoluteDiscount(
				percentageAmount, 2, orderLineItem);
		assertThat(adjustedAbsoluteAmount.stripTrailingZeros()).isEqualTo("0");

		percentageAmount = BigDecimal.valueOf(100.0);
		adjustedAbsoluteAmount = getRuleEngineCalculationService().convertPercentageDiscountToAbsoluteDiscount(percentageAmount, 2,
				orderLineItem);
		assertThat(adjustedAbsoluteAmount.stripTrailingZeros()).isEqualTo("25");

		percentageAmount = BigDecimal.valueOf(100.0);
		adjustedAbsoluteAmount = getRuleEngineCalculationService().convertPercentageDiscountToAbsoluteDiscount(percentageAmount, 1,
				orderLineItem);
		assertThat(adjustedAbsoluteAmount.stripTrailingZeros()).isEqualTo("12.5");

		percentageAmount = BigDecimal.valueOf(50.0);
		adjustedAbsoluteAmount = getRuleEngineCalculationService().convertPercentageDiscountToAbsoluteDiscount(percentageAmount, 1,
				orderLineItem);
		assertThat(adjustedAbsoluteAmount.stripTrailingZeros()).isEqualTo("6.25");

		percentageAmount = BigDecimal.valueOf(50.0);
		orderLineItem = new NumberedLineItem(new Money("12.50", usdCurrency), 2);
		final LineItemDiscount lineItemDiscount = new LineItemDiscount(new Money("2.0", usdCurrency), true, 1);
		orderLineItem.addDiscount(lineItemDiscount);
		adjustedAbsoluteAmount = getRuleEngineCalculationService().convertPercentageDiscountToAbsoluteDiscount(percentageAmount, 1,
				orderLineItem);
		assertThat(adjustedAbsoluteAmount.stripTrailingZeros()).isEqualTo("6.25");
	}

	@Test
	public void testConvertPercentageDiscountToAbsoluteDiscountDiscountConsumedItems()
	{
		final Currency usdCurrency = Currency.valueOf("USD", 2);

		final BigDecimal percentageAmount = BigDecimal.valueOf(50.0);
		NumberedLineItem orderLineItem = new NumberedLineItem(new Money("12.50", usdCurrency), 2);

		final CalculationStrategies calculationStrategies = new CalculationStrategies(new DefaultRoundingStrategy() ,
                new DefaultTaxRoundingStrategy());

		final Order order = new Order(usdCurrency, calculationStrategies);
		orderLineItem.setOrder(order);
		LineItemDiscount lineItemDiscount = new LineItemDiscount(new Money("2.0", usdCurrency), true, 2);
		orderLineItem.addDiscount(lineItemDiscount);
		BigDecimal adjustedAbsoluteAmount = getRuleEngineCalculationService().convertPercentageDiscountToAbsoluteDiscount(
				percentageAmount, 1, orderLineItem);
		assertThat(adjustedAbsoluteAmount.stripTrailingZeros()).isEqualTo("5.25");

		adjustedAbsoluteAmount = getRuleEngineCalculationService().convertPercentageDiscountToAbsoluteDiscount(percentageAmount, 2,
				orderLineItem);
		assertThat(adjustedAbsoluteAmount.stripTrailingZeros()).isEqualTo("10.5");

		orderLineItem = new NumberedLineItem(new Money("12.50", usdCurrency), 2);
		orderLineItem.setOrder(order);
		lineItemDiscount = new LineItemDiscount(new Money("2.0", usdCurrency), true, 1);
		orderLineItem.addDiscount(lineItemDiscount);
		adjustedAbsoluteAmount = getRuleEngineCalculationService().convertPercentageDiscountToAbsoluteDiscount(percentageAmount, 2,
				orderLineItem);
		assertThat(adjustedAbsoluteAmount.stripTrailingZeros()).isEqualTo("11.5");
	}


}
