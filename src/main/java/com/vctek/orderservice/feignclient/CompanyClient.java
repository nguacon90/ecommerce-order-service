package com.vctek.orderservice.feignclient;

import com.vctek.orderservice.feignclient.dto.CompanyData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Component
@FeignClient(name = "${vctek.microservices.auth:auth-service}")
public interface CompanyClient {

    @GetMapping("/companies/{companyId}/sell-less-zero")
    Boolean checkSellLessZero(@PathVariable("companyId") Long companyId);

    @GetMapping("/companies/{companyId}")
    CompanyData getDetailCompany(@PathVariable("companyId") Long companyId);

    @GetMapping("/companies/{companyId}/is-delivery-date")
    Boolean getCompulsoryDeliveryDate(@PathVariable("companyId") Long companyId);
}
