package com.vctek.orderservice.strategy.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.dto.CommerceAddComboParameter;
import com.vctek.orderservice.dto.CommerceCartModification;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.ProductIsCombo;
import com.vctek.orderservice.feignclient.dto.UpdateProductInventoryDetailData;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.service.CartService;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.strategy.CommerceUpdateCartEntryStrategy;
import com.vctek.orderservice.util.DiscountType;
import com.vctek.orderservice.util.PriceType;
import com.vctek.util.ComboType;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class DefaultAddToCartStrategy extends AbstractAddToCartStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAddToCartStrategy.class);
    private CartService cartService;
    private OrderService orderService;
    private CommerceUpdateCartEntryStrategy commerceUpdateCartEntryStrategy;

    @Override
    public CommerceCartModification addToCart(CommerceAbstractOrderParameter parameter) {
        final CommerceCartModification modification = doAddToCart(parameter);
        parameter.setRecalculateVat(true);
        commerceCartCalculationStrategy.recalculateCart(parameter);
        return modification;
    }

    @Override
    public CommerceCartModification addEntryToOrder(CommerceAbstractOrderParameter parameter) {
        final long quantityToAdd = parameter.getQuantity();
        validateAddToCart(parameter);
        validateValidAddEntryToOnlineOrder(parameter);
        OrderModel order = (OrderModel) parameter.getOrder();
        Long productId = parameter.getProductId();
        OrderEntryModel entryModel = orderService.addNewEntry(order, productId, quantityToAdd, false);
        entryModel.setDiscount(parameter.getDiscount());
        entryModel.setDiscountType(parameter.getDiscountType());
        entryModel.setBasePrice(parameter.getBasePrice());
        entryModel.setOriginBasePrice(parameter.getBasePrice());
        entryModel.setRecommendedRetailPrice(parameter.getRecommendedRetailPrice());
        entryModel.setWarehouseId(parameter.getWarehouseId());
        CommerceAddComboParameter param = new CommerceAddComboParameter();
        param.setEntryModel(entryModel);
        param.setAbstractOrderModel(order);
        param.setQuantityToAdd(quantityToAdd);
        param.setProductComboId(productId);

        orderService.doAddComboToCart(param);
        modelService.save(order);
        parameter.setRecalculateVat(true);
        commerceCartCalculationStrategy.recalculateCart(parameter);
        return createAddToCartResp(entryModel, quantityToAdd);
    }

    @Override
    public void changeOrderEntryToComboEntry(CommerceAbstractOrderParameter parameter) {
        validateValidAddEntryToOnlineOrder(parameter);
        AbstractOrderModel abstractOrderModel = parameter.getOrder();
        AbstractOrderEntryModel fixedComboEntry = this.findFixedComboEntryOf(abstractOrderModel, parameter.getComboId());
        AbstractOrderEntryModel normalEntry = getEntryForId(abstractOrderModel, parameter.getEntryId());
        abstractOrderModel.getEntries().remove(normalEntry);

        if(fixedComboEntry != null) {
            resetHoldingStockOrPreOrderComboExits(abstractOrderModel, fixedComboEntry, normalEntry);
            long oldQty = fixedComboEntry.getQuantity() == null ? 0 : fixedComboEntry.getQuantity();
            long newQty = oldQty + 1;
            fixedComboEntry.setQuantity(newQty);
            commerceUpdateCartEntryStrategy.updateBasePriceForComboIfNeed(abstractOrderModel, fixedComboEntry, (int) newQty);
            updateSubOrderEntryQty(fixedComboEntry, (int) oldQty, (int) newQty);
            commerceUpdateCartEntryStrategy.validatePriceForCartEntry(fixedComboEntry, abstractOrderModel);
            abstractOrderModel.setCalculated(false);

            addHoldingStockOrPreOrderChangeToCombo(abstractOrderModel, fixedComboEntry, normalEntry);
            modelService.save(abstractOrderModel);
            parameter.setRecalculateVat(true);
            commerceCartCalculationStrategy.recalculateCart(parameter);
            return;
        }

        int comboQty = normalEntry.getQuantity() == null ? 0 : normalEntry.getQuantity().intValue();
        ProductIsCombo comboData = productService.checkIsCombo(parameter.getComboId(), parameter.getCompanyId(), comboQty);
        if(!comboData.isCombo()) {
            ErrorCodes err = ErrorCodes.PRODUCT_IS_NOT_COMBO;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        validateWholesalePriceTypeWithCombo(abstractOrderModel, comboData);

        final AbstractOrderEntryModel abstractOrderEntryModel;
        if(abstractOrderModel instanceof CartModel) {
            abstractOrderEntryModel = cartService.addNewEntry((CartModel) abstractOrderModel, comboData.getId(), comboQty, false);
        } else {
            abstractOrderEntryModel = orderService.addNewEntry((OrderModel) abstractOrderModel, comboData.getId(), comboQty, false);
        }

        orderService.updateComboPriceOfEntryWith(abstractOrderModel, abstractOrderEntryModel, comboData,abstractOrderEntryModel.getProductId());
        abstractOrderEntryModel.setWarehouseId(abstractOrderModel.getWarehouseId());
        abstractOrderEntryModel.setFixedDiscount(0d);
        abstractOrderEntryModel.setQuantity(Long.valueOf(comboQty));
        abstractOrderEntryModel.setDiscountType(DiscountType.PERCENT.toString());
        String comboType = comboData.getComboType();
        abstractOrderEntryModel.setComboType(comboType);
        abstractOrderEntryModel.setPreOrder(normalEntry.isPreOrder());
        abstractOrderEntryModel.setHolding(normalEntry.isHolding());
        if (ComboType.FIXED_COMBO.toString().equals(comboType)) {
            cartService.addSubOrderEntriesToComboEntry(abstractOrderEntryModel, comboData.getComboProducts(), comboQty);
        } else {
            changeProductToSubOrderEntryWithDynamicCombo(parameter.getComboGroupNumber(), normalEntry, abstractOrderEntryModel);
        }

        super.normalizeEntryNumbers(abstractOrderModel);
        addHoldingStockOrPreOrderChangeToCombo(abstractOrderModel, abstractOrderEntryModel, normalEntry);
        commerceUpdateCartEntryStrategy.validatePriceForCartEntry(abstractOrderEntryModel, abstractOrderModel);
        modelService.save(abstractOrderModel);
        parameter.setRecalculateVat(true);
        commerceCartCalculationStrategy.recalculateCart(parameter);
    }

    private void resetHoldingStockOrPreOrderComboExits(AbstractOrderModel orderModel, AbstractOrderEntryModel entryModel, AbstractOrderEntryModel normalEntry) {
        if ((orderModel instanceof CartModel) || !OrderType.ONLINE.toString().equals(orderModel.getType())
                || (OrderType.ONLINE.toString().equals(orderModel.getType())
                && !OrderStatus.PRE_ORDER.code().equals(orderModel.getOrderStatus()))
                || (!entryModel.isPreOrder() && !entryModel.isHolding() && !normalEntry.isHolding() && !normalEntry.isPreOrder())) {
            return;
        }

        if ((entryModel.isPreOrder() || entryModel.isHolding()) && (normalEntry.isHolding() || normalEntry.isPreOrder())) {
            reSetHoldingStockOrPreOrder(orderModel, entryModel);
        }

        if (normalEntry.isHolding() || normalEntry.isPreOrder()) {
            entryModel.setHolding(normalEntry.isHolding());
            entryModel.setPreOrder(normalEntry.isPreOrder());
        }
    }

    private void reSetHoldingStockOrPreOrder(AbstractOrderModel order, AbstractOrderEntryModel entry) {
        if (entry.isPreOrder()) {
            List<UpdateProductInventoryDetailData> dataList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(entry.getSubOrderEntries())) {
                for (SubOrderEntryModel subOrderEntry : entry.getSubOrderEntries()) {
                    UpdateProductInventoryDetailData resetPreOrderData = new UpdateProductInventoryDetailData();
                    resetPreOrderData.setProductId(subOrderEntry.getProductId());
                    resetPreOrderData.setValue(subOrderEntry.getQuantity().longValue());
                    dataList.add(resetPreOrderData);
                }
                inventoryService.updatePreOrderProductOfList((OrderModel) order, dataList, false);
                return;
            }

            UpdateProductInventoryDetailData resetPreOrderData = new UpdateProductInventoryDetailData();
            resetPreOrderData.setProductId(entry.getProductId());
            resetPreOrderData.setValue(entry.getHoldingStock());
            dataList.add(resetPreOrderData);
            inventoryService.updatePreOrderProductOfList((OrderModel) order, dataList, false);
            return;
        }

        if (entry.isHolding()) {
            inventoryService.resetHoldingStockOf((OrderModel) order, (OrderEntryModel) entry);
        }
    }

    private void addHoldingStockOrPreOrderChangeToCombo(AbstractOrderModel orderModel, AbstractOrderEntryModel entryModel,
                                                        AbstractOrderEntryModel normalEntry) {
        if ((orderModel instanceof CartModel) || !OrderType.ONLINE.toString().equals(orderModel.getType())
                || (OrderType.ONLINE.toString().equals(orderModel.getType())
                && !OrderStatus.PRE_ORDER.code().equals(orderModel.getOrderStatus()))) {
            return;
        }

        if (!entryModel.isHolding() && !entryModel.isPreOrder()) return;

        entryModel.setHoldingStock(entryModel.getQuantity());
        OrderModel order = (OrderModel) orderModel;

        if (entryModel.isPreOrder() || entryModel.isHolding()) {
            List<UpdateProductInventoryDetailData> inventoryDetailList = new ArrayList<>();
            for (SubOrderEntryModel subOrderEntry : entryModel.getSubOrderEntries()) {
                UpdateProductInventoryDetailData data = new UpdateProductInventoryDetailData();
                data.setProductId(subOrderEntry.getProductId());
                data.setValue(subOrderEntry.getQuantity().longValue());
                inventoryDetailList.add(data);
            }

            addHoldingAndPreOrderOfList(order, inventoryDetailList, entryModel.isHolding(), entryModel.isPreOrder());
        }

        if (normalEntry.isHolding() || normalEntry.isPreOrder()) {
            reSetHoldingStockOrPreOrder(order, normalEntry);
        }
    }

    private void addHoldingAndPreOrderOfList(OrderModel order, List<UpdateProductInventoryDetailData> dataList, boolean holding, boolean preOrder) {
        if(holding) {
            inventoryService.updateStockHoldingProductOfList(order, dataList, holding);
            return;
        }

        if(preOrder) {
            inventoryService.updatePreOrderProductOfList(order, dataList, preOrder);
        }
    }

    private void validateWholesalePriceTypeWithCombo(AbstractOrderModel abstractOrderModel, ProductIsCombo comboData) {
        if(!PriceType.WHOLESALE_PRICE.toString().equals(abstractOrderModel.getPriceType())) {
            return;
        }

        if(comboData.getWholesalePrice() == null) {
            ErrorCodes err = ErrorCodes.PRODUCT_HAS_NOT_WHOLESALE_PRICE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    protected void changeProductToSubOrderEntryWithDynamicCombo(Integer comboGroupNumber,
                                            AbstractOrderEntryModel entryModel, AbstractOrderEntryModel cartEntryModel) {
        SubOrderEntryModel subOrderEntryModel = new SubOrderEntryModel();
        subOrderEntryModel.setComboGroupNumber(comboGroupNumber);
        subOrderEntryModel.setProductId(entryModel.getProductId());
        subOrderEntryModel.setOriginPrice(entryModel.getBasePrice());
        subOrderEntryModel.setQuantity(Math.toIntExact(cartEntryModel.getQuantity()));
        subOrderEntryModel.setOrderEntry(cartEntryModel);
        cartEntryModel.getSubOrderEntries().add(subOrderEntryModel);
    }

    private AbstractOrderEntryModel findFixedComboEntryOf(AbstractOrderModel cart, Long comboId) {
        Optional<AbstractOrderEntryModel> fixedComboEntryOption = cart.getEntries().stream()
                .filter(e -> e.getProductId() != null && e.getProductId().equals(comboId)
                    && ComboType.FIXED_COMBO.toString().equals(e.getComboType()))
                .findFirst();
        return fixedComboEntryOption.isPresent() ? fixedComboEntryOption.get() : null;
    }

    protected CommerceCartModification doAddToCart(final CommerceAbstractOrderParameter parameter) {
        validateAddToCart(parameter);
        final long quantityToAdd = parameter.getQuantity();
        LOGGER.debug("quantityToAdd: {}", quantityToAdd);
        CartModel cart = (CartModel) parameter.getOrder();
        Long productId = parameter.getProductId();
        CartEntryModel entryModel = cartService.addNewEntry(cart, productId, quantityToAdd, false);
        entryModel.setDiscount(parameter.getDiscount());
        entryModel.setDiscountType(parameter.getDiscountType());
        entryModel.setWarehouseId(parameter.getWarehouseId());
        entryModel.setBasePrice(parameter.getBasePrice());
        entryModel.setOriginBasePrice(parameter.getOriginBasePrice());
        entryModel.setRecommendedRetailPrice(parameter.getRecommendedRetailPrice());

        CommerceAddComboParameter param = new CommerceAddComboParameter();
        param.setEntryModel(entryModel);
        param.setAbstractOrderModel(cart);
        param.setQuantityToAdd(quantityToAdd);
        param.setProductComboId(productId);

        cartService.doAddComboToCart(param);

        modelService.save(cart);
        return createAddToCartResp(entryModel, quantityToAdd);
    }

    protected CommerceCartModification createAddToCartResp(final AbstractOrderEntryModel entry, final long quantityAdded)
    {
        final CommerceCartModification modification = new CommerceCartModification();
        modification.setQuantityAdded(quantityAdded);
        modification.setEntry(entry);
        return modification;
    }


    @Autowired
    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    public void setCommerceUpdateCartEntryStrategy(CommerceUpdateCartEntryStrategy commerceUpdateCartEntryStrategy) {
        this.commerceUpdateCartEntryStrategy = commerceUpdateCartEntryStrategy;
    }
}
