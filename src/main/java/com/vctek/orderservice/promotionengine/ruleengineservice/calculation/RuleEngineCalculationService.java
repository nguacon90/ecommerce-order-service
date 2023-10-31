package com.vctek.orderservice.promotionengine.ruleengineservice.calculation;

import com.vctek.orderservice.promotionengine.ruleengineservice.rao.*;
import com.vctek.redis.ProductData;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RuleEngineCalculationService {
    void calculateTotals(AbstractOrderRAO rao);

    DiscountRAO addOrderLevelDiscount(AbstractOrderRAO orderRAO, boolean absolute, BigDecimal amount);

    int getProductAvailableQuantityInOrderEntry(OrderEntryRAO orderEntryRAO);

    BigDecimal calculateSubTotals(CartRAO cartRAO, Collection<ProductRAO> excludedProducts);

    int getConsumedQuantityForOrderEntry(OrderEntryRAO orderEntryRao);

    DiscountRAO addOrderEntryLevelDiscount(OrderEntryRAO orderEntryRao, boolean absolute, BigDecimal amount);

    BigDecimal getAdjustedUnitPrice(int quantity, OrderEntryRAO orderEntryRao);

    FreeProductRAO addFreeProductsToCart(CartRAO cartRao, ProductData product, Integer quantity);

    DiscountRAO addFixedPriceEntryDiscount(OrderEntryRAO orderEntryRao, BigDecimal valueForCurrency);

    FixedPriceProductRAO addFixedPriceEntryDiscount(OrderEntryRAO orderEntryRao, BigDecimal valueForCurrency, int qty);

    FixedPriceProductRAO addFixedPriceEntry(OrderEntryRAO orderEntryRao, BigDecimal fixedPrice);

    DiscountRAO addOrderEntryLevelDiscountWithConsumableQty(OrderEntryRAO orderEntryRao, boolean b, BigDecimal value, int qty);

    List<DiscountRAO> addOrderEntryLevelDiscount(Map<Integer, Integer> selectedOrderEntryMap, Set<OrderEntryRAO> selectedOrderEntryRaos, boolean absolute, BigDecimal amount);

}
