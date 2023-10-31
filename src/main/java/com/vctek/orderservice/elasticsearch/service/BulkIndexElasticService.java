package com.vctek.orderservice.elasticsearch.service;

import com.vctek.orderservice.elasticsearch.model.ElasticItemModel;

import java.util.List;

public interface BulkIndexElasticService {
    <T extends ElasticItemModel> void bulkIndex(List<T> models);

    boolean existedIndex();
}
