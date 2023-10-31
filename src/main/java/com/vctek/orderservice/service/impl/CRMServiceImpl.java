package com.vctek.orderservice.service.impl;

import com.vctek.dto.PrintSettingData;
import com.vctek.dto.redis.AddressData;
import com.vctek.dto.redis.DistrictData;
import com.vctek.dto.redis.ProvinceData;
import com.vctek.dto.redis.WardData;
import com.vctek.orderservice.feignclient.AddressClient;
import com.vctek.orderservice.feignclient.dto.CustomerData;
import com.vctek.orderservice.service.CRMService;
import com.vctek.orderservice.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CRMServiceImpl implements CRMService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CRMServiceImpl.class);
    private CustomerService customerService;
    private AddressClient addressClient;

    @Autowired
    public CRMServiceImpl(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    @Cacheable(unless = "#result == null", value = "customer", key = "#customerId", cacheManager = "microServiceCacheManager")
    public CustomerData getCustomer(Long customerId, Long companyId) {
        return customerService.getCustomerById(customerId, companyId);
    }

    @Override
    @Cacheable(unless = "#result == null", value = "address", key = "#shippingAddressId", cacheManager = "microServiceCacheManager")
    public AddressData getAddress(Long shippingAddressId) {
        try {
            return addressClient.getAddress(shippingAddressId);
        } catch (RuntimeException e) {
            LOGGER.error("GET ADDRESS ERROR: addressId: {}", shippingAddressId);
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public PrintSettingData getPrintSettingById(Long printSettingId, Long companyId) {
        return addressClient.getPrintSettingById(printSettingId, companyId);
    }

    @Override
    @Cacheable(unless = "#result == null", value = "customer_basic", key = "#customerId", cacheManager = "microServiceCacheManager")
    public CustomerData getBasicCustomerInfo(Long customerId, Long companyId) {
        return customerService.getBasicCustomerInfo(customerId, companyId);
    }

    @Override
    @Cacheable(unless = "#result == null", value = "province", key = "#id", cacheManager = "microServiceCacheManager")
    public ProvinceData getProvinceById(Long id) {
        return addressClient.getProvinceById(id);
    }

    @Override
    @Cacheable(unless = "#result == null", value = "district", key = "#id", cacheManager = "microServiceCacheManager")
    public DistrictData getDistrictById(Long id) {
        return addressClient.getDistrictById(id);
    }

    @Override
    @Cacheable(unless = "#result == null", value = "ward", key = "#id", cacheManager = "microServiceCacheManager")
    public WardData getWardById(Long id) {
        return addressClient.getWardById(id);
    }

    @Autowired
    public void setAddressClient(AddressClient addressClient) {
        this.addressClient = addressClient;
    }
}
