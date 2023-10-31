package com.vctek.orderservice.service;

import com.vctek.dto.CheckCreateTransferWarehouseData;
import com.vctek.dto.request.CheckCreateTransferWarehouseRequest;
import com.vctek.orderservice.dto.DistributorData;
import com.vctek.orderservice.dto.WarehouseData;
import com.vctek.orderservice.dto.request.storefront.ShippingFeeData;
import com.vctek.orderservice.dto.request.storefront.ShippingFeeRequest;
import com.vctek.orderservice.feignclient.dto.DistributorSetingPriceData;
import com.vctek.orderservice.model.OrderModel;

import java.util.List;
import java.util.Map;

public interface LogisticService {
    WarehouseData findByIdAndCompanyId(Long warehouseId, Long companyId);

    Map<Long, DistributorSetingPriceData> getProductPriceSetting(Long distributorId, Long companyId, List<Long> productIds);

    Double calculateDistributorSettingPrice(DistributorSetingPriceData setingPriceData, Double basePrice);

    DistributorData getDetailDistributor(Long distributorId, Long companyId);

    Map<String, CheckCreateTransferWarehouseData> checkValidCreateTransferWarehouse(CheckCreateTransferWarehouseRequest requestValid);

    void validateTransferLessZero(OrderModel orderModel, Long warehouseId);

    List<ShippingFeeData> getStoreFrontShippingFee(ShippingFeeRequest request);
}
