package com.vctek.orderservice.promotionengine.ruleengineservice.converter;

import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.AbstractRuleEngineTest;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.Order;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.DiscountRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.OrderEntryRAO;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;


public class AbstractOrderRaoToOrderConverterTest extends AbstractRuleEngineTest
{
	@Test
	public void testCartToOrderConversion()
	{
		final CartRAO cart = createCartRAO("cart_code", VND);
		final DiscountRAO orderDiscount = createDiscount(cart, "1.00", VND);
		final OrderEntryRAO orderEntry1 = createOrderEntryRAO(cart, "12.34", VND, 2, 0);
		final DiscountRAO orderEntry1Discount = createDiscount(orderEntry1, "1.34", VND);
		cart.setEntries(set(orderEntry1, createOrderEntryRAO(cart, "23.45", VND, 3, 1)));
		final BigDecimal deliveryCost = new BigDecimal("5.00");
		cart.setDeliveryCost(deliveryCost);
		final BigDecimal paymentCost = new BigDecimal("3.00");
		cart.setPaymentCost(paymentCost);

		final Order conversionResult = getCartRaoToOrderConverter().convert(cart);
		// check currency:
		Assert.assertEquals(VND, conversionResult.getCurrency().getIsoCode());
		Assert.assertEquals(0, conversionResult.getCharges().size());
		// check charges:
//		boolean shippingChargePresent = false, paymentChargePresent = false;
//		for (final OrderCharge charge : conversionResult.getCharges())
//		{
//			if (ChargeType.SHIPPING.equals(charge.getChargeType()))
//			{
//				shippingChargePresent = true;
//				Assert.assertEquals(deliveryCost, ((Money) charge.getAmount()).getAmount());
//			}
//			if (ChargeType.PAYMENT.equals(charge.getChargeType()))
//			{
//				paymentChargePresent = true;
//				Assert.assertEquals(paymentCost, ((Money) charge.getAmount()).getAmount());
//			}
//		}
//		Assert.assertTrue("Payment charge is absent!", paymentChargePresent);
//		Assert.assertTrue("Shipping charge is absent!", shippingChargePresent);
//		// check discounts:
//		Assert.assertEquals(1, conversionResult.getDiscounts().size());
//		final Money discountMoney = (Money) conversionResult.getDiscounts().get(0).getAmount();
//		Assert.assertEquals(orderDiscount.getValue(), discountMoney.getAmount());
//		Assert.assertEquals(orderDiscount.getCurrencyIsoCode(), discountMoney.getCurrency().getIsoCode());
//		// check line items:
//		Assert.assertEquals(2, conversionResult.getLineItems().size());
//		boolean checkedEntry1 = false;
//		for (final LineItem li : conversionResult.getLineItems())
//		{
//			if (li.getNumberOfUnits() == orderEntry1.getQuantity()
//					&& li.getBasePrice().getAmount().equals(orderEntry1.getBasePrice())
//					&& li.getBasePrice().getCurrency().getIsoCode().equals(orderEntry1.getCurrencyIsoCode()))
//			{
//				checkedEntry1 = true;
//				// line item discounts:
//				Assert.assertEquals(1, li.getDiscounts().size());
//				final Money discountEntryMoney = (Money) li.getDiscounts().get(0).getAmount();
//				Assert.assertEquals(orderEntry1Discount.getValue(), discountEntryMoney.getAmount());
//				Assert.assertEquals(orderEntry1Discount.getCurrencyIsoCode(), discountEntryMoney.getCurrency().getIsoCode());
//			}
//		}
//		Assert.assertTrue("Order entry 1 not found!", checkedEntry1);
	}
}
