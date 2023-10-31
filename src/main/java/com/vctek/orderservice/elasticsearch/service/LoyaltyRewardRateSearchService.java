package com.vctek.orderservice.elasticsearch.service;

import com.vctek.orderservice.elasticsearch.model.LoyaltyRewardRateSearchModel;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.query.SearchQuery;

public interface LoyaltyRewardRateSearchService extends BulkIndexElasticService {

    void save(LoyaltyRewardRateSearchModel model);

    Page<LoyaltyRewardRateSearchModel> search(SearchQuery query);

    void deleteById(Long id);
}
