package com.vctek.orderservice.promotionengine.ruleengineservice.rao;

import java.math.BigDecimal;

public class FixedPriceProductRAO extends DiscountRAO {
    private BigDecimal fixedPrice;
    private OrderEntryRAO orderEntryRAO;

    public BigDecimal getFixedPrice() {
        return fixedPrice;
    }

    public void setFixedPrice(BigDecimal fixedPrice) {
        this.fixedPrice = fixedPrice;
    }

    public OrderEntryRAO getOrderEntryRAO() {
        return orderEntryRAO;
    }

    public void setOrderEntryRAO(OrderEntryRAO orderEntryRAO) {
        this.orderEntryRAO = orderEntryRAO;
    }
}
