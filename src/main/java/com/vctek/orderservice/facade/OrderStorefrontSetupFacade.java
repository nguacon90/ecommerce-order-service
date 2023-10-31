package com.vctek.orderservice.facade;

import com.vctek.dto.redis.OrderStorefrontSetupData;

public interface OrderStorefrontSetupFacade {
    OrderStorefrontSetupData createOrUpdate(OrderStorefrontSetupData request);

    OrderStorefrontSetupData findByCompanyId(Long companyId);
}
