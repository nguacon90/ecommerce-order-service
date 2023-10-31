package com.vctek.orderservice.promotionengine.ruleengineservice.compiler.validation.impl;

import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerProblem;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerProblemFactory;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrVariablesContainer;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.validation.RuleParameterValidator;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.AbstractRuleDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterDefinitionData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component("ruleContainersParameterValidator")
public class RuleContainersParameterValidator implements RuleParameterValidator {
    private static final String INVALID_MESSAGE_KEY = "Error at parameter {0}, invalid container IDs: ({1})";
    private static final String NOT_EXIST_MESSAGE_KEY = "Error at parameter {0}, the following container IDs does not exist: ({1})";
    protected static final Pattern CONTAINER_ID_PATTERN = Pattern.compile("[a-zA-Z0-9_-]*$");
    private RuleCompilerProblemFactory ruleCompilerProblemFactory;

    @Override
    public void validate(RuleCompilerContext context, AbstractRuleDefinitionData ruleDefinition, RuleParameterData parameter, RuleParameterDefinitionData parameterDefinition) {
        if (parameter == null) {
            return;
        }

        Map<String, Integer> qualifyingContainers = (Map) parameter.getValue();
        if (MapUtils.isEmpty(qualifyingContainers)) {
            return;
        }
        List<String> invalidContainerIds = new ArrayList();
        List<String> notExistContainerIds = new ArrayList();
        Iterator var9 = qualifyingContainers.keySet().iterator();

        while (var9.hasNext()) {
            String containerId = (String) var9.next();
            if (!this.isValidContainerId(containerId)) {
                invalidContainerIds.add(containerId);
            }

            if (!this.isContainerExists(context, containerId)) {
                notExistContainerIds.add(containerId);
            }
        }

        if (CollectionUtils.isNotEmpty(invalidContainerIds)) {
            context.addProblem(this.ruleCompilerProblemFactory.createParameterProblem(RuleCompilerProblem.Severity.ERROR, INVALID_MESSAGE_KEY,
                    parameter, parameterDefinition, new Object[]{parameterDefinition.getName(), invalidContainerIds}));
        }

        if (CollectionUtils.isNotEmpty(notExistContainerIds)) {
            context.addProblem(this.ruleCompilerProblemFactory.createParameterProblem(RuleCompilerProblem.Severity.ERROR, NOT_EXIST_MESSAGE_KEY,
                    parameter, parameterDefinition, new Object[]{parameterDefinition.getName(), notExistContainerIds}));
        }

    }

    protected boolean isValidContainerId(String containerId) {
        return StringUtils.isBlank(containerId) ? false : CONTAINER_ID_PATTERN.matcher(containerId).matches();
    }

    protected boolean isContainerExists(RuleCompilerContext context, String containerId) {
        RuleIrVariablesContainer rootContainer = context.getVariablesGenerator().getRootContainer();
        return rootContainer.getChildren().containsKey(containerId);
    }

    @Autowired
    public void setRuleCompilerProblemFactory(RuleCompilerProblemFactory ruleCompilerProblemFactory) {
        this.ruleCompilerProblemFactory = ruleCompilerProblemFactory;
    }
}
