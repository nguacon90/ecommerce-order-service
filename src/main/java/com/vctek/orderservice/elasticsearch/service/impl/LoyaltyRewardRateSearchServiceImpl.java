package com.vctek.orderservice.elasticsearch.service.impl;

import com.vctek.orderservice.elasticsearch.model.LoyaltyRewardRateSearchModel;
import com.vctek.orderservice.elasticsearch.repository.LoyaltyRewardRateSearchRepository;
import com.vctek.orderservice.elasticsearch.service.LoyaltyRewardRateSearchService;
import com.vctek.orderservice.util.ElasticSearchIndex;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;


@Service
public class LoyaltyRewardRateSearchServiceImpl extends BulkIndexElasticServiceImpl implements LoyaltyRewardRateSearchService {
    private LoyaltyRewardRateSearchRepository loyaltyRewardRateSearchRepository;

    public LoyaltyRewardRateSearchServiceImpl(LoyaltyRewardRateSearchRepository loyaltyRewardRateSearchRepository,
                                              ElasticsearchTemplate elasticsearchTemplate) {
        super(elasticsearchTemplate);
        this.loyaltyRewardRateSearchRepository = loyaltyRewardRateSearchRepository;
    }

    @Override
    public void save(LoyaltyRewardRateSearchModel model) {
        loyaltyRewardRateSearchRepository.save(model);
    }

    @Override
    public Page<LoyaltyRewardRateSearchModel> search(SearchQuery query) {
        return loyaltyRewardRateSearchRepository.search(query);
    }

    @Override
    public void deleteById(Long id) {
        loyaltyRewardRateSearchRepository.deleteById(id);
    }

    @Override
    protected Class getClassIndex() {
        return LoyaltyRewardRateSearchModel.class;
    }

    @Override
    public String getIndexName() {
        return ElasticSearchIndex.LOYALTY_REWARD_RATE_INDEX;
    }
}
