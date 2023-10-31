package com.vctek.orderservice.service;

import com.vctek.orderservice.dto.CheckOutOfStockParam;
import com.vctek.orderservice.dto.HoldingData;
import com.vctek.orderservice.dto.request.HoldingProductRequest;
import com.vctek.orderservice.feignclient.dto.ProductStockData;
import com.vctek.orderservice.feignclient.dto.UpdateProductInventoryDetailData;
import com.vctek.orderservice.model.OrderEntryModel;
import com.vctek.orderservice.model.OrderModel;

import java.util.List;
import java.util.Map;

public interface InventoryService {

    void holdingAllQuantityOf(OrderModel order);

    void changeAllHoldingToAvailableOf(OrderModel order);

    void subtractPreOrder(OrderModel order);

    void resetHoldingStockOf(OrderModel order);

    void holdingProducts(HoldingProductRequest request, OrderModel orderModel);

    void updateHoldingProductOf(OrderModel order, OrderEntryModel entryModel, HoldingData holdingData);

    void resetHoldingStockOf(OrderModel order, OrderEntryModel orderEntry);

    void subtractPreOrder(OrderModel abstractOrderModel, OrderEntryModel entryToUpdate);

    void updateHoldingStockOf(OrderModel orderModel, OrderEntryModel orderEntryModel);

    void updatePreOrderOf(OrderModel order, OrderEntryModel orderEntry);

    ProductStockData getAvailableStock(Long productId, Long companyId, Long warehouseId);

    ProductStockData getBrokenStock(Long productId, Long companyId, Long warehouseId);

    void updateStockHoldingProductOfList(OrderModel order, List<UpdateProductInventoryDetailData> inventoryDetailList, boolean holding);

    void updatePreOrderProductOfList(OrderModel order, List<UpdateProductInventoryDetailData> inventoryDetailList, boolean isPreOrder);

    void validateOutOfStock(CheckOutOfStockParam param);

    ProductStockData getStoreFrontStockOfProduct(Long productId, Long companyId);

    Map<Long, Integer> getStoreFrontAvailableStockOfProductList(Long companyId, List<Long> productIds);
}
