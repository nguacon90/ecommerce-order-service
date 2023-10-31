package com.vctek.orderservice.strategy.impl;

import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.dto.ProductCanRewardDto;
import com.vctek.orderservice.dto.SplitPromotionEntryData;
import com.vctek.orderservice.feignclient.dto.CustomerData;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionEngineService;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.OrderEntryConsumedRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.OrderEntryRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.ProductRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.RuleEngineResultRAO;
import com.vctek.orderservice.repository.EntryRepository;
import com.vctek.orderservice.repository.ToppingItemRepository;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.CustomerService;
import com.vctek.orderservice.service.LoyaltyService;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.strategy.CommerceCartCalculationStrategy;
import com.vctek.orderservice.util.CurrencyUtils;
import com.vctek.util.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component("defaultCommerceCartCalculationStrategy")
public class DefaultCommerceCartCalculationStrategy implements CommerceCartCalculationStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCommerceCartCalculationStrategy.class);
    private CalculationService calculationService;
    private PromotionEngineService promotionEngineService;
    private OrderService orderService;
    private CustomerService customerService;
    private LoyaltyService loyaltyService;
    private EntryRepository entryRepository;
    private ToppingItemRepository toppingItemRepository;

    @Override
    public boolean calculateCart(CommerceAbstractOrderParameter parameter) {
        final AbstractOrderModel cartModel = parameter.getOrder();
        if (Boolean.FALSE.equals(cartModel.isCalculated())) {
            parameter.setRecalculate(false);
            calculationService.calculate(cartModel);
            promotionEngineService.updatePromotions(Collections.emptyList(), cartModel);
            this.splitOrderPromotionToEntries(cartModel);
            calculationService.calculateVatByProductOf(cartModel, parameter.isRecalculateVat());
        }
        return true;
    }

    @Override
    public boolean recalculateCart(CommerceAbstractOrderParameter parameter) {
        final AbstractOrderModel cartModel = parameter.getOrder();
        parameter.setRecalculate(false);
        calculationService.recalculate(cartModel);
        promotionEngineService.updatePromotions(Collections.emptyList(), cartModel);
        this.splitOrderPromotionToEntries(cartModel);
        calculationService.calculateVatByProductOf(cartModel, parameter.isRecalculateVat());
        return true;
    }

    @Override
    public void splitOrderPromotionToEntries(AbstractOrderModel orderModel) {
        Double subTotal = orderModel.getSubTotal();
        Double totalOrderDiscount = orderModel.getTotalDiscount();
        if (subTotal == null || subTotal == 0 || totalOrderDiscount == null || totalOrderDiscount == 0) {
            revertAllOrderDiscountToItems(orderModel);
            return;
        }
        revertAllOrderDiscountToItems(orderModel);
        List<AbstractOrderEntryModel> orderEntries = entryRepository.findAllByOrder(orderModel);
        List<AbstractOrderEntryModel> entryGetDiscountList = orderEntries;
        List<AbstractOrderEntryModel> ignoreEntryDiscountList = new ArrayList<>();
        boolean onlyComboEntry = isOnlyComboEntryAndSaleOffEntry(orderEntries);
        if (!onlyComboEntry) {
            entryGetDiscountList = orderEntries
                    .stream().filter(entry -> orderService.isValidEntryForPromotion(entry) && !entry.isGiveAway())
                    .collect(Collectors.toList());
            ignoreEntryDiscountList = orderEntries
                    .stream().filter(entry -> !orderService.isValidEntryForPromotion(entry))
                    .collect(Collectors.toList());
            double ignoreFinalPrice = ignoreEntryDiscountList.stream().mapToDouble(ce -> CommonUtils.readValue(ce.getFinalPrice())).sum();
            subTotal -= ignoreFinalPrice;
        }

        Map<String, Object> entriesMap = new HashMap<>();
        List<SplitPromotionEntryData> splitPromotionEntries = new ArrayList<>();
        populateEntryGetDiscount(entryGetDiscountList, entriesMap, splitPromotionEntries);

        double remainDiscount = splitOrderDiscountToEntries(subTotal, totalOrderDiscount, entriesMap, splitPromotionEntries);
        if(remainDiscount > 0) {
            if(CollectionUtils.isNotEmpty(ignoreEntryDiscountList)) {
                entriesMap = new HashMap<>();
                splitPromotionEntries = new ArrayList<>();
                populateEntryGetDiscount(ignoreEntryDiscountList, entriesMap, splitPromotionEntries);
                double ignoreFinalPrice = ignoreEntryDiscountList.stream().mapToDouble(ce -> CommonUtils.readValue(ce.getFinalPrice())).sum();
                splitOrderDiscountToEntries(ignoreFinalPrice, remainDiscount, entriesMap, splitPromotionEntries);
                entryGetDiscountList.addAll(ignoreEntryDiscountList);
            } else {
                LOGGER.warn("Has remain discount not been divided to entry: orderCode: {}, discount: {}", orderModel.getCode(), remainDiscount);
            }
        }

        for (AbstractOrderEntryModel entryModel : entryGetDiscountList) {
            if(orderService.isComboEntry(entryModel)) {
                calculationService.calculateSubEntryPriceWithCombo(entryModel);
            }
        }

        saveEntries(entriesMap);
    }

    private void populateEntryGetDiscount(List<AbstractOrderEntryModel> entryGetDiscountList,
                                          Map<String, Object> entriesMap,
                                          List<SplitPromotionEntryData> splitPromotionEntries) {
        SplitPromotionEntryData entryData;
        for (AbstractOrderEntryModel entry : entryGetDiscountList) {
            Long entryId = entry.getId();
            entryData = new SplitPromotionEntryData();
            entryData.setEntryId(entryId);
            entryData.setFinalPrice(entry.getFinalPrice());
            splitPromotionEntries.add(entryData);
            entriesMap.put(generateKey(entryId, null, null), entry);

            Set<ToppingItemModel> toppingItemModels = toppingItemRepository.findAllByEntryId(entryId);
            if (CollectionUtils.isEmpty(toppingItemModels)) {
                continue;
            }

            for (ToppingItemModel item : toppingItemModels) {
                ToppingOptionModel toppingOptionModel = item.getToppingOptionModel();
                entryData = new SplitPromotionEntryData();
                entryData.setEntryId(entryId);
                entryData.setToppingOptionId(toppingOptionModel.getId());
                entryData.setToppingItemId(item.getId());
                double basePrice = CommonUtils.readValue(item.getBasePrice());
                int quantity = CommonUtils.readValue(item.getQuantity());
                int optionQty = CommonUtils.readValue(toppingOptionModel.getQuantity());
                double totalPrice = basePrice * quantity * optionQty;
                double fixedDiscount = CurrencyUtils.computeValue(item.getDiscount(), item.getDiscountType(), totalPrice);
                entryData.setFinalPrice(totalPrice - fixedDiscount);
                splitPromotionEntries.add(entryData);
                entriesMap.put(generateKey(entryId, toppingOptionModel.getId(), item.getId()), item);
            }
        }
    }

    private void saveEntries(Map<String, Object> entriesMap) {
        List<ToppingItemModel> toppingItemModelList = new ArrayList<>();
        List<AbstractOrderEntryModel> abstractOrderEntryModels = new ArrayList<>();

        for (Object value : entriesMap.values()) {
            if (value instanceof AbstractOrderEntryModel) {
                AbstractOrderEntryModel abstractOrderEntryModel = (AbstractOrderEntryModel) value;
                abstractOrderEntryModels.add(abstractOrderEntryModel);
                continue;
            }
            if (value instanceof ToppingItemModel) {
                ToppingItemModel toppingItemModel = (ToppingItemModel) value;
                toppingItemModelList.add(toppingItemModel);
            }
        }

        if (CollectionUtils.isNotEmpty(abstractOrderEntryModels)) {
            entryRepository.saveAll(abstractOrderEntryModels);
        }

        if (CollectionUtils.isNotEmpty(toppingItemModelList)) {
            toppingItemRepository.saveAll(toppingItemModelList);
        }
    }

    private boolean isOnlyComboEntryAndSaleOffEntry(List<AbstractOrderEntryModel> orderEntries) {
        for (AbstractOrderEntryModel entryModel : orderEntries) {
            if (!orderService.isComboEntry(entryModel) && !entryModel.isSaleOff()) {
                return false;
            }
        }

        return true;
    }

    private void revertAllOrderDiscountToItems(AbstractOrderModel orderModel) {
        List<AbstractOrderEntryModel> entries = entryRepository.findAllByOrder(orderModel);
        if (CollectionUtils.isEmpty(entries)) return;
        for (AbstractOrderEntryModel entry : entries) {
            entry.setDiscountOrderToItem(0d);
            Set<ToppingItemModel> toppingItemModels = toppingItemRepository.findAllByEntryId(entry.getId());
            if (CollectionUtils.isNotEmpty(toppingItemModels)) {
                toppingItemModels.stream().forEach(item -> item.setDiscountOrderToItem(0d));
                toppingItemRepository.saveAll(toppingItemModels);
            }
            calculationService.calculateSubEntryPriceWithCombo(entry);
        }
        entryRepository.saveAll(entries);
    }

    private double splitOrderDiscountToEntries(Double subTotal, Double totalOrderDiscount, Map<String, Object> entriesMap,
                                             List<SplitPromotionEntryData> splitPromotionEntries) {
        Collections.sort(splitPromotionEntries, (o1, o2) -> {
            if (o2.getFinalPrice() == null || o1.getFinalPrice() == null) {
                return 0;
            }

            return o2.getFinalPrice().compareTo(o1.getFinalPrice());
        });

        double remainDiscount = totalOrderDiscount;
        int size = splitPromotionEntries.size();
        for (int i = 0; i < size; i++) {
            SplitPromotionEntryData entry = splitPromotionEntries.get(i);
            if (remainDiscount <= 0) {
                setDiscountOrderToItem(entriesMap, entry, 0);
                continue;
            }

            if (i == size - 1) {
                double discount = Math.min(entry.getFinalPrice(), remainDiscount);
                remainDiscount -= discount;
                setDiscountOrderToItem(entriesMap, entry, discount);
                break;
            }

            double discountToEntry = Math.round(entry.getFinalPrice() / subTotal * totalOrderDiscount);
            setDiscountOrderToItem(entriesMap, entry, discountToEntry);
            remainDiscount -= discountToEntry;
        }

        return remainDiscount;
    }

    private void setDiscountOrderToItem(Map<String, Object> entriesMap, SplitPromotionEntryData entry, double discountToEntry) {
        String key = generateKey(entry.getEntryId(), entry.getToppingOptionId(), entry.getToppingItemId());
        Object item = entriesMap.get(key);
        if (item instanceof AbstractOrderEntryModel) {
            AbstractOrderEntryModel abstractOrderEntryModel = (AbstractOrderEntryModel) item;
            abstractOrderEntryModel.setDiscountOrderToItem(discountToEntry);
        } else if (item instanceof ToppingItemModel) {
            ToppingItemModel toppingItemModel = (ToppingItemModel) item;
            toppingItemModel.setDiscountOrderToItem(discountToEntry);
        }
    }

    private String generateKey(Long entryId, Long optionToppingId, Long toppingItemId) {
        StringJoiner joiner = new StringJoiner(CommonUtils.UNDERSCORE);
        if (entryId != null) {
            joiner.add(entryId.toString());
        }
        if (optionToppingId != null) {
            joiner.add(optionToppingId.toString());
        }
        if (toppingItemId != null) {
            joiner.add(toppingItemId.toString());
        }

        return joiner.toString();
    }

    @Override
    @Transactional
    public void calculateLoyaltyRewardOrder(OrderModel orderModel) {
        if (StringUtils.isBlank(orderModel.getCardNumber()) && orderModel.getCustomerId() == null) {
            return;
        }
        Long customerId = orderModel.getCustomerId();
        CustomerData customer = customerService.getBasicCustomerInfo(customerId, orderModel.getCompanyId());
        if (customer.isLimitedApplyPromotionAndReward()) return;
        List<ProductCanRewardDto> productCanRewardDtoList = loyaltyService.getAwardProducts(orderModel);
        if (CollectionUtils.isEmpty(productCanRewardDtoList)) return;

        if(StringUtils.isNotBlank(orderModel.getCardNumber())) {
            boolean isApplied = loyaltyService.isApplied(orderModel);
            if (!isApplied) return;
            populateRewardToOrder(productCanRewardDtoList, orderModel);
            return;
        }

        if(customer == null || StringUtils.isBlank(customer.getPhone())) {
            LOGGER.warn("CANNOT REWARD INVALID CUSTOMER INFO: id: {}", customerId);
            return;
        }
        populateRewardToOrder(productCanRewardDtoList, orderModel);
    }

    private void populateRewardToOrder(List<ProductCanRewardDto> productCanRewardDtoList, OrderModel orderModel) {
        double rewardAmount = calculationService.calculateLoyaltyAmount(productCanRewardDtoList, orderModel.getCompanyId());
        if (rewardAmount <= 0) {
            LOGGER.warn("TOTAL REWARD AMOUNT TO ORDER: {}, AMOUNT: {}", orderModel.getCode(), rewardAmount);
            return;
        }
        double rewardPoint = loyaltyService.convertAmountToPoint(rewardAmount, orderModel.getCompanyId());
        orderModel.setTotalRewardAmount(rewardAmount);
        orderModel.setRewardPoint(rewardPoint);
    }

    @Override
    public Double calculateTotalRewardAmount(OrderModel orderModel) {
        if (StringUtils.isBlank(orderModel.getCardNumber()) && orderModel.getCustomerId() == null) {
            return null;
        }
        Long customerId = orderModel.getCustomerId();
        boolean isLimitedApplyPromotionAndReward = customerService.limitedApplyPromotionAndReward(customerId, orderModel.getCompanyId());
        if (isLimitedApplyPromotionAndReward) return null;
        List<ProductCanRewardDto> productCanRewardDtoList = loyaltyService.getAwardProducts(orderModel);
        if (CollectionUtils.isEmpty(productCanRewardDtoList)) return null;
        return calculationService.calculateLoyaltyAmount(productCanRewardDtoList, orderModel.getCompanyId());
    }

    @Override
    public Map<Long, Double> doAppliedCartTemp(CartModel cartModel) {
        calculateOf(cartModel);
        Map<Long, Double> discountProductPriceMap = new HashMap<>();
        RuleEngineResultRAO ruleEngineResultRAO = promotionEngineService.doEvaluateCartTemp(cartModel);
        if(ruleEngineResultRAO == null || CollectionUtils.isEmpty(ruleEngineResultRAO.getActions())) {
            return discountProductPriceMap;
        }
        List<OrderEntryConsumedRAO> consumedRAOList = ruleEngineResultRAO.getActions().stream()
                .filter(action -> CollectionUtils.isNotEmpty(action.getConsumedEntries()))
                .flatMap(action -> action.getConsumedEntries().stream())
                .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(consumedRAOList)) {
            return discountProductPriceMap;
        }

        consumedRAOList.forEach(cse -> {
            OrderEntryRAO orderEntry = cse.getOrderEntry();
            if(orderEntry != null && orderEntry.getProduct() != null) {
                ProductRAO product = orderEntry.getProduct();
                if(cse.getAdjustedUnitPrice() != null) {
                    double promotionPrice = cse.getAdjustedUnitPrice().doubleValue();
                    discountProductPriceMap.put(product.getId(), promotionPrice);
                }
            }
        });

        return discountProductPriceMap;
    }

    private void calculateOf(CartModel cartModel) {
        double subtotal = 0.0;
        final List<AbstractOrderEntryModel> entries = cartModel.getEntries();
        for (final AbstractOrderEntryModel entry : entries) {
            double totalPrice = CommonUtils.readValue(entry.getBasePrice()) * CommonUtils.readValue(entry.getQuantity());
            subtotal += totalPrice;
            entry.setTotalPrice(totalPrice);
            entry.setFinalPrice(totalPrice);
        }
        // store subtotal
        cartModel.setSubTotal(subtotal);
        cartModel.setTotalPrice(subtotal);
        cartModel.setFinalPrice(subtotal);

    }

    @Autowired
    public void setCalculationService(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @Autowired
    public void setPromotionsService(PromotionEngineService promotionEngineService) {
        this.promotionEngineService = promotionEngineService;
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Autowired
    public void setLoyaltyService(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    @Autowired
    public void setEntryRepository(EntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    @Autowired
    public void setToppingItemRepository(ToppingItemRepository toppingItemRepository) {
        this.toppingItemRepository = toppingItemRepository;
    }
}
