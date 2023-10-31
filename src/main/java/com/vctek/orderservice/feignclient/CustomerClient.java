package com.vctek.orderservice.feignclient;

import com.vctek.health.VersionClient;
import com.vctek.orderservice.dto.CustomerGroupData;
import com.vctek.orderservice.dto.request.CustomerRequest;
import com.vctek.orderservice.feignclient.dto.CustomerData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Component
@FeignClient(name = "${vctek.microservices.crm:crm-service}")
public interface CustomerClient extends VersionClient {

    @PostMapping("/customers/order")
    CustomerData createNew(@RequestBody CustomerRequest customerRequest);

    @GetMapping("/customers/search-customer")
    List<CustomerData> searchCustomerByIds(@RequestParam("companyId") Long companyId,
                                           @RequestParam("ids") String ids);

    @GetMapping("/customers/{id}/basic-info")
    CustomerData getBasicCustomerInfo(@PathVariable("id") Long id, @RequestParam("companyId") Long companyId);

    @GetMapping("/customers/{id}/groups")
    List<CustomerGroupData> getCustomerGroups(@PathVariable("id") Long id);

}

