package com.vctek.orderservice.service;

import com.vctek.dto.PrintSettingData;
import com.vctek.dto.redis.AddressData;
import com.vctek.dto.redis.DistrictData;
import com.vctek.dto.redis.ProvinceData;
import com.vctek.dto.redis.WardData;
import com.vctek.orderservice.dto.CustomerGroupData;
import com.vctek.orderservice.feignclient.dto.CustomerData;

import java.util.List;

public interface CRMService {
    CustomerData getCustomer(Long customerId, Long companyId);

    AddressData getAddress(Long shippingAddressId);

    PrintSettingData getPrintSettingById(Long printSettingId, Long companyId);

    CustomerData getBasicCustomerInfo(Long customerId, Long companyId);

    ProvinceData getProvinceById(Long id);

    DistrictData getDistrictById(Long id);

    WardData getWardById(Long id);
}
