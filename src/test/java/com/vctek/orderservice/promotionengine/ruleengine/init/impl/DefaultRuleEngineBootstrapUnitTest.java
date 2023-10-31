package com.vctek.orderservice.promotionengine.ruleengine.init.impl;

import com.vctek.orderservice.promotionengine.ruleengine.KieContainerListener;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEngineActionResult;
import com.vctek.orderservice.promotionengine.ruleengine.cache.KIEModuleCacheBuilder;
import com.vctek.orderservice.promotionengine.ruleengine.cache.RuleEngineCacheService;
import com.vctek.orderservice.promotionengine.ruleengine.init.IncrementalRuleEngineUpdateStrategy;
import com.vctek.orderservice.promotionengine.ruleengine.init.RuleEngineContainerRegistry;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIESessionModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.repository.DroolKIEBaseRepository;
import com.vctek.orderservice.promotionengine.ruleengine.repository.DroolsKIEModuleRepository;
import com.vctek.orderservice.promotionengine.ruleengine.repository.DroolsKIESessionRepository;
import com.vctek.orderservice.promotionengine.ruleengine.repository.DroolsRuleRepository;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.assertj.core.util.Lists;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.compiler.kie.builder.impl.MemoryKieModule;
import org.drools.compiler.kie.builder.impl.ResultsImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.builder.IncrementalResults;
import org.kie.internal.builder.InternalKieBuilder;
import org.kie.internal.builder.KieBuilderSet;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.*;

public class DefaultRuleEngineBootstrapUnitTest {
    private static final String MODULE_NAME = "MODULE_NAME";
    private static final String DEPLOYED_VERSION = "DEPLOYED_VERSION";
    private static final String SUSPEND_MESSAGE = "Rule engine module deployment is in progress";
    @Mock
    private KieFileSystem kfs;
    @Mock
    private ReleaseId releaseId;
    @Mock
    private KieBaseModel baseKieSessionModel;
    @Mock
    private KieContainer kieContainer;
    @Mock
    private KieRepository kieRepository;
    @Mock
    private DroolsKIEModuleModel droolsModule;
    @Mock
    private RuleEngineActionResult ruleEngineActionResult;
    @Mock
    private KIEModuleCacheBuilder cache;
    @Mock
    private KieModuleModel kieModuleModel;
    @Mock
    private InternalKieBuilder kieBuilder;
    @Mock
    private Results kieBuilderResults;
    @Mock
    private MemoryKieModule kieModule;
    @Mock
    private DroolsKIEModuleRepository droolsKIEModuleRepository;
    @Mock
    private DroolKIEBaseRepository droolKIEBaseRepository;
    @Mock
    private DroolsKIESessionRepository droolsKIESessionRepository;
    @Mock
    private DroolsRuleRepository droolsRuleRepository;
    @Mock
    private KieServices kieServices;
    @Mock
    private MemoryFileSystem memoryFileSystem;
    @Mock
    private RuleEngineCacheService ruleEngineCacheService;
    @Mock
    private RuleEngineContainerRegistry<ReleaseId, KieContainer> ruleEngineContainerRegistry;
    @Mock
    private KieContainerListener kieContainerListener;
    @Mock
    private DroolsKIESessionModel kieSession;
    @Mock
    private ThreadFactory threadFactory;
    @Mock
    private DefaultContentMatchRulesFilter contentMatchRulesFilter;
    @Mock
    private IncrementalRuleEngineUpdateStrategy incrementalRuleEngineUpdateStrategy;
    @Mock
    private MemoryKieModule incrementalKieModule;

    private DefaultRuleEngineBootstrap bootstrap;

    public DefaultRuleEngineBootstrapUnitTest() {
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        bootstrap = new DefaultRuleEngineBootstrap(droolsKIEModuleRepository, droolKIEBaseRepository, droolsKIESessionRepository,
                droolsRuleRepository, ruleEngineCacheService);
        bootstrap.setKieServices(kieServices);
        bootstrap.setRuleEngineContainerRegistry(ruleEngineContainerRegistry);
        bootstrap.setThreadFactory(threadFactory);
        bootstrap.setIncrementalRuleEngineUpdateStrategy(incrementalRuleEngineUpdateStrategy);
        bootstrap.setContentMatchRulesFilter(contentMatchRulesFilter);

        when(kieServices.newReleaseId(anyString(), anyString(), anyString())).thenReturn(releaseId);
        when(kieServices.newKieFileSystem()).thenReturn(kfs);
        when(kieServices.newKieModuleModel()).thenReturn(kieModuleModel);
        when(kieModule.getKieModuleModel()).thenReturn(kieModuleModel);
        when(kieServices.newKieBuilder(kfs)).thenReturn(kieBuilder);
        when(kieServices.getRepository()).thenReturn(kieRepository);
        when(kieServices.newKieContainer(releaseId)).thenReturn(kieContainer);
        when(kieBuilder.getKieModule()).thenReturn(kieModule);
        when(kieBuilder.getResults()).thenReturn(kieBuilderResults);
        when(kieModule.getReleaseId()).thenReturn(releaseId);
        when(droolsModule.getDeployedMvnVersion()).thenReturn("1.0.0");
        when(droolsModule.getMvnArtifactId()).thenReturn("promotions");
        when(droolsModule.getMvnGroupId()).thenReturn("com.vctek.company2");
        when(kieContainer.getReleaseId()).thenReturn(releaseId);
        when(releaseId.getVersion()).thenReturn(DEPLOYED_VERSION);
        when(droolsModule.getName()).thenReturn(MODULE_NAME);
        when(releaseId.toExternalForm()).thenReturn("EXTERNAL_FORM");
//        when(rulesModuleDao.findByName(MODULE_NAME)).thenReturn(droolsModule);
        when(ruleEngineCacheService.createKIEModuleCacheBuilder(any())).thenReturn(cache);
        when(kieModule.getMemoryFileSystem()).thenReturn(memoryFileSystem);
        when(kieSession.getName()).thenReturn("kiesession_name");
//        when(suspendResumeTaskManager.isSystemRunning()).thenReturn(Boolean.TRUE);
        bootstrap.activeRuleModule();
    }

    @Test
    public void testAddKieSessionModel()
    {
        final DroolsKIESessionModel session = mock(DroolsKIESessionModel.class);
        final KieBaseModel base = mock(KieBaseModel.class);
        final KieSessionModel kieSessionModel = mock(KieSessionModel.class);
        final DroolsKIEBaseModel droolsKIEBaseModel = mock(DroolsKIEBaseModel.class);

        when(session.getId()).thenReturn(1235L);
        when(session.getName()).thenReturn("session name");
        when(session.getSessionType()).thenReturn(KieSessionModel.KieSessionType.STATEFUL.toString());
        when(droolsKIEBaseModel.getDefaultKieSession()).thenReturn(session);
        when(base.newKieSessionModel(any(String.class))).thenReturn(kieSessionModel);
        when(session.getDroolsKIEBase()).thenReturn(droolsKIEBaseModel);
        bootstrap.addKieSession(base, droolsKIEBaseModel, session);

        verify(kieSessionModel).setDefault(true);
        verify(kieSessionModel).setType(KieSessionModel.KieSessionType.STATEFUL);
    }

    @Test
    public void testAddRulesRuleInactive()
    {
        final KieFileSystem kieFileSystem = mock(KieFileSystem.class);

        final Set<DroolsRuleModel> rules = newHashSet();
        final DroolsRuleModel rule = mock(DroolsRuleModel.class);
        when(rule.getRuleContent()).thenReturn("rule content");
        when(rule.isActive()).thenReturn(Boolean.FALSE);
        rules.add(rule);

        bootstrap.writeRulesToKieFileSystem(kieFileSystem, rules);

        verifyZeroInteractions(kieFileSystem);
    }

    @Test
    public void testAddRulesRuleWithoutContent()
    {
        final KieFileSystem kieFileSystem = mock(KieFileSystem.class);
        final Set<DroolsRuleModel> rules = newHashSet();
        final DroolsRuleModel rule = mock(DroolsRuleModel.class);
        rules.add(rule);

        bootstrap.writeRulesToKieFileSystem(kieFileSystem, rules);

        verifyZeroInteractions(kieFileSystem);
    }

    @Test
    public void testAddRulesEmptyRules()
    {
        final KieFileSystem kieFileSystem = mock(KieFileSystem.class);

        bootstrap.writeRulesToKieFileSystem(kieFileSystem, Collections.emptySet());
        verifyZeroInteractions(kieFileSystem);
    }

    @Test
    public void testAddKieBaseModel()
    {
        final KieSessionModel session = mock(KieSessionModel.class);
        final DroolsKIEBaseModel base = mock(DroolsKIEBaseModel.class);
        when(base.getName()).thenReturn("Mock Base");
        when(kieModuleModel.newKieBaseModel("Mock Base")).thenReturn(baseKieSessionModel);
        when(baseKieSessionModel.newKieSessionModel(anyString())).thenReturn(session);
        when(droolsKIESessionRepository.findAllByDroolsKIEBase(base)).thenReturn(Arrays.asList(kieSession));
        bootstrap.addKieBase(kieModuleModel, base);

        verify(kieModuleModel).newKieBaseModel("Mock Base");
        verify(baseKieSessionModel).setEqualsBehavior(EqualityBehaviorOption.EQUALITY);
        verify(baseKieSessionModel).setEventProcessingMode(EventProcessingOption.STREAM);
        verify(session).setType(any(KieSessionModel.KieSessionType.class));
    }

    @Test
    public void testWriteKModuleXML()
    {
        final KieModuleModel module = mock(KieModuleModel.class);
        final KieFileSystem kieFileSystem = mock(KieFileSystem.class);
        when(module.toXML()).thenReturn("xml");
        bootstrap.writeKModuleXML(module, kieFileSystem);
        verify(kieFileSystem).writeKModuleXML(anyString());
        verify(module).toXML();
    }

    @Test
    public void testWritePomXML()
    {
        final DroolsKIEModuleModel module = mock(DroolsKIEModuleModel.class);
        final KieFileSystem kieFileSystem = mock(KieFileSystem.class);

        bootstrap.writePomXML(module, kieFileSystem);

        verify(kieFileSystem).generateAndWritePomXML(eq(null));
    }

    @Test
    public void testGetReleaseId()
    {
        final String groupId = "groupId";
        final String artifactId = "artifactId";
        final String version = "version.0";
        final DroolsKIEModuleModel module = mock(DroolsKIEModuleModel.class);

        when(module.getMvnGroupId()).thenReturn(groupId);
        when(module.getMvnArtifactId()).thenReturn(artifactId);
        when(module.getMvnVersion()).thenReturn(version);
        when(module.getDeployedMvnVersion()).thenReturn(version);

        bootstrap.getReleaseId(module);

        final ArgumentCaptor<String> argArtifactId = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> argGroupId = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> argVersion = ArgumentCaptor.forClass(String.class);
        verify(kieServices).newReleaseId(argGroupId.capture(), argArtifactId.capture(), argVersion.capture());
        Assert.assertThat(argArtifactId.getValue(), is(artifactId));
        Assert.assertThat(argGroupId.getValue(), is(groupId));
        Assert.assertThat(argVersion.getValue(), is(version));
    }

    @Test
    public void startUpByModuleName() {

        when(kieServices.newKieContainer(eq(null))).thenReturn(kieContainer);
        when(kieBuilder.getResults()).thenReturn(kieBuilderResults);
        when(kieBuilderResults.hasMessages(any(Message.Level.class))).thenReturn(true, false);
        when(kieServices.newKieBuilder(kfs)).thenReturn(kieBuilder);
        when(kieServices.newReleaseId(eq(null), eq(null), eq(null))).thenReturn(releaseId);
        when(releaseId.getVersion()).thenReturn("version.1.0");
        DroolsKIEModuleModel droolsKIEModuleModel = new DroolsKIEModuleModel();
        droolsKIEModuleModel.setName("promotion-module");
        when(droolsKIEModuleRepository.findByName("promotion-module")).thenReturn(droolsKIEModuleModel);

        bootstrap.startup("promotion-module");
        verify(droolsKIEModuleRepository).save(any(DroolsKIEModuleModel.class));
    }

    @Test
    public void startup() {
        ResultsImpl results = new ResultsImpl();
        results.addMessage(Message.Level.INFO, "", "");
        when(kieModuleModel.newKieBaseModel(anyString())).thenReturn(baseKieSessionModel);
        when(kieServices.newKieContainer(eq(null))).thenReturn(kieContainer);
        when(kieBuilder.getResults()).thenReturn(results);
        when(kieBuilderResults.hasMessages(any(Message.Level.class))).thenReturn(true, false);
        when(kieServices.newKieBuilder(kfs)).thenReturn(kieBuilder);
        when(kieServices.newReleaseId(eq(null), eq(null), eq(null))).thenReturn(releaseId);
        when(releaseId.getVersion()).thenReturn("version.1.0");
        when(droolsKIEModuleRepository.findByName(anyString())).thenReturn(new DroolsKIEModuleModel());
        DroolsKIEModuleModel droolsKIEModuleModel = new DroolsKIEModuleModel();
        droolsKIEModuleModel.setName("promotion-module");
        when(droolsKIEModuleRepository.findAll()).thenReturn(Arrays.asList(droolsKIEModuleModel));
        DroolsKIEBaseModel droolsKIEBaseModel = new DroolsKIEBaseModel();
        droolsKIEBaseModel.setName("base");
        when(droolKIEBaseRepository.findAllByDroolsKIEModule(droolsKIEModuleModel)).thenReturn(Arrays.asList(droolsKIEBaseModel));

        bootstrap.activeRuleModule();
        verify(droolsKIEModuleRepository).save(any(DroolsKIEModuleModel.class));
    }

    @Test
    public void testSwitchKieModuleExecutePostTasks()
    {
        when(droolsKIESessionRepository.findAllByDroolsKIEBase(any(DroolsKIEBaseModel.class))).thenReturn(Arrays.asList(kieSession));
        final LinkedList<Supplier<Object>> postTaskList = newLinkedList();
        postTaskList.addAll(asList(() -> "task1", () -> "task2"));                           // NOSONAR
        final List<Object> resultsAccumulator = bootstrap.switchKieModule(droolsModule,
                kieContainerListener, postTaskList, false, ruleEngineActionResult);
        verify(kieContainerListener).onSuccess(kieContainer, cache);
        assertThat(resultsAccumulator).isNotNull().hasSize(2).containsSequence("task1", "task2");
    }

    @Test
    public void testSwitchKieModuleAsyncExecutePostTasksWhenCompleted() throws InterruptedException
    {
        when(droolsKIEModuleRepository.findByName(MODULE_NAME)).thenReturn(droolsModule);
        when(droolsKIESessionRepository.findAllByDroolsKIEBase(any(DroolsKIEBaseModel.class))).thenReturn(Arrays.asList(kieSession));
        final List<Object> resultsAccumulator = newArrayList();
        final LinkedList<Supplier<Object>> postTaskList = newLinkedList();
        postTaskList.addAll(asList(() -> "task1", () -> "task2"));
        Thread tenantAwareThread = createTenantAwareThread(resultsAccumulator, postTaskList);
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(tenantAwareThread);
        bootstrap.switchKieModuleAsync(MODULE_NAME, kieContainerListener, resultsAccumulator,
                () -> "task2", postTaskList, false, ruleEngineActionResult);
        tenantAwareThread.join();
        verify(kieContainerListener).onSuccess(kieContainer, cache);
        assertThat(resultsAccumulator).isNotNull().hasSize(2).containsSequence("task1", "task2");
    }

    @Test
    public void testSwitchKieModuleAsyncIncrementalUpdate() throws InterruptedException
    {

        when(droolsKIEModuleRepository.findByName(MODULE_NAME)).thenReturn(droolsModule);
        when(droolsKIESessionRepository.findAllByDroolsKIEBase(any(DroolsKIEBaseModel.class))).thenReturn(Arrays.asList(kieSession));
        setUpForIncrementalRuleEngineUpdate();

        final List<Object> resultsAccumulator = newArrayList();

        final LinkedList<Supplier<Object>> postTaskList = newLinkedList();
        postTaskList.addAll(asList(() -> "task1", () -> "task2"));
        final Thread tenantAwareThread = createTenantAwareThread(resultsAccumulator, postTaskList);
        when(threadFactory.newThread(any(Runnable.class))).thenReturn(tenantAwareThread);

        bootstrap.switchKieModuleAsync(MODULE_NAME, kieContainerListener, resultsAccumulator,
                () -> "task2", postTaskList, true, ruleEngineActionResult);
        tenantAwareThread.join();
        verify(kieModule).getMemoryFileSystem();
        verify(incrementalKieModule).getMemoryFileSystem();
        verify(kieContainerListener).onSuccess(kieContainer, cache);
        assertThat(resultsAccumulator).isNotNull().hasSize(2).containsSequence("task1", "task2");
    }

    private Thread createTenantAwareThread(final List<Object> resultsAccumulator, final LinkedList<Supplier<Object>> postTaskList)
    {
        return new Thread(bootstrap.switchKieModuleRunnableTask(MODULE_NAME, kieContainerListener,
                resultsAccumulator, () -> "resetFlag", postTaskList, true, ruleEngineActionResult));
    }

    private void setUpForIncrementalRuleEngineUpdate()
    {
        when(droolsModule.getMvnVersion()).thenReturn("NEW_MVN_VERSION");
        when(droolsModule.getVersion()).thenReturn(1L);
        when(droolsModule.getDeployedMvnVersion()).thenReturn(DEPLOYED_VERSION);
        when(kieRepository.getKieModule(releaseId)).thenReturn(kieModule);
        final ReleaseId newReleaseId = mock(ReleaseId.class);
        when(kieServices.newReleaseId(anyString(), anyString(), eq("NEW_MVN_VERSION.1"))).thenReturn(newReleaseId);
        when(newReleaseId.getVersion()).thenReturn("NEW_MVN_VERSION.1");
        final DroolsKIEBaseModel kieBase = mock(DroolsKIEBaseModel.class);
        when(kieBase.getDroolsKIEModule()).thenReturn(droolsModule);
        when(kieBase.getName()).thenReturn("KIE_BASE");
        final KieBaseModel kieBaseModel = mock(KieBaseModel.class);
        KieSessionModel kieSessionModel = mock(KieSessionModel.class);
        when(kieBaseModel.newKieSessionModel(anyString())).thenReturn(kieSessionModel);
        when(kieModuleModel.newKieBaseModel("KIE_BASE")).thenReturn(kieBaseModel);
        when(droolKIEBaseRepository.findAllByDroolsKIEModule(droolsModule))
                .thenReturn(Collections.singletonList(kieBase));

        final DroolsRuleModel rule = mock(DroolsRuleModel.class);
        when(rule.isActive()).thenReturn(Boolean.TRUE);
        when(rule.isCurrentVersion()).thenReturn(Boolean.TRUE);
        when(rule.getRuleContent()).thenReturn("RULE_CONTENT");
        when(droolsRuleRepository.findAllByKieBaseAndActiveAndDate(anyLong(), any(Date.class)))
                .thenReturn(Collections.singletonList(rule));

        when(rule.getUuid()).thenReturn("RULE_UUID");
        when(rule.getCode()).thenReturn("RULE_CODE");
        when(contentMatchRulesFilter.apply(anyList(), eq(1L)))
                .thenReturn(ImmutablePair.of(Collections.singletonList(rule),
                        Lists.emptyList()));
        when(incrementalRuleEngineUpdateStrategy
                .shouldUpdateIncrementally(eq(releaseId), eq(MODULE_NAME), anyCollection(), anyCollection()))
                .thenReturn(Boolean.TRUE);
        when(kieModuleModel.toXML()).thenReturn("<xml/>");
        final InternalKieBuilder incrKieBuilder = mock(InternalKieBuilder.class);
        final KieBuilderSet kieBuilderSet = mock(KieBuilderSet.class);
        final IncrementalResults incrementalResults = mock(IncrementalResults.class);
        when(incrKieBuilder.createFileSet(any())).thenReturn(kieBuilderSet);
        when(incrKieBuilder.getKieModule()).thenReturn(incrementalKieModule);
        final Results incrBuilderResults = mock(Results.class);
        when(incrKieBuilder.getResults()).thenReturn(incrBuilderResults);
        when(kieBuilderSet.build()).thenReturn(incrementalResults);
        when(kieServices.newKieBuilder(any(KieFileSystem.class))).thenReturn(incrKieBuilder);
        final MemoryFileSystem incrMemoryFileSystem = mock(MemoryFileSystem.class);
        when(incrementalKieModule.getMemoryFileSystem()).thenReturn(incrMemoryFileSystem);
        when(kieServices.newKieContainer(newReleaseId)).thenReturn(kieContainer);
    }
}
