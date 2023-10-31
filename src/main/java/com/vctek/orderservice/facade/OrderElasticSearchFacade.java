package com.vctek.orderservice.facade;

import com.vctek.kafka.data.CustomerDto;
import com.vctek.kafka.data.ReturnOrdersDTO;
import com.vctek.kafka.producer.ProductInfoKafkaData;
import com.vctek.orderservice.dto.OrderSearchExcelData;
import com.vctek.orderservice.dto.request.OrderPartialIndexRequest;
import com.vctek.orderservice.dto.request.OrderReportRequest;
import com.vctek.orderservice.dto.request.OrderSearchRequest;
import com.vctek.orderservice.elasticsearch.model.OrderSearchModel;
import com.vctek.orderservice.model.OrderModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderElasticSearchFacade {
    void index(OrderModel orderModel);

    void fullIndex();

    void partialIndex(OrderPartialIndexRequest request);

    Page<OrderSearchModel> search(OrderSearchRequest request, Pageable pageableRequest);

    Page<OrderSearchModel> searchForUpdateIndex(OrderSearchRequest request, Pageable pageableRequest);

    void indexReturnOrderIds(ReturnOrdersDTO returnOrdersDTO);

    void updateSkuOrName(ProductInfoKafkaData productInfoKafkaData);

    void createOrderReport(OrderReportRequest orderReportRequest);

    void createOrderHistoryReport(OrderReportRequest orderReportRequest);

    OrderSearchExcelData exportExcelOrder(OrderSearchRequest request, boolean directly);

    void fullIndexOrderEntry();

    void fullIndexOrderPaymentData(Long companyId);

    void requestExportExcelAllProduct(OrderSearchRequest request);

    byte[] downloadExcelOrder(OrderSearchRequest request);

    byte[] exportExcelOrderTypeDistributor(Long companyId, String orderCode, Long printSettingId);

    void updateCustomerName(CustomerDto customerDto);

    void fullIndexOrderEntrySaleOff();

    Page<OrderSearchModel> orderStorefrontSearch(OrderSearchRequest request, Pageable pageableRequest);

    void fullIndexOrderSellSignal(Long companyId);
}
