package com.vctek.orderservice.service.impl;

import com.vctek.dto.VatData;
import com.vctek.orderservice.dto.ProductCanRewardDto;
import com.vctek.orderservice.dto.ReturnOrderCommerceParameter;
import com.vctek.orderservice.dto.request.ReturnOrderEntryRequest;
import com.vctek.orderservice.dto.request.ReturnOrderRequest;
import com.vctek.orderservice.kafka.producer.LoyaltyInvoiceProducerService;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.promotionengine.promotionservice.util.DiscountValue;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.CalculationException;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.Currency;
import com.vctek.orderservice.repository.EntryRepository;
import com.vctek.orderservice.repository.SubOrderEntryRepository;
import com.vctek.orderservice.repository.ToppingOptionRepository;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.util.CurrencyUtils;
import com.vctek.orderservice.util.DiscountType;
import com.vctek.util.CommonUtils;
import com.vctek.util.CurrencyType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class DefaultCalculationService implements CalculationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCalculationService.class);
    private ModelService modelService;
    private ToppingItemService toppingItemService;
    private ProductLoyaltyRewardRateService productLoyaltyRewardRateService;
    private LoyaltyInvoiceProducerService loyaltyInvoiceProducerService;
    private SubOrderEntryRepository subOrderEntryRepository;
    private EntryRepository entryRepository;
    private ToppingOptionRepository toppingOptionRepository;
    private ProductService productService;

    public DefaultCalculationService(ModelService modelService, ToppingItemService toppingItemService) {
        this.modelService = modelService;
        this.toppingItemService = toppingItemService;
    }

    @Override
    public void calculateTotals(final AbstractOrderModel order, final boolean recalculate) throws CalculationException {
        calculateSubtotal(order, recalculate);

        if (recalculate || Boolean.FALSE.equals(order.isCalculated())) {
            double totalDiscountTopping = totalDiscountTopping(order);
            // subtotal
            order.setTotalToppingDiscount(totalDiscountTopping);

            order.setSubTotal(order.getSubTotal() - totalDiscountTopping);
            final double subtotal = order.getSubTotal();
            // discounts promotions
            final double promotionDiscount = calculatePromotionDiscountValues(order, recalculate);
            // set total price
            double paymentCost = order.getPaymentCost() != null ? order.getPaymentCost() : 0;
            double deliveryCost = order.getDeliveryCost() != null ? order.getDeliveryCost() : 0;
            final double total = subtotal + paymentCost - promotionDiscount;
            final double fixedOrderDiscount = CurrencyUtils.computeValue(order.getDiscount(), order.getDiscountType(), total);
            if (DiscountType.CASH.toString().equals(order.getDiscountType())) {
                order.setDiscount(fixedOrderDiscount);
            }
            order.setFixedDiscount(fixedOrderDiscount);

            order.setTotalDiscount((promotionDiscount + fixedOrderDiscount));

            double totalPrice = total - fixedOrderDiscount;
            order.setTotalPrice(totalPrice);
            // tax
            double tax = CurrencyUtils.computeValue(order.getVat(), order.getVatType(), totalPrice);
            order.setTotalTax(tax);
            // final price
            order.setFinalPrice(totalPrice + tax + deliveryCost);

            setCalculatedStatus(order);
        }
    }

    @Override
    public void recalculate(AbstractOrderModel order) {
        calculateTotals(order, true);
    }

    /**
     * Must only calculate, not save order to database. Using for calculate promotions that can be applied for product
     * in PLP page
     * @param order
     */
    @Override
    public void calculate(AbstractOrderModel order) {
        calculateTotals(order, false);
    }

    @Override
    @Transactional
    public void calculateVat(AbstractOrderModel order) {
        double tax = CurrencyUtils.computeValue(order.getVat(), order.getVatType(), order.getTotalPrice());
        order.setTotalTax(tax);
        double deliveryCost = CommonUtils.readValue(order.getDeliveryCost());
        order.setFinalPrice(CommonUtils.readValue(order.getTotalPrice()) + tax + deliveryCost);
        order.setCalculated(true);
        modelService.save(order);
    }

    @Override
    public double calculateFinalDiscountOfEntry(AbstractOrderEntryModel abstractOrderEntry) {
        double totalDiscount = abstractOrderEntry.getTotalDiscount() == null ? 0d : abstractOrderEntry.getTotalDiscount();
        double orderDiscountToItem = abstractOrderEntry.getDiscountOrderToItem() == null ? 0 : abstractOrderEntry.getDiscountOrderToItem();
        return totalDiscount + orderDiscountToItem;
    }

    @Override
    public void calculateSubEntryPriceWithCombo(AbstractOrderEntryModel entry) {
        List<SubOrderEntryModel> subOrderEntryList = subOrderEntryRepository.findAllByOrderEntry(entry);
        if (CollectionUtils.isEmpty(subOrderEntryList)) {
            return;
        }
        sortSubOrderEntryByOriginPrice(subOrderEntryList);

        double totalOriginPrice = subOrderEntryList.stream()
                .mapToDouble(sub -> sub.getOriginPrice() * sub.getQuantity()).sum();
        double entryBasePrice = entry.getBasePrice();
        double totalEntryPrice = entryBasePrice * entry.getQuantity();

        int length = subOrderEntryList.size();
        double entryDiscount = this.calculateFinalDiscountOfEntry(entry);
        double totalEntryPriceRemain = totalEntryPrice;
        double remainDiscount = entryDiscount;

        for (int i = 0; i < length; i++) {
            SubOrderEntryModel subOrderEntryModel = subOrderEntryList.get(i);
            Integer entryQty = subOrderEntryModel.getQuantity();
            if (i == length - 1) {
                double price = totalEntryPriceRemain / entryQty;
                subOrderEntryModel.setPrice(price);
                double totalSubEntryPrice = price * entryQty;
                subOrderEntryModel.setTotalPrice(totalSubEntryPrice);
                double discount = remainDiscount > 0 ? remainDiscount : 0;
                subOrderEntryModel.setDiscountValue(discount);
                subOrderEntryModel.setFinalPrice(totalSubEntryPrice - discount);
                break;
            }

            double subEntryTotalOriginPrice = subOrderEntryModel.getOriginPrice() * entryQty;
            double price = Math.round(subEntryTotalOriginPrice * totalEntryPrice / (totalOriginPrice * entryQty));
            subOrderEntryModel.setPrice(price);
            double totalSubEntryPrice = price * entryQty;

            double discount = 0;
            if (remainDiscount > 0) {
                discount = entryBasePrice != 0 ? (subOrderEntryModel.getPrice() / entryBasePrice * entryDiscount) : 0;
                discount = Math.round(discount);
                remainDiscount -= discount;
            }
            subOrderEntryModel.setDiscountValue(discount);
            subOrderEntryModel.setTotalPrice(totalSubEntryPrice);
            subOrderEntryModel.setFinalPrice(totalSubEntryPrice - discount);
            totalEntryPriceRemain -= totalSubEntryPrice;
        }
        subOrderEntryRepository.saveAll(subOrderEntryList);
    }

    private void sortSubOrderEntryByOriginPrice(List<SubOrderEntryModel> subOrderEntryList) {
        subOrderEntryList.sort((o1, o2) -> {
            if (o1.getOriginPrice() != null && o2.getOriginPrice() != null) {
                if (o2.getOriginPrice().equals(o1.getOriginPrice())) {
                    return o2.getProductId().compareTo(o1.getProductId());
                }

                return o2.getOriginPrice().compareTo(o1.getOriginPrice());
            }

            return 0;
        });
    }

    @Override
    public void clearComboEntryPrices(CartEntryModel entryModel) {
        Set<SubOrderEntryModel> subOrderEntryModels = entryModel.getSubOrderEntries();
        if (CollectionUtils.isEmpty(subOrderEntryModels)) {
            return;
        }
        subOrderEntryModels.stream().forEach(e -> {
            e.setPrice(null);
            e.setDiscountValue(null);
            e.setTotalPrice(null);
            e.setFinalPrice(null);
        });
    }

    @Override
    @Transactional
    public void calculateVatByProductOf(AbstractOrderModel abstractOrderModel, boolean recalculate) {
        if(!recalculate) {
            return;
        }

        if(!abstractOrderModel.isHasGotVat()) {
            return;
        }

        List<AbstractOrderEntryModel> entries = abstractOrderModel.getEntries();
        Set<Long> productIds = new HashSet<>();
        entries.forEach(e -> {
            Set<SubOrderEntryModel> subOrderEntries = e.getSubOrderEntries();
            if(CollectionUtils.isNotEmpty(subOrderEntries)) {
                subOrderEntries.forEach(soe -> {
                    soe.setVat(null);
                    soe.setVatType(null);
                    productIds.add(soe.getProductId());
                });
            }
            if(StringUtils.isBlank(e.getComboType())) {
                e.setVat(null);
                e.setVatType(null);
                productIds.add(e.getProductId());
            }
        });

        Set<ToppingItemModel> toppingItemModels = toppingItemService.findAllByOrderId(abstractOrderModel.getId());
        toppingItemModels.forEach(t -> {
            productIds.add(t.getProductId());
            t.setVat(null);
            t.setVatType(null);
        });
        Map<Long, VatData> vatDataMap = productService.getVATOf(productIds);
        if(MapUtils.isEmpty(vatDataMap)) {
            abstractOrderModel.setVat(null);
            abstractOrderModel.setVatType(null);
            this.calculateVat(abstractOrderModel);
            return;
        }

        double vat = 0;
        vat += calculateVatForEntryAndComboEntry(entries, vatDataMap);
        vat += calculateVatForToppingItems(toppingItemModels, vatDataMap);
        if(hasProductNotSetVat(productIds, vatDataMap)) {
            abstractOrderModel.setVat(null);
            abstractOrderModel.setVatType(null);
            this.calculateVat(abstractOrderModel);
            return;
        }

        abstractOrderModel.setVat(vat);
        abstractOrderModel.setVatType(CurrencyType.CASH.toString());
        this.calculateVat(abstractOrderModel);
    }

    @Override
    public void resetVatOf(AbstractOrderModel abstractOrderModel) {
        abstractOrderModel.setVat(null);
        abstractOrderModel.setVatType(null);
        abstractOrderModel.setHasGotVat(false);
        abstractOrderModel.getEntries().forEach(entry -> {
            entry.setVat(null);
            entry.setVatType(null);
            entry.getSubOrderEntries().forEach(soe -> {
                soe.setVat(null);
                soe.setVatType(null);
            });

            entry.getToppingOptionModels().forEach(toppingOptionModel -> {
                toppingOptionModel.getToppingItemModels().forEach(item -> {
                    item.setVat(null);
                    item.setVatType(null);
                });
            });
        });
    }

    private boolean hasProductNotSetVat(Set<Long> productIds, Map<Long, VatData> vatDataMap) {
        if(productIds.size() != vatDataMap.size()) {
            return true;
        }

        for(Map.Entry<Long, VatData> entry : vatDataMap.entrySet()) {
            if(entry.getValue() == null || entry.getValue().getVat() == null) {
                return true;
            }
        }

        return false;
    }

    private double calculateVatForToppingItems(Set<ToppingItemModel> toppingItemModels, Map<Long, VatData> vatDataMap) {
        double vat = 0;
        for(ToppingItemModel toppingItemModel : toppingItemModels) {
            VatData vatData = vatDataMap.get(toppingItemModel.getProductId());
            if(vatData != null) {
                toppingItemModel.setVat(vatData.getVat());
                toppingItemModel.setVatType(vatData.getVatType());
            }

            if(hasVat(vatData)) {
                ToppingOptionModel toppingOptionModel = toppingItemModel.getToppingOptionModel();
                double totalPrice = CommonUtils.readValue(toppingItemModel.getBasePrice()) *
                        CommonUtils.readValue(toppingItemModel.getQuantity()) *
                        CommonUtils.readValue(toppingOptionModel.getQuantity());
                double fixedDiscount = CurrencyUtils.computeValue(toppingItemModel.getDiscount(), toppingItemModel.getDiscountType(), totalPrice);
                double finalPrice =  totalPrice - fixedDiscount - CommonUtils.readValue(toppingItemModel.getDiscountOrderToItem());
                vat += CurrencyUtils.computeValue(vatData.getVat(), vatData.getVatType(), finalPrice);
            }
        }
        return vat;
    }

    private double calculateVatForEntryAndComboEntry(List<AbstractOrderEntryModel> entries, Map<Long, VatData> vatDataMap) {
        double vat = 0;
        for(AbstractOrderEntryModel entry : entries) {
            Set<SubOrderEntryModel> subOrderEntries = entry.getSubOrderEntries();
            for(SubOrderEntryModel soe : subOrderEntries) {
                Long productId = soe.getProductId();
                LOGGER.debug("=== combo entry finalPrice: {}", soe.getFinalPrice());
                if(soe.getFinalPrice() == null) {
                    continue;
                }
                vat += calculateVatOfSubEntry(vatDataMap, soe, productId);
            }

            if(StringUtils.isBlank(entry.getComboType()) && CollectionUtils.isEmpty(subOrderEntries)) {
                vat += calculateVatOfEntry(vatDataMap, entry);
            }
        }
        return vat;
    }

    private double calculateVatOfEntry(Map<Long, VatData> vatDataMap, AbstractOrderEntryModel entry) {
        VatData vatData = vatDataMap.get(entry.getProductId());
        if(vatData != null) {
            entry.setVat(vatData.getVat());
            entry.setVatType(vatData.getVatType());
        }

        if(hasVat(vatData)) {
            double finalPrice = CommonUtils.readValue(entry.getFinalPrice()) - CommonUtils.readValue(entry.getDiscountOrderToItem());
            return CurrencyUtils.computeValue(vatData.getVat(), vatData.getVatType(), finalPrice);
        }

        return 0;
    }

    private double calculateVatOfSubEntry(Map<Long, VatData> vatDataMap, SubOrderEntryModel soe, Long productId) {
        VatData vatData = vatDataMap.get(productId);
        if(vatData != null) {
            soe.setVat(vatData.getVat());
            soe.setVatType(vatData.getVatType());
        }

        if(hasVat(vatData)) {
            return CurrencyUtils.computeValue(vatData.getVat(), vatData.getVatType(), soe.getFinalPrice());
        }

        return 0;
    }

    private boolean hasVat(VatData vatData) {
        return vatData != null && vatData.getVat() != null && vatData.getVat() > 0;
    }

    @Override
    public double totalDiscountTopping(AbstractOrderModel order) {
        double totalDiscount = 0;

        List<AbstractOrderEntryModel> entryHasToppingList = entryRepository.findAllEntryHasToppingOf(order.getId());
        if(CollectionUtils.isEmpty(entryHasToppingList)) {
            return totalDiscount;
        }

        List<ToppingOptionModel> toppingOptionModels = entryHasToppingList.stream()
                .flatMap(entryModel -> toppingOptionRepository.findAllByOrderEntry(entryModel).stream())
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(toppingOptionModels)) {
            totalDiscount = toppingItemService.totalDiscountToppingItem(toppingOptionModels);
        }
        return totalDiscount;
    }

    @Override
    public double calculateLoyaltyAmount(List<ProductCanRewardDto> productCanRewardDtoList, Long companyId) {
        Set<Long> productIds = productCanRewardDtoList.stream().filter(p -> CollectionUtils.isEmpty(p.getSubOrderEntries()))
                .map(ProductCanRewardDto::getProductId).collect(Collectors.toSet());
        Set<Long> comboIds = new HashSet<>();
        for (ProductCanRewardDto productCanRewardDto : productCanRewardDtoList) {
            if (CollectionUtils.isEmpty(productCanRewardDto.getSubOrderEntries())) continue;
            comboIds.add(productCanRewardDto.getProductId());
            Set<Long> productInComboIds = productCanRewardDto.getSubOrderEntries().stream()
                    .map(ProductCanRewardDto::getProductId).collect(Collectors.toSet());
            productIds.addAll(productInComboIds);
        }
        Map<Long, Double> rewardRateByProductIds = productLoyaltyRewardRateService.getRewardRateByProductIds(productIds, companyId, false);
        Map<Long, Double> comboRewardRates = productLoyaltyRewardRateService.getRewardRateByProductIds(comboIds, companyId, true);
        for (ProductCanRewardDto dto : productCanRewardDtoList) {

            if (CollectionUtils.isEmpty(dto.getSubOrderEntries())) {
                Double rewardRate = rewardRateByProductIds.get(dto.getProductId());
                Double rewardAmount = dto.getFinalPrice() * rewardRate / 100;
                dto.setAwardAmount(rewardAmount);
                dto.setRewardRate(rewardRate);
                continue;
            }
            calculateRewardAmountWithCombo(dto, comboRewardRates, rewardRateByProductIds);
        }

        return productCanRewardDtoList.stream().mapToDouble(ProductCanRewardDto::getAwardAmount).sum();
    }

    private void calculateRewardAmountWithCombo(ProductCanRewardDto dto, Map<Long, Double> comboRewardRates, Map<Long, Double> rewardRateByProductIds) {
        Double comboRewardRate = comboRewardRates.get(dto.getProductId());
        if (comboRewardRate != null) {
            Double rewardAmount = dto.getFinalPrice() * comboRewardRate / 100;
            dto.setAwardAmount(rewardAmount);
            dto.setRewardRate(comboRewardRate);
            return;
        }
        double rewardAmount = 0d;
        for (ProductCanRewardDto productInCombo : dto.getSubOrderEntries()) {
            Double rewardRate = rewardRateByProductIds.get(productInCombo.getProductId());
            rewardAmount += productInCombo.getFinalPrice() * rewardRate / 100;
        }
        dto.setAwardAmount(rewardAmount);
    }

    @Override
    public AbstractOrderModel saveRewardAmountToEntries(AbstractOrderModel orderModel, double point, double loyaltyAmount, List<ProductCanRewardDto> productCanRewards, Boolean isUpdateOrder) {
        if (!isUpdateOrder) {
            orderModel.setRewardPoint(point);
            if (point <= 0) {
                LOGGER.info("ORDER HAD NOT REWARDED: orderCode: {}, point: {}", orderModel.getCode(), point);
                return orderModel;
            }
        }
        if (orderModel.getTotalRewardAmount() != null || (orderModel.getTotalRewardAmount() == null && loyaltyAmount > 0)) {
            orderModel.setTotalRewardAmount(loyaltyAmount);
        }
        updateRewardAmountToEntriesAndToppingItems(productCanRewards, orderModel);
        AbstractOrderModel saveModel = modelService.save(orderModel);
        loyaltyInvoiceProducerService.createOrUpdateLoyaltyImbursementInvoice((OrderModel) saveModel);
        return saveModel;
    }

    private void updateRewardAmountToEntriesAndToppingItems(List<ProductCanRewardDto> productCanRewards, AbstractOrderModel orderModel) {
        List<AbstractOrderEntryModel> entries = orderModel.getEntries();
        Map<Long, AbstractOrderEntryModel> mapEntry = entries.stream()
                .collect(Collectors.toMap(orderEntry -> orderEntry.getId(), orderEntry -> orderEntry));
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Product Reward DTO: " + productCanRewards);
        }

        if (CollectionUtils.isEmpty(productCanRewards)) {
            for (AbstractOrderEntryModel entry : entries) {
                entry.setRewardAmount(null);
            }
            return;
        }
        for (ProductCanRewardDto productCanReward : productCanRewards) {
            if (productCanReward.getToppingItemId() != null) {
                ToppingItemModel toppingItemModel = this.getToppingItemBy(entries, productCanReward);
                if (toppingItemModel == null) {
                    LOGGER.error("Invalid topping item id :{}, order code :{}", productCanReward.getToppingItemId(), orderModel.getCode());
                    continue;
                }
                toppingItemModel.setRewardAmount(productCanReward.getAwardAmount());
                continue;
            }
            AbstractOrderEntryModel abstractOrderEntryModel = mapEntry.get(productCanReward.getOrderEntryId());
            if (abstractOrderEntryModel == null) {
                LOGGER.error("Invalid order entry id :{}, order code :{}", productCanReward.getOrderEntryId(), orderModel.getCode());
                continue;
            }
            abstractOrderEntryModel.setRewardAmount(productCanReward.getAwardAmount());
        }
    }

    private ToppingItemModel getToppingItemBy(List<AbstractOrderEntryModel> entries, ProductCanRewardDto productCanReward) {
        Optional<AbstractOrderEntryModel> entryModelOptional = entries.stream()
                .filter(e -> e.getId().equals(productCanReward.getOrderEntryId())).findFirst();
        if (!entryModelOptional.isPresent()) {
            return null;
        }

        Optional<ToppingOptionModel> toppingOptionModel = entryModelOptional.get().getToppingOptionModels()
                .stream().filter(opt -> opt.getId().equals(productCanReward.getToppingOptionId()))
                .findFirst();
        if (!toppingOptionModel.isPresent()) {
            return null;
        }

        Optional<ToppingItemModel> toppingItemModel = toppingOptionModel.get().getToppingItemModels()
                .stream().filter(item -> item.getId().equals(productCanReward.getToppingItemId()))
                .findFirst();
        return toppingItemModel.isPresent() ? toppingItemModel.get() : null;
    }

    @Override
    public double calculateMaxRevertAmount(ReturnOrderRequest returnOrderRequest, OrderModel originOrder) {
        Map<Long, AbstractOrderEntryModel> orderEntryMap = originOrder.getEntries().stream()
                .collect(Collectors.toMap(e -> e.getId(), e -> e));
        double totalRevertAmount = 0;
        for (ReturnOrderEntryRequest entryRequest : returnOrderRequest.getReturnOrderEntries()) {
            AbstractOrderEntryModel entryModel = orderEntryMap.get(entryRequest.getOrderEntryId());
            double revertAmount = 0;
            revertAmount = entryRequest.getQuantity() * CommonUtils.readValue(entryModel.getRewardAmount()) / entryModel.getQuantity();
            if (CollectionUtils.isNotEmpty(entryModel.getToppingOptionModels())) {
                revertAmount += calculateRevertAmountTopping(entryModel.getToppingOptionModels());
            }
            totalRevertAmount += revertAmount;
        }
        return totalRevertAmount;
    }

    @Override
    public double calculateMaxRefundAmount(ReturnOrderCommerceParameter commerceParameter) {
        OrderModel originOrder = commerceParameter.getOriginOrder();
        double redeemAmount = CommonUtils.readValue(originOrder.getRedeemAmount());
        double refundAmount = CommonUtils.readValue(originOrder.getRefundAmount());
        if (redeemAmount == 0 || refundAmount >= redeemAmount) {
            return 0;
        }

        double finalCost = CommonUtils.readValue(commerceParameter.getBillRequest().getFinalCost());
        if (redeemAmount - refundAmount - finalCost <= 0) {
            return redeemAmount - refundAmount;
        } else {
            return finalCost;
        }
    }

    private double calculateRevertAmountTopping(Set<ToppingOptionModel> toppingOptionModels) {
        double totalRevertAmount = 0;
        for (ToppingOptionModel toppingOptionModel : toppingOptionModels) {
            if (CollectionUtils.isEmpty(toppingOptionModel.getToppingItemModels())) {
                continue;
            }
            totalRevertAmount += toppingOptionModel.getToppingItemModels().stream()
                    .mapToDouble(toppingItemModel -> CommonUtils.readValue(toppingItemModel.getRewardAmount())).sum();
        }
        return totalRevertAmount;
    }

    protected double calculatePromotionDiscountValues(final AbstractOrderModel order, final boolean recalculate) {
        if (recalculate || Boolean.FALSE.equals(order.isCalculated())) {
            final List<DiscountValue> discountValues = order.getDiscountValues();
            if (CollectionUtils.isNotEmpty(discountValues)) {
                final String iso = order.getCurrencyCode();
                final int digits = Currency.DEFAULT_DIGITS;
                final double discountablePrice = order.getSubTotal();
                final List appliedDiscounts = DiscountValue.apply(1.0, discountablePrice, digits, discountValues, iso);
                order.setDiscountValues(appliedDiscounts);
                return DiscountValue.sumAppliedValues(appliedDiscounts);
            }

            return 0.0;
        }
        return DiscountValue.sumAppliedValues(order.getDiscountValues());
    }

    private void calculateSubtotal(final AbstractOrderModel order, final boolean recalculate) {
        if (recalculate || Boolean.FALSE.equals(order.isCalculated())) {
            double subtotal = 0.0;
            double subTotalDiscount = 0.0;
            final List<AbstractOrderEntryModel> entries = entryRepository.findAllByOrder(order);
            for (final AbstractOrderEntryModel entry : entries) {
                calculateTotalOfEntries(entry, recalculate);
                final double entryTotal = entry.getFinalPrice();
                final double toppingTotalPrice = calculateToppingTotalPrice(entry);
                subtotal += (entryTotal + toppingTotalPrice);
                subTotalDiscount += entry.getTotalDiscount();
            }
            // store subtotal
            order.setSubTotal(subtotal);
            order.setSubTotalDiscount(subTotalDiscount);
        }
    }

    private void calculateTotalOfEntries(AbstractOrderEntryModel entry, boolean recalculate) {
        if (recalculate || Boolean.FALSE.equals(entry.isCalculated())) {
            final AbstractOrderModel order = entry.getOrder();

            final double totalPriceWithoutPromotion = entry.getBasePrice() * entry.getQuantity();
            final double quantity = entry.getQuantity().doubleValue();
            final List<DiscountValue> appliedDiscounts = DiscountValue.apply(quantity, totalPriceWithoutPromotion, Currency.DEFAULT_DIGITS,
                    entry.getDiscountValues(), order.getCurrencyCode());
            entry.setDiscountValues(appliedDiscounts);
            double totalPrice = totalPriceWithoutPromotion;
            double totalDiscount = 0d;
            for (final Iterator it = appliedDiscounts.iterator(); it.hasNext(); ) {
                double appliedValue = ((DiscountValue) it.next()).getAppliedValue();
                totalPrice -= appliedValue;
                totalDiscount += appliedValue;
            }
            entry.setTotalPrice(totalPrice);//not contain fixed discount
            double finalPrice = totalPrice;
            double fixedDiscount = CurrencyUtils.computeValue(entry.getDiscount(), entry.getDiscountType(), totalPrice);
            if (DiscountType.CASH.toString().equals(entry.getDiscountType())) {
                entry.setDiscount(fixedDiscount);
            }
            finalPrice -= fixedDiscount;
            totalDiscount += fixedDiscount;

            entry.setFixedDiscount(fixedDiscount);
            entry.setTotalDiscount(totalDiscount);
            entry.setFinalPrice(finalPrice);
            // Calculate sub order entry price for combo
            this.calculateSubEntryPriceWithCombo(entry);
            setCalculatedStatus(entry);
        }
    }

    protected double calculateToppingTotalPrice(AbstractOrderEntryModel entry) {
        List<ToppingOptionModel> toppingOptionModels = toppingOptionRepository.findAllByOrderEntry(entry);
        double totalToppingPrice = 0;
        if (CollectionUtils.isNotEmpty(toppingOptionModels)) {
            Map<ToppingOptionModel, Set<ToppingItemModel>> toppingMap = toppingOptionModels.stream()
                    .collect(Collectors.toMap(Function.identity(), (tp) -> toppingItemService.findAllByToppingOptionModel(tp)));

            for (Map.Entry<ToppingOptionModel, Set<ToppingItemModel>> topping : toppingMap.entrySet()) {
                ToppingOptionModel toppingOption = topping.getKey();
                double toppingPrice = topping.getValue().stream().filter(sot -> sot.getBasePrice() != null && sot.getQuantity() != null)
                        .mapToDouble(sot -> sot.getBasePrice() * sot.getQuantity())
                        .sum();
                int quantity = CommonUtils.readValue(toppingOption.getQuantity());
                totalToppingPrice += toppingPrice * quantity;

            }
        }

        return totalToppingPrice;
    }

    protected void setCalculatedStatus(final AbstractOrderModel order) {
        order.setCalculated(Boolean.TRUE);
        final List<AbstractOrderEntryModel> entries = entryRepository.findAllByOrder(order);
        if (entries != null) {
            for (final AbstractOrderEntryModel entry : entries) {
                entry.setCalculated(Boolean.TRUE);
            }
        }
    }

    private void setCalculatedStatus(final AbstractOrderEntryModel entry) {
        entry.setCalculated(Boolean.TRUE);
    }

    @Override
    public double calculateRemainCashAmount(ReturnOrderCommerceParameter commerceParameter) {
        OrderModel originOrder = commerceParameter.getOriginOrder();
        double totalRemain = CommonUtils.readValue(originOrder.getFinalPrice()) - CommonUtils.readValue(originOrder.getRedeemAmount());
        double latestRefundAmount = 0;
        for (AbstractOrderEntryModel entry : originOrder.getEntries()) {
            long returnQty = CommonUtils.readValue(entry.getReturnQuantity());
            long originQty = CommonUtils.readValue(entry.getQuantity());
            latestRefundAmount += returnQty * CommonUtils.readValue(entry.getBasePrice()) - returnQty * (calculateFinalDiscountOfEntry(entry) / originQty);
            if (CollectionUtils.isNotEmpty(entry.getToppingOptionModels())) {
                latestRefundAmount += calculateRemainAmountOfTopping(entry.getToppingOptionModels(), entry.getReturnQuantity());
            }
        }
        return totalRemain - latestRefundAmount + CommonUtils.readValue(originOrder.getRefundAmount());
    }

    @Override
    public double round(Double value, int places) {
        if (value == null) {
            return 0;
        }

        if (places < 0) {
            throw new IllegalArgumentException("place must not be negative");
        }
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private double calculateRemainAmountOfTopping(Set<ToppingOptionModel> toppingOptionModels, Long returnQuantity) {
        double refundAmount = 0;
        for (ToppingOptionModel toppingOptionModel : toppingOptionModels) {
            if (CollectionUtils.isEmpty(toppingOptionModel.getToppingItemModels())) continue;
            for (ToppingItemModel toppingItemModel : toppingOptionModel.getToppingItemModels()) {
                Long totalQty = CommonUtils.readValue(toppingItemModel.getQuantity()) * CommonUtils.readValue(returnQuantity);
                double discount = CommonUtils.readValue(toppingItemModel.getDiscountOrderToItem());
                double totalPrice = CommonUtils.readValue(toppingItemModel.getBasePrice()) * totalQty;
                double totalDiscount = discount + CurrencyUtils.computeValue(toppingItemModel.getDiscount(), toppingItemModel.getDiscountType(), totalPrice);
                refundAmount += (totalPrice - totalDiscount);
            }
        }
        return refundAmount;
    }

    @Autowired
    public void setProductLoyaltyRewardRateService(ProductLoyaltyRewardRateService productLoyaltyRewardRateService) {
        this.productLoyaltyRewardRateService = productLoyaltyRewardRateService;
    }

    @Autowired
    public void setLoyaltyInvoiceProducerService(LoyaltyInvoiceProducerService loyaltyInvoiceProducerService) {
        this.loyaltyInvoiceProducerService = loyaltyInvoiceProducerService;
    }

    @Autowired
    public void setSubOrderEntryRepository(SubOrderEntryRepository subOrderEntryRepository) {
        this.subOrderEntryRepository = subOrderEntryRepository;
    }

    @Autowired
    public void setEntryRepository(EntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    @Autowired
    public void setToppingOptionRepository(ToppingOptionRepository toppingOptionRepository) {
        this.toppingOptionRepository = toppingOptionRepository;
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }
}
