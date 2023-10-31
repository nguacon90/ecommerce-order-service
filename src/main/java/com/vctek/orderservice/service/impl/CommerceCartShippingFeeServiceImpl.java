package com.vctek.orderservice.service.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.request.AddressRequest;
import com.vctek.orderservice.dto.request.storefront.ProductInfoRequest;
import com.vctek.orderservice.dto.request.storefront.ShippingFeeData;
import com.vctek.orderservice.dto.request.storefront.ShippingFeeRequest;
import com.vctek.orderservice.dto.request.storefront.StoreFrontCheckoutRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.SubOrderEntryModel;
import com.vctek.orderservice.service.CommerceCartShippingFeeService;
import com.vctek.orderservice.service.LogisticService;
import com.vctek.util.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CommerceCartShippingFeeServiceImpl implements CommerceCartShippingFeeService {
    private LogisticService logisticService;

    @Override
    public ShippingFeeData getValidateShippingFee(AbstractOrderModel model, StoreFrontCheckoutRequest request) {
        AddressRequest addressRequest = request.getCustomer().getShippingAddress();
        ShippingFeeRequest shippingFeeRequest = new ShippingFeeRequest();
        shippingFeeRequest.setCompanyId(model.getCompanyId());
        shippingFeeRequest.setOrderAmount(model.getFinalPrice());
        shippingFeeRequest.setProvinceId(addressRequest.getProvinceId());
        shippingFeeRequest.setDistrictId(addressRequest.getDistrictId());
        List<ProductInfoRequest> products = new ArrayList<>();
        for (AbstractOrderEntryModel entry : model.getEntries()) {
            if (CollectionUtils.isEmpty(entry.getSubOrderEntries())) {
                populateShippingFeeByProductId(entry.getProductId(), entry.getQuantity().intValue(), products);
                continue;
            }
            for (SubOrderEntryModel subOrderEntry : entry.getSubOrderEntries()) {
                populateShippingFeeByProductId(subOrderEntry.getProductId(), subOrderEntry.getQuantity(), products);
            }
        }
        shippingFeeRequest.setProducts(products);
        List<ShippingFeeData> shippingFeeDataList = logisticService.getStoreFrontShippingFee(shippingFeeRequest);
        if (request.getShippingFeeSettingId() == null && CollectionUtils.isEmpty(shippingFeeDataList)) {
            return null;
        }
        if ((request.getShippingFeeSettingId() != null && CollectionUtils.isEmpty(shippingFeeDataList))) {
            ErrorCodes err = ErrorCodes.DELIVERY_COST_DIFFERENT_SETTING_SHIPPING_FEE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        if ((request.getShippingFeeSettingId() == null && CollectionUtils.isNotEmpty(shippingFeeDataList))) {
            ErrorCodes err = ErrorCodes.DELIVERY_COST_DIFFERENT_SETTING_SHIPPING_FEE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        Optional<ShippingFeeData> optional = shippingFeeDataList.stream().filter(i -> i.getShippingFeeSettingId().equals(request.getShippingFeeSettingId())).findFirst();
        if (!optional.isPresent()) {
            ErrorCodes err = ErrorCodes.DELIVERY_COST_DIFFERENT_SETTING_SHIPPING_FEE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        ShippingFeeData shippingFeeData = optional.get();
        if (shippingFeeData.getShippingFee() != CommonUtils.readValue(request.getDeliveryCost())) {
            ErrorCodes err = ErrorCodes.DELIVERY_COST_DIFFERENT_SETTING_SHIPPING_FEE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        return shippingFeeData;
    }

    private void populateShippingFeeByProductId(Long productId, int quantity, List<ProductInfoRequest> products) {
        Optional<ProductInfoRequest> optional = products.stream().filter(i -> i.getProductId().equals(productId)).findFirst();
        if (optional.isPresent()) {
            ProductInfoRequest productQty = optional.get();
            productQty.setQuantity(productQty.getQuantity() + quantity);
            return;
        }
        ProductInfoRequest productQty = new ProductInfoRequest();
        productQty.setProductId(productId);
        productQty.setQuantity(quantity);
        products.add(productQty);
    }

    @Autowired
    public void setLogisticService(LogisticService logisticService) {
        this.logisticService = logisticService;
    }
}
