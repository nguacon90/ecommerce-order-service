package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.promotionengine.promotionservice.model.CampaignModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.repository.CampaignRepository;
import com.vctek.orderservice.service.CampaignService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class CampaignServiceImpl implements CampaignService {
    private CampaignRepository campaignRepository;

    public CampaignServiceImpl(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    @Override
    public CampaignModel findById(Long id) {
        Optional<CampaignModel> optionalModel = campaignRepository.findById(id);
        return optionalModel.isPresent() ? optionalModel.get() : null;
    }

    @Override
    public CampaignModel save(CampaignModel model) {
        return campaignRepository.save(model);
    }

    @Override
    public List<CampaignModel> findAllByCompanyId(Long companyId) {
        return campaignRepository.findAllByCompanyId(companyId);
    }

    @Override
    public List<CampaignModel> findAllByCompanyIdAndStatus(Long companyId, String status) {
        return campaignRepository.findAllByCompanyIdAndStatus(companyId, status);
    }

    @Override
    public Set<CampaignModel> findAllByPromotionSourceRule(PromotionSourceRuleModel source) {
        return campaignRepository.findAllByPromotionSourceRules(source);
    }
}
