package com.vctek.orderservice.promotionengine.ruleengineservice.converter;

import com.vctek.converter.AbstractPopulatingConverter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionCategoryData;
import com.vctek.orderservice.promotionengine.ruleengineservice.model.RuleActionDefinitionCategoryModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ActionDefinitionCategoryConverter
        extends AbstractPopulatingConverter<RuleActionDefinitionCategoryModel, RuleActionDefinitionCategoryData> {
    @Autowired
    private Populator<RuleActionDefinitionCategoryModel, RuleActionDefinitionCategoryData> actionDefinitionCategoryPopulator;

    @Override
    public void setTargetClass() {
        super.setTargetClass(RuleActionDefinitionCategoryData.class);
    }

    @Override
    public void setPopulators() {
        super.setPopulators(actionDefinitionCategoryPopulator);
    }
}
