package com.vctek.orderservice.elasticsearch.repository;

import com.vctek.orderservice.elasticsearch.model.LoyaltyRewardRateSearchModel;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoyaltyRewardRateSearchRepository extends ElasticsearchRepository<LoyaltyRewardRateSearchModel, Long> {
}
