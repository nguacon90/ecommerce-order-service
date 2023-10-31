package com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.impl;

import com.vctek.converter.Converter;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.RuleEngineCalculationService;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.*;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.providers.RAOFactsExtractor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("cartRAOProvider")
public class DefaultCartRAOProvider extends AbstractExpandedRAOProvider<AbstractOrderModel, CartRAO> {
    public static final String INCLUDE_CART = "INCLUDE_CART";
    public static final String EXPAND_ENTRIES = "EXPAND_ENTRIES";
    public static final String EXPAND_CONSUMED = "EXPAND_CONSUMED";
    public static final String EXPAND_PRODUCTS = "EXPAND_PRODUCTS";
    public static final String EXPAND_DISCOUNTS = "EXPAND_DISCOUNTS";
    public static final String EXPAND_AVAILABLE_DELIVERY_MODES = "EXPAND_AVAILABLE_DELIVERY_MODES";
    public static final String EXPAND_CATEGORIES = "EXPAND_CATEGORIES";
    public static final String EXPAND_USERS = "EXPAND_USERS";
    public static final String EXPAND_PAYMENT_MODE = "EXPAND_PAYMENT_MODE";
    public static final String EXPAND_SUPPLIERS = "EXPAND_SUPPLIERS";
    public static final String EXPAND_CONSUMED_BUDGET = "EXPAND_CONSUMED_BUDGET";
    private Converter<AbstractOrderModel, CartRAO> cartRaoConverter;
    private RuleEngineCalculationService ruleEngineCalculationService;

    public DefaultCartRAOProvider(Converter<AbstractOrderModel, CartRAO> cartRaoConverter,
                                  RuleEngineCalculationService ruleEngineCalculationService,
                                  @Qualifier("factExtractorList") List<RAOFactsExtractor> factExtractorList) {
        this.cartRaoConverter = cartRaoConverter;
        this.setFactExtractorList(factExtractorList);
        this.ruleEngineCalculationService = ruleEngineCalculationService;
        this.validOptions = Arrays.asList(INCLUDE_CART, EXPAND_ENTRIES, EXPAND_CONSUMED, EXPAND_PRODUCTS,
                EXPAND_CATEGORIES, EXPAND_USERS, EXPAND_PAYMENT_MODE, EXPAND_AVAILABLE_DELIVERY_MODES, EXPAND_DISCOUNTS,
                EXPAND_SUPPLIERS, EXPAND_CONSUMED_BUDGET);
        this.defaultOptions = Arrays.asList(INCLUDE_CART, EXPAND_ENTRIES, EXPAND_CONSUMED, EXPAND_PRODUCTS,
                EXPAND_CATEGORIES, EXPAND_USERS, EXPAND_PAYMENT_MODE, EXPAND_AVAILABLE_DELIVERY_MODES, EXPAND_SUPPLIERS,
                EXPAND_CONSUMED_BUDGET);
        this.minOptions = Collections.singletonList(INCLUDE_CART);
    }

    @Override
    protected CartRAO createRAO(AbstractOrderModel order) {
        CartRAO rao = this.cartRaoConverter.convert(order);
        this.ruleEngineCalculationService.calculateTotals(rao);
        return rao;
    }

    protected Set<Object> expandRAO(CartRAO order, Collection<String> options) {
        Set<Object> facts = new LinkedHashSet();
        facts.addAll(super.expandRAO(order, options));
        Iterator var5 = options.iterator();

        while(var5.hasNext()) {
            String option = (String)var5.next();
            Set<OrderEntryRAO> entries = order.getEntries();
            switch(option) {
                case INCLUDE_CART:
                    facts.add(order);
                    break;
                case EXPAND_PRODUCTS:
                    this.addProducts(facts, entries);
                    break;
                case EXPAND_USERS:
                    this.addUserGroups(facts, order.getUser());
                    break;
                case EXPAND_CONSUMED:
                    this.addConsumed(facts, order, entries);
                    break;
//                case -858518047:
//                    if (option.equals("EXPAND_PAYMENT_MODE")) {
//                        this.addPaymentMode(facts, order.getPaymentMode());
//                    }
//                    break;
                case EXPAND_DISCOUNTS:
                    facts.add(order.getDiscountValue());
                    break;
                case EXPAND_CATEGORIES:
                    this.addProductCategories(facts, entries);
                    break;
                case EXPAND_ENTRIES:
                    this.addEntries(facts, entries);
                    break;
                case EXPAND_SUPPLIERS:
                    this.addSuppliers(facts, entries);
                    break;
                case EXPAND_CONSUMED_BUDGET:
                    this.addConsumedBudget(facts, order);
                    break;
                default:
                    break;
            }
        }

        return facts;
    }

    private void addConsumedBudget(Set<Object> facts, CartRAO order) {
        List<PromotionBudgetRAO> promotionBudgetList = order.getPromotionBudgetList();
        if(CollectionUtils.isNotEmpty(promotionBudgetList)) {
            facts.addAll(promotionBudgetList);
        }
    }

    private void addSuppliers(Set<Object> facts, Set<OrderEntryRAO> entries) {
        if (CollectionUtils.isNotEmpty(entries)) {
            entries.forEach((orderEntry) -> {
                if(orderEntry.getProduct() != null && orderEntry.getProduct().getSupplier() != null
                    && orderEntry.getProduct().getSupplier().getSupplierId() != null) {
                    facts.add(orderEntry.getProduct().getSupplier());
                }
            });
        }
    }

    protected void addEntries(Set<Object> facts, Set<OrderEntryRAO> entries) {
        if (CollectionUtils.isNotEmpty(entries)) {
            facts.addAll(entries);
        }

    }

    protected void addConsumed(Set<Object> facts, CartRAO cart, Set<OrderEntryRAO> entries) {
        if (CollectionUtils.isNotEmpty(entries)) {
            facts.addAll(entries.stream().map(this::createProductConsumedRAO).collect(Collectors.toSet()));
        }

    }

    protected ProductConsumedRAO createProductConsumedRAO(OrderEntryRAO orderEntryRAO) {
        ProductConsumedRAO productConsumedRAO = new ProductConsumedRAO();
        productConsumedRAO.setOrderEntry(orderEntryRAO);
        int productAvailableQuantityInOrderEntry = this.ruleEngineCalculationService.getProductAvailableQuantityInOrderEntry(orderEntryRAO);
        productConsumedRAO.setAvailableQuantity(productAvailableQuantityInOrderEntry);
        return productConsumedRAO;
    }

    protected void addProducts(Set<Object> facts, Set<OrderEntryRAO> entries) {
        if (CollectionUtils.isNotEmpty(entries)) {
            entries.forEach((orderEntry) -> {
                facts.add(orderEntry.getProduct());
            });
        }

    }

    protected void addUserGroups(Set<Object> facts, UserRAO userRAO) {
        if (Objects.nonNull(userRAO)) {
            facts.add(userRAO);
            Set<UserGroupRAO> groups = userRAO.getGroups();
            if (CollectionUtils.isNotEmpty(groups)) {
                facts.addAll(groups);
            }
        }

    }

    protected void addProductCategories(Set<Object> facts, Set<OrderEntryRAO> entries) {
        if (CollectionUtils.isNotEmpty(entries)) {
            Iterator var4 = entries.iterator();

            while(var4.hasNext()) {
                OrderEntryRAO orderEntry = (OrderEntryRAO)var4.next();
                ProductRAO product = orderEntry.getProduct();
                if (Objects.nonNull(product) && CollectionUtils.isNotEmpty(product.getCategories())) {
                    facts.addAll(product.getCategories());
                }
            }
        }

    }

}
