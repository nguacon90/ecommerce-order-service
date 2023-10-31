package com.vctek.orderservice.promotionengine.ruleengineservice.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.OrderEntryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.*;
import com.vctek.orderservice.service.ModelService;
import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OrderUtils {
    private ModelService modelService;

    public OrderCharge createShippingCharge(Currency currency, boolean absolute, BigDecimal value) {
        Object amount;
        if (absolute) {
            amount = new Money(value, currency);
        } else {
            amount = new Percentage(value);
        }

        return new OrderCharge((AbstractAmount)amount, ChargeType.SHIPPING);
    }

    public void updateOrderQuantities(OrderModel order, Map<Integer, Long> quantities) {
        Preconditions.checkArgument(order != null, "cart cannot be null");
        if (MapUtils.isNotEmpty(quantities)) {
            Collection<OrderEntryModel> toRemove = Lists.newArrayList();
            Collection<OrderEntryModel> toSave = Lists.newArrayList();
            Iterator var6 = this.getEntryQuantityMap(order, quantities).entrySet().iterator();

            while(true) {
                while(var6.hasNext()) {
                    Map.Entry<OrderEntryModel, Long> e = (Map.Entry)var6.next();
                    OrderEntryModel cartEntry = e.getKey();
                    Long quantity = e.getValue();
                    if (quantity != null && quantity >= 1L) {
                        cartEntry.setQuantity(quantity);
                        toSave.add(cartEntry);
                    } else {
                        toRemove.add(cartEntry);
                    }
                }

                order.getEntries().removeAll(toRemove);
                this.modelService.saveAll(toSave);
                break;
            }
        }

    }

    protected Map<OrderEntryModel, Long> getEntryQuantityMap(OrderModel order, Map<Integer, Long> quantities) {
        List<AbstractOrderEntryModel> entries = order.getEntries();
        return quantities.entrySet().stream().collect(Collectors.toMap((e) ->
                this.getEntry(entries, e.getKey()), (e) -> (Long)e.getValue()));
    }

    protected OrderEntryModel getEntry(List<AbstractOrderEntryModel> entries, Integer entryNumber) {
        return (OrderEntryModel) entries.stream().filter((e) -> entryNumber.equals(e.getEntryNumber()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("no cart entry found with entry number "
                        + entryNumber + " (got " + entries + ")"));
    }

    protected ModelService getModelService() {
        return this.modelService;
    }

    @Autowired
    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }
}
