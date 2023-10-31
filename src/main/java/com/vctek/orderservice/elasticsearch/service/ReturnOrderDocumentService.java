package com.vctek.orderservice.elasticsearch.service;

import com.vctek.orderservice.elasticsearch.model.returnorder.ExchangeOrder;
import com.vctek.orderservice.elasticsearch.model.returnorder.ReturnOrderDocument;
import com.vctek.orderservice.model.ReturnOrderModel;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

import java.util.List;

public interface ReturnOrderDocumentService extends BulkIndexElasticService {
    ReturnOrderDocument save(ReturnOrderDocument document);

    void deleteAllDocuments();

    ReturnOrderDocument findById(Long id);

    void updateExchangeOrder(Long returnOrderId, ExchangeOrder exchangeOrderDoc);

    Page<ReturnOrderDocument> search(SearchQuery query);

    void updateReturnOrderInfo(ReturnOrderModel returnOrder);

    ReturnOrderDocument findByIdAndCompanyId(Long returnOrderId, Long companyId);

    void saveAll(List<ReturnOrderDocument> returnOrderDocuments);
}
