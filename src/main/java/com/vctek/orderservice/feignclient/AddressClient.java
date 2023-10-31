package com.vctek.orderservice.feignclient;

import com.vctek.dto.PrintSettingData;
import com.vctek.dto.redis.AddressData;
import com.vctek.dto.redis.DistrictData;
import com.vctek.dto.redis.ProvinceData;
import com.vctek.dto.redis.WardData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@FeignClient(name = "${vctek.microservices.crm:crm-service}")
public interface AddressClient {

    @GetMapping("/address/{addressId}")
    AddressData getAddress(@PathVariable("addressId") Long addressId);

    @GetMapping("/print-settings/{printSettingId}")
    PrintSettingData getPrintSettingById(@PathVariable("printSettingId") Long printSettingId,
                                         @RequestParam("companyId") Long companyId);

    @GetMapping("/provinces/{id}")
    ProvinceData getProvinceById(@PathVariable("id") Long id);

    @GetMapping("/districts/{id}")
    DistrictData getDistrictById(@PathVariable("id") Long id);

    @GetMapping("/wards/{id}")
    WardData getWardById(@PathVariable("id") Long id);
}
