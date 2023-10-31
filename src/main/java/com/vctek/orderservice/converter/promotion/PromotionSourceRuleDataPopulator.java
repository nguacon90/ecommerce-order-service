package com.vctek.orderservice.converter.promotion;

import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.dto.promotion.ActionDTO;
import com.vctek.dto.promotion.ConditionDTO;
import com.vctek.dto.promotion.ParameterDTO;
import com.vctek.dto.promotion.PromotionSourceRuleDTO;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.promotionengine.promotionservice.model.CampaignModel;
import com.vctek.orderservice.promotionengine.ruledefinition.enums.MembershipOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.PromotionSourceRuleData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleStatus;
import com.vctek.orderservice.promotionengine.util.CommonUtils;
import com.vctek.orderservice.repository.CampaignRepository;
import com.vctek.orderservice.util.ConditionDefinitionParameter;
import com.vctek.orderservice.util.DateUtil;
import com.vctek.orderservice.util.PromotionDefinitionCode;
import com.vctek.util.OrderType;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PromotionSourceRuleDataPopulator implements Populator<PromotionSourceRuleDTO, PromotionSourceRuleData> {
    private CampaignRepository campaignRepository;
    private Converter<ConditionDTO, RuleConditionData> sourceRuleConditionDataConverter;
    private Converter<ActionDTO, RuleActionData> sourceRuleActionDataConverter;

    @Override
    public void populate(PromotionSourceRuleDTO source, PromotionSourceRuleData target) {
        target.setId(source.getId());
        target.setCompanyId(source.getCompanyId());
        target.setCode(generateCode());
        target.setDescription(source.getDescription());
        target.setStartDate(DateUtil.getDateWithoutTime(source.getStartDate()));
        target.setEndDate(DateUtil.getEndDay(source.getEndDate()));
        target.setMessageFired(source.getMessageFired());
        target.setName(source.getName());
        target.setPriority(source.getPriority());
        target.setStatus(RuleStatus.UNPUBLISHED.toString());
        target.setActive(source.isActive());
        target.setAppliedOnlyOne(source.isAppliedOnlyOne());
        target.setAppliedWarehouseIds(CommonUtils.join(source.getWarehouseIds(), CommonUtils.COMMA));
        List<String> orderTypes = source.getOrderTypes();
        target.setAppliedOrderTypes(CommonUtils.join(orderTypes, CommonUtils.COMMA));
        target.setAppliedPriceTypes(CommonUtils.join(source.getPriceTypes(), CommonUtils.COMMA));
        if(isValidToSetExcludeOrderSources(source)) {
            target.setExcludeOrderSources(CommonUtils.join(source.getExcludeOrderSourceIds(), CommonUtils.COMMA));
        } else {
            target.setExcludeOrderSources(null);
        }
        populateConditions(source, target);
        target.setActions(sourceRuleActionDataConverter.convertAll(source.getActions()));
        target.setCampaigns(populateCampaigns(source.getCampaignId(), source.getCompanyId()));
        target.setAllowReward(source.isAllowReward());
    }

    private boolean isValidToSetExcludeOrderSources(PromotionSourceRuleDTO source) {
        return (CollectionUtils.isEmpty(source.getOrderTypes()) || source.getOrderTypes().contains(OrderType.ONLINE.toString()))
                && CollectionUtils.isNotEmpty(source.getExcludeOrderSourceIds());
    }

    private void populateConditions(PromotionSourceRuleDTO source, PromotionSourceRuleData target) {
        List<ConditionDTO> conditions = source.getConditions();
        if(CollectionUtils.isNotEmpty(source.getWarehouseIds())) {
            conditions.add(buildCondition(PromotionDefinitionCode.WAREHOUSE.code(), source.getWarehouseIds(),
                    MembershipOperator.IN.toString()));
        }

        if(CollectionUtils.isNotEmpty(source.getOrderTypes())) {
            conditions.add(buildCondition(PromotionDefinitionCode.ORDER_TYPES.code(), source.getOrderTypes(),
                    MembershipOperator.IN.toString()));
        }

        if(CollectionUtils.isNotEmpty(source.getPriceTypes())) {
            conditions.add(buildCondition(PromotionDefinitionCode.PRICE_TYPES.code(), source.getPriceTypes(), MembershipOperator.IN.toString()));
        }

        if(CollectionUtils.isNotEmpty(source.getExcludeOrderSourceIds())) {
            conditions.add(buildCondition(PromotionDefinitionCode.EXCLUDE_ORDER_SOURCES.code(), source.getExcludeOrderSourceIds(), MembershipOperator.NOT_IN.toString()));
        }

        target.setConditions(sourceRuleConditionDataConverter.convertAll(conditions));
    }

    private ConditionDTO buildCondition(String definitionId, Object value, String operator) {
        ConditionDTO conditionDTO = new ConditionDTO();
        conditionDTO.setDefinitionId(definitionId);
        Map<String, ParameterDTO> params = new HashMap<>();
        params.put(ConditionDefinitionParameter.VALUE.code(), generateParam(value));
        params.put(ConditionDefinitionParameter.OPERATOR.code(), generateParam(operator));
        conditionDTO.setParameters(params);
        return conditionDTO;
    }

    private ParameterDTO generateParam(Object value) {
        ParameterDTO param = new ParameterDTO();
        param.setValue(value);
        return param;
    }

    private String generateCode() {
        return UUID.randomUUID().toString() + CommonUtils.MINUS + Calendar.getInstance().getTimeInMillis();
    }

    private Set<CampaignModel> populateCampaigns(Long campaignId, Long companyId) {
        Set<CampaignModel> campaignModels = new HashSet<>();
        if(campaignId == null) {
            return campaignModels;
        }

        CampaignModel model = campaignRepository.findByIdAndCompanyId(campaignId, companyId);
        if(model == null) {
            ErrorCodes err = ErrorCodes.INVALID_CAMPAIGN_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        campaignModels.add(model);
        return campaignModels;
    }

    @Autowired
    public void setSourceRuleConditionDataConverter(Converter<ConditionDTO, RuleConditionData> sourceRuleConditionDataConverter) {
        this.sourceRuleConditionDataConverter = sourceRuleConditionDataConverter;
    }

    @Autowired
    public void setSourceRuleActionDataConverter(Converter<ActionDTO, RuleActionData> sourceRuleActionDataConverter) {
        this.sourceRuleActionDataConverter = sourceRuleActionDataConverter;
    }

    @Autowired
    public void setCampaignRepository(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }
}
