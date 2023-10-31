package com.vctek.orderservice.converter.sourcerule;

import com.vctek.dto.promotion.ParameterDTO;
import com.vctek.dto.promotion.PromotionSourceRuleDTO;
import com.vctek.orderservice.promotionengine.promotionservice.model.CampaignModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.strategy.RuleParameterValueConverter;
import com.vctek.orderservice.promotionengine.util.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

public class AbstractPromotionSourceRulePopulator {
    private RuleParameterValueConverter ruleParameterValueConverter;

    protected Map<String, ParameterDTO> populateParameters(Map<String, RuleParameterData> parameterData) {
        Map<String, ParameterDTO> parameters = new HashMap<>();
        for(Map.Entry<String, RuleParameterData> entry : parameterData.entrySet()) {
            String key = entry.getKey();
            RuleParameterData ruleParameterData = entry.getValue();
            ParameterDTO paramDto = new ParameterDTO();
            paramDto.setUuid(ruleParameterData.getUuid());
            paramDto.setValue(ruleParameterData.getValue());
            parameters.put(key, paramDto);
        }

        return parameters;
    }

    protected void populateExcludeOrderSources(PromotionSourceRuleModel source, PromotionSourceRuleDTO target) {
        String excludeOrderSources = source.getExcludeOrderSources();
        if(StringUtils.isNotBlank(excludeOrderSources)) {
            String[] excludeOrderSourceIds = excludeOrderSources.split(CommonUtils.COMMA);
            List<Long> excludeOrderSourceIdList = Arrays.stream(excludeOrderSourceIds).map(item -> Long.valueOf(item)).collect(Collectors.toList());
            target.setExcludeOrderSourceIds(excludeOrderSourceIdList);
        }
    }

    protected void populatePriceTypes(PromotionSourceRuleModel source, PromotionSourceRuleDTO target) {
        String appliedPriceTypes = source.getAppliedPriceTypes();
        if(StringUtils.isNotBlank(appliedPriceTypes)) {
            String[] priceTypes = appliedPriceTypes.split(CommonUtils.COMMA);
            target.setPriceTypes(Arrays.asList(priceTypes));
        }
    }

    protected void populateOrderTypes(PromotionSourceRuleModel source, PromotionSourceRuleDTO target) {
        String appliedOrderTypes = source.getAppliedOrderTypes();
        if(StringUtils.isNotBlank(appliedOrderTypes)) {
            String[] orderTypes = appliedOrderTypes.replaceAll(StringUtils.SPACE, StringUtils.EMPTY)
                    .split(CommonUtils.COMMA);
            target.setOrderTypes(Arrays.asList(orderTypes));
        }
    }

    protected void populateWarehouses(PromotionSourceRuleModel source, PromotionSourceRuleDTO target) {
        String appliedWarehouseIds = source.getAppliedWarehouseIds();
        if(StringUtils.isNotBlank(appliedWarehouseIds)) {
            String[] warehouseId = appliedWarehouseIds.replaceAll(StringUtils.SPACE, StringUtils.EMPTY)
                    .split(CommonUtils.COMMA);
            List<Long> warehouseIds = Arrays.stream(warehouseId).map(id -> Long.valueOf(id))
                    .collect(Collectors.toList());
            target.setWarehouseIds(warehouseIds);
        }
    }

    @Autowired
    public void setRuleParameterValueConverter(RuleParameterValueConverter ruleParameterValueConverter) {
        this.ruleParameterValueConverter = ruleParameterValueConverter;
    }
}
