package com.vctek.orderservice.promotionengine.ruleengineservice.rao.util;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.PaymentModeRAO;
import com.vctek.orderservice.promotionengine.util.CurrencyIsoCode;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;


public class CartTestContextBuilder
{
	private static final String CURRENCY_ISO_CODE = "USD";

	private final CartModel cartModel;
	private final CartRAO cartRAO;
	private PaymentModeRAO paymentModeRAO;

	public CartTestContextBuilder()
	{
		cartModel = new CartModel();
		cartModel.setId(Calendar.getInstance().getTimeInMillis());
		cartModel.setCode(UUID.randomUUID().toString());
		cartModel.setCurrencyCode(CurrencyIsoCode.VND.toString());
		cartRAO = new CartRAO();
	}

	public CartModel getCartModel()
	{
		return cartModel;
	}

	public CartRAO getCartRAO()
	{
		return cartRAO;
	}

	public PaymentModeRAO getPaymentModeRAO()
	{
		return paymentModeRAO;
	}

	public CartTestContextBuilder withPaymentModeRAO(final String code)
	{
		paymentModeRAO = new PaymentModeRAO();
		cartRAO.setPaymentMode(paymentModeRAO);
		return this;
	}

	public CartTestContextBuilder withEntries(final List<AbstractOrderEntryModel> entries)
	{
		cartModel.setEntries(entries);
		return this;
	}

	public CartTestContextBuilder addEntry(final AbstractOrderEntryModel entry)
	{
		entry.setOrder(cartModel);
		List<AbstractOrderEntryModel> entries = cartModel.getEntries();
		if (CollectionUtils.isEmpty(entries))
		{
			entries = new ArrayList<>();
			cartModel.setEntries(entries);
		}
		entries.add(entry);
		return this;
	}

	public CartTestContextBuilder addNewEntry()
	{
		final AbstractOrderEntryModel entry = new AbstractOrderEntryModel();
		return addEntry(entry);
	}

	public CartTestContextBuilder addNewEntry(final Long productId)
	{
		final AbstractOrderEntryModel entry = new AbstractOrderEntryModel();
		entry.setProductId(productId);
		return addEntry(entry);
	}
//
//	public CartTestContextBuilder addNewEntry(final CategoryModel... categoryModels)
//	{
//		final AbstractOrderEntryModel entry = new AbstractOrderEntryModel();
//		final ProductModel product = new ProductModel();
//		entry.setProduct(product);
//		if (ArrayUtils.isNotEmpty(categoryModels))
//		{
//			product.setSupercategories(Arrays.asList(categoryModels));
//		}
//		return addEntry(entry);
//	}

	public CartTestContextBuilder withDiscounts(Double discount, String discountType)
	{
		cartModel.setDiscount(discount);
		cartModel.setDiscountType(discountType);
		return this;
	}
//
//	public CartTestContextBuilder addDiscount(final DiscountModel discount)
//	{
//		List<DiscountModel> discounts = cartModel.getDiscounts();
//		if (CollectionUtils.isEmpty(discounts))
//		{
//			discounts = new ArrayList<>();
//			cartModel.setDiscounts(discounts);
//		}
//		discounts.add(discount);
//		return this;
//	}

	public CartTestContextBuilder withTotalPrice(final Double totalPrice)
	{
		cartModel.setTotalPrice(totalPrice);
		return this;
	}

	public CartTestContextBuilder withSubtotal(final Double subTotal)
	{
		cartModel.setSubTotal(subTotal);
		return this;
	}

	public CartTestContextBuilder withDeliveryCost(final Double deliveryCost)
	{
		cartModel.setDeliveryCost(deliveryCost);
		return this;
	}

    public CartTestContextBuilder withUser(final long userId)
    {
        cartModel.setCustomerId(userId);
        return this;
    }

	public CartTestContextBuilder withPaymentCost(final Double paymentCost)
	{
		cartModel.setPaymentCost(paymentCost);
		return this;
	}

	public CartTestContextBuilder withCurrency(final String currencyIsoCode)
	{
		cartModel.setCurrencyCode(currencyIsoCode);
		return this;
	}

}
