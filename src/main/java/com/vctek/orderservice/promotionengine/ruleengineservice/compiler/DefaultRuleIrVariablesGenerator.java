package com.vctek.orderservice.promotionengine.ruleengineservice.compiler;


import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrLocalVariablesContainer;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.RuleIrVariablesContainer;

import java.util.*;

public class DefaultRuleIrVariablesGenerator implements RuleIrVariablesGenerator {
    public static final String DEFAULT_VARIABLES_CONTAINER_ID = "default";
    private final Deque<RuleIrVariablesContainer> containers = new ArrayDeque();
    private final RuleIrVariablesContainer rootContainer = createNewContainerForId(DEFAULT_VARIABLES_CONTAINER_ID, (RuleIrVariablesContainer)null);
    private int count = 0;
    public DefaultRuleIrVariablesGenerator() {
        this.containers.push(this.rootContainer);
    }

    @Override
    public String generateVariable(Class<?> type) {
        RuleIrVariablesContainer container = this.getCurrentContainer();
        RuleIrVariable variable = this.findVariable(container, type);
        if (variable == null) {
            String variableName = this.generateVariableName(type);
            variable = new RuleIrVariable();
            variable.setName(variableName);
            variable.setType(type);
            variable.setPath(container.getPath());
            container.getVariables().put(variableName, variable);
        }

        return variable.getName();
    }

    protected RuleIrVariable findVariable(RuleIrVariablesContainer container, Class<?> type) {
        Iterator var4 = container.getVariables().values().iterator();
        while(var4.hasNext()) {
            RuleIrVariable variable = (RuleIrVariable)var4.next();
            if (type.equals(variable.getType())) {
                return variable;
            }
        }

        return container.getParent() != null ? this.findVariable(container.getParent(), type) : null;
    }

    protected String generateVariableName(Class<?> type) {
        ++this.count;
        return "v" + this.count;
    }

    public RuleIrVariablesContainer getCurrentContainer() {
        if (this.containers.isEmpty()) {
            throw new IllegalStateException("There should exist at least one root container but no container found");
        } else {
            return this.containers.peek();
        }
    }

    private static RuleIrVariablesContainer createNewContainerForId(String id, RuleIrVariablesContainer parent) {
        RuleIrVariablesContainer container = new RuleIrVariablesContainer();
        container.setName(id);
        container.setVariables(new HashMap());
        container.setChildren(new HashMap());
        if (parent != null) {
            parent.getChildren().put(id, container);
            container.setParent(parent);
            int parentPathLength = parent.getPath().length;
            String[] path = Arrays.copyOf(parent.getPath(), parentPathLength + 1);
            path[parentPathLength] = id;
            container.setPath(path);
        } else {
            container.setPath(new String[0]);
        }

        return container;
    }

    @Override
    public RuleIrLocalVariablesContainer createLocalContainer() {
        RuleIrLocalVariablesContainer container = new RuleIrLocalVariablesContainer();
        container.setVariables(new HashMap());
        return container;
    }

    @Override
    public RuleIrVariablesContainer getRootContainer() {
        return this.rootContainer;
    }

    @Override
    public String generateLocalVariable(RuleIrLocalVariablesContainer container, Class<?> type) {
        String variableName = this.generateVariableName(type);
        RuleIrVariable variable = new RuleIrVariable();
        variable.setName(variableName);
        variable.setType(type);
        variable.setPath(new String[0]);
        container.getVariables().put(variableName, variable);
        return variableName;
    }

    @Override
    public RuleIrVariablesContainer createContainer(String id) {
        RuleIrVariablesContainer parentContainer = this.getCurrentContainer();
        RuleIrVariablesContainer container = createNewContainerForId(id, parentContainer);
        this.containers.push(container);
        return container;
    }

    @Override
    public void closeContainer() {
        if (this.containers.size() == 1) {
            throw new IllegalStateException("Root container cannot be closed, only previously created containers can be closed");
        } else {
            this.containers.pop();
        }
    }
}
