package com.vctek.orderservice.strategy.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.ComboData;
import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.dto.CommerceCartModification;
import com.vctek.orderservice.dto.PriceData;
import com.vctek.orderservice.dto.request.EntryRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.DistributorSetingPriceData;
import com.vctek.orderservice.feignclient.dto.UpdateProductInventoryDetailData;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.ComboPriceSettingService;
import com.vctek.orderservice.service.LogisticService;
import com.vctek.orderservice.strategy.CommerceUpdateCartEntryStrategy;
import com.vctek.orderservice.util.*;
import com.vctek.util.CommonUtils;
import com.vctek.util.OrderStatus;
import com.vctek.util.SettingPriceType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class DefaultCommerceUpdateCartEntryStrategy
        extends AbstractCommerceCartStrategy implements CommerceUpdateCartEntryStrategy {

    private CalculationService calculationService;
    private ComboPriceSettingService comboPriceSettingService;
    private LogisticService logisticService;

    @Override
    public CommerceCartModification updateQuantityForCartEntry(CommerceAbstractOrderParameter parameter) {
        CommerceCartModification commerceCartModification = new CommerceCartModification();
        final AbstractOrderModel abstractOrderModel = parameter.getOrder();
        final long newQuantity = parameter.getQuantity();
        final long entryId = parameter.getEntryId();

        final AbstractOrderEntryModel entryToUpdate = getEntryForId(abstractOrderModel, entryId);
        validateEntryBeforeModification(newQuantity, entryToUpdate);

        // Work out how many we want to add (could be negative if we are removing items)
        long currentEntryQuantity = entryToUpdate.getQuantity();
        final long quantityToAdd = newQuantity - currentEntryQuantity;
        final long actualAllowedQuantityChange = getAllowedCartAdjustmentForProduct(abstractOrderModel, entryToUpdate,
                quantityToAdd);
        final long entryNewQuantity = currentEntryQuantity + actualAllowedQuantityChange;
        commerceCartModification.setQuantityAdded(quantityToAdd);
        commerceCartModification.setProductId(entryToUpdate.getProductId());

        if (entryNewQuantity <= 0) {
            handleRevertPreOrder(abstractOrderModel, entryToUpdate);
            handleRemoveFreeGift(abstractOrderModel, entryToUpdate);
            abstractOrderModel.getEntries().remove(entryToUpdate);
            getModelService().save(abstractOrderModel);
            normalizeEntryNumbers(abstractOrderModel);
            commerceCartModification.setDeletedEntry(true);
        } else {
            recalculateDisCountEntryWithCash(entryToUpdate, entryNewQuantity);
            entryToUpdate.setQuantity(entryNewQuantity);
            recalculateComboOrderEntry(abstractOrderModel, entryToUpdate, currentEntryQuantity, entryNewQuantity);
            handleUpdatePreOrder(abstractOrderModel, entryToUpdate);
            getModelService().save(entryToUpdate);
            getModelService().save(abstractOrderModel);
        }
        handleUpdateEntryStockHoldingOnline(abstractOrderModel, entryToUpdate, actualAllowedQuantityChange);
        commerceCartModification.setEntry(entryToUpdate);
        if(parameter.isRecalculate()) {
            recalculate(abstractOrderModel, false);
        }
        return commerceCartModification;
    }

    private void recalculateDisCountEntryWithCash(AbstractOrderEntryModel entryToUpdate, long entryNewQuantity) {

        if (entryToUpdate.getDiscount() != null && entryToUpdate.getDiscount() > 0 && DiscountType.CASH.toString().equals(entryToUpdate.getDiscountType())) {
            double discount = entryToUpdate.getDiscount() / entryToUpdate.getQuantity();
            entryToUpdate.setDiscount(discount * entryNewQuantity);
        }
    }

    private void handleRemoveFreeGift(AbstractOrderModel abstractOrderModel, AbstractOrderEntryModel entryToUpdate) {
        if (entryToUpdate.isGiveAway()) {
            abstractOrderModel.setAppliedPromotionSourceRuleId(null);
        }
    }

    private void handleUpdatePreOrder(AbstractOrderModel abstractOrderModel, AbstractOrderEntryModel entryToUpdate) {
        if (!isValidUpdatePreOrder(abstractOrderModel, entryToUpdate)) {
            return;
        }

        OrderModel order = (OrderModel) abstractOrderModel;
        OrderEntryModel orderEntry = (OrderEntryModel) entryToUpdate;

        if (entryToUpdate.isHolding()) {
            inventoryService.updateHoldingStockOf(order, orderEntry);
            return;
        }

        if (entryToUpdate.isPreOrder()) {
            inventoryService.updatePreOrderOf(order, orderEntry);
        }
    }

    private boolean isValidUpdatePreOrder(AbstractOrderModel abstractOrderModel, AbstractOrderEntryModel entryToUpdate) {
        if (!OrderStatus.PRE_ORDER.code().equals(abstractOrderModel.getOrderStatus())) {
            return false;
        }
        if (!(abstractOrderModel instanceof OrderModel) || !(entryToUpdate instanceof OrderEntryModel)) {
            return false;
        }
        return true;
    }

    private void handleRevertPreOrder(AbstractOrderModel abstractOrderModel, AbstractOrderEntryModel entryToUpdate) {
        if (!isValidUpdatePreOrder(abstractOrderModel, entryToUpdate)) {
            return;
        }
        OrderModel order = (OrderModel) abstractOrderModel;
        OrderEntryModel orderEntry = (OrderEntryModel) entryToUpdate;

        if (entryToUpdate.isHolding()) {
            inventoryService.resetHoldingStockOf(order, orderEntry);
            return;
        }

        if (entryToUpdate.isPreOrder()) {
            inventoryService.subtractPreOrder(order, orderEntry);
        }
    }

    @Override
    public void handleUpdateEntryStockHoldingOnline(AbstractOrderModel abstractOrderModel, AbstractOrderEntryModel entryToUpdate, long actualAllowedQuantityChange) {
        if (actualAllowedQuantityChange == 0 || !(abstractOrderModel instanceof OrderModel) || !(entryToUpdate instanceof OrderEntryModel)) {
            return;
        }
        OrderStatus currentStatus = OrderStatus.findByCode(abstractOrderModel.getOrderStatus());
        if (currentStatus == null || OrderStatus.CONFIRMED.value() > currentStatus.value() || OrderStatus.SHIPPING.value() <= currentStatus.value()) {
            return;
        }
        OrderModel order = (OrderModel) abstractOrderModel;
        OrderEntryModel orderEntry = (OrderEntryModel) entryToUpdate;
        List<UpdateProductInventoryDetailData> dataList = new ArrayList<>();
        if (StringUtils.isBlank(orderEntry.getComboType())) {
            UpdateProductInventoryDetailData data = new UpdateProductInventoryDetailData();
            data.setValue(Math.abs(actualAllowedQuantityChange));
            data.setProductId(orderEntry.getProductId());
            dataList.add(data);
        } else if (CollectionUtils.isNotEmpty(orderEntry.getSubOrderEntries())){
            for (SubOrderEntryModel subOrderEntry : orderEntry.getSubOrderEntries()) {
                UpdateProductInventoryDetailData data = new UpdateProductInventoryDetailData();
                long quantity = (subOrderEntry.getQuantity() / orderEntry.getQuantity()) * actualAllowedQuantityChange;
                data.setValue(Math.abs(quantity));
                data.setProductId(subOrderEntry.getProductId());
                dataList.add(data);
            }
        }
        if (CollectionUtils.isNotEmpty(dataList)) {
            inventoryService.updateStockHoldingProductOfList(order, dataList, actualAllowedQuantityChange > 0 ? true : false);
        }
        if (entryToUpdate.getQuantity() <= 0 && CollectionUtils.isNotEmpty(entryToUpdate.getToppingOptionModels())) {
            addOrRemoveStockHoldingToppingWithOrder(order, entryToUpdate.getToppingOptionModels().stream().collect(Collectors.toList()), true);
        }
    }

    @Override
    public void addOrRemoveStockHoldingToppingWithOrder(OrderModel orderModel, List<ToppingOptionModel> toppingOptionModels, boolean deleted) {
        OrderStatus currentStatus = OrderStatus.findByCode(orderModel.getOrderStatus());
        if (currentStatus == null || OrderStatus.CONFIRMED.value() > currentStatus.value() || OrderStatus.SHIPPING.value() <= currentStatus.value()) {
            return;
        }
        List<UpdateProductInventoryDetailData> dataList = new ArrayList<>();
        for (ToppingOptionModel toppingOptionModel : toppingOptionModels) {
            for (ToppingItemModel toppingItemModel : toppingOptionModel.getToppingItemModels()) {
                UpdateProductInventoryDetailData data = new UpdateProductInventoryDetailData();
                int quantity = toppingItemModel.getQuantity() * toppingOptionModel.getQuantity();
                data.setValue((long) quantity);
                data.setProductId(toppingItemModel.getProductId());
                dataList.add(data);
            }
        }
        if (CollectionUtils.isEmpty(dataList)) return;
        inventoryService.updateStockHoldingProductOfList(orderModel, dataList, !deleted);
    }

    @Override
    public void updateStockHoldingToppingOptionWithOrder(OrderModel orderModel, ToppingOptionModel toppingOptionModel, int optionQty) {
        List<UpdateProductInventoryDetailData> dataList = new ArrayList<>();
        for (ToppingItemModel toppingItemModel : toppingOptionModel.getToppingItemModels()) {
            UpdateProductInventoryDetailData data = new UpdateProductInventoryDetailData();
            int quantity = toppingItemModel.getQuantity() * Math.abs(optionQty);
            data.setValue((long) quantity);
            data.setProductId(toppingItemModel.getProductId());
            dataList.add(data);
        }
        if (CollectionUtils.isEmpty(dataList)) return;
        inventoryService.updateStockHoldingProductOfList(orderModel, dataList, optionQty > 0);
    }

    @Override
    public void handleUpdateToppingItemStockHoldingOnline(OrderModel orderModel, Long productId, Integer quantity) {
        OrderStatus currentStatus = OrderStatus.findByCode(orderModel.getOrderStatus());
        if (quantity == 0 || currentStatus == null || OrderStatus.CONFIRMED.value() > currentStatus.value() || OrderStatus.SHIPPING.value() <= currentStatus.value()) {
            return;
        }
        UpdateProductInventoryDetailData data = new UpdateProductInventoryDetailData();
        data.setValue(Math.abs(quantity.longValue()));
        data.setProductId(productId);
        inventoryService.updateStockHoldingProductOfList(orderModel, Arrays.asList(data), quantity > 0);
    }

    @Override
    public void updateDiscountForCartEntry(CommerceAbstractOrderParameter parameter) {
        final AbstractOrderModel abstractOrderModel = parameter.getOrder();
        final AbstractOrderEntryModel entryToUpdate = getEntryForId(abstractOrderModel, parameter.getEntryId());
        if (entryToUpdate == null) {
            ErrorCodes err = ErrorCodes.INVALID_ENTRY_NUMBER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        CurrencyType currencyType = CurrencyType.findByCode(parameter.getDiscountType());
        if (currencyType == null) {
            ErrorCodes err = ErrorCodes.INVALID_DISCOUNT_TYPE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        entryToUpdate.setDiscount(parameter.getDiscount());
        entryToUpdate.setDiscountType(parameter.getDiscountType());

        getModelService().save(entryToUpdate);
        recalculate(abstractOrderModel, false);
    }

    @Override
    public void updatePriceForCartEntry(CommerceAbstractOrderParameter parameter) {
        final AbstractOrderModel abstractOrderModel = parameter.getOrder();
        final AbstractOrderEntryModel entryToUpdate = getEntryForId(abstractOrderModel, parameter.getEntryId());
        if (entryToUpdate == null) {
            ErrorCodes err = ErrorCodes.INVALID_ENTRY_NUMBER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        entryToUpdate.setBasePrice(parameter.getBasePrice());
        if (CollectionUtils.isNotEmpty(entryToUpdate.getSubOrderEntries())) {
            this.validatePriceForCartEntry(entryToUpdate, abstractOrderModel);
        }
        getModelService().save(entryToUpdate);
        recalculate(abstractOrderModel, false);
    }

    @Override
    public void updateWeightForOrderEntry(CommerceAbstractOrderParameter parameter) {
        final AbstractOrderModel orderModel = parameter.getOrder();
        final AbstractOrderEntryModel entryToUpdate = getEntryForId(orderModel, parameter.getEntryId());
        if (entryToUpdate == null) {
            ErrorCodes err = ErrorCodes.INVALID_ENTRY_NUMBER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        entryToUpdate.setWeight(parameter.getWeight());

        getModelService().save(entryToUpdate);
    }

    @Override
    public void updateSubOrderEntry(AbstractOrderEntryModel entry) {
        calculationService.calculateSubEntryPriceWithCombo(entry);
        getModelService().save(entry);
    }

    @Override
    public CommerceCartModification removeListCartEntry(AbstractOrderModel model, EntryRequest request) {
        CommerceCartModification commerceCartModification = new CommerceCartModification();
        List<Long> entryIds = CommonUtils.parseLongStringByComma(request.getEntryIds());
        List<AbstractOrderEntryModel> entryModels = model.getEntries().stream()
                .filter(e -> entryIds.contains(e.getId())).collect(Collectors.toList());
        model.getEntries().removeAll(entryModels);
        getModelService().save(model);
        normalizeEntryNumbers(model);
        commerceCartModification.setOrder(model);
        recalculate(model, false);

        return commerceCartModification;
    }

    @Override
    public void validatePriceForCartEntry(AbstractOrderEntryModel orderEntryModel, AbstractOrderModel model) {
        if(SellSignal.ECOMMERCE_WEB.toString().equalsIgnoreCase(model.getSellSignal())) {
            //ignore when selling on ecommerce website
            return;
        }

        Long companyId = model.getCompanyId();
        ComboData comboData = productService.getCombo(orderEntryModel.getProductId(), companyId);
        Set<SubOrderEntryModel> subOrderEntries = orderEntryModel.getSubOrderEntries();
        int totalItems = subOrderEntries.stream().filter(soe -> soe.getQuantity() != null)
                .mapToInt(SubOrderEntryModel::getQuantity).sum();
        int maxTotalItems = (int) (comboData.getTotalItemQuantity() * orderEntryModel.getQuantity());
        Double comboPrice;
        if (PriceType.WHOLESALE_PRICE.toString().equals(model.getPriceType())) {
            comboPrice = comboData.getWholesalePrice();
        } else {
            PriceData priceData = productService.getPriceOfProduct(comboData.getId(), orderEntryModel.getQuantity().intValue());
            comboPrice = priceData.getPrice();
        }

        if (!comboPrice.equals(orderEntryModel.getBasePrice()) && totalItems == maxTotalItems) {
            double totalOriginPriceInCombo = subOrderEntries.stream().filter(soe -> soe.getOriginPrice() != null)
                    .mapToDouble(soe -> soe.getOriginPrice() * soe.getQuantity()).sum() / orderEntryModel.getQuantity();
            if (orderEntryModel.getBasePrice() > totalOriginPriceInCombo) {
                ErrorCodes err = ErrorCodes.INVALID_COMBO_PRICE_LESS_THAN;
                throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{totalOriginPriceInCombo, orderEntryModel.getProductId(), err.code()});
            }
            double minimumComboPrice = calculateMinimumComboPrice(totalOriginPriceInCombo, companyId);
            if (orderEntryModel.getBasePrice() < minimumComboPrice) {
                ErrorCodes err = ErrorCodes.INVALID_COMBO_PRICE_LARGER_THAN;
                throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{minimumComboPrice, orderEntryModel.getProductId(), err.code()});
            }
        }
    }

    private double calculateMinimumComboPrice(Double price, Long companyId) {
        OrderSettingModel orderSettingModel = comboPriceSettingService.findByTypeAndCompanyId(OrderSettingType.COMBO_PRICE_SETTING.code(), companyId);
        if (orderSettingModel == null) {
            return 0;
        }

        return CurrencyUtils.computeValue(orderSettingModel.getAmount(), orderSettingModel.getType(), price);
    }

    protected void recalculate(AbstractOrderModel cartModel, boolean isMarkEntrySaleOff) {
        final CommerceAbstractOrderParameter cartParameter = new CommerceAbstractOrderParameter();
        cartParameter.setOrder(cartModel);
        cartParameter.setRecalculateVat(!isMarkEntrySaleOff);
        getCommerceCartCalculationStrategy().recalculateCart(cartParameter);
        getModelService().save(cartModel);
    }

    protected void validateEntryBeforeModification(final long newQuantity,
                                                   final AbstractOrderEntryModel entryToUpdate) {
        if (newQuantity < 0) {
            ErrorCodes err = ErrorCodes.INVALID_QUANTITY;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (entryToUpdate == null) {
            ErrorCodes err = ErrorCodes.INVALID_ENTRY_NUMBER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    protected void recalculateComboOrderEntry(AbstractOrderModel model, AbstractOrderEntryModel entry, long currentEntryQuantity, long entryNewQuantity) {
        if (StringUtils.isBlank(entry.getComboType()) && CollectionUtils.isEmpty(entry.getSubOrderEntries())) {
            return;
        }

        int entryQty = entry.getQuantity() == null ? 0 : entry.getQuantity().intValue();
        updateBasePriceForComboIfNeed(model, entry, entryQty);
        updateSubOrderEntryQty(entry, (int) currentEntryQuantity, (int) entryNewQuantity);
        this.validatePriceForCartEntry(entry, model);
    }

    @Override
    public void updateBasePriceForComboIfNeed(AbstractOrderModel model, AbstractOrderEntryModel entry, int entryQty) {
        String priceType = entry.getOrder().getPriceType();
        if (PriceType.WHOLESALE_PRICE.toString().equals(priceType)) {
            return;
        }

        PriceData priceData = productService.getPriceOfProduct(entry.getProductId(), entryQty);
        Double productPrice = priceData != null ? priceData.getPrice() : null;
        if (PriceType.DISTRIBUTOR_PRICE.toString().equals(model.getPriceType())) {
            populatePriceWithPriceTypeDistributor(model, entry, productPrice);
            return;
        }
        Double originBasePrice = entry.getOriginBasePrice();
        if (originBasePrice == null || !originBasePrice.equals(productPrice)) {
            entry.setBasePrice(productPrice);
            entry.setOriginBasePrice(productPrice);
        }
    }

    @Override
    @Transactional
    public AbstractOrderEntryModel markEntrySaleOff(CommerceAbstractOrderParameter parameter) {
        final AbstractOrderModel orderModel = parameter.getOrder();
        final AbstractOrderEntryModel entryToUpdate = getEntryForId(orderModel, parameter.getEntryId());
        if (entryToUpdate == null) {
            ErrorCodes err = ErrorCodes.INVALID_ENTRY_NUMBER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (parameter.isSaleOff() || orderModel instanceof OrderModel) {
            entryToUpdate.setSaleOff(parameter.isSaleOff());
            getModelService().save(entryToUpdate);
            recalculate(orderModel, true);
            return entryToUpdate;
        }

        Optional<AbstractOrderEntryModel> normalEntryOptional = orderModel.getEntries().stream()
                .filter(e -> e.getProductId().equals(entryToUpdate.getProductId())
                        && !e.isGiveAway() && !e.isSaleOff()
                        && !e.getId().equals(entryToUpdate.getId())).findFirst();
        if (!normalEntryOptional.isPresent()) {
            entryToUpdate.setSaleOff(false);
            entryToUpdate.setDiscount(null);
            entryToUpdate.setDiscountType(null);
            getModelService().save(entryToUpdate);
            recalculate(orderModel, true);
            return entryToUpdate;
        }

        AbstractOrderEntryModel normalEntry = normalEntryOptional.get();
        final long quantityToAdd = entryToUpdate.getQuantity();
        final long actualAllowedQuantityChange = getAllowedCartAdjustmentForProduct(orderModel, normalEntry,
                quantityToAdd);
        final long entryNewQuantity = normalEntry.getQuantity() + actualAllowedQuantityChange;
        normalEntry.setQuantity(entryNewQuantity);
        orderModel.getEntries().remove(entryToUpdate);
        normalizeEntryNumbers(orderModel);
        getModelService().save(entryToUpdate);
        recalculate(orderModel, true);
        return entryToUpdate;
    }

    private void populatePriceWithPriceTypeDistributor(AbstractOrderModel model, AbstractOrderEntryModel entry, Double productPrice) {
        Double originBasePrice = entry.getOriginBasePrice();
        DistributorSetingPriceData setingPriceData = getDistributorPriceSettingBy(model, entry.getProductId());
        if ((originBasePrice != null && originBasePrice.equals(productPrice))) return;
        if (setingPriceData == null) {
            entry.setRecommendedRetailPrice(productPrice);
            entry.setOriginBasePrice(productPrice);
            entry.setBasePrice(productPrice);
        }
        if (setingPriceData != null && setingPriceData.getRecommendedRetailPrice() == null) {
            entry.setRecommendedRetailPrice(productPrice);
            entry.setOriginBasePrice(productPrice);
            productPrice = logisticService.calculateDistributorSettingPrice(setingPriceData, entry.getRecommendedRetailPrice());
            entry.setBasePrice(productPrice);
        }
    }

    @Override
    public boolean updateRecommendedRetailPriceForCartEntry(CommerceAbstractOrderParameter parameter) {
        final AbstractOrderModel abstractOrderModel = parameter.getOrder();
        final AbstractOrderEntryModel entryToUpdate = getEntryForId(abstractOrderModel, parameter.getEntryId());
        if (entryToUpdate == null) {
            ErrorCodes err = ErrorCodes.INVALID_ENTRY_NUMBER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        entryToUpdate.setRecommendedRetailPrice(parameter.getRecommendedRetailPrice());
        getModelService().save(entryToUpdate);
        DistributorSetingPriceData setingPriceData = getDistributorPriceSettingBy(abstractOrderModel, entryToUpdate.getProductId());
        if (setingPriceData != null && SettingPriceType.PRICE_BY_DISCOUNT.toString().equals(setingPriceData.getType())) {
            double basePrice = logisticService.calculateDistributorSettingPrice(setingPriceData, parameter.getRecommendedRetailPrice());
            parameter.setBasePrice(basePrice);
            updatePriceForCartEntry(parameter);
            return true;
        }
        return false;
    }

    private DistributorSetingPriceData getDistributorPriceSettingBy(AbstractOrderModel model, Long productId) {
        if (!PriceType.DISTRIBUTOR_PRICE.toString().equals(model.getPriceType())) return null;
        Map<Long, DistributorSetingPriceData> priceDataMap = logisticService.getProductPriceSetting(
                model.getDistributorId(), model.getCompanyId(), Arrays.asList(productId));
        if (priceDataMap.containsKey(productId)) {
            return priceDataMap.get(productId);
        }
        return null;
    }

    @Override
    public void updateSubOrderEntryQty(AbstractOrderEntryModel orderEntry, int entryOldQuantity, int entryNewQuantity) {
        super.updateSubOrderEntryQty(orderEntry, entryOldQuantity, entryNewQuantity);
    }

    @Autowired
    public void setCalculationService(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @Autowired
    public void setComboPriceSettingService(ComboPriceSettingService comboPriceSettingService) {
        this.comboPriceSettingService = comboPriceSettingService;
    }

    @Autowired
    public void setLogisticService(LogisticService logisticService) {
        this.logisticService = logisticService;
    }
}
