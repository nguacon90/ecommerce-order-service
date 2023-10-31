package com.vctek.orderservice.promotionengine.ruleengine.init.impl;

import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.service.DroolsRuleService;
import com.vctek.orderservice.promotionengine.ruleengine.versioning.ModuleVersionResolver;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultContentMatchRulesFilterTest {
    private DefaultContentMatchRulesFilter contentMatchRulesFilter;

    @Mock
    private ModuleVersionResolver<DroolsKIEModuleModel> moduleVersionResolver;

    @Mock
    private DroolsRuleService droolsRuleService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        contentMatchRulesFilter = new DefaultContentMatchRulesFilter();
        contentMatchRulesFilter.setDroolsRuleService(droolsRuleService);
        contentMatchRulesFilter.setModuleVersionResolver(moduleVersionResolver);
    }

    @Test
    public void testApplyDifferentModules()
    {
        final DroolsKIEBaseModel kieBase1 = newKieBase(newKieModule("MODULE_NAME1"));
        final DroolsKIEBaseModel kieBase2 = newKieBase(newKieModule("MODULE_NAME2"));
        final List<DroolsRuleModel> droolsRules = Arrays.asList(newEngineRule(kieBase1, "rule1"),
                newEngineRule(kieBase2, "rule2"));

        assertThatThrownBy(() -> contentMatchRulesFilter.apply(droolsRules)).isInstanceOf(IllegalStateException.class)
                .hasMessage("All the rules in the collection should have the same DroolsKIEModuleModel [MODULE_NAME1]");
    }

    @Test
    public void testApplyNoDeployedVersion()
    {
        final DroolsKIEModuleModel module = newKieModule("MODULE_NAME");
        final DroolsKIEBaseModel kieBase = newKieBase(module);
        final List<DroolsRuleModel> droolsRules = Arrays.asList(newEngineRule(kieBase, "rule1"),
                newEngineRule(kieBase, "rule2"));

        when(moduleVersionResolver.getDeployedModuleVersion(module)).thenReturn(Optional.empty());

        final Pair<Collection<DroolsRuleModel>, Collection<DroolsRuleModel>> filteredRules = contentMatchRulesFilter
                .apply(droolsRules);
        assertThat(filteredRules.getLeft()).isEqualTo(droolsRules);
        assertThat(filteredRules.getRight()).isEmpty();
    }

    @Test
    public void testApplyValidDeployedVersionWrongFormat()
    {
        final DroolsKIEModuleModel module = newKieModule("MODULE_NAME");
        when(module.getDeployedMvnVersion()).thenReturn("basic_module_version_1");
        final DroolsKIEBaseModel kieBase = newKieBase(module);
        final List<DroolsRuleModel> droolsRules = Arrays.asList(newEngineRule(kieBase, "rule1"),
                newEngineRule(kieBase, "rule2"));

        when(moduleVersionResolver.getDeployedModuleVersion(module)).thenThrow(IllegalArgumentException.class);

        assertThatThrownBy(() -> contentMatchRulesFilter.apply(droolsRules)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testApplyValidDeployedVersionWrongFormatNonNumeric()
    {
        final DroolsKIEModuleModel module = newKieModule("MODULE_NAME");
        when(module.getDeployedMvnVersion()).thenReturn("basic_module_version.N");
        final DroolsKIEBaseModel kieBase = newKieBase(module);
        final List<DroolsRuleModel> droolsRules = Arrays.asList(newEngineRule(kieBase, "rule1"),
                newEngineRule(kieBase, "rule2"));

        when(moduleVersionResolver.getDeployedModuleVersion(module)).thenThrow(IllegalArgumentException.class);

        assertThatThrownBy(() -> contentMatchRulesFilter.apply(droolsRules)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testApplyOnlyRemove()
    {
        final DroolsKIEModuleModel module = newKieModule("MODULE_NAME");
        when(module.getDeployedMvnVersion()).thenReturn("basic_module_version.1");
        final DroolsKIEBaseModel kieBase = newKieBase(module);
        final DroolsRuleModel rule1 = newEngineRule(kieBase, "rule1");
        final DroolsRuleModel rule2 = newEngineRule(kieBase, "rule2");
        final List<DroolsRuleModel> droolsRules = Arrays.asList(rule1, rule2);

        when(moduleVersionResolver.getDeployedModuleVersion(module)).thenReturn(Optional.of(Long.valueOf(1)));
        final DroolsRuleModel droolsRuleToRemove = newEngineRule(kieBase, "rule3");
        final List<DroolsRuleModel> deployedDroolsRules = Arrays.asList(rule1,
                rule2, droolsRuleToRemove);
        when(droolsRuleService.getDeployedEngineRulesForModule("MODULE_NAME")).thenReturn(deployedDroolsRules);

        final Pair<Collection<DroolsRuleModel>, Collection<DroolsRuleModel>> filteredRules = contentMatchRulesFilter
                .apply(droolsRules);
        assertThat(filteredRules.getLeft()).isEmpty();
        assertThat(filteredRules.getRight()).isNotEmpty().hasSize(1).contains(droolsRuleToRemove);
    }

    @Test
    public void testApplyOnlyAdd()
    {
        final DroolsKIEModuleModel module = newKieModule("MODULE_NAME");
        when(module.getDeployedMvnVersion()).thenReturn("basic_module_version.1");
        final DroolsKIEBaseModel kieBase = newKieBase(module);
        final DroolsRuleModel rule3 = newEngineRule(kieBase, "rule3");
        final DroolsRuleModel droolsRuleToAdd = rule3;
        when(droolsRuleToAdd.getVersion()).thenReturn(2L);
        final DroolsRuleModel rule1 = newEngineRule(kieBase, "rule1");
        final DroolsRuleModel rule2 = newEngineRule(kieBase, "rule2");

        final List<DroolsRuleModel> droolsRules = Arrays.asList(rule1, rule2,
                droolsRuleToAdd);

        when(moduleVersionResolver.getDeployedModuleVersion(module)).thenReturn(Optional.of(Long.valueOf(1)));

        final DroolsRuleModel droolsRuleToRemove = newEngineRule(kieBase, "rule4");
        final List<DroolsRuleModel> deployedDroolsRules = Arrays.asList(rule1, rule2, droolsRuleToRemove);
        when(droolsRuleService.getDeployedEngineRulesForModule("MODULE_NAME")).thenReturn(deployedDroolsRules);

        final Pair<Collection<DroolsRuleModel>, Collection<DroolsRuleModel>> filteredRules = contentMatchRulesFilter
                .apply(droolsRules);
        assertThat(filteredRules.getLeft()).isNotEmpty().hasSize(1).contains(droolsRuleToAdd);
        assertThat(filteredRules.getRight()).isNotEmpty().hasSize(1).contains(droolsRuleToRemove);
    }

    @Test
    public void testApplyOK()
    {
        final DroolsKIEModuleModel module = newKieModule("MODULE_NAME");
        when(module.getDeployedMvnVersion()).thenReturn("basic_module_version.1");
        final DroolsKIEBaseModel kieBase = newKieBase(module);
        final DroolsRuleModel droolsRuleToAdd = newEngineRule(kieBase, "rule3");
        when(droolsRuleToAdd.getVersion()).thenReturn(2L);
        final DroolsRuleModel rule1 = newEngineRule(kieBase, "rule1");
        final DroolsRuleModel rule2 = newEngineRule(kieBase, "rule2");

        final List<DroolsRuleModel> droolsRules = Arrays.asList(rule1, rule2,
                droolsRuleToAdd);

        when(moduleVersionResolver.getDeployedModuleVersion(module)).thenReturn(Optional.of(Long.valueOf(1)));

        final List<DroolsRuleModel> deployedDroolsRules = Arrays.asList(rule1, rule2);
        when(droolsRuleService.getDeployedEngineRulesForModule("MODULE_NAME")).thenReturn(deployedDroolsRules);

        final Pair<Collection<DroolsRuleModel>, Collection<DroolsRuleModel>> filteredRules = contentMatchRulesFilter
                .apply(droolsRules);
        assertThat(filteredRules.getLeft()).isNotEmpty().hasSize(1).contains(droolsRuleToAdd);
        assertThat(filteredRules.getRight()).isEmpty();
    }

    private DroolsRuleModel newEngineRule(final DroolsKIEBaseModel kieBase, final String ruleCode)
    {
        final DroolsRuleModel engineRule = mock(DroolsRuleModel.class);
        when(engineRule.getCode()).thenReturn(ruleCode);
        when(engineRule.getKieBase()).thenReturn(kieBase);
        when(engineRule.isActive()).thenReturn(Boolean.TRUE);
        when(engineRule.isCurrentVersion()).thenReturn(Boolean.TRUE);
        return engineRule;
    }

    private DroolsKIEBaseModel newKieBase(final DroolsKIEModuleModel module)
    {
        final DroolsKIEBaseModel kieBase = mock(DroolsKIEBaseModel.class);
        when(kieBase.getDroolsKIEModule()).thenReturn(module);
        return kieBase;
    }

    private DroolsKIEModuleModel newKieModule(final String moduleName)
    {
        final DroolsKIEModuleModel kieModule = mock(DroolsKIEModuleModel.class);
        when(kieModule.getName()).thenReturn(moduleName);
        return kieModule;
    }
}
