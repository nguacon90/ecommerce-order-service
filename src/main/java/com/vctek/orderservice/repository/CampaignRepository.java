package com.vctek.orderservice.repository;

import com.vctek.orderservice.promotionengine.promotionservice.model.CampaignModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface CampaignRepository extends JpaRepository<CampaignModel, Long> {
    CampaignModel findByIdAndCompanyId(Long campaignId, Long companyId);

    List<CampaignModel> findAllByCompanyId(Long companyId);

    List<CampaignModel> findAllByCompanyIdAndStatus(Long companyId, String status);

    Set<CampaignModel> findAllByPromotionSourceRules(PromotionSourceRuleModel sourceRuleModel);
}
