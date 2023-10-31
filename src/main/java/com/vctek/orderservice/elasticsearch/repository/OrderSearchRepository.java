package com.vctek.orderservice.elasticsearch.repository;

import com.vctek.orderservice.elasticsearch.model.OrderSearchModel;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface OrderSearchRepository extends ElasticsearchRepository<OrderSearchModel, String> {
    OrderSearchModel findByIdAndCompanyId(String orderCode, Long companyId);
}
