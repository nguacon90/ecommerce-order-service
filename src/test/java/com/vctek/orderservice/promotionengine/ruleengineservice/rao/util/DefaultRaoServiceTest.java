package com.vctek.orderservice.promotionengine.ruleengineservice.rao.util;

import com.vctek.orderservice.promotionengine.ruleengineservice.rao.*;
import com.vctek.orderservice.promotionengine.ruleengineservice.util.RaoUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;


public class DefaultRaoServiceTest
{

	private static final double DELTA = 0.005d;
	private DefaultRaoService raoService;
	private CartRAO cart;
	private ProductRAO product;
	private OrderEntryRAO entry;
	private ProductRAO promotedProduct;
	private RaoUtils raoUtils;

	/**
     */
	@Before
	public void setUp() {
		raoUtils = new RaoUtils();

		cart = new CartRAO();
		cart.setCurrencyIsoCode("USD");
		product = new ProductRAO();
		entry = new OrderEntryRAO();
		raoService = new DefaultRaoService();
		promotedProduct = new ProductRAO();
		promotedProduct.setId(23456l);
	}

	@Test
	public void testAbsoluteDiscountAddedToCart()
	{
		raoService.addCartDiscount(true, 1.0, cart);

		Assert.assertNotNull(cart.getActions());
		Assert.assertEquals(1, cart.getActions().size());
		final DiscountRAO discountRAO = (DiscountRAO) cart.getActions().stream().findFirst().get();
		Assert.assertEquals(1.0d, discountRAO.getValue().doubleValue(), 0.005);
		Assert.assertTrue(raoUtils.isAbsolute(discountRAO));

	}

	@Test
	public void testNonAbsoluteDiscountAddedToCart()
	{
		raoService.addCartDiscount(false, 0.05, cart);

		Assert.assertNotNull(cart.getActions());
		Assert.assertEquals(1, cart.getActions().size());
		final DiscountRAO discountRAO = (DiscountRAO) cart.getActions().stream().findFirst().get();
		Assert.assertEquals(0.05d, discountRAO.getValue().doubleValue(), 0.005);
		Assert.assertFalse(raoUtils.isAbsolute(discountRAO));

	}


	@Test
	public void testCreatePopulatedOrderEntry()
	{

		final OrderEntryRAO orderEntryRAO = raoService.createOrderEntry(CartRaoBuilderTestUtil.newCart("code").setCurrency("USD").getCart(),
				product, 12.34d, 3, 5);

		Assert.assertEquals(12.34d, orderEntryRAO.getBasePrice().doubleValue(), DELTA);
		Assert.assertEquals(product, orderEntryRAO.getProduct());
		Assert.assertEquals(3, orderEntryRAO.getQuantity());
		Assert.assertEquals(Integer.valueOf(5), orderEntryRAO.getEntryNumber());
		Assert.assertNull(orderEntryRAO.getCurrencyIsoCode());
		Assert.assertNull(orderEntryRAO.getActions());
	}

	@Test
	public void testAddEntryDiscount()
	{
		final OrderEntryRAO entry = raoService.createOrderEntry(CartRaoBuilderTestUtil.newCart("code").setCurrency("USD").getCart(),
				product, 12.34d, 3, 0);

		raoService.addEntryDiscount(true, 4.32d, entry);

		Assert.assertEquals(1, entry.getActions().size());
		final DiscountRAO discount = (DiscountRAO) entry.getActions().stream().findFirst().get();
		Assert.assertEquals(4.32d, discount.getValue().doubleValue(), 0.005);
		Assert.assertTrue(raoUtils.isAbsolute(discount));
		Assert.assertNotNull(discount.getCurrencyIsoCode());
		Assert.assertNull(discount.getFiredRuleCode());
		Assert.assertNull(discount.getActionStrategyKey());
	}

	@Test
	public void testAddEntryToCart()
	{
		Assert.assertNull(cart.getEntries());

		raoService.addEntry(entry, cart);

		Assert.assertNotNull(cart.getEntries());
		Assert.assertEquals(1, cart.getEntries().size());
		final OrderEntryRAO orderEntryRAO = cart.getEntries().stream().findFirst().get();
		Assert.assertEquals(entry, orderEntryRAO);

	}

	@Test
	public void testAddPromotedProduct()
	{
		raoService.addPromotedProduct(promotedProduct, 1, 1.0, 0.0, cart);

		Assert.assertNull(cart.getActions());
		Assert.assertEquals(1, cart.getEntries().size());
		final OrderEntryRAO entry = cart.getEntries().stream().findFirst().get();
		Assert.assertEquals(promotedProduct, entry.getProduct());
		Assert.assertEquals(promotedProduct.getId(), entry.getProduct().getId());
		Assert.assertEquals(1.0d, entry.getBasePrice().doubleValue(), DELTA);
		Assert.assertEquals(1, entry.getQuantity());
		Assert.assertEquals(Integer.valueOf(0), entry.getEntryNumber());
		Assert.assertNotNull(entry.getActions());
		Assert.assertNull(entry.getCurrencyIsoCode());
		final DiscountRAO discount = (DiscountRAO) entry.getActions().stream().findFirst().get();
		Assert.assertTrue(raoUtils.isAbsolute(discount));
		Assert.assertEquals(0.0d, discount.getValue().doubleValue(), DELTA);
		Assert.assertNull(discount.getFiredRuleCode());
		Assert.assertNull(discount.getActionStrategyKey());
	}

	@Test
	public void testCreateProduct()
	{
		final ProductRAO productRao = raoService.createProduct();
		assertBasicProduct(productRao);
	}

	/**
	 * @param productRao
	 */
	private void assertBasicProduct(final ProductRAO productRao)
	{
		Assert.assertNotNull(productRao);
		Assert.assertNotNull(productRao.getCategories());
		Assert.assertTrue(productRao.getCategories().isEmpty());
	}

	@Test
	public void testCreateTypesafeCart()
	{
		final CartRAO cartRao = raoService.createCart();
		Assert.assertNotNull(cartRao.getActions());
		Assert.assertTrue(cartRao.getActions().isEmpty());
		Assert.assertNotNull(cartRao.getOriginalTotal());
		Assert.assertEquals(BigDecimal.ZERO, cartRao.getOriginalTotal());
		Assert.assertNotNull(cartRao.getEntries());
		Assert.assertTrue(cartRao.getEntries().isEmpty());
		Assert.assertNotNull(cartRao.getTotal());
		Assert.assertEquals(BigDecimal.ZERO, cartRao.getTotal());
	}

	@Test
	public void testCreateTypesafeCategory()
	{
		final CategoryRAO categoryRao = raoService.createCategory();
		Assert.assertNotNull(categoryRao);
	}

	@Test
	public void testCreateTypesafeOrderEntry()
	{
		final OrderEntryRAO orderEntryRao = raoService.createOrderEntry();
		Assert.assertNotNull(orderEntryRao);
		Assert.assertNotNull(orderEntryRao.getBasePrice());
		Assert.assertEquals(BigDecimal.ZERO, orderEntryRao.getBasePrice());
		Assert.assertNotNull(orderEntryRao.getActions());
		Assert.assertTrue(orderEntryRao.getActions().isEmpty());

	}

	@Test
	public void testCreateTypesafeDiscount()
	{
		final DiscountRAO discountRao = raoService.createDiscount();
		Assert.assertNotNull(discountRao);
		Assert.assertNotNull(discountRao.getValue());
		Assert.assertEquals(BigDecimal.ZERO, discountRao.getValue());
	}

	@Test
	public void testCreateProductString()
	{
		final ProductRAO productRao = raoService.createProduct(123l);
		assertBasicProduct(productRao);
		Assert.assertEquals(123l, productRao.getId(), 0);
	}
}
