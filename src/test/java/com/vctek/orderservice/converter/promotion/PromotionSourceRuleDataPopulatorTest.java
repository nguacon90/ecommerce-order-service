package com.vctek.orderservice.converter.promotion;

import com.vctek.converter.Converter;
import com.vctek.dto.promotion.ActionDTO;
import com.vctek.dto.promotion.ConditionDTO;
import com.vctek.dto.promotion.PromotionSourceRuleDTO;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.promotionengine.promotionservice.model.CampaignModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.PromotionSourceRuleData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleStatus;
import com.vctek.orderservice.repository.CampaignRepository;
import com.vctek.util.OrderType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PromotionSourceRuleDataPopulatorTest {
    private PromotionSourceRuleDataPopulator populator;

    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private Converter<ConditionDTO, RuleConditionData> sourceRuleConditionDataConverter;
    @Mock
    private Converter<ActionDTO, RuleActionData> sourceRuleActionDataConverter;
    @Mock
    private PromotionSourceRuleDTO source;
    private PromotionSourceRuleData target = new PromotionSourceRuleData();
    @Mock
    private CampaignModel campaign;
    private ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        populator = new PromotionSourceRuleDataPopulator();
        populator.setCampaignRepository(campaignRepository);
        populator.setSourceRuleActionDataConverter(sourceRuleActionDataConverter);
        populator.setSourceRuleConditionDataConverter(sourceRuleConditionDataConverter);
        when(source.getCampaignId()).thenReturn(1l);
        when(source.getWarehouseIds()).thenReturn(Arrays.asList(17l, 18l));
        when(source.getOrderTypes()).thenReturn(Arrays.asList(OrderType.RETAIL.toString()));
        when(source.getConditions()).thenReturn(new ArrayList<>());
    }

    @Test
    public void populate_invalidCampaign() {
        try {
            populator.populate(source, target);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_CAMPAIGN_ID.code(), e.getCode());
        }
    }

    @Test
    public void populate() {
        when(campaignRepository.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(campaign);

        populator.populate(source, target);
        assertEquals(RuleStatus.UNPUBLISHED.toString(), target.getStatus());
        verify(sourceRuleConditionDataConverter).convertAll(captor.capture());
        assertEquals(2, captor.getValue().size());
    }
}
