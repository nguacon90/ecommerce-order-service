package com.vctek.orderservice.controller;

import com.vctek.dto.health.HealthCheckResponse;
import com.vctek.orderservice.service.impl.HealthCheckService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class IndexController {
    private HealthCheckService healthCheckService;

    @GetMapping("/version")
    public ResponseEntity<String> getVersion() {
        Optional<String> version = Optional.ofNullable(IndexController.class.getPackage().getImplementationVersion());
        return new ResponseEntity<>(version.orElse(StringUtils.EMPTY), HttpStatus.OK);
    }

    @GetMapping("/health")
    public ResponseEntity<HealthCheckResponse> checkHealth() {
        HealthCheckResponse response = healthCheckService.checkHealthServices();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Autowired
    public void setHealthCheckService(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }
}
