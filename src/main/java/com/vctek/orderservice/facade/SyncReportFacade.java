package com.vctek.orderservice.facade;

import com.vctek.orderservice.dto.request.OrderReportRequest;

public interface SyncReportFacade {
    void syncPromotion(Long companyId, String type);

    void updateOriginBasePrice(OrderReportRequest orderReportRequest);
}
