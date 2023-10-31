package com.vctek.orderservice.feignclient;

import com.vctek.orderservice.dto.CheckPermissionData;
import com.vctek.orderservice.dto.request.CheckPermissionRequest;
import com.vctek.orderservice.dto.request.UserHasWarehouseRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@FeignClient(name = "${vctek.microservices.auth:auth-service}")
public interface CheckPermissionClient {

    @PostMapping("permissions/checkPermissions")
    CheckPermissionData checkPermission(@RequestBody CheckPermissionRequest request);

    @PostMapping("users/user-has-warehouse")
    boolean userHasWarehouse(@RequestBody UserHasWarehouseRequest request);

    @GetMapping("users/belong-to-company")
    boolean isUserBelongTo(@RequestParam("companyId") Long companyId);

}
