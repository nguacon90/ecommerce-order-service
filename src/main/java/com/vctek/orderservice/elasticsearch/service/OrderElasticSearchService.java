package com.vctek.orderservice.elasticsearch.service;

import com.vctek.kafka.data.InvoiceKafkaData;
import com.vctek.orderservice.dto.OrderSearchExcelData;
import com.vctek.orderservice.dto.request.OrderSearchRequest;
import com.vctek.orderservice.elasticsearch.model.OrderSearchModel;
import com.vctek.orderservice.model.OrderModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

import java.util.List;
import java.util.Optional;

public interface OrderElasticSearchService extends BulkIndexElasticService {

    void save(OrderSearchModel model);

    Page<OrderSearchModel> search(SearchQuery query);

    void bulkOrderIndex(List<OrderSearchModel> models);

    Optional<OrderSearchModel> findById(String orderCode);

    void saveAll(List<OrderSearchModel> orderSearchModels);

    void updatePaymentTransactionDataAndPaidAmount(OrderModel model, InvoiceKafkaData kafkaData);

    void indexReturnOrderIds(OrderModel orderModel);

    Page<OrderSearchModel> findAll(Pageable pageable);

    OrderSearchExcelData exportExcelOrder(List<OrderSearchModel> models, OrderSearchRequest request);

    void indexOrder(OrderModel orderModel);

    OrderSearchModel findByIdAndCompanyId(String orderCode, Long companyId);

    void bulkIndexOrderEntries(List<OrderSearchModel> orderSearchModels);

    void bulkIndexCustomerName(List<OrderSearchModel> orderSearchModels);

    void indexTags(OrderModel orderModel);
}
