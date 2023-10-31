package com.vctek.orderservice.facade;

import com.vctek.orderservice.dto.OrderData;
import com.vctek.orderservice.dto.ReturnOrderData;
import com.vctek.orderservice.dto.ReturnOrderVatData;
import com.vctek.orderservice.dto.ReturnRewardRedeemData;
import com.vctek.orderservice.dto.request.ReturnOrderRequest;
import com.vctek.orderservice.dto.request.ReturnOrderSearchRequest;
import com.vctek.orderservice.dto.request.ReturnOrderUpdateParameter;

public interface ReturnOrderFacade {
    ReturnOrderData create(ReturnOrderRequest returnOrderRequest);

    ReturnOrderData getDetail(Long returnOrderId, Long companyId);

    OrderData createOrGetExchangeOrder(ReturnOrderUpdateParameter parameter);

    OrderData doChangeWarehouse(ReturnOrderUpdateParameter parameter);

    ReturnOrderData updateInfo(ReturnOrderRequest returnOrderRequest);

    void updateReport(ReturnOrderSearchRequest request);

    void updateOriginOrderBill(Long companyId);

    ReturnRewardRedeemData getReturnRewardRedeem(ReturnOrderRequest request);

    void createRevenueReturnOrder(ReturnOrderSearchRequest request);

    ReturnOrderVatData getInfoVatOfReturnOrderWithOriginOrderCode(String originOrderCode, Long companyId);

    ReturnOrderData updateRefundPoint(ReturnOrderRequest returnOrderRequest);
}
