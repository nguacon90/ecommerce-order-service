package com.vctek.orderservice.facade;

import com.vctek.orderservice.dto.LoyaltyRewardSearchExcelData;
import com.vctek.orderservice.dto.request.LoyaltyRewardRateElasticRequest;
import com.vctek.orderservice.elasticsearch.model.LoyaltyRewardRateSearchModel;
import com.vctek.orderservice.model.ProductLoyaltyRewardRateModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LoyaltyRewardRateSearchFacade {

    void index(ProductLoyaltyRewardRateModel model);

    void fullIndex();

    Page<LoyaltyRewardRateSearchModel> search(LoyaltyRewardRateElasticRequest loyaltyRewardRateElasticRequest, Pageable pageableRequest);

    LoyaltyRewardSearchExcelData exportExcel(Long companyId);
}
