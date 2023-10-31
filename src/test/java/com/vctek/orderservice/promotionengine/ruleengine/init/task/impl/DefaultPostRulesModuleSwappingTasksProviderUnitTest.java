package com.vctek.orderservice.promotionengine.ruleengine.init.task.impl;

import com.google.common.collect.Lists;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEngineActionResult;
import com.vctek.orderservice.promotionengine.ruleengine.init.task.PostRulesModuleSwappingTask;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class DefaultPostRulesModuleSwappingTasksProviderUnitTest {
    private DefaultPostRulesModuleSwappingTasksProvider tasksProvider;
    @Mock
    private RuleEngineActionResult ruleEngineActionResult;
    @Mock
    private PostRulesModuleSwappingTask task1;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        tasksProvider = new DefaultPostRulesModuleSwappingTasksProvider();
        tasksProvider.setUpdateRulesStatusPostRulesModuleSwappingTask(task1);
        tasksProvider.setUp();
        when(task1.execute(ruleEngineActionResult)).thenReturn(Boolean.TRUE);
    }

    @Test
    public void testGetTasks() {
        final List<Supplier<Object>> supplierList = tasksProvider.getTasks(ruleEngineActionResult);
        final List<Object> taskResults = supplierList.stream().map(Supplier::get).collect(toList());
        verify(task1).execute(ruleEngineActionResult);
        assertThat(taskResults).containsAll(Lists.newArrayList(Boolean.TRUE));
    }

}
