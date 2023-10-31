package com.vctek.orderservice.promotionengine.ruleengine.init.impl;

import com.vctek.orderservice.promotionengine.ruleengine.cache.KIEModuleCacheBuilder;
import com.vctek.orderservice.promotionengine.ruleengine.concurrency.TaskContext;
import com.vctek.orderservice.promotionengine.ruleengine.concurrency.TaskResultState;
import com.vctek.orderservice.promotionengine.ruleengine.concurrency.impl.DefaultAwareThreadFactory;
import com.vctek.orderservice.promotionengine.ruleengine.init.RuleEngineBootstrap;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.service.DroolsRuleService;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.Results;
import org.kie.api.builder.model.KieModuleModel;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class DefaultRulePublishingSpliteratorTest {
    private DefaultRulePublishingSpliterator spliterator;

    @Mock
    private DroolsRuleService droolsRuleService;
    @Mock
    private RuleEngineBootstrap ruleEngineBootstrap;
    @Mock
    private TaskContext taskContext;
    @Mock
    private KieModuleModel kieModuleModel;
    @Mock
    private ReleaseId releaseId;
    @Mock
    private KIEModuleCacheBuilder cache;
    @Mock
    private KieServices kieServices;
    @Mock
    private KieFileSystem kfs;
    @Mock
    private KieBuilder kieBuilder;
    @Mock
    private Results kieBuilderResults;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        spliterator = new DefaultRulePublishingSpliterator();
        spliterator.setDroolsRuleService(droolsRuleService);
        spliterator.setRuleEngineBootstrap(ruleEngineBootstrap);
        spliterator.setTaskContext(taskContext);
        when(taskContext.getNumberOfThreads()).thenReturn(2);
        when(taskContext.getThreadFactory()).thenReturn(new DefaultAwareThreadFactory());
        when(droolsRuleService.getRulesByUuids(anyList())).thenReturn(Arrays.asList(new DroolsRuleModel()));
        when(ruleEngineBootstrap.getEngineServices()).thenReturn(kieServices);
        final ReleaseId newReleaseId = mock(ReleaseId.class);
        when(kieServices.newReleaseId(anyString(), anyString(), eq("NEW_MVN_VERSION.1"))).thenReturn(newReleaseId);
        when(newReleaseId.getVersion()).thenReturn("NEW_MVN_VERSION.1");
        when(kieServices.newReleaseId(anyString(), anyString(), anyString())).thenReturn(releaseId);
        when(kieServices.newKieFileSystem()).thenReturn(kfs);
        when(kieServices.newKieBuilder(kfs)).thenReturn(kieBuilder);
        when(kieBuilder.getResults()).thenReturn(kieBuilderResults);
    }

    @Test
    public void publishRulesAsync() {
        List<String> ruleUuids = Arrays.asList("uuid1", "uuid2", "uuid3", "uuid4", "uuid5", "uuid6");
        RulePublishingFuture rulePublishingFuture = spliterator.publishRulesAsync(kieModuleModel, releaseId, ruleUuids, cache);
        rulePublishingFuture.waitForTasksToFinish();
        assertEquals(TaskResultState.SUCCESS, rulePublishingFuture.getTaskResult().getState());
        verify(kfs, times(2)).generateAndWritePomXML(releaseId);
    }
}
