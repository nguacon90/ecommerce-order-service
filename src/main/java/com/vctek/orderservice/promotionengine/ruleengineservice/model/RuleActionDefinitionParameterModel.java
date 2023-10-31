package com.vctek.orderservice.promotionengine.ruleengineservice.model;

import javax.persistence.*;

@Entity
@Table(name = "rule_action_definition_parameter")
public class RuleActionDefinitionParameterModel extends AbstractRuleDefinitionParameterModel {
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_action_definition_id")
    private RuleActionDefinitionModel actionDefinition;

    public RuleActionDefinitionModel getActionDefinition() {
        return actionDefinition;
    }

    public void setActionDefinition(RuleActionDefinitionModel actionDefinition) {
        this.actionDefinition = actionDefinition;
    }
}
