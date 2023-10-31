package com.vctek.orderservice.promotionengine.ruleengineservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionCategoryData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleConditionDefinitionCategoryModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RuleConditionDefinitionCategoryConverter
        extends AbstractPopulatingConverter<RuleConditionDefinitionCategoryModel, RuleConditionDefinitionCategoryData> {
    @Autowired
    private Populator<RuleConditionDefinitionCategoryModel, RuleConditionDefinitionCategoryData> ruleConditionDefinitionCategoryPopulator;

    @Override
    public void setTargetClass() {
        super.setTargetClass(RuleConditionDefinitionCategoryData.class);
    }

    @Override
    public void setPopulators() {
        super.setPopulators(ruleConditionDefinitionCategoryPopulator);
    }
}
