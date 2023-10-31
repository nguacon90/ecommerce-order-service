package com.vctek.orderservice.elasticsearch.service.impl;

import com.vctek.orderservice.elasticsearch.model.ElasticItemModel;
import com.vctek.orderservice.elasticsearch.service.BulkIndexElasticService;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.ResourceAlreadyExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;

import java.util.ArrayList;
import java.util.List;

public abstract class BulkIndexElasticServiceImpl implements BulkIndexElasticService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BulkIndexElasticServiceImpl.class);
    protected ElasticsearchTemplate elasticsearchTemplate;
    protected BulkIndexElasticServiceImpl(ElasticsearchTemplate elasticsearchTemplate) {
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    @Override
    public <T extends ElasticItemModel> void bulkIndex(List<T> models) {
        String indexName = this.getIndexName();
        createIndexIfNotExisted(indexName);

        List<IndexQuery> queries = new ArrayList<>();
        for(T model : models) {
            IndexQuery indexQuery = new IndexQuery();
            indexQuery.setId(model.getId() == null ? null : model.getId().toString());
            indexQuery.setIndexName(indexName);
            indexQuery.setObject(model);
            queries.add(indexQuery);
        }
        if (CollectionUtils.isNotEmpty(queries)) {
            elasticsearchTemplate.bulkIndex(queries);
            elasticsearchTemplate.refresh(indexName);
        }

    }

    protected void createIndexIfNotExisted(String indexName) {
        if (!elasticsearchTemplate.indexExists(indexName)) {
            try {
                elasticsearchTemplate.createIndex(getClassIndex());
                elasticsearchTemplate.putMapping(getClassIndex());
            } catch (ResourceAlreadyExistsException e) {
                LOGGER.info("Resource already existed: {}", e.getMessage());
            }
        }
    }

    protected abstract Class getClassIndex();

    public abstract String getIndexName();

    @Override
    public boolean existedIndex() {
        return elasticsearchTemplate.indexExists(getIndexName());
    }
}
