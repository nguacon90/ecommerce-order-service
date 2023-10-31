package com.vctek.orderservice.service.impl;

import com.vctek.dto.CheckCreateTransferWarehouseData;
import com.vctek.dto.request.CheckCreateTransferWarehouseRequest;
import com.vctek.dto.request.ValidateProductTransferLessZeroData;
import com.vctek.dto.request.ValidateTransferLessZeroData;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.DistributorData;
import com.vctek.orderservice.dto.WarehouseData;
import com.vctek.orderservice.dto.request.storefront.ShippingFeeData;
import com.vctek.orderservice.dto.request.storefront.ShippingFeeRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.LogisticClient;
import com.vctek.orderservice.feignclient.dto.DistributorSetingPriceData;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.service.LogisticService;
import com.vctek.orderservice.util.CurrencyUtils;
import com.vctek.util.OrderStatus;
import com.vctek.util.SettingPriceType;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LogisticServiceImpl implements LogisticService {
    private LogisticClient logisticClient;

    public LogisticServiceImpl(LogisticClient logisticClient) {
        this.logisticClient = logisticClient;
    }

    @Override
    @Cacheable(unless="#result == null", value = "redis_warehouse_data", key = "#warehouseId", cacheManager = "microServiceCacheManager")
    public WarehouseData findByIdAndCompanyId(Long warehouseId, Long companyId) {
        return logisticClient.getBasicWarehouse(warehouseId, companyId);
    }

    @Override
    public Map<Long, DistributorSetingPriceData> getProductPriceSetting(Long distributorId, Long companyId, List<Long> productIds) {
        return logisticClient.getProductPriceSetting(distributorId, companyId, productIds);
    }

    @Override
    public Double calculateDistributorSettingPrice(DistributorSetingPriceData setingPriceData, Double basePrice) {
        if (SettingPriceType.PRICE_NET.type().equals(setingPriceData.getType())) {
            return setingPriceData.getPrice();
        }
        if (SettingPriceType.PRICE_BY_DISCOUNT.type().equals(setingPriceData.getType())) {
            double discount = CurrencyUtils.computeValue(setingPriceData.getDiscount(), setingPriceData.getDiscountType(), basePrice);
            return basePrice - discount;
        }
        return basePrice;
    }

    @Override
    public DistributorData getDetailDistributor(Long distributorId, Long companyId) {
        return logisticClient.getDetailDistributor(distributorId, companyId);
    }

    @Override
    public Map<String, CheckCreateTransferWarehouseData> checkValidCreateTransferWarehouse(CheckCreateTransferWarehouseRequest requestValid) {
        Map<String, CheckCreateTransferWarehouseData> settingMap = logisticClient.checkValidCreateTransferWarehouse(requestValid);
        Map<String, CheckCreateTransferWarehouseData> checkValid = new HashMap<>();
        for (CheckCreateTransferWarehouseRequest request : requestValid.getRequests()) {
            CheckCreateTransferWarehouseData data = settingMap.get(request.getOrderCode());
            if (data == null) {
                checkValid.put(request.getOrderCode(), data);
                continue;
            }
            OrderStatus settingOrderStatus = OrderStatus.findByCode(data.getOrderStatus());
            OrderStatus oldStatus = OrderStatus.findByCode(request.getCurrentOrderStatus());
            OrderStatus newStatus = OrderStatus.findByCode(request.getOrderStatus());
            if (data.isHasCreateTransferWarehouse() && data.getTransferWarehouseId() == null && request.getWarehouseId().equals(data.getReceiptWarehouseId())
                    && oldStatus.value() < settingOrderStatus.value() && newStatus.value() > settingOrderStatus.value()) {
                checkValid.put(request.getOrderCode(), data);
                continue;
            }
            data.setHasCreateTransferWarehouse(false);
            checkValid.put(request.getOrderCode(), data);
        }
        return checkValid;
    }

    @Override
    public void validateTransferLessZero(OrderModel orderModel, Long warehouseId) {
        ValidateTransferLessZeroData request = new ValidateTransferLessZeroData();
        request.setCompanyId(orderModel.getCompanyId());
        request.setWarehouseId(warehouseId);
        List<ValidateProductTransferLessZeroData> dataList = new ArrayList<>();
        for (AbstractOrderEntryModel entry : orderModel.getEntries()) {
            if (CollectionUtils.isNotEmpty(entry.getSubOrderEntries())) {
                for (SubOrderEntryModel subOrderEntry : entry.getSubOrderEntries()) {
                    populateValidateData(dataList, subOrderEntry.getProductId(), subOrderEntry.getQuantity());
                }
                continue;
            }
            populateValidateData(dataList, entry.getProductId(), entry.getQuantity().intValue());
            if (CollectionUtils.isNotEmpty(entry.getToppingOptionModels())) {
                populateToppingItems(dataList, entry);
            }
        }
        request.setProducts(dataList);
        try {
            logisticClient.validateTransferLessZero(request);
        } catch (ServiceException e) {
            ErrorCodes err = ErrorCodes.CHANGE_STATUS_CREATE_TRANSFER_WAREHOUSE_QUANTITY_OVER_AVAILABLE_STOCK;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    private void populateToppingItems(List<ValidateProductTransferLessZeroData> dataList, AbstractOrderEntryModel entry) {
        for (ToppingOptionModel toppingOptionModel : entry.getToppingOptionModels()) {
            if (CollectionUtils.isNotEmpty(toppingOptionModel.getToppingItemModels())) {
                for (ToppingItemModel toppingItemModel : toppingOptionModel.getToppingItemModels()) {
                    populateValidateData(dataList, toppingItemModel.getProductId(), toppingItemModel.getQuantity());
                }
            }
        }
    }

    private void populateValidateData(List<ValidateProductTransferLessZeroData> dataList, Long productId, Integer quantity) {
        Optional<ValidateProductTransferLessZeroData> optional = dataList.stream().filter(i -> i.getProductId().equals(productId)).findFirst();
        ValidateProductTransferLessZeroData data;
        if (!optional.isPresent()) {
            data = new ValidateProductTransferLessZeroData();
            data.setProductId(productId);
            data.setQuantity(0);
            dataList.add(data);
        } else {
            data = optional.get();
        }
        data.setQuantity(data.getQuantity() + quantity);
    }

    @Override
    public List<ShippingFeeData> getStoreFrontShippingFee(ShippingFeeRequest request) {
        return logisticClient.getStoreFrontShippingFee(request, request.getCompanyId());
    }
}
