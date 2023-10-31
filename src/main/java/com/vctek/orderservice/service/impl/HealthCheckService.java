package com.vctek.orderservice.service.impl;

import com.vctek.dto.health.ServiceName;
import com.vctek.health.AbstractHealthCheckService;
import com.vctek.orderservice.feignclient.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class HealthCheckService extends AbstractHealthCheckService {
    private CustomerClient customerClient;
    private LogisticClient logisticClient;
    private LoyaltyClient loyaltyClient;
    private ProductClient productClient;
    private UserClient userClient;
    private FinanceClient financeClient;

    @PostConstruct
    public void init() {
        addVersionClient(ServiceName.CRM, customerClient);
        addVersionClient(ServiceName.LOGISTIC, logisticClient);
        addVersionClient(ServiceName.LOYALTY, loyaltyClient);
        addVersionClient(ServiceName.PRODUCT, productClient);
        addVersionClient(ServiceName.AUTH, userClient);
        addVersionClient(ServiceName.FINANCE, financeClient);
    }

    @Autowired
    public void setCustomerClient(CustomerClient customerClient) {
        this.customerClient = customerClient;
    }

    @Autowired
    public void setLogisticClient(LogisticClient logisticClient) {
        this.logisticClient = logisticClient;
    }

    @Autowired
    public void setLoyaltyClient(LoyaltyClient loyaltyClient) {
        this.loyaltyClient = loyaltyClient;
    }

    @Autowired
    public void setProductClient(ProductClient productClient) {
        this.productClient = productClient;
    }

    @Autowired
    public void setUserClient(UserClient userClient) {
        this.userClient = userClient;
    }

    @Autowired
    public void setFinanceClient(FinanceClient financeClient) {
        this.financeClient = financeClient;
    }
}
