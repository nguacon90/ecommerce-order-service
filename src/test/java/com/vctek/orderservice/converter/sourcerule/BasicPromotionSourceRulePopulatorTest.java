package com.vctek.orderservice.converter.sourcerule;

import com.vctek.dto.promotion.PromotionSourceRuleDTO;
import com.vctek.orderservice.promotionengine.promotionservice.model.CampaignModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.service.CampaignService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class BasicPromotionSourceRulePopulatorTest {
    private BasicPromotionSourceRulePopulator populator;
    private PromotionSourceRuleModel source = new PromotionSourceRuleModel();
    private PromotionSourceRuleDTO target = new PromotionSourceRuleDTO();
    private Set<CampaignModel> campaigns = new HashSet<>();
    @Mock
    private CampaignService campaignService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        populator = new BasicPromotionSourceRulePopulator();
        populator.setCampaignService(campaignService);
        when(campaignService.findAllByPromotionSourceRule(source)).thenReturn(campaigns);
    }

    @Test
    public void populate() {
        source.setId(1l);
        campaigns.add(new CampaignModel());
        source.setCampaigns(campaigns);
        populator.populate(source, target);
        assertEquals(1l, target.getId(), 0);
    }
}
