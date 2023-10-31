package com.vctek.orderservice.promotionengine.ruleengineservice.rao.util;

import com.vctek.orderservice.promotionengine.ruleengineservice.rao.*;
import com.vctek.orderservice.promotionengine.util.CurrencyIsoCode;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.LinkedHashSet;


public class CartRAOBuilder {
    public static final String DEFAULT_CURRENCY_ISO_CODE = CurrencyIsoCode.VND.toString();
    private final CartRAO cartRAO;
    private ProductRAO lastProduct;
    private OrderEntryRAO lastOrderEntry;
    private static final DefaultRaoService raoService = new DefaultRaoService();


    public CartRAOBuilder() {
        this("" + Math.random());
    }

    public CartRAOBuilder(final String cartId) {
        this(cartId, DEFAULT_CURRENCY_ISO_CODE);
    }

    public CartRAOBuilder(final String cartId, final String currencyIsoCode) {
        this(raoService.createCart());
        cartRAO.setCode(cartId);
        //		cartRAO.setOrderCode(cartId);
        cartRAO.setTotal(BigDecimal.ZERO);
        cartRAO.setActions(new LinkedHashSet<>());
        cartRAO.setEntries(new HashSet<>());
        cartRAO.setCurrencyIsoCode(currencyIsoCode);
    }

    public CartRAOBuilder(final CartRAO cart) {
        this.cartRAO = cart;
    }

    public CartRAOBuilder addProductLine(final Long productId, final int quantity, final double price,
                                         final Long... categories) {
        lastProduct = raoService.createProduct();
        lastProduct.setId(productId);

        for (final Long category : categories) {
            final CategoryRAO categoryToAdd = new CategoryRAO();
            categoryToAdd.setCode(category);
            if (!lastProduct.getCategories().contains(categoryToAdd)) {
                lastProduct.getCategories().add(categoryToAdd);
            }
        }
        return addProductQuantity(lastProduct, quantity, price);
    }

    public CartRAOBuilder addProductQuantity(final ProductRAO product, final int quantity, final double price) {
        lastOrderEntry = raoService.createOrderEntry();
        lastOrderEntry.setProduct(product);
        lastOrderEntry.setQuantity(quantity);
        lastOrderEntry.setBasePrice(BigDecimal.valueOf(price));

        return addEntry(lastOrderEntry);
    }


    public CartRAOBuilder addCartDiscount(final boolean absolute, final double value) {
        final DiscountRAO discountRAO = createDiscount(value);
        if (absolute) {
            discountRAO.setCurrencyIsoCode(cartRAO.getCurrencyIsoCode());
        }
        cartRAO.getActions().add(discountRAO);
        return this;
    }

    private DiscountRAO createDiscount(final double value) {
        final DiscountRAO discountRAO = raoService.createDiscount();
        discountRAO.setValue(BigDecimal.valueOf(value));
        return discountRAO;
    }

    public CartRAOBuilder addProductDiscount(final boolean absolute, final double value) {
        final DiscountRAO discountRAO = createDiscount(value);
        if (absolute) {
            discountRAO.setCurrencyIsoCode(cartRAO.getCurrencyIsoCode());
        }
        getLastOrderEntry().getActions().add(discountRAO);
        return this;
    }

    public CartRAOBuilder addEntry(final OrderEntryRAO rao) {
        cartRAO.getEntries().add(rao);
        return this;
    }

    public CartRAO toCart() {
        return cartRAO;
    }

    public OrderEntryRAO getLastOrderEntry() {
        return lastOrderEntry;
    }

    public ProductRAO getLastProduct() {
        return lastProduct;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
