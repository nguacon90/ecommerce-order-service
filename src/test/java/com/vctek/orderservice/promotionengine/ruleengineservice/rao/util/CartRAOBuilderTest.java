package com.vctek.orderservice.promotionengine.ruleengineservice.rao.util;

import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CartRAOBuilderTest {

    private CartRAOBuilder builder;

    @Before
    public void setUp() {
        builder = new CartRAOBuilder();
    }

    @Test
    public void testBasicBuilder() {
        final CartRAO cart = builder.toCart();
        Assert.assertNotNull(cart.getTotal());
        Assert.assertNotNull(cart.getActions());
        Assert.assertTrue(cart.getActions().isEmpty());
        Assert.assertNotNull(cart.getEntries());
        Assert.assertTrue(cart.getEntries().isEmpty());
    }

}
