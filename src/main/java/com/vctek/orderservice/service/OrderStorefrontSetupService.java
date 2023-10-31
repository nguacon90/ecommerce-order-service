package com.vctek.orderservice.service;

import com.vctek.orderservice.model.OrderStorefrontSetupModel;

public interface OrderStorefrontSetupService {
    OrderStorefrontSetupModel findByCompanyId(Long companyId);

    OrderStorefrontSetupModel save(OrderStorefrontSetupModel model);
}
