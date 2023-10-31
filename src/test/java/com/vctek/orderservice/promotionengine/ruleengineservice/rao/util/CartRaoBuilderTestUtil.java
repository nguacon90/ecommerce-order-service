package com.vctek.orderservice.promotionengine.ruleengineservice.rao.util;

import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.OrderEntryRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.ProductRAO;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashSet;


public class CartRaoBuilderTestUtil
{
	public static CartRaoDraft newCart(final String code)
	{
		return new CartRaoDraft().setCode(code);
	}

	public static class CartRaoDraft
	{
		private final CartRAO cartRao = new CartRAO();
		{
			cartRao.setActions(new LinkedHashSet<>());
			cartRao.setEntries(new HashSet<>());
		}

		public CartRaoDraft setCode(final String code)
		{
			cartRao.setCode(code);
			return this;
		}

		public CartRaoDraft setCurrency(final String currencyIsoCode)
		{
			cartRao.setCurrencyIsoCode(currencyIsoCode);
			return this;
		}

		public CartRaoDraft setTotal(final BigDecimal total)
		{
			cartRao.setTotal(total);
			return this;
		}

		public CartRaoDraft setOriginalTotal(final BigDecimal total)
		{
			cartRao.setOriginalTotal(total);
			return this;
		}

		public CartRaoDraft addEntry(final ProductRAO product, final int quantity, final BigDecimal basePrice)
		{
			final OrderEntryRAO entry = new OrderEntryRAO();
			entry.setProduct(product);
			entry.setQuantity(quantity);
			entry.setOrder(cartRao);
			entry.setCurrencyIsoCode(cartRao.getCurrencyIsoCode());
			entry.setBasePrice(basePrice);
			cartRao.getEntries().add(entry);
			return this;
		}

		public CartRAO getCart()
		{
			return cartRao;
		}
	}
}
