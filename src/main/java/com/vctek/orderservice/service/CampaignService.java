package com.vctek.orderservice.service;

import com.vctek.orderservice.promotionengine.promotionservice.model.CampaignModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;

import java.util.List;
import java.util.Set;

public interface CampaignService {
    CampaignModel findById(Long id);

    CampaignModel save(CampaignModel model);

    List<CampaignModel> findAllByCompanyId(Long companyId);

    List<CampaignModel> findAllByCompanyIdAndStatus(Long companyId, String status);

    Set<CampaignModel> findAllByPromotionSourceRule(PromotionSourceRuleModel source);
}
