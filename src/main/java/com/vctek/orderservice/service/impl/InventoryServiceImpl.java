package com.vctek.orderservice.service.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CheckOutOfStockParam;
import com.vctek.orderservice.dto.HoldingData;
import com.vctek.orderservice.dto.request.HoldingProductRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.InventoryClient;
import com.vctek.orderservice.feignclient.dto.ProductStockData;
import com.vctek.orderservice.feignclient.dto.UpdateInventoryStatusRequest;
import com.vctek.orderservice.feignclient.dto.UpdateProductInventoryDetailData;
import com.vctek.orderservice.feignclient.dto.UpdateProductInventoryRequest;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.service.InventoryService;
import com.vctek.orderservice.util.SellSignal;
import com.vctek.util.CommonUtils;
import com.vctek.util.InventoryStatus;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class InventoryServiceImpl implements InventoryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryServiceImpl.class);
    private InventoryClient inventoryClient;

    @Override
    public void holdingAllQuantityOf(OrderModel order) {
        UpdateProductInventoryRequest request = createUpdateProductInventoryRequest(order);
        request.setFrom(InventoryStatus.AVAILABLE.code());
        request.setTo(InventoryStatus.HOLDING.code());
        List<UpdateProductInventoryDetailData> detailDataList = populateUpdateProductInventoryDetails(order.getEntries(), false, false);
        if(CollectionUtils.isNotEmpty(detailDataList)) {
            request.setDetailDataList(detailDataList);
            inventoryClient.changeInventoryByStatus(request);
        }
    }

    private UpdateProductInventoryRequest createUpdateProductInventoryRequest(OrderModel order) {
        UpdateProductInventoryRequest request = new UpdateProductInventoryRequest();
        request.setWarehouseId(order.getWarehouseId());
        request.setCompanyId(order.getCompanyId());
        return request;
    }

    @Override
    public void changeAllHoldingToAvailableOf(OrderModel order) {
        UpdateProductInventoryRequest request = createUpdateProductInventoryRequest(order);
        request.setFrom(InventoryStatus.HOLDING.code());
        request.setTo(InventoryStatus.AVAILABLE.code());
        List<UpdateProductInventoryDetailData> detailDataList = populateUpdateProductInventoryDetails(order.getEntries(), true, true);
        request.setDetailDataList(detailDataList);
        if(CollectionUtils.isNotEmpty(detailDataList)) {
            inventoryClient.changeInventoryByStatus(request);
        }
    }

    @Override
    public void subtractPreOrder(OrderModel order) {
        List<AbstractOrderEntryModel> entries = order.getEntries();
        UpdateInventoryStatusRequest preOrderRequest = createUpdateInventoryStatusRequest(order);
        preOrderRequest.setStatusCode(InventoryStatus.PRE_ORDER.code());
        List<UpdateProductInventoryDetailData> details = new ArrayList<>();

        UpdateProductInventoryDetailData detailData;
        for(AbstractOrderEntryModel entryModel : entries) {
            if(entryModel.isPreOrder()) {
                Set<SubOrderEntryModel> subOrderEntries = entryModel.getSubOrderEntries();
                if(CollectionUtils.isNotEmpty(subOrderEntries)) {
                    List<UpdateProductInventoryDetailData> comboEntries = populateUpdateComboEntries(subOrderEntries);
                    details.addAll(comboEntries);
                    continue;
                }

                detailData = new UpdateProductInventoryDetailData();
                detailData.setValue(entryModel.getHoldingStock());
                detailData.setProductId(entryModel.getProductId());
                details.add(detailData);
            }
        }
        if(CollectionUtils.isNotEmpty(details)) {
            preOrderRequest.setDetailDataList(details);
            inventoryClient.subtractStockWithInventoryStatus(InventoryStatus.PRE_ORDER.code(), preOrderRequest);
        }

    }

    private UpdateInventoryStatusRequest createUpdateInventoryStatusRequest(OrderModel order) {
        UpdateInventoryStatusRequest preOrderRequest = new UpdateInventoryStatusRequest();
        preOrderRequest.setWarehouseId(order.getWarehouseId());
        preOrderRequest.setCompanyId(order.getCompanyId());
        return preOrderRequest;
    }

    @Override
    public void resetHoldingStockOf(OrderModel order) {
        UpdateProductInventoryRequest request = createUpdateProductInventoryRequest(order);
        request.setFrom(InventoryStatus.HOLDING.code());
        request.setTo(InventoryStatus.AVAILABLE.code());
        List<AbstractOrderEntryModel> entries = order.getEntries();
        List<UpdateProductInventoryDetailData> details = new ArrayList<>();
        UpdateProductInventoryDetailData detailData;
        for(AbstractOrderEntryModel entryModel : entries) {
            if(entryModel.isHolding()) {
                if(entryModel.getHoldingStock() == null || entryModel.getHoldingStock() <= 0) {
                    continue;
                }

                Set<SubOrderEntryModel> subOrderEntries = entryModel.getSubOrderEntries();
                if(CollectionUtils.isNotEmpty(subOrderEntries)) {
                    List<UpdateProductInventoryDetailData> comboEntries = populateUpdateComboEntries(subOrderEntries);
                    details.addAll(comboEntries);
                    continue;
                }

                detailData = new UpdateProductInventoryDetailData();
                detailData.setProductId(entryModel.getProductId());
                detailData.setValue(entryModel.getHoldingStock());
                details.add(detailData);
            }
        }
        if(CollectionUtils.isNotEmpty(details)) {
            request.setDetailDataList(details);
            inventoryClient.changeInventoryByStatus(request);
        }
    }

    @Override
    public void holdingProducts(HoldingProductRequest request, OrderModel orderModel) {
        List<HoldingData> holdingDataList = request.getHoldingDataList();
        if(CollectionUtils.isEmpty(holdingDataList)) {
            return;
        }

        Map<Long, HoldingData> holdingProductMap = new HashMap<>();
        for(HoldingData data : holdingDataList) {
            if(data.getProductId() != null) {
                holdingProductMap.put(data.getProductId(), data);
            }
        }

        UpdateProductInventoryRequest updateHoldingRequest = createUpdateProductInventoryRequest(orderModel);
        updateHoldingRequest.setFrom(InventoryStatus.AVAILABLE.code());
        updateHoldingRequest.setTo(InventoryStatus.HOLDING.code());

        UpdateInventoryStatusRequest updatePreOrderRequest = createUpdateInventoryStatusRequest(orderModel);
        List<UpdateProductInventoryDetailData> holdingInventoryList = new ArrayList<>();
        List<UpdateProductInventoryDetailData> preOrderList = new ArrayList<>();

        List<AbstractOrderEntryModel> entries = orderModel.getEntries();
        for(AbstractOrderEntryModel entry : entries) {
            Long productId = entry.getProductId();
            HoldingData holdingData = holdingProductMap.get(productId);
            if(holdingData != null) {
                entry.setHoldingStock(holdingData.getQuantity());
                List<UpdateProductInventoryDetailData> inventoryDetailList = populateProductInventoryDetailList(entry, holdingData.getQuantity());

                if(holdingData.isHolding()) {
                    holdingInventoryList.addAll(inventoryDetailList);
                    entry.setPreOrder(false);
                    entry.setHolding(true);
                } else if(holdingData.isPreOrder()) {
                    preOrderList.addAll(inventoryDetailList);
                    entry.setHolding(false);
                    entry.setPreOrder(true);
                }
            }
        }

        if(CollectionUtils.isNotEmpty(holdingInventoryList)) {
            updateHoldingRequest.setDetailDataList(holdingInventoryList);
            inventoryClient.changeInventoryByStatus(updateHoldingRequest);
        }

        if(CollectionUtils.isNotEmpty(preOrderList)) {
            updatePreOrderRequest.setDetailDataList(preOrderList);
            inventoryClient.addStockWithInventoryStatus(InventoryStatus.PRE_ORDER.code(), updatePreOrderRequest);
        }
    }

    private List<UpdateProductInventoryDetailData> populateProductInventoryDetailList(AbstractOrderEntryModel entry, Long quantity) {
        Set<SubOrderEntryModel> subOrderEntries = entry.getSubOrderEntries();
        if(CollectionUtils.isNotEmpty(subOrderEntries)) {
            return populateUpdateComboEntries(subOrderEntries);
        }

        UpdateProductInventoryDetailData data = new UpdateProductInventoryDetailData();
        data.setProductId(entry.getProductId());
        data.setValue(quantity);
        return Arrays.asList(data);
    }

    @Override
    public void updateHoldingProductOf(OrderModel order, OrderEntryModel entryModel, HoldingData holdingData) {

        boolean updateHolding = entryModel.isHolding() != holdingData.isHolding();
        boolean updatePreOrder = entryModel.isPreOrder() != holdingData.isPreOrder();

        if(updateHolding) {
            updateHoldingStock(order, entryModel, holdingData);
            return;
        }

        if(updatePreOrder) {
            updatePreOrder(order, entryModel, holdingData);
        }
    }

    @Override
    public void resetHoldingStockOf(OrderModel order, OrderEntryModel orderEntry) {
        if(!orderEntry.isHolding()) {
            return;
        }

        UpdateProductInventoryRequest request = createUpdateProductInventoryRequest(order);
        request.setFrom(InventoryStatus.HOLDING.code());
        request.setTo(InventoryStatus.AVAILABLE.code());

        List<UpdateProductInventoryDetailData> holdingList = populateProductInventoryDetailList(orderEntry, orderEntry.getHoldingStock());
        request.setDetailDataList(holdingList);
        inventoryClient.changeInventoryByStatus(request);

        orderEntry.setHolding(false);
        orderEntry.setHoldingStock(0l);
    }

    @Override
    public void subtractPreOrder(OrderModel orderModel, OrderEntryModel orderEntryModel) {
        if(!orderEntryModel.isPreOrder()) {
            return;
        }
        UpdateInventoryStatusRequest updatePreOrderRequest = createUpdateInventoryStatusRequest(orderModel);
        updatePreOrderRequest.setStatusCode(InventoryStatus.PRE_ORDER.code());

        List<UpdateProductInventoryDetailData> preOrderList = populateProductInventoryDetailList(orderEntryModel, orderEntryModel.getHoldingStock());
        updatePreOrderRequest.setDetailDataList(preOrderList);
        inventoryClient.subtractStockWithInventoryStatus(InventoryStatus.PRE_ORDER.code(), updatePreOrderRequest);
    }

    @Override
    public void updateHoldingStockOf(OrderModel orderModel, OrderEntryModel orderEntryModel) {
        if(!orderEntryModel.isHolding()) {
            return;
        }

        long actualHoldingStock = orderEntryModel.getHoldingStock() == null ? 0 : orderEntryModel.getHoldingStock();
        long quantity = orderEntryModel.getQuantity() == null ? 0 : orderEntryModel.getQuantity();
        long updateHolingQty = quantity - actualHoldingStock;
        if(updateHolingQty == 0) return;
        String fromStatus = updateHolingQty > 0 ? InventoryStatus.AVAILABLE.code() : InventoryStatus.HOLDING.code();
        String toStatus = updateHolingQty > 0 ? InventoryStatus.HOLDING.code() : InventoryStatus.AVAILABLE.code();
        UpdateProductInventoryRequest updateHoldingRequest = createUpdateProductInventoryRequest(orderModel);

        List<UpdateProductInventoryDetailData> detailDataList = populateUpdateStockAndPreOrderOf(orderEntryModel, updateHolingQty);
        updateHoldingRequest.setDetailDataList(detailDataList);

        updateHoldingRequest.setFrom(fromStatus);
        updateHoldingRequest.setTo(toStatus);
        inventoryClient.changeInventoryByStatus(updateHoldingRequest);
        orderEntryModel.setHoldingStock(quantity);
    }

    private List<UpdateProductInventoryDetailData> populateUpdateStockAndPreOrderOf(OrderEntryModel orderEntryModel, long updateQty) {
        Set<SubOrderEntryModel> subOrderEntries = orderEntryModel.getSubOrderEntries();
        if (CollectionUtils.isNotEmpty(subOrderEntries)) {
            List<UpdateProductInventoryDetailData> inventoryDetailData = new ArrayList<>();
            for (SubOrderEntryModel subEntry : subOrderEntries) {
                UpdateProductInventoryDetailData data = new UpdateProductInventoryDetailData();
                data.setProductId(subEntry.getProductId());
                long quantity = updateQty * (subEntry.getQuantity() / orderEntryModel.getQuantity());
                data.setValue(Math.abs(quantity));
                inventoryDetailData.add(data);
            }

            return inventoryDetailData;
        }

        UpdateProductInventoryDetailData detail = new UpdateProductInventoryDetailData();
        detail.setProductId(orderEntryModel.getProductId());
        detail.setValue(Math.abs(updateQty));
        return Arrays.asList(detail);
    }

    @Override
    public void updatePreOrderOf(OrderModel order, OrderEntryModel orderEntry) {
        if(!orderEntry.isPreOrder()) {
            return;
        }
        long actualPreOrder = orderEntry.getHoldingStock() == null ? 0 : orderEntry.getHoldingStock();
        long quantity = orderEntry.getQuantity() == null ? 0 : orderEntry.getQuantity();
        long updatePreOrderQty = quantity - actualPreOrder;
        if(updatePreOrderQty == 0) return;

        UpdateInventoryStatusRequest updatePreOrderRequest = createUpdateInventoryStatusRequest(order);
        updatePreOrderRequest.setStatusCode(InventoryStatus.PRE_ORDER.code());
        List<UpdateProductInventoryDetailData> detailDataList = populateUpdateStockAndPreOrderOf(orderEntry, updatePreOrderQty);
        updatePreOrderRequest.setDetailDataList(detailDataList);

        if(updatePreOrderQty > 0) {
            inventoryClient.addStockWithInventoryStatus(InventoryStatus.PRE_ORDER.code(), updatePreOrderRequest);
        } else {
            inventoryClient.subtractStockWithInventoryStatus(InventoryStatus.PRE_ORDER.code(), updatePreOrderRequest);
        }
        orderEntry.setHoldingStock(quantity);
    }

    @Override
    public ProductStockData getAvailableStock(Long productId, Long companyId, Long warehouseId) {
        return inventoryClient.getAvailableStock(productId, companyId, warehouseId);
    }

    @Override
    public ProductStockData getBrokenStock(Long productId, Long companyId, Long warehouseId) {
        return inventoryClient.getBrokenStock(productId, companyId, warehouseId);
    }

    @Override
    public void updateStockHoldingProductOfList(OrderModel order, List<UpdateProductInventoryDetailData> inventoryDetailList, boolean holding) {
        UpdateProductInventoryRequest updateHoldingRequest = createUpdateProductInventoryRequest(order);
        String fromStatus = holding ? InventoryStatus.AVAILABLE.code() : InventoryStatus.HOLDING.code();
        String toStatus = holding ? InventoryStatus.HOLDING.code() : InventoryStatus.AVAILABLE.code();
        updateHoldingRequest.setFrom(fromStatus);
        updateHoldingRequest.setTo(toStatus);
        updateHoldingRequest.setDetailDataList(inventoryDetailList);
        inventoryClient.changeInventoryByStatus(updateHoldingRequest);
    }

    @Override
    public void updatePreOrderProductOfList(OrderModel order, List<UpdateProductInventoryDetailData> inventoryDetailList, boolean isPreOrder) {
        UpdateInventoryStatusRequest updatePreOrderRequest = new UpdateInventoryStatusRequest();
        updatePreOrderRequest.setCompanyId(order.getCompanyId());
        updatePreOrderRequest.setWarehouseId(order.getWarehouseId());
        updatePreOrderRequest.setStatusCode(InventoryStatus.PRE_ORDER.code());
        updatePreOrderRequest.setDetailDataList(inventoryDetailList);

        if (isPreOrder) {
            inventoryClient.addStockWithInventoryStatus(InventoryStatus.PRE_ORDER.code(), updatePreOrderRequest);
        } else {
            inventoryClient.subtractStockWithInventoryStatus(InventoryStatus.PRE_ORDER.code(), updatePreOrderRequest);
        }
    }

    private void validateOutOfStock(Long productId, Long companyId, Long warehouseId, long quantity) {
        ProductStockData availableStockData = inventoryClient.getAvailableStock(productId,
                companyId, warehouseId);
        if(availableStockData.getQuantity() == null || quantity > availableStockData.getQuantity()) {
            ErrorCodes err = ErrorCodes.PRODUCT_OUT_OF_STOCK;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Override
    public void validateOutOfStock(CheckOutOfStockParam param) {
        AbstractOrderModel abstractOrderModel = param.getAbstractOrderModel();
        if(abstractOrderModel == null || !SellSignal.ECOMMERCE_WEB.toString().equalsIgnoreCase(abstractOrderModel.getSellSignal())) {
            validateOutOfStock(param.getProductId(), param.getCompanyId(), param.getWarehouseId(), param.getQuantity());
            return;
        }

        Map<Long, Integer> storeFrontStockOfProduct = inventoryClient.getStoreFrontStockOfProduct(param.getCompanyId(), Arrays.asList(param.getProductId()));
        Integer availableStock = storeFrontStockOfProduct.get(param.getProductId());
        if(availableStock == null || availableStock <= 0) {
            ErrorCodes err = ErrorCodes.PRODUCT_OUT_OF_STOCK;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{param.getProductId()});
        }

        LOGGER.debug("validateOutOfStock: paramQty: {}, availableQty: {}", param.getQuantity(), availableStock);
        if(param.getQuantity() > availableStock) {
            param.setQuantity(availableStock);
        }

    }

    @Override
    public ProductStockData getStoreFrontStockOfProduct(Long productId, Long companyId) {
        ProductStockData data = new ProductStockData();
        data.setProductId(productId);
        data.setCompanyId(companyId);
        Map<Long, Integer> stockMap = inventoryClient.getStoreFrontStockOfProduct(companyId, Arrays.asList(productId));
        data.setQuantity(stockMap.get(productId));
        return data;
    }

    @Override
    public Map<Long, Integer> getStoreFrontAvailableStockOfProductList(Long companyId, List<Long> productIds) {
        if(CollectionUtils.isEmpty(productIds)) {
            return new HashMap<>();
        }
        return inventoryClient.getStoreFrontStockOfProduct(companyId, productIds);
    }

    private void updateHoldingStock(OrderModel order, OrderEntryModel entryModel, HoldingData holdingData) {
        UpdateProductInventoryRequest updateHoldingRequest = createUpdateProductInventoryRequest(order);
        String fromStatus = entryModel.isHolding()? InventoryStatus.HOLDING.code() : InventoryStatus.AVAILABLE.code();

        String toStatus = entryModel.isHolding() ? InventoryStatus.AVAILABLE.code() : InventoryStatus.HOLDING.code();
        Long quantity = entryModel.isHolding() ? entryModel.getHoldingStock() : holdingData.getQuantity();
        updateHoldingRequest.setFrom(fromStatus);
        updateHoldingRequest.setTo(toStatus);

        List<UpdateProductInventoryDetailData> holdingList = populateProductInventoryDetailList(entryModel, quantity);
        updateHoldingRequest.setDetailDataList(holdingList);
        inventoryClient.changeInventoryByStatus(updateHoldingRequest);
        if(!holdingData.isHolding()) {
            entryModel.setHolding(false);
            entryModel.setHoldingStock(0l);
        } else {
            entryModel.setHolding(true);
            entryModel.setHoldingStock(holdingData.getQuantity());
        }
    }

    private void updatePreOrder(OrderModel order, OrderEntryModel entryModel, HoldingData holdingData) {
        UpdateInventoryStatusRequest updatePreOrderRequest = createUpdateInventoryStatusRequest(order);
        updatePreOrderRequest.setStatusCode(InventoryStatus.PRE_ORDER.code());
        Long quantity = entryModel.isPreOrder() ? entryModel.getHoldingStock() : holdingData.getQuantity();

        List<UpdateProductInventoryDetailData> preOrderList = populateProductInventoryDetailList(entryModel, quantity);
        updatePreOrderRequest.setDetailDataList(preOrderList);
        if(entryModel.isPreOrder()) {
            entryModel.setPreOrder(false);
            entryModel.setHoldingStock(0l);
            inventoryClient.subtractStockWithInventoryStatus(InventoryStatus.PRE_ORDER.code(), updatePreOrderRequest);
        } else {
            entryModel.setPreOrder(true);
            entryModel.setHoldingStock(holdingData.getQuantity());
            inventoryClient.addStockWithInventoryStatus(InventoryStatus.PRE_ORDER.code(), updatePreOrderRequest);
        }
    }

    private List<UpdateProductInventoryDetailData> populateUpdateProductInventoryDetails(List<AbstractOrderEntryModel> entries, boolean isHoldingToAvailable, boolean isHoldCombo) {
        List<UpdateProductInventoryDetailData> detailDataList = new ArrayList<>();
        UpdateProductInventoryDetailData detailData;
        for (AbstractOrderEntryModel entry : entries) {
            detailData = new UpdateProductInventoryDetailData();
            Set<SubOrderEntryModel> subOrderEntries = entry.getSubOrderEntries();
            if(CollectionUtils.isNotEmpty(subOrderEntries)) {
                if (!isHoldCombo && entry.isHolding()) {
                    continue;
                }

                List<UpdateProductInventoryDetailData> comboEntries = populateUpdateComboEntries(subOrderEntries);
                detailDataList.addAll(comboEntries);
                continue;
            }
            Set<ToppingOptionModel> toppingOptionModels = entry.getToppingOptionModels();
            if(CollectionUtils.isNotEmpty(toppingOptionModels)) {
                List<UpdateProductInventoryDetailData> toppingEntries = populateUpdateToppingEntries(toppingOptionModels);
                detailDataList.addAll(toppingEntries);
            }
            detailData.setProductId(entry.getProductId());
            Long quantity = CommonUtils.readValue(entry.getQuantity());
            if(entry.isHolding() && entry.getHoldingStock() <= quantity && !isHoldingToAvailable) {
                quantity -= entry.getHoldingStock();
            }

            if(quantity == 0) {
                continue;
            }
            detailData.setValue(quantity);
            detailDataList.add(detailData);
        }
        return detailDataList;
    }

    private List<UpdateProductInventoryDetailData> populateUpdateToppingEntries(Set<ToppingOptionModel> toppingOptionModels) {
        List<UpdateProductInventoryDetailData> toppingEntries = new ArrayList<>();
        UpdateProductInventoryDetailData detailData;
        for (ToppingOptionModel toppingOptionModel : toppingOptionModels) {
            int optionQty = toppingOptionModel.getQuantity();
            Set<ToppingItemModel> toppingItemModels = toppingOptionModel.getToppingItemModels();
            if (CollectionUtils.isNotEmpty(toppingItemModels)) {
                for (ToppingItemModel itemModel : toppingItemModels) {
                    detailData = new UpdateProductInventoryDetailData();
                    detailData.setProductId(itemModel.getProductId());
                    detailData.setValue((long) (itemModel.getQuantity() * optionQty));
                    toppingEntries.add(detailData);
                }
            }
        }
        return toppingEntries;
    }

    private List<UpdateProductInventoryDetailData> populateUpdateComboEntries(Set<SubOrderEntryModel> subOrderEntries) {
        List<UpdateProductInventoryDetailData> comboEntries = new ArrayList<>();
        UpdateProductInventoryDetailData detailData;
        for(SubOrderEntryModel comboEntry : subOrderEntries) {
            detailData = new UpdateProductInventoryDetailData();
            detailData.setProductId(comboEntry.getProductId());
            detailData.setValue(Long.valueOf(comboEntry.getQuantity()));
            comboEntries.add(detailData);
        }
        return comboEntries;
    }

    @Autowired
    public void setInventoryClient(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }
}
