package com.vctek.orderservice.facade;

import com.vctek.kafka.data.InvoiceKafkaData;
import com.vctek.kafka.data.OrderData;
import com.vctek.kafka.producer.ProductInfoKafkaData;
import com.vctek.orderservice.dto.OrderSearchExcelData;
import com.vctek.orderservice.dto.request.ReturnOrderSearchRequest;
import com.vctek.orderservice.elasticsearch.model.returnorder.ReturnOrderDocument;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReturnOrderDocumentFacade {
    void partialIndex(Long companyId);

    void index(ReturnOrderModel returnOrderModel);

    void updateExchangeOrder(OrderModel orderModel, ReturnOrderModel returnOrder);

    Page<ReturnOrderDocument> search(ReturnOrderSearchRequest returnOrderSearchRequest, Pageable pageableRequest);

    void updateReturnOrderInfo(ReturnOrderModel returnOrder);

    void updateWarehouseExchangeOrder();

    void updatePaymentData(InvoiceKafkaData invoiceKafkaData);

    OrderSearchExcelData exportExcelListReturnOrder(ReturnOrderSearchRequest returnOrderSearchRequest);

    void updateSkuOrName(ProductInfoKafkaData productInfoKafkaData);

    void indexOrderSource(OrderData orderData);
}
