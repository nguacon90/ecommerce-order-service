package com.vctek.orderservice.promotionengine.ruleengineservice.rao.util;

import com.vctek.orderservice.promotionengine.ruleengineservice.rao.*;

import java.math.BigDecimal;
import java.util.LinkedHashSet;

public class DefaultRaoService
{
	public void addCartDiscount(final boolean absolute, final double value, final CartRAO cart)
	{
		final DiscountRAO discount = createDiscount(value);
		if (absolute)
		{
			discount.setCurrencyIsoCode(cart.getCurrencyIsoCode());
		}
		addCartDiscount(discount, cart);
	}

	protected void addCartDiscount(final DiscountRAO discount, final CartRAO cart)
	{
		if (cart.getActions() == null)
		{
			cart.setActions(new LinkedHashSet<>());
		}
		cart.getActions().add(discount);
	}

	public void addEntry(final OrderEntryRAO entry, final CartRAO cart)
	{
		if (cart.getEntries() == null)
		{
			cart.setEntries(new LinkedHashSet<>());
		}
		cart.getEntries().add(entry);
		entry.setOrder(cart);
	}

	public void addEntryDiscount(final boolean absolute, final double value, final OrderEntryRAO entry)
	{
		final DiscountRAO discount = createDiscount(value);
		if (absolute)
		{
			discount.setCurrencyIsoCode(entry.getOrder().getCurrencyIsoCode());
		}
		addEntryDiscount(discount, entry);
	}

	protected void addEntryDiscount(final DiscountRAO discount, final OrderEntryRAO entry)
	{
		if (entry.getActions() == null)
		{
			entry.setActions(new LinkedHashSet<>());
		}
		entry.getActions().add(discount);
	}

	public CartRAO createCart()
	{
		final CartRAO cart = new CartRAO();
		cart.setOriginalTotal(BigDecimal.ZERO);
		cart.setActions(new LinkedHashSet<>());
		cart.setEntries(new LinkedHashSet<>());
		cart.setTotal(BigDecimal.ZERO);
		return cart;
	}

//	public EntriesSelectionStrategyRPD createEntriesSelectionStrategyRPD()
//	{
//		final EntriesSelectionStrategyRPD entriesSelectionStrategyRPD = new EntriesSelectionStrategyRPD();
//		entriesSelectionStrategyRPD.setOrderEntries(new ArrayList<OrderEntryRAO>());
//		return entriesSelectionStrategyRPD;
//	}

	public CategoryRAO createCategory()
	{
		return new CategoryRAO();
	}

	public DiscountRAO createDiscount()
	{
		final DiscountRAO discount = new DiscountRAO();
		discount.setValue(BigDecimal.ZERO);
		return discount;
	}

	protected DiscountRAO createDiscount(final double value)
	{
		final DiscountRAO discount = createDiscount();
		discount.setValue(BigDecimal.valueOf(value));
		return discount;
	}

	public OrderEntryRAO createOrderEntry()
	{
		final OrderEntryRAO orderEntry = new OrderEntryRAO();
		orderEntry.setBasePrice(BigDecimal.ZERO);
		orderEntry.setActions(new LinkedHashSet<>());
		return orderEntry;
	}

	public OrderEntryRAO createOrderEntry(final AbstractOrderRAO order, final ProductRAO product, final double basePrice,
                                          final int quantity, final int entryNumber)
	{
		final OrderEntryRAO orderEntry = new OrderEntryRAO();
		orderEntry.setProduct(product);
		orderEntry.setBasePrice(BigDecimal.valueOf(basePrice));
		orderEntry.setQuantity(quantity);
		orderEntry.setOrder(order);
		orderEntry.setEntryNumber(Integer.valueOf(entryNumber));
		return orderEntry;
	}

	public ProductRAO createProduct()
	{
		final ProductRAO product = new ProductRAO();
		product.setCategories(new LinkedHashSet<>());
		return product;
	}

	public ProductRAO createProduct(final Long productId)
	{
		final ProductRAO p = createProduct();
		p.setId(productId);
		return p;
	}

	public void addPromotedProduct(final Long productId, final int quantity, final double basePrice, final double value,
			final CartRAO cart)
	{
		addPromotedProduct(createProduct(productId), quantity, basePrice, value, cart);
	}

	public void addPromotedProduct(final ProductRAO promotedProduct, final int quantity, final double basePrice,
			final double promotionValue, final CartRAO cart)
	{
		final OrderEntryRAO discountedEntry = addProduct(promotedProduct, quantity, basePrice, cart);
		addEntryDiscount(true, promotionValue, discountedEntry);

	}

	public OrderEntryRAO addProduct(final ProductRAO promotedProduct, final int quantity, final double basePrice,
			final CartRAO cart)
	{
		final OrderEntryRAO entry = createOrderEntry(cart, promotedProduct, basePrice, quantity, getNewOrderEntryNumber(cart));
		addEntry(entry, cart);
		return entry;
	}

	protected int getNewOrderEntryNumber(final CartRAO cart)
	{
		Integer maxEntryNumber = null;
		if (cart.getEntries() == null)
		{
			return 0;
		}
		for (final OrderEntryRAO entry : cart.getEntries())
		{
			if (entry.getEntryNumber() != null)
			{
				if (maxEntryNumber == null)
				{
					maxEntryNumber = Integer.valueOf(0);
				}
				if (maxEntryNumber.compareTo(entry.getEntryNumber()) < 0)
				{
					maxEntryNumber = entry.getEntryNumber();
				}
			}
		}
		return (maxEntryNumber != null) ? maxEntryNumber.intValue() : 0;
	}

	public OrderEntryRAO addProduct(final Long productId, final int quantity, final double basePrice, final CartRAO cart)
	{
		final ProductRAO product = createProduct(productId);
		return addProduct(product, quantity, basePrice, cart);
	}

}
