package com.vctek.orderservice.strategy.impl;

import com.vctek.orderservice.feignclient.CompanyClient;
import com.vctek.orderservice.feignclient.dto.ProductStockData;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.CartEntryModel;
import com.vctek.orderservice.model.SubOrderEntryModel;
import com.vctek.orderservice.repository.CartEntryRepository;
import com.vctek.orderservice.service.InventoryService;
import com.vctek.orderservice.service.ModelService;
import com.vctek.orderservice.service.ProductService;
import com.vctek.orderservice.strategy.CommerceCartCalculationStrategy;
import com.vctek.orderservice.util.SellSignal;
import com.vctek.util.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public abstract class AbstractCommerceCartStrategy {
    protected ModelService modelService;
    protected CartEntryRepository cartEntryRepository;
    protected CommerceCartCalculationStrategy commerceCartCalculationStrategy;
    protected CompanyClient companyClient;
    protected InventoryService inventoryService;
    protected ProductService productService;

    protected AbstractOrderEntryModel getEntryForId(final AbstractOrderModel order, final Long entryId) {
        final List<AbstractOrderEntryModel> entries = order.getEntries();
        if (CollectionUtils.isNotEmpty(entries)) {
            for (final AbstractOrderEntryModel entry : entries) {
                if (entry != null && entryId.equals(entry.getId())) {
                    return entry;
                }
            }
        }

        return null;
    }

    protected long getAllowedCartAdjustmentForProduct(final AbstractOrderModel abstractOrderModel, final AbstractOrderEntryModel entryToUpdate,
                                                      final long quantityToAdd) {
        Long productId = entryToUpdate.getProductId();
        Long warehouseId = entryToUpdate.getWarehouseId();
        final long cartLevel = checkCartLevel(productId, abstractOrderModel, warehouseId);
        final long stockLevel = getAvailableStockLevel(abstractOrderModel, entryToUpdate);

        // How many will we have in our cart if we add quantity
        final long newTotalQuantity = cartLevel + quantityToAdd;

        // Now limit that to the total available in stock
        final long newTotalQuantityAfterStockLimit = Math.min(newTotalQuantity, stockLevel);
        return newTotalQuantityAfterStockLimit - cartLevel;
    }

    private long getAvailableStockLevel(AbstractOrderModel abstractOrderModel, final AbstractOrderEntryModel entryToUpdate) {
        if(SellSignal.ECOMMERCE_WEB.toString().equals(abstractOrderModel.getSellSignal())) {
            return getStorefrontAvailableStock(abstractOrderModel, entryToUpdate);
        }

        Boolean sellLessZero = companyClient.checkSellLessZero(abstractOrderModel.getCompanyId());
        if (sellLessZero != null && sellLessZero) {
            if (CollectionUtils.isNotEmpty(entryToUpdate.getSubOrderEntries()) || StringUtils.isNotBlank(entryToUpdate.getComboType())) {
                return productService.getComboAvailableStock(entryToUpdate.getProductId(),
                        abstractOrderModel.getCompanyId());
            }

            return Long.MAX_VALUE;
        }

        if (CollectionUtils.isNotEmpty(entryToUpdate.getSubOrderEntries()) || StringUtils.isNotBlank(entryToUpdate.getComboType())) {
            return productService.getComboAvailableStock(entryToUpdate.getProductId(),
                    abstractOrderModel.getCompanyId());
        }

        ProductStockData availableStockData = inventoryService.getAvailableStock(entryToUpdate.getProductId(),
                abstractOrderModel.getCompanyId(), abstractOrderModel.getWarehouseId());
        if (availableStockData == null) {
            return 0l;
        }

        return availableStockData.getQuantity() == null ? 0l : availableStockData.getQuantity();
    }

    private int getStorefrontAvailableStock(AbstractOrderModel abstractOrderModel, AbstractOrderEntryModel entryToUpdate) {
        ProductStockData stockOfProduct = inventoryService.getStoreFrontStockOfProduct(entryToUpdate.getProductId(), abstractOrderModel.getCompanyId());
        return CommonUtils.readValue(stockOfProduct.getQuantity());
    }

    private long checkCartLevel(Long productId, AbstractOrderModel cartModel, Long warehouseId) {
        long cartLevel = 0;
        List<CartEntryModel> entries;
        if (warehouseId == null) {
            entries = cartEntryRepository.findAllByOrderAndProductId(cartModel, productId);
        } else {
            entries = cartEntryRepository.findAllByOrderAndProductIdAndWarehouseId(cartModel, productId, warehouseId);
        }

        if (CollectionUtils.isEmpty(entries)) {
            return cartLevel;
        }

        for (CartEntryModel entry : entries) {
            if (!entry.isSaleOff()) {
                cartLevel += entry.getQuantity() != null ? entry.getQuantity() : 0;
            }
        }

        return cartLevel;
    }

    protected void normalizeEntryNumbers(final AbstractOrderModel cartModel) {
        final List<AbstractOrderEntryModel> entries = cartModel.getEntries();
        Collections.sort(entries, Comparator.comparing(AbstractOrderEntryModel::getEntryNumber));
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).setEntryNumber(Integer.valueOf(i));
            getModelService().save(entries.get(i));
        }
    }

    protected void updateSubOrderEntryQty(AbstractOrderEntryModel orderEntry, final int entryOldQuantity, final int entryNewQuantity) {
        Set<SubOrderEntryModel> subOrderEntries = orderEntry.getSubOrderEntries();
        if (CollectionUtils.isNotEmpty(subOrderEntries)) {
            subOrderEntries.forEach(soe -> {
                int quantity = soe.getQuantity() == null ? 0 : soe.getQuantity();
                int qtyPerUnit = quantity / entryOldQuantity;
                soe.setQuantity(qtyPerUnit * entryNewQuantity);
            });
        }
    }

    @Autowired
    public void setCartEntryRepository(CartEntryRepository cartEntryRepository) {
        this.cartEntryRepository = cartEntryRepository;
    }

    public ModelService getModelService() {
        return modelService;
    }

    @Autowired
    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    public CommerceCartCalculationStrategy getCommerceCartCalculationStrategy() {
        return commerceCartCalculationStrategy;
    }

    @Autowired
    public void setCommerceCartCalculationStrategy(CommerceCartCalculationStrategy commerceCartCalculationStrategy) {
        this.commerceCartCalculationStrategy = commerceCartCalculationStrategy;
    }

    @Autowired
    public void setCompanyClient(CompanyClient companyClient) {
        this.companyClient = companyClient;
    }

    @Autowired
    public void setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }
}
