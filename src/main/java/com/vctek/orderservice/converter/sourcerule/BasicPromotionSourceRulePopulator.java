package com.vctek.orderservice.converter.sourcerule;

import com.vctek.converter.Populator;
import com.vctek.dto.promotion.PromotionSourceRuleDTO;
import com.vctek.orderservice.promotionengine.promotionservice.model.CampaignModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.util.CommonUtils;
import com.vctek.orderservice.service.CampaignService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Component
public class BasicPromotionSourceRulePopulator extends AbstractPromotionSourceRulePopulator implements Populator<PromotionSourceRuleModel, PromotionSourceRuleDTO> {
    private CampaignService campaignService;
    @Override
    public void populate(PromotionSourceRuleModel source, PromotionSourceRuleDTO target) {
        target.setId(source.getId());
        target.setCode(source.getCode());
        target.setCompanyId(source.getCompanyId());
        target.setMessageFired(source.getMessageFired());
        target.setStartDate(source.getStartDate());
        target.setEndDate(source.getEndDate());
        target.setActive(source.isActive());
        target.setPublishedStatus(source.getStatus());
        target.setPriority(source.getPriority());
        target.setAllowReward(source.isAllowReward());
        target.setAppliedOnlyOne(source.isAppliedOnlyOne());
        target.setDescription(source.getDescription());
        populateWarehouses(source, target);
        populateOrderTypes(source, target);
        populatePriceTypes(source, target);
        populateExcludeOrderSources(source, target);
        populateCampaign(source, target);
    }

    private void populateCampaign(PromotionSourceRuleModel source, PromotionSourceRuleDTO target) {
        Set<CampaignModel> campaigns = campaignService.findAllByPromotionSourceRule(source);
        if(CollectionUtils.isNotEmpty(campaigns) && campaigns.iterator().hasNext()) {
            CampaignModel campaignModel = campaigns.iterator().next();
            target.setCampaignId(campaignModel.getId());
            target.setCampaignName(campaignModel.getName());
        }
    }

    @Autowired
    public void setCampaignService(CampaignService campaignService) {
        this.campaignService = campaignService;
    }
}
