package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.CustomerGroupData;
import com.vctek.orderservice.dto.request.AddressRequest;
import com.vctek.orderservice.dto.request.CustomerRequest;
import com.vctek.orderservice.feignclient.CustomerClient;
import com.vctek.orderservice.feignclient.dto.CustomerData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.service.CustomerService;
import com.vctek.orderservice.service.ModelService;
import com.vctek.service.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerServiceImpl.class);
    private CustomerClient customerClient;
    private ModelService modelService;
    private UserService userService;

    public CustomerServiceImpl(CustomerClient customerClient) {
        this.customerClient = customerClient;
    }

    @Override
    @Transactional
    public CustomerData update(OrderModel order, CustomerRequest customerRequest) {
        if (customerRequest == null || StringUtils.isBlank(customerRequest.getName())) {
            LOGGER.warn("Customer is empty name or null");
            return null;
        }

        try {
            customerRequest.setFirstPurchaseDate(order.getCreatedTime());
            customerRequest.setLatestPurchaseDate(order.getCreatedTime());
            CustomerData data = customerClient.createNew(customerRequest);
            if (data != null && data.getId() != null) {
                order.setCustomerId(data.getId());
                order.setCustomerPhone(data.getPhone());
                populateOrderShippingAddress(data, customerRequest, order);
                modelService.save(order);
            }

            return data;
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        }

        if(customerRequest.getId() != null) {
            order.setCustomerId(customerRequest.getId());
        }

        modelService.save(order);
        return null;
    }

    @Override
    public CustomerData getCustomerById(Long customerId, Long companyId) {
        try {
            List<CustomerData> customerData = customerClient.searchCustomerByIds(companyId, String.valueOf(customerId));
            if(CollectionUtils.isNotEmpty(customerData)) {
                return customerData.get(0);
            }

            return null;
        } catch (RuntimeException e) {
            LOGGER.error("ERROR POPULATE CUSTOMER FOR ORDER: customerId: {}, companyId: {}", customerId, companyId);
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    private void populateOrderShippingAddress(CustomerData data, CustomerRequest customerRequest, OrderModel order) {
        AddressRequest shippingAddress = customerRequest.getShippingAddress();
        if(shippingAddress == null) {
            return;
        }
        order.setShippingAddressId(shippingAddress.getId());
        if(order.getShippingAddressId() == null) {
            order.setShippingAddressId(data.getNewAddressId());
        }
        order.setShippingCustomerName(shippingAddress.getCustomerName());
        order.setShippingCustomerPhone(shippingAddress.getPhone1());
        order.setShippingProvinceId(shippingAddress.getProvinceId());
        order.setShippingDistrictId(shippingAddress.getDistrictId());
        order.setShippingWardId(shippingAddress.getWardId());
        order.setShippingAddressDetail(shippingAddress.getAddressDetail());
    }

    @Override
    @Cacheable(unless = "#result == null", value = "customer_basic", key = "#customerId", cacheManager = "microServiceCacheManager")
    public CustomerData getBasicCustomerInfo(Long customerId, Long companyId) {
        return customerClient.getBasicCustomerInfo(customerId, companyId);
    }

    @Override
    public boolean limitedApplyPromotionAndReward(Long customerId, Long companyId){
        if (customerId == null || companyId == null) {
            return false;
        }
        CustomerData customerData = getBasicCustomerInfo(customerId, companyId);
        if (customerData == null) {
            return false;
        }
        return customerData.isLimitedApplyPromotionAndReward();
    }

    @Override
    @Cacheable(unless = "#result == null", value = "customer_groups", key = "#customerId", cacheManager = "microServiceCacheManager")
    public List<CustomerGroupData> getCustomerGroups(Long customerId) {
        return customerClient.getCustomerGroups(customerId);
    }

    @Autowired
    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
