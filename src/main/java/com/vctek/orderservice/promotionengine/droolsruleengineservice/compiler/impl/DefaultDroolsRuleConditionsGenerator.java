package com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.impl;

import com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.DroolsRuleConditionsGenerator;
import com.vctek.orderservice.promotionengine.droolsruleengineservice.compiler.DroolsRuleGeneratorContext;
import com.vctek.orderservice.promotionengine.droolsruleengineservice.exception.DroolsRuleValueFormatterException;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleCompilerException;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleIr;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleIrVariable;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.conditions.*;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.formatter.DroolsRuleValueFormatter;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleIrGroupOperator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class DefaultDroolsRuleConditionsGenerator implements DroolsRuleConditionsGenerator {
    public static final String NON_SUPPORTED_CONDITION = "Not supported RuleIrCondition";
    public static final String ERROR_NOT_FOUND = "Variable with name {0} not found";
    public static final int BUFFER_SIZE = 4096;
    private DroolsRuleValueFormatter droolsRuleValueFormatter;

    public DefaultDroolsRuleConditionsGenerator(DroolsRuleValueFormatter droolsRuleValueFormatter) {
        this.droolsRuleValueFormatter = droolsRuleValueFormatter;
    }

    @Override
    public String generateConditions(DroolsRuleGeneratorContext context, String indentation) {
        try {
            RuleIr ruleIr = context.getRuleIr();
            return this.generateConditions(context, ruleIr.getConditions(), RuleIrGroupOperator.AND, StringUtils.EMPTY, indentation);
        } catch (DroolsRuleValueFormatterException e) {
            throw new RuleCompilerException(e);
        }
    }

    @Override
    public String generateRequiredFactsCheckPattern(DroolsRuleGeneratorContext context) {
        RuleIr ruleIr = context.getRuleIr();
        return this.generateWhenConditions(context, ruleIr.getConditions(), RuleIrGroupOperator.AND, StringUtils.EMPTY, context.getIndentationSize());
    }

    protected String generateWhenConditions(DroolsRuleGeneratorContext context, List<RuleIrCondition> conditions, RuleIrGroupOperator operator, String conditionPrefix, String indentation) {
        if (CollectionUtils.isEmpty(conditions)) {
            return StringUtils.EMPTY;
        }
        RuleIrConditionsByType ruleIrConditionsByType = this.evaluateRuleConditionType(conditions);
        ListValuedMap<String, AbstractRuleIrPatternCondition> patternConditions = ruleIrConditionsByType.getPatternConditions();
        List<RuleIrGroupCondition> groupConditions = ruleIrConditionsByType.getGroupConditions();
        List<RuleIrGroupCondition> normalizedGroupConditions = groupConditions.stream()
                .filter(c -> this.filterOutNonGroupConditions(c.getChildren()).size() <= 1
                        || c.getOperator().compareTo(RuleIrGroupOperator.OR) != 0).collect(Collectors.toList());
        List<RuleIrNotCondition> notConditions = ruleIrConditionsByType.getNotConditions();
        StringJoiner conditionsJoiner = new StringJoiner(StringUtils.EMPTY);
        int conditionsCount = patternConditions.size() + normalizedGroupConditions.size() + notConditions.size();
        if(conditionsCount <= 0) {
            return conditionsJoiner.toString();
        }

        String conditionsIndentation;
        if ((operator == null || conditionsCount == 1) && StringUtils.isEmpty(conditionPrefix)) {
            conditionsIndentation = indentation;
        } else {
            String operatorAsString = conditionsCount > 1 && operator != null ? operator.toString().toLowerCase() : StringUtils.EMPTY;
            String delimiter = indentation + context.getIndentationSize() + operatorAsString + "\n";
            String prefix = indentation + (conditionPrefix == null ? StringUtils.EMPTY : conditionPrefix) + "(\n";
            String suffix = indentation + ")\n";
            conditionsIndentation = indentation + context.getIndentationSize();
            conditionsJoiner = new StringJoiner(delimiter, prefix, suffix);
        }

        if (patternConditions.asMap().values().stream().flatMap(l -> l.stream())
                .anyMatch(condition -> this.isConditionDependentOnOthers(condition, normalizedGroupConditions, patternConditions.asMap().keySet()))) {
            this.generateWhenGroupConditions(context, normalizedGroupConditions, conditionsJoiner, conditionsIndentation);
            this.generateWhenPatternConditions(context, patternConditions.asMap(), operator, conditionsJoiner, conditionsIndentation);
        } else {
            this.generateWhenPatternConditions(context, patternConditions.asMap(), operator, conditionsJoiner, conditionsIndentation);
            this.generateWhenGroupConditions(context, normalizedGroupConditions, conditionsJoiner, conditionsIndentation);
        }

        this.generateWhenNotConditions(context, notConditions, conditionsJoiner, conditionsIndentation);

        return conditionsJoiner.toString();
    }

    protected boolean isConditionDependentOnOthers(RuleIrCondition condition, Collection<? extends RuleIrCondition> others, Collection<String> definedVariables) {
        Collection<String> variableNamesToCheck = new HashSet();
        if (condition instanceof RuleIrAttributeRelCondition) {
            variableNamesToCheck.add(((RuleIrAttributeRelCondition)condition).getTargetVariable());
        } else {
            if (!(condition instanceof RuleIrGroupCondition)) {
                return false;
            }

            this.findVariablesOfPatternConditions(variableNamesToCheck, ((RuleIrGroupCondition)condition).getChildren());
        }

        variableNamesToCheck.removeAll(definedVariables);
        return this.isAnyVariableReferredInConditions(variableNamesToCheck, others);
    }

    protected boolean isAnyVariableReferredInConditions(Collection<String> variableNamesToCheck, Collection<? extends RuleIrCondition> others) {
        if (!this.isAnyVariableReferredInAttrRelConditions(variableNamesToCheck, others) && !this.isAnyVariableReferredInPatternConditions(variableNamesToCheck, others)) {
            Collection<RuleIrCondition> conditionsInGroups = this.getConditionsInGroups(others);
            return conditionsInGroups.isEmpty() ? false : this.isAnyVariableReferredInConditions(variableNamesToCheck, conditionsInGroups);
        } else {
            return true;
        }
    }

    protected boolean isAnyVariableReferredInPatternConditions(Collection<String> variableNamesToCheck, Collection<? extends RuleIrCondition> others) {
        return others.stream().filter(c -> c instanceof AbstractRuleIrPatternCondition)
                .map(c -> ((AbstractRuleIrPatternCondition)c).getVariable())
                .filter(Objects::nonNull).anyMatch(variableNamesToCheck::contains);
    }

    protected boolean isAnyVariableReferredInAttrRelConditions(Collection<String> variableNamesToCheck, Collection<? extends RuleIrCondition> others) {
        return others.stream().filter(c -> c instanceof RuleIrAttributeRelCondition)
                .map(c -> ((RuleIrAttributeRelCondition)c).getTargetVariable())
                .filter(Objects::nonNull).anyMatch(variableNamesToCheck::contains);
    }

    protected void findVariablesOfPatternConditions(Collection<String> variableNames, Collection<RuleIrCondition> conditions) {
        variableNames.addAll(conditions.stream()
                .filter(c -> c instanceof AbstractRuleIrPatternCondition)
                .map(c -> ((AbstractRuleIrPatternCondition)c).getVariable()).collect(Collectors.toSet()));
        Collection<RuleIrCondition> conditionsInGroups = this.getConditionsInGroups(conditions);
        if (!conditionsInGroups.isEmpty()) {
            this.findVariablesOfPatternConditions(variableNames, conditionsInGroups);
        }

    }

    protected Collection<RuleIrCondition> getConditionsInGroups(Collection<? extends RuleIrCondition> conditions) {
        return conditions.stream().filter(c -> c instanceof RuleIrConditionWithChildren)
                .map(c -> (RuleIrConditionWithChildren)c)
                .filter(c -> c.getChildren() != null)
                .flatMap(c -> c.getChildren().stream()).collect(Collectors.toSet());
    }

    protected Collection<RuleIrCondition> filterOutNonGroupConditions(Collection<RuleIrCondition> conditions) {
        return (Collection)(CollectionUtils.isNotEmpty(conditions) ?
                conditions.stream().filter(c -> c instanceof RuleIrGroupCondition)
                        .collect(Collectors.toList()) : Collections.emptyList());
    }

    protected String generateConditions(DroolsRuleGeneratorContext context, List<RuleIrCondition> conditions, RuleIrGroupOperator operator, String conditionPrefix, String indentation) {
        if (CollectionUtils.isEmpty(conditions)) {
            return StringUtils.EMPTY;
        }

        RuleIrConditionsByType ruleIrConditionsList = this.evaluateRuleConditionType(conditions);
        ListValuedMap<Boolean, AbstractRuleIrBooleanCondition> booleanConditions = ruleIrConditionsList.getBooleanConditions();
        ListValuedMap<String, AbstractRuleIrPatternCondition> patternConditions = ruleIrConditionsList.getPatternConditions();
        List<RuleIrGroupCondition> groupConditions = ruleIrConditionsList.getGroupConditions();
        List<RuleIrExecutableCondition> executableConditions = ruleIrConditionsList.getExecutableConditions();
        List<RuleIrExistsCondition> existsConditions = ruleIrConditionsList.getExistsConditions();
        List<RuleIrNotCondition> notConditions = ruleIrConditionsList.getNotConditions();
        int conditionsCount = booleanConditions.size() + patternConditions.size() + groupConditions.size() + existsConditions.size() + notConditions.size() + executableConditions.size();
        if (conditionsCount == 0) {
            return StringUtils.EMPTY;
        } else {
            String conditionsIndentation;
            StringJoiner conditionsJoiner;
            if ((operator == null || conditionsCount == 1) && StringUtils.isEmpty(conditionPrefix)) {
                conditionsIndentation = indentation;
                conditionsJoiner = new StringJoiner(StringUtils.EMPTY);
            } else {
                String operatorAsString = operator != null ? operator.toString().toLowerCase() : StringUtils.EMPTY;
                String delimiter = indentation + context.getIndentationSize() + operatorAsString + "\n";
                String prefix = indentation + (conditionPrefix == null ? StringUtils.EMPTY : conditionPrefix) + "(\n";
                String suffix = indentation + ")\n";
                conditionsIndentation = indentation + context.getIndentationSize();
                conditionsJoiner = new StringJoiner(delimiter, prefix, suffix);
            }

            this.generateBooleanConditions(booleanConditions.asMap(), conditionsJoiner, conditionsIndentation);
            this.generatePatternConditions(context, patternConditions.asMap(), operator, conditionsJoiner, conditionsIndentation);
            this.generateGroupConditions(context, groupConditions, conditionsJoiner, conditionsIndentation);
            this.generateExistsConditions(context, existsConditions, conditionsJoiner, conditionsIndentation);
            this.generateNotConditions(context, notConditions, conditionsJoiner, conditionsIndentation);
            return conditionsJoiner.toString();
        }
    }

    protected RuleIrConditionsByType evaluateRuleConditionType(List<RuleIrCondition> conditions) {
        RuleIrConditionsByType ruleIrConditionsList = new RuleIrConditionsByType();
        ListValuedMap<Boolean, AbstractRuleIrBooleanCondition> booleanConditions = new ArrayListValuedHashMap();
        ListValuedMap<String, AbstractRuleIrPatternCondition> patternConditions = new ArrayListValuedHashMap();
        List<RuleIrGroupCondition> groupConditions = new ArrayList();
        List<RuleIrExecutableCondition> executableConditions = new ArrayList();
        List<RuleIrExistsCondition> existsConditions = new ArrayList();
        List<RuleIrNotCondition> notConditions = new ArrayList();
        Iterator var10 = conditions.iterator();

        while(var10.hasNext()) {
            RuleIrCondition ruleIrCondition = (RuleIrCondition)var10.next();
            if (ruleIrCondition instanceof RuleIrTrueCondition) {
                booleanConditions.put(Boolean.TRUE, (RuleIrTrueCondition)ruleIrCondition);
            } else if (ruleIrCondition instanceof RuleIrFalseCondition) {
                booleanConditions.put(Boolean.FALSE, (RuleIrFalseCondition)ruleIrCondition);
            } else if (ruleIrCondition instanceof AbstractRuleIrPatternCondition) {
                AbstractRuleIrPatternCondition ruleIrPatternCondition = (AbstractRuleIrPatternCondition)ruleIrCondition;
                patternConditions.put(ruleIrPatternCondition.getVariable(), ruleIrPatternCondition);
            } else if (ruleIrCondition instanceof RuleIrGroupCondition) {
                groupConditions.add((RuleIrGroupCondition)ruleIrCondition);
            } else if (ruleIrCondition instanceof RuleIrExistsCondition) {
                existsConditions.add((RuleIrExistsCondition)ruleIrCondition);
            } else if (ruleIrCondition instanceof RuleIrNotCondition) {
                notConditions.add((RuleIrNotCondition)ruleIrCondition);
            } else {
                if (!(ruleIrCondition instanceof RuleIrExecutableCondition)) {
                    throw new RuleCompilerException(NON_SUPPORTED_CONDITION);
                }

                executableConditions.add((RuleIrExecutableCondition)ruleIrCondition);
            }
        }

        ruleIrConditionsList.setBooleanConditions(booleanConditions);
        ruleIrConditionsList.setPatternConditions(patternConditions);
        ruleIrConditionsList.setGroupConditions(groupConditions);
        ruleIrConditionsList.setExecutableConditions(executableConditions);
        ruleIrConditionsList.setExistsConditions(existsConditions);
        ruleIrConditionsList.setNotConditions(notConditions);
        return ruleIrConditionsList;
    }

    protected void generateBooleanConditions(Map<Boolean, Collection<AbstractRuleIrBooleanCondition>> booleanConditions, StringJoiner conditionsJoiner, String indentation) {
        Iterator var5 = booleanConditions.entrySet().iterator();

        while(var5.hasNext()) {
            Map.Entry<Boolean, Collection<AbstractRuleIrBooleanCondition>> entry = (Map.Entry)var5.next();
            if (Boolean.TRUE.equals(entry.getKey())) {
                conditionsJoiner.add(indentation + "eval(true)\n");
            } else {
                if (!Boolean.FALSE.equals(entry.getKey())) {
                    throw new RuleCompilerException(NON_SUPPORTED_CONDITION);
                }

                conditionsJoiner.add(indentation + "eval(false)\n");
            }
        }

    }

    protected void generateWhenPatternConditions(DroolsRuleGeneratorContext context, Map<String, Collection<AbstractRuleIrPatternCondition>> patternConditions, RuleIrGroupOperator groupOperator, StringJoiner conditionsJoiner, String indentation) {
        Set<Dependency> dependencies = this.buildDependencies(patternConditions);
        Map<String, Collection<AbstractRuleIrPatternCondition>> sortedPatternConditions = new TreeMap(new DependencyComparator(dependencies));
        sortedPatternConditions.putAll(patternConditions);

        String conditionsBufferStr;
        String conditionTerminator;
        for(Iterator var9 = sortedPatternConditions.entrySet().iterator(); var9.hasNext(); conditionsJoiner.add(conditionsBufferStr + conditionTerminator)) {
            Map.Entry<String, Collection<AbstractRuleIrPatternCondition>> entry = (Map.Entry)var9.next();
            String separator = groupOperator == RuleIrGroupOperator.AND ? ", " : " || ";
            StringBuilder conditionsBuffer = new StringBuilder(BUFFER_SIZE);
            String variableName = entry.getKey();
            RuleIrVariable variable = this.findVariable(context, variableName);
            if (variable == null) {
                throw new RuleCompilerException(MessageFormat.format(ERROR_NOT_FOUND, variableName));
            }

            String variableClassName = context.generateClassName(variable.getType());
            Supplier<String> variablePrefixSupplier = () -> context.getVariablePrefix() + "rao_";
            boolean variableIsTerminal = this.isVariableTerminal(variableName, context);
            if (variableIsTerminal) {
                conditionsBuffer.append(indentation).append("exists (").append(variableClassName).append('(');
            } else {
                conditionsBuffer.append(indentation).append(variablePrefixSupplier.get()).append(variableName)
                        .append(" := ").append(variableClassName).append('(');
            }

            entry.getValue().stream()
                    .filter((c) -> c instanceof AbstractRuleIrAttributeCondition)
                    .map((c) -> (AbstractRuleIrAttributeCondition)c).forEach((c) -> {
                conditionsBuffer.append(c.getAttribute()).append(' ')
                        .append(c.getOperator().getOriginalCode())
                        .append(' ').append(this.evaluatePatternConditionType(context, c, variablePrefixSupplier))
                        .append(separator);
            });
            conditionsBufferStr = conditionsBuffer.toString();
            conditionsBufferStr = conditionsBufferStr.endsWith(separator) ?
                    conditionsBufferStr.substring(0, conditionsBufferStr.length() - separator.length()) : conditionsBufferStr;
            conditionTerminator = ")\n";
            if (variableIsTerminal) {
                conditionTerminator = "))\n";
            }
        }

    }

    protected boolean isVariableTerminal(String variableName, DroolsRuleGeneratorContext context) {
        return this.isVariableTerminal(variableName, context.getRuleIr().getConditions());
    }

    protected boolean isVariableTerminal(String variableName, Collection<RuleIrCondition> conditions) {
        if (conditions.stream()
                .filter(c -> c instanceof RuleIrAttributeRelCondition)
                .map(c -> ((RuleIrAttributeRelCondition)c).getTargetVariable())
                .filter(Objects::nonNull).anyMatch(variableName::equals)) {
            return false;
        } else {
            Collection<RuleIrCondition> conditionsInGroups = this.getConditionsInGroups(conditions);
            return conditionsInGroups.isEmpty() ? true : this.isVariableTerminal(variableName, conditionsInGroups);
        }
    }

    protected void generatePatternConditions(DroolsRuleGeneratorContext context, Map<String, Collection<AbstractRuleIrPatternCondition>> patternConditions, RuleIrGroupOperator groupOperator, StringJoiner conditionsJoiner, String indentation) {
        Set<Dependency> dependencies = this.buildDependencies(patternConditions);
        Map<String, Collection<AbstractRuleIrPatternCondition>> sortedPatternConditions = new TreeMap(new DependencyComparator(dependencies));
        sortedPatternConditions.putAll(patternConditions);
        Iterator var9 = sortedPatternConditions.entrySet().iterator();

        while(var9.hasNext()) {
            Map.Entry<String, Collection<AbstractRuleIrPatternCondition>> entry = (Map.Entry)var9.next();
            String separator = groupOperator == RuleIrGroupOperator.AND ? ", " : " || ";
            StringBuilder conditionsBuffer = new StringBuilder(BUFFER_SIZE);
            String variableName = entry.getKey();
            RuleIrVariable variable = this.findVariable(context, variableName);
            if (variable == null) {
                throw new RuleCompilerException(MessageFormat.format(ERROR_NOT_FOUND, variableName));
            }

            String variableClassName = context.generateClassName(variable.getType());
            conditionsBuffer.append(indentation).append(context.getVariablePrefix()).append(variableName).append(" := ").append(variableClassName).append('(');
            entry.getValue().stream().
                    filter(c -> c instanceof AbstractRuleIrAttributeCondition)
                    .map(c -> (AbstractRuleIrAttributeCondition)c).forEach(c ->
                            conditionsBuffer.append(c.getAttribute()).append(' ')
                            .append(c.getOperator().getOriginalCode()).append(' ')
                            .append(this.evaluatePatternConditionType(context, c)).append(separator));
            String conditionsBufferStr = conditionsBuffer.toString();
            conditionsBufferStr = conditionsBufferStr.endsWith(separator) ?
                    conditionsBufferStr.substring(0, conditionsBufferStr.length() - separator.length()) : conditionsBufferStr;
            conditionsJoiner.add(conditionsBufferStr + ")\n");
        }

    }

    protected String evaluatePatternConditionType(DroolsRuleGeneratorContext context, AbstractRuleIrPatternCondition patternCondition) {
        return this.evaluatePatternConditionType(context, patternCondition, context::getVariablePrefix);
    }

    protected String evaluatePatternConditionType(DroolsRuleGeneratorContext context,
                                                  AbstractRuleIrPatternCondition patternCondition,
                                                  Supplier<String> variablePrefixSupplier) {
        if (patternCondition instanceof RuleIrAttributeCondition) {
            RuleIrAttributeCondition attributeCondition = (RuleIrAttributeCondition)patternCondition;
            return this.droolsRuleValueFormatter.formatValue(context, attributeCondition.getValue());
        } else if (patternCondition instanceof RuleIrAttributeRelCondition) {
            RuleIrAttributeRelCondition attributeRelCondition = (RuleIrAttributeRelCondition)patternCondition;
            String targetVariableName = attributeRelCondition.getTargetVariable();
            if (Objects.isNull(this.findVariable(context, targetVariableName))) {
                throw new RuleCompilerException(MessageFormat.format(ERROR_NOT_FOUND, targetVariableName));
            } else {
                String result = variablePrefixSupplier.get() + targetVariableName;
                if (StringUtils.isNotEmpty(attributeRelCondition.getTargetAttribute())) {
                    result = result + context.getAttributeDelimiter() + attributeRelCondition.getTargetAttribute();
                }

                return result;
            }
        } else {
            throw new RuleCompilerException(NON_SUPPORTED_CONDITION);
        }
    }

    protected void generateWhenGroupConditions(DroolsRuleGeneratorContext context, List<RuleIrGroupCondition> groupConditions, StringJoiner conditionsJoiner, String indentation) {
        this.generateGroupConditions(groupConditions, c -> this.generateWhenConditions(context, c.getChildren(), c.getOperator(), StringUtils.EMPTY, indentation), conditionsJoiner);
    }

    protected void generateGroupConditions(DroolsRuleGeneratorContext context, List<RuleIrGroupCondition> groupConditions, StringJoiner conditionsJoiner, String indentation) {
        this.generateGroupConditions(groupConditions, c -> this.generateConditions(context, c.getChildren(), c.getOperator(), StringUtils.EMPTY, indentation), conditionsJoiner);
    }

    protected void generateGroupConditions(List<RuleIrGroupCondition> groupConditions, Function<RuleIrGroupCondition, String> generateConditionsFunction, StringJoiner conditionsJoiner) {
        Iterator var5 = groupConditions.iterator();

        while(var5.hasNext()) {
            RuleIrGroupCondition groupCondition = (RuleIrGroupCondition)var5.next();
            if (groupCondition.getOperator() == null) {
                throw new RuleCompilerException("Group operator cannot be null");
            }

            if (CollectionUtils.isNotEmpty(groupCondition.getChildren())) {
                String generatedConditions = generateConditionsFunction.apply(groupCondition);
                if (StringUtils.isNotEmpty(generatedConditions)) {
                    conditionsJoiner.add(generatedConditions);
                }
            }
        }

    }

    protected void generateExistsConditions(DroolsRuleGeneratorContext context, List<RuleIrExistsCondition> existsConditions, StringJoiner conditionsJoiner, String indentation) {
        Iterator var6 = existsConditions.iterator();

        while(var6.hasNext()) {
            RuleIrExistsCondition exitsCondition = (RuleIrExistsCondition)var6.next();
            if (CollectionUtils.isNotEmpty(exitsCondition.getChildren())) {
                if (exitsCondition.getVariablesContainer() != null) {
                    context.addLocalVariables(exitsCondition.getVariablesContainer().getVariables());
                }

                String generatedConditions = this.generateConditions(context, exitsCondition.getChildren(), RuleIrGroupOperator.AND, "exists ", indentation);
                conditionsJoiner.add(generatedConditions);
                if (exitsCondition.getVariablesContainer() != null) {
                    context.getLocalVariables().pollFirst();
                }
            }
        }

    }

    protected void generateNotConditions(DroolsRuleGeneratorContext context, List<RuleIrNotCondition> notConditions, StringJoiner conditionsJoiner, String indentation) {
        this.generateNotConditions(context, notConditions, notCondition -> this.generateConditions(context, notCondition.getChildren(), RuleIrGroupOperator.AND, "not ", indentation), conditionsJoiner);
    }

    protected void generateWhenNotConditions(DroolsRuleGeneratorContext context, List<RuleIrNotCondition> notConditions, StringJoiner conditionsJoiner, String indentation) {
        this.generateNotConditions(context, notConditions, notCondition -> this.generateWhenConditions(context, notCondition.getChildren(), RuleIrGroupOperator.AND, "not ", indentation), conditionsJoiner);
    }

    protected void generateNotConditions(DroolsRuleGeneratorContext context, List<RuleIrNotCondition> notConditions, Function<RuleIrNotCondition, String> generateConditionsSupplier, StringJoiner conditionsJoiner) {
        Iterator var6 = notConditions.iterator();

        while(var6.hasNext()) {
            RuleIrNotCondition notCondition = (RuleIrNotCondition)var6.next();
            if (CollectionUtils.isNotEmpty(notCondition.getChildren())) {
                if (notCondition.getVariablesContainer() != null) {
                    context.addLocalVariables(notCondition.getVariablesContainer().getVariables());
                }

                String generatedConditions = generateConditionsSupplier.apply(notCondition);
                conditionsJoiner.add(generatedConditions);
                if (notCondition.getVariablesContainer() != null) {
                    context.getLocalVariables().pollFirst();
                }
            }
        }

    }

    protected RuleIrVariable findVariable(DroolsRuleGeneratorContext context, String variableName) {
        Iterator var4 = context.getLocalVariables().iterator();

        while(var4.hasNext()) {
            Map<String, RuleIrVariable> variables = (Map)var4.next();
            RuleIrVariable variable = variables.get(variableName);
            if (variable != null) {
                return variable;
            }
        }

        return context.getVariables().get(variableName);
    }

    protected Set<Dependency> buildDependencies(Map<String, Collection<AbstractRuleIrPatternCondition>> patternConditions) {
        Set<Dependency> dependencies = new HashSet();
        Iterator var4 = patternConditions.values().iterator();

        while(var4.hasNext()) {
            Collection<AbstractRuleIrPatternCondition> conditions = (Collection)var4.next();
            Iterator var6 = conditions.iterator();

            while(var6.hasNext()) {
                AbstractRuleIrPatternCondition condition = (AbstractRuleIrPatternCondition)var6.next();
                if (condition instanceof RuleIrAttributeRelCondition) {
                    RuleIrAttributeRelCondition attributeRelCondition = (RuleIrAttributeRelCondition)condition;
                    Dependency dependency = new Dependency(attributeRelCondition.getVariable(), attributeRelCondition.getTargetVariable());
                    dependencies.add(dependency);
                }
            }
        }

        this.expandDependencies(dependencies);
        return dependencies;
    }

    protected void expandDependencies(Set<Dependency> dependencies) {
        Set<Dependency> newDependencies = new HashSet();
        Iterator var4 = dependencies.iterator();

        while(var4.hasNext()) {
            Dependency dependency1 = (Dependency)var4.next();
            String source = dependency1.source;
            Iterator var7 = dependencies.iterator();

            while(var7.hasNext()) {
                Dependency dependency2 = (Dependency)var7.next();
                if (dependency2.source.equals(dependency1.target)) {
                    String target = dependency2.target;
                    newDependencies.add(new Dependency(source, target));
                }
            }
        }

        if (dependencies.addAll(newDependencies)) {
            this.expandDependencies(dependencies);
        }

    }

    protected static class Dependency {
        private final String source;
        private final String target;

        public Dependency(String source, String target) {
            this.source = source;
            this.target = target;
        }

        public String getVariable1() {
            return this.source;
        }

        public String getVariable2() {
            return this.target;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null) {
                return false;
            } else if (!(obj instanceof Dependency)) {
                return false;
            } else {
                Dependency other = (Dependency)obj;
                return Objects.equals(this.source, other.source) && Objects.equals(this.target, other.target);
            }
        }

        public int hashCode() {
            return Objects.hash(new Object[]{this.source, this.target});
        }

        public String toString() {
            return this.source + " -> " + this.target;
        }
    }

    protected static class DependencyComparator implements Comparator<String> {
        private final Set<Dependency> dependencies;

        public DependencyComparator(Set<Dependency> dependencies) {
            this.dependencies = dependencies;
        }

        public int compare(String variable1, String variable2) {
            if (this.dependencies.contains(new Dependency(variable1, variable2))) {
                return 1;
            } else {
                return this.dependencies.contains(new Dependency(variable2, variable1)) ? -1 : variable1.compareTo(variable2);
            }
        }
    }
}
