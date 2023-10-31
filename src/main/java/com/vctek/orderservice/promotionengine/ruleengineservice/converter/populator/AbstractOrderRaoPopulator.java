package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.CustomerGroupData;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.*;
import com.vctek.orderservice.repository.EntryRepository;
import com.vctek.orderservice.service.CRMService;
import com.vctek.orderservice.service.CartService;
import com.vctek.orderservice.service.CustomerService;
import com.vctek.orderservice.util.CurrencyUtils;
import com.vctek.util.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractOrderRaoPopulator<T extends AbstractOrderModel, P extends AbstractOrderRAO>
        implements Populator<T, P> {
    private CartService cartService;
    private EntryRepository entryRepository;
    private Populator<AbstractOrderEntryModel, OrderEntryRAO> orderEntryRaoPopulator;
    private CustomerService customerService;
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOrderRaoPopulator.class);

    public void populate(T source, P target) {
        if (target.getActions() == null) {
            target.setActions(new LinkedHashSet());
        }

        target.setCurrencyIsoCode(source.getCurrencyCode());
        target.setId(source.getId());
        target.setCreatedDate(Calendar.getInstance().getTime());
        target.setCode(source.getCode());
        target.setType(source.getType());
        target.setFixedOrderDiscount(source.getFixedDiscount() == null ? BigDecimal.ZERO : BigDecimal.valueOf(source.getFixedDiscount()));
        target.setTotal(Objects.isNull(source.getTotalPrice()) ? BigDecimal.ZERO : BigDecimal.valueOf(source.getTotalPrice()));
        target.setSubTotal(Objects.isNull(source.getSubTotal()) ? BigDecimal.ZERO : BigDecimal.valueOf(source.getSubTotal()));
        Double deliveryCost = CommonUtils.readValue(source.getDeliveryCost());
        target.setDeliveryCost(BigDecimal.valueOf(deliveryCost));
        target.setPaymentCost(Objects.isNull(source.getPaymentCost()) ? BigDecimal.ZERO : BigDecimal.valueOf(source.getPaymentCost()));
        List<AbstractOrderEntryModel> entries = getEntries(source);
        if (CollectionUtils.isNotEmpty(entries)) {
            List<AbstractOrderEntryModel> entryModels = entries.stream()
                    .filter(e -> cartService.isValidEntryForPromotion(e)).collect(Collectors.toList());
            List<OrderEntryRAO> list = new ArrayList<>();
            OrderEntryRAO entryRAO;
            for (AbstractOrderEntryModel entry : entryModels) {
                entryRAO = new OrderEntryRAO();
                orderEntryRaoPopulator.populate(entry, entryRAO);
                calculateToppingPriceForEntryRao(entry, entryRAO);
                list.add(entryRAO);
            }
            list.forEach((entry) -> entry.setOrder(target));
            target.setEntries(new LinkedHashSet(list));
            if (entryModels.size() != entries.size()) {
                recalculateOrderRao(source, target);
            }
        } else {
            LOGGER.warn("Order entry list is empty, skipping the conversion");
        }

        if (source.getDiscount() != null) {
            target.setDiscountValue(this.convertDiscount(source));
        } else {
            LOGGER.warn("Order discount list is empty, skipping the conversion");
        }

        this.convertAndSetUser(target, source.getCustomerId());
//        this.convertAndSetPaymentMode(target, source.getPaymentMode());

        convertAndSetWareHouse(target, source.getWarehouseId());
        convertAndSetPriceType(target, source.getPriceType());
        convertAndSetOrderSource(target, source.getOrderSourceModel());
    }

    private void convertAndSetOrderSource(P target, OrderSourceModel orderSource) {
        if(orderSource != null) {
            target.setOrderSource(orderSource.getId());
        }
    }

    private void convertAndSetPriceType(P target, String priceType) {
        target.setPriceType(priceType);
    }

    private List<AbstractOrderEntryModel> getEntries(T source) {
        try {
            return entryRepository.findAllByOrder(source);
        } catch (InvalidDataAccessApiUsageException e) {
            return source.getEntries();
        }
    }

    private void calculateToppingPriceForEntryRao(AbstractOrderEntryModel entry, OrderEntryRAO entryRAO) {
        Set<ToppingOptionModel> toppingOptionModels = entry.getToppingOptionModels();
        if (CollectionUtils.isEmpty(toppingOptionModels)) {
            return;
        }

        double totalToppingPrice = 0;
        double totalToppingFixedDiscount = 0;
        for (ToppingOptionModel opt : toppingOptionModels) {
            Set<ToppingItemModel> toppingItemModels = opt.getToppingItemModels();
            if (CollectionUtils.isEmpty(toppingItemModels)) {
                return;
            }

            int optionQty = CommonUtils.readValue(opt.getQuantity());
            for (ToppingItemModel item : toppingItemModels) {
                totalToppingPrice += CommonUtils.readValue(item.getBasePrice()) *
                        CommonUtils.readValue(item.getQuantity()) * optionQty;
                if (item.getDiscount() != null && StringUtils.isNotEmpty(item.getDiscountType())) {
                    totalToppingFixedDiscount += CurrencyUtils.computeValue(item.getDiscount(),
                            item.getDiscountType(), item.getBasePrice());
                }
            }
        }

        entryRAO.setTotalToppingFixedDiscount(BigDecimal.valueOf(totalToppingFixedDiscount));
        entryRAO.setTotalToppingPrice(BigDecimal.valueOf(totalToppingPrice));
    }

    private void recalculateOrderRao(T source, P target) {
        List<AbstractOrderEntryModel> ignoreEntries = getEntries(source).stream()
                .filter(e -> !cartService.isValidEntryForPromotion(e))
                .collect(Collectors.toList());
        double subTotalIgnoreEntry = 0;

        for (AbstractOrderEntryModel ignoreEntry : ignoreEntries) {
            subTotalIgnoreEntry += CommonUtils.readValue(ignoreEntry.getFinalPrice());
        }
        double suTotal = target.getSubTotal().doubleValue() - subTotalIgnoreEntry;
        double total = target.getTotal().doubleValue() - subTotalIgnoreEntry;
        target.setTotal(BigDecimal.valueOf(total));
        target.setSubTotal(BigDecimal.valueOf(suTotal));
    }

    protected void convertAndSetUser(P target, Long customerId) {
        if (customerId != null) {
            UserRAO userRAO = new UserRAO();
            userRAO.setId(customerId);
            target.setUser(userRAO);
            List<CustomerGroupData> customerGroups = customerService.getCustomerGroups(customerId);
            Set<UserGroupRAO> groups = new HashSet<>();
            customerGroups.forEach(cg -> {
                UserGroupRAO userGroupRAO = new UserGroupRAO();
                userGroupRAO.setId(cg.getId());
                groups.add(userGroupRAO);
            });
            userRAO.setGroups(groups);
        }
    }

    protected DiscountValueRAO convertDiscount(T source) {
        DiscountValueRAO discountValueRAO = new DiscountValueRAO();
        discountValueRAO.setDiscountType(source.getDiscountType());
        discountValueRAO.setValue(new BigDecimal(source.getDiscount()));
        return discountValueRAO;
    }

    private void convertAndSetWareHouse(P target, Long warehouseId) {
        target.setWarehouse(warehouseId);
    }

    @Autowired
    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }

    @Autowired
    public void setOrderEntryRaoPopulator(Populator<AbstractOrderEntryModel, OrderEntryRAO> orderEntryRaoPopulator) {
        this.orderEntryRaoPopulator = orderEntryRaoPopulator;
    }

    @Autowired
    public void setEntryRepository(EntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    @Autowired
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }
}
