package com.vctek.orderservice.promotionengine.ruleengineservice.model;

import javax.persistence.*;

@Entity
@Table(name = "rule_condition_definition_parameter")
public class RuleConditionDefinitionParameterModel extends AbstractRuleDefinitionParameterModel{

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_condition_definition_id")
    private RuleConditionDefinitionModel conditionDefinition;


    public RuleConditionDefinitionModel getConditionDefinition() {
        return conditionDefinition;
    }

    public void setConditionDefinition(RuleConditionDefinitionModel conditionDefinition) {
        this.conditionDefinition = conditionDefinition;
    }
}
