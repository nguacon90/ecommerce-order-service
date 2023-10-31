package com.vctek.orderservice.service;

import com.vctek.kafka.data.ReturnOrderBillDTO;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.orderservice.dto.ReturnOrderCommerceParameter;
import com.vctek.orderservice.dto.ReturnRewardRedeemData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.specification.ReturnOrderSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

public interface ReturnOrderService {
    ReturnOrderModel create(ReturnOrderCommerceParameter parameter);

    ReturnOrderModel findByIdAndCompanyId(Long returnOrderId, Long companyId);

    Page<ReturnOrderModel> findAllByCompanyId(Long companyId, Pageable pageable);

    ReturnOrderModel save(ReturnOrderModel returnOrderModel);

    List<ReturnOrderModel> findAllByOriginOrder(OrderModel source);

    OrderModel getOriginOrderOf(ReturnOrderModel returnOrderModel);

    Page<ReturnOrderModel> search(ReturnOrderSpecification specification, Pageable pageable);

    ReturnRewardRedeemData getReturnRewardRedeem(ReturnOrderCommerceParameter commerceParameter);

    void updateReturnOrder(final KafkaMessage<ReturnOrderBillDTO> returnOrderBillMessage);

    ReturnOrderModel findByExportExternalIdAndCompanyId(Long billId, Long companyId);

    ReturnOrderModel onlySave(ReturnOrderModel returnOrderModel);

    void linkReturnOrderForBill(Long companyId);

    List<ReturnOrderModel> findAllByCompanyIdAndCreatedTimeGreaterThanEqual(Long companyId, Date fromDate);

    List<ReturnOrderModel> findAllByCompanyId(Long companyId);

    double sumVatReturnOrderForOriginOrder(OrderModel originOrder);
}
