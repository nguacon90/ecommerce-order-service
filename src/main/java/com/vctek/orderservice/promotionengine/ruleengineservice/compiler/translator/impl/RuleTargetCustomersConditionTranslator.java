package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.translator.impl;

import com.google.common.collect.Lists;
import com.vctek.orderservice.promotionengine.ruledefinition.condition.builders.*;
import com.vctek.orderservice.promotionengine.ruledefinition.enums.CollectionOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.*;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrAttributeOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrGroupOperator;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.UserGroupRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.UserRAO;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("ruleTargetCustomersConditionTranslator")
public class RuleTargetCustomersConditionTranslator extends AbstractRuleConditionTranslator {
    public static final String CUSTOMER_GROUPS_OPERATOR_PARAM = "customer_groups_operator";
    public static final String CUSTOMER_GROUPS_PARAM = "customer_groups";
    public static final String CUSTOMERS_PARAM = "customers";
    public static final String EXCLUDED_CUSTOMER_GROUPS_PARAM = "excluded_customer_groups";
    public static final String EXCLUDED_USERS_PARAM = "excluded_customers";
    public static final String USER_GROUP_RAO_ID_ATTRIBUTE = "id";
    public static final String USER_RAO_ID_ATTRIBUTE = "id";
    public static final String USER_RAO_GROUPS_ATTRIBUTE = "groups";
    public static final String CART_RAO_USER_ATTRIBUTE = "user";

    @Override
    public RuleIrCondition translate(RuleCompilerContext context, RuleConditionData condition, RuleConditionDefinitionData ruleConditionDefinitionData) {
        Map<String, RuleParameterData> conditionParameters = condition.getParameters();
        RuleParameterData customerGroupsOperatorParameter = conditionParameters.get(CUSTOMER_GROUPS_OPERATOR_PARAM);
        RuleParameterData customerGroupsParameter = conditionParameters.get(CUSTOMER_GROUPS_PARAM);
        RuleParameterData customersParameter = conditionParameters.get(CUSTOMERS_PARAM);
        RuleParameterData excludedCustomerGroupsParameter = conditionParameters.get(EXCLUDED_CUSTOMER_GROUPS_PARAM);
        RuleParameterData excludedCustomersParameter = conditionParameters.get(EXCLUDED_USERS_PARAM);
        if (this.verifyAnyPresent(new Object[]{customerGroupsParameter, customersParameter})) {
            CollectionOperator customerGroupsOperator = (CollectionOperator)customerGroupsOperatorParameter.getValue();
            List<Long> customerGroups = Objects.isNull(customerGroupsParameter) ? Collections.emptyList() : (List)customerGroupsParameter.getValue();
            List<Long> customers = Objects.isNull(customersParameter) ? Collections.emptyList() : (List)customersParameter.getValue();
            if (this.verifyAllPresent(new Object[]{customerGroupsOperator}) && this.verifyAnyPresent(new Object[]{customerGroups, customers})) {
                RuleIrGroupCondition irTargetCustomersCondition = RuleIrGroupConditionBuilder.newGroupConditionOf(RuleIrGroupOperator.AND).build();
                this.addTargetCustomersConditions(context, customerGroupsOperator, customerGroups, customers, irTargetCustomersCondition);
                if (!CollectionOperator.NOT_CONTAINS.equals(customerGroupsOperator)) {
                    this.addExcludedCustomersAndCustomerGroupsConditions(context, excludedCustomerGroupsParameter, excludedCustomersParameter, irTargetCustomersCondition);
                }

                return irTargetCustomersCondition;
            }
        }

        return IrConditions.newIrRuleFalseCondition();
    }
    protected void addTargetCustomersConditions(RuleCompilerContext context, CollectionOperator customerGroupsOperator, List<Long> customerGroups, List<Long> customers, RuleIrGroupCondition irTargetCustomersCondition) {
        String userRaoVariable = context.generateVariable(UserRAO.class);
        String cartRaoVariable = context.generateVariable(CartRAO.class);
        List<RuleIrCondition> irConditions = Lists.newArrayList();
        RuleIrTypeCondition irUserCondition = new RuleIrTypeCondition();
        irUserCondition.setVariable(userRaoVariable);
        RuleIrAttributeRelCondition irCartUserRel = RuleIrAttributeRelConditionBuilder.newAttributeRelationConditionFor(cartRaoVariable)
                .withAttribute(CART_RAO_USER_ATTRIBUTE).withOperator(RuleIrAttributeOperator.EQUAL).withTargetVariable(userRaoVariable).build();
        irConditions.add(irUserCondition);
        irConditions.add(irCartUserRel);
        RuleIrGroupCondition irCustomerGroupsCondition = this.getCustomerGroupConditions(context, customerGroupsOperator, customerGroups);
        RuleIrAttributeCondition irCustomersCondition = this.getCustomerConditions(context, customers);
        if (this.verifyAllPresent(new Object[]{irCustomerGroupsCondition, irCustomersCondition})) {
            RuleIrGroupCondition groupCondition = RuleIrGroupConditionBuilder.newGroupConditionOf(RuleIrGroupOperator.OR).build();
            groupCondition.setChildren(Arrays.asList(irCustomerGroupsCondition, irCustomersCondition));
            irConditions.add(groupCondition);
        } else if (Objects.nonNull(irCustomerGroupsCondition)) {
            irConditions.add(irCustomerGroupsCondition);
        } else if (Objects.nonNull(irCustomersCondition)) {
            irConditions.add(irCustomersCondition);
        }

        if (CollectionOperator.NOT_CONTAINS.equals(customerGroupsOperator)) {
            irTargetCustomersCondition.getChildren().add(RuleIrNotConditionBuilder.newNotCondition().withChildren(irConditions).build());
        } else {
            irTargetCustomersCondition.getChildren().addAll(irConditions);
        }

    }

    protected RuleIrGroupCondition getCustomerGroupConditions(RuleCompilerContext context, CollectionOperator customerGroupsOperator, List<Long> customerGroups) {
        RuleIrGroupCondition irCustomerGroupsCondition = null;
        if (CollectionUtils.isNotEmpty(customerGroups)) {
            String userRaoVariable = context.generateVariable(UserRAO.class);
            String userGroupRaoVariable = context.generateVariable(UserGroupRAO.class);
            List<RuleIrCondition> irCustomerGroupsConditions = Lists.newArrayList();
            RuleIrAttributeCondition irUserGroupCondition = RuleIrAttributeConditionBuilder.newAttributeConditionFor(userGroupRaoVariable)
                    .withAttribute(USER_GROUP_RAO_ID_ATTRIBUTE).withOperator(RuleIrAttributeOperator.IN).withValue(customerGroups).build();
            RuleIrAttributeRelCondition irUserUserGroupRel = RuleIrAttributeRelConditionBuilder.newAttributeRelationConditionFor(userRaoVariable)
                    .withAttribute(USER_RAO_GROUPS_ATTRIBUTE).withOperator(RuleIrAttributeOperator.CONTAINS).withTargetVariable(userGroupRaoVariable).build();
            irCustomerGroupsConditions.add(irUserGroupCondition);
            irCustomerGroupsConditions.add(irUserUserGroupRel);
            this.addContainsAllCustomerGroupConditions(context, customerGroupsOperator, customerGroups, irCustomerGroupsConditions);
            irCustomerGroupsCondition = new RuleIrGroupCondition();
            irCustomerGroupsCondition.setOperator(RuleIrGroupOperator.AND);
            irCustomerGroupsCondition.setChildren(irCustomerGroupsConditions);
        }

        return irCustomerGroupsCondition;
    }

    protected void addContainsAllCustomerGroupConditions(RuleCompilerContext context, CollectionOperator customerGroupsOperator, List<Long> customerGroups, List<RuleIrCondition> irCustomerGroupsConditions) {
        if (CollectionOperator.CONTAINS_ALL.equals(customerGroupsOperator)) {
            String userRaoVariable = context.generateVariable(UserRAO.class);
            Iterator var7 = customerGroups.iterator();

            while(var7.hasNext()) {
                String customerGroup = (String)var7.next();
                RuleIrLocalVariablesContainer variablesContainer = context.createLocalContainer();
                String containsUserGroupRaoVariable = context.generateLocalVariable(variablesContainer, UserGroupRAO.class);
                RuleIrAttributeCondition irContainsUserGroupCondition = RuleIrAttributeConditionBuilder.newAttributeConditionFor(containsUserGroupRaoVariable)
                        .withAttribute(USER_GROUP_RAO_ID_ATTRIBUTE).withOperator(RuleIrAttributeOperator.EQUAL).withValue(customerGroup).build();
                RuleIrAttributeRelCondition irContainsUserUserGroupRel = RuleIrAttributeRelConditionBuilder.newAttributeRelationConditionFor(userRaoVariable)
                        .withAttribute(USER_RAO_GROUPS_ATTRIBUTE).withOperator(RuleIrAttributeOperator.CONTAINS).withTargetVariable(containsUserGroupRaoVariable).build();
                RuleIrExistsCondition irContainsCustomerGroupsCondition = new RuleIrExistsCondition();
                irContainsCustomerGroupsCondition.setVariablesContainer(variablesContainer);
                irContainsCustomerGroupsCondition.setChildren(Arrays.asList(irContainsUserGroupCondition, irContainsUserUserGroupRel));
                irCustomerGroupsConditions.add(irContainsCustomerGroupsCondition);
            }
        }

    }

    protected RuleIrAttributeCondition getCustomerConditions(RuleCompilerContext context, List<Long> customers) {
        RuleIrAttributeCondition irCustomersCondition = null;
        if (CollectionUtils.isNotEmpty(customers)) {
            irCustomersCondition = RuleIrAttributeConditionBuilder.newAttributeConditionFor(context.generateVariable(UserRAO.class))
                    .withAttribute(USER_RAO_ID_ATTRIBUTE).withOperator(RuleIrAttributeOperator.IN).withValue(customers).build();
        }

        return irCustomersCondition;
    }

    protected void addExcludedCustomersAndCustomerGroupsConditions(RuleCompilerContext context, RuleParameterData excludedCustomerGroupsParameter, RuleParameterData excludedCustomersParameter, RuleIrGroupCondition irTargetCustomersCondition) {
        String userRaoVariable = context.generateVariable(UserRAO.class);
        if (this.verifyAllPresent(new Object[]{excludedCustomerGroupsParameter, excludedCustomerGroupsParameter}) && CollectionUtils.isNotEmpty((Collection)excludedCustomerGroupsParameter.getValue())) {
            RuleIrLocalVariablesContainer variablesContainer = context.createLocalContainer();
            String excludedUserGroupRaoVariable = context.generateLocalVariable(variablesContainer, UserGroupRAO.class);
            RuleIrAttributeCondition irExcludedUserGroupCondition = RuleIrAttributeConditionBuilder.newAttributeConditionFor(excludedUserGroupRaoVariable)
                    .withAttribute(USER_GROUP_RAO_ID_ATTRIBUTE).withOperator(RuleIrAttributeOperator.IN).withValue(excludedCustomerGroupsParameter.getValue()).build();
            RuleIrAttributeRelCondition irExcludedUserUserGroupRel = RuleIrAttributeRelConditionBuilder.newAttributeRelationConditionFor(userRaoVariable)
                    .withAttribute(USER_RAO_GROUPS_ATTRIBUTE).withOperator(RuleIrAttributeOperator.CONTAINS).withTargetVariable(excludedUserGroupRaoVariable).build();
            RuleIrNotCondition irExcludedCustomerGroupsCondition = RuleIrNotConditionBuilder.newNotCondition().withVariablesContainer(variablesContainer).withChildren(Arrays.asList(irExcludedUserGroupCondition, irExcludedUserUserGroupRel)).build();
            irTargetCustomersCondition.getChildren().add(irExcludedCustomerGroupsCondition);
        }

        if (excludedCustomersParameter != null && this.verifyAllPresent(new Object[]{excludedCustomersParameter, excludedCustomersParameter.getValue()})) {
            irTargetCustomersCondition.getChildren().add(RuleIrAttributeConditionBuilder.newAttributeConditionFor(userRaoVariable)
                    .withAttribute(USER_RAO_ID_ATTRIBUTE).withOperator(RuleIrAttributeOperator.NOT_IN).withValue(excludedCustomersParameter.getValue()).build());
        }

    }
}
