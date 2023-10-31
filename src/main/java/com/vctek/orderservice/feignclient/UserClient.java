package com.vctek.orderservice.feignclient;

import com.vctek.health.VersionClient;
import com.vctek.orderservice.dto.UserData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Component
@FeignClient(name = "${vctek.microservices.auth:auth-service}")
public interface UserClient extends VersionClient {

    @GetMapping("users/{userId}/basic-info")
    UserData getUserById(@PathVariable("userId") Long userId);

    @GetMapping("users/{userId}/warehouses")
    List<Long> getAllWarehouses(@PathVariable("userId") Long userId, @RequestParam("companyId") Long companyId);

    @GetMapping("users/{userId}/warehouses")
    List<Long> getAllWarehouseByUser(@PathVariable("userId") Long userId, @RequestParam("companyId") Long companyId);

    @PostMapping("users/is-admin-company")
    Boolean isAdminCompanyUser();

}

