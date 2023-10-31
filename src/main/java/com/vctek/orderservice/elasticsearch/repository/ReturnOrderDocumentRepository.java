package com.vctek.orderservice.elasticsearch.repository;

import com.vctek.orderservice.elasticsearch.model.returnorder.ReturnOrderDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ReturnOrderDocumentRepository extends ElasticsearchRepository<ReturnOrderDocument, Long> {

    ReturnOrderDocument findByIdAndCompanyId(Long returnOrderId, Long companyId);
}
