package com.vctek.orderservice.elasticsearch.index;

import com.vctek.converter.Converter;
import com.vctek.orderservice.elasticsearch.model.LoyaltyRewardRateSearchModel;
import com.vctek.orderservice.elasticsearch.service.LoyaltyRewardRateSearchService;
import com.vctek.orderservice.model.ProductLoyaltyRewardRateModel;
import com.vctek.orderservice.service.ProductLoyaltyRewardRateService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

public class LoyaltyRewardRateElasticIndexRunnable implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoyaltyRewardRateElasticIndexRunnable.class);
    private Authentication authentication;
    private ProductLoyaltyRewardRateService service;
    private int indexOfThread;
    private int pageSize;
    private int numOfThread;
    private Converter<ProductLoyaltyRewardRateModel, LoyaltyRewardRateSearchModel> loyaltyRewardRateSearchModelConverter;
    private LoyaltyRewardRateSearchService elasticSearchService;

    public LoyaltyRewardRateElasticIndexRunnable(Authentication authentication, int indexOfThread, int pageSize, int numOfThread) {
        this.authentication = authentication;
        this.indexOfThread = indexOfThread;
        this.pageSize = pageSize;
        this.numOfThread = numOfThread;
    }

    @Override
    public void run() {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        int index = this.indexOfThread;
        Pageable pageable = PageRequest.of(index, this.pageSize);
        while (true) {
            Page<ProductLoyaltyRewardRateModel> loyaltyRewardModels = service.findAll(pageable);
            List<LoyaltyRewardRateSearchModel> loyaltyRewardDocuments = this.index(loyaltyRewardModels);
            if (CollectionUtils.isEmpty(loyaltyRewardDocuments)) {
                LOGGER.info("Index done!");
                break;
            }

            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("Indexed: success {} items / {} pages/ {} totalItems", loyaltyRewardDocuments.size(),
                        index, loyaltyRewardModels.getTotalElements());
            }
            index += this.numOfThread;
            pageable = PageRequest.of(index, this.pageSize);
        }
    }

    protected List<LoyaltyRewardRateSearchModel> index(Page<ProductLoyaltyRewardRateModel> data) {
        List<ProductLoyaltyRewardRateModel> loyaltyRewardModels = data.getContent();
        if (CollectionUtils.isEmpty(loyaltyRewardModels)) {
            return new ArrayList<>();
        }

        List<LoyaltyRewardRateSearchModel> loyaltyRewardDocuments = new ArrayList<>();
        for (ProductLoyaltyRewardRateModel model : loyaltyRewardModels) {
            try {
                loyaltyRewardDocuments.add(getConvertloyaltyReward(model));
            } catch (RuntimeException e) {
                LOGGER.error("Convert error: loyaltyRewardId: {}, message: {}", model.getId(), e.getMessage(), e);
            }
        }

        elasticSearchService.bulkIndex(loyaltyRewardDocuments);
        return loyaltyRewardDocuments;
    }

    protected LoyaltyRewardRateSearchModel getConvertloyaltyReward(ProductLoyaltyRewardRateModel order) {
        return loyaltyRewardRateSearchModelConverter.convert(order);
    }

    public void setLoyaltyRewardRateSearchModelConverter(Converter<ProductLoyaltyRewardRateModel, LoyaltyRewardRateSearchModel> loyaltyRewardRateSearchModelConverter) {
        this.loyaltyRewardRateSearchModelConverter = loyaltyRewardRateSearchModelConverter;
    }

    public void setElasticSearchService(LoyaltyRewardRateSearchService elasticSearchService) {
        this.elasticSearchService = elasticSearchService;
    }

    public void setService(ProductLoyaltyRewardRateService service) {
        this.service = service;
    }
}
