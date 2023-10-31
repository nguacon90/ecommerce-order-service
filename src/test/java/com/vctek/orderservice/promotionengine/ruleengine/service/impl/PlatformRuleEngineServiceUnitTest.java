package com.vctek.orderservice.promotionengine.ruleengine.service.impl;

import com.vctek.orderservice.promotionengine.ruleengine.ExecutionContext;
import com.vctek.orderservice.promotionengine.ruleengine.KieContainerListener;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEngineActionResult;
import com.vctek.orderservice.promotionengine.ruleengine.cache.KIEModuleCacheBuilder;
import com.vctek.orderservice.promotionengine.ruleengine.cache.RuleEngineCacheService;
import com.vctek.orderservice.promotionengine.ruleengine.drools.KieSessionHelper;
import com.vctek.orderservice.promotionengine.ruleengine.init.MultiFlag;
import com.vctek.orderservice.promotionengine.ruleengine.init.RuleEngineContainerRegistry;
import com.vctek.orderservice.promotionengine.ruleengine.init.impl.DefaultRuleEngineBootstrap;
import com.vctek.orderservice.promotionengine.ruleengine.init.task.PostRulesModuleSwappingTasksProvider;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.compiler.kie.builder.impl.MemoryKieModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.runtime.KieContainer;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Supplier;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;


public class PlatformRuleEngineServiceUnitTest {

    private PlatformRuleEngineService service;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    private KieServices kieServices;
    @Mock
    private KieFileSystem kieFileSystem;
    @Mock
    private KieModuleModel kieModuleModel;
    @Mock
    private KieRepository kieRepository;
    @Mock
    private ReleaseId releaseId;
    @Mock
    private KieBuilder kieBuilder;
    @Mock
    private Results results;
    @Mock
    private RuleEngineCacheService ruleEngineCacheService;
    @Mock
    private KIEModuleCacheBuilder cache;
    @Mock
    private MemoryKieModule kieModule;
    @Mock
    private MemoryFileSystem memoryFileSystem;
    @Mock
    private RuleEngineContainerRegistry<ReleaseId, KieContainer> ruleEngineContainerRegistry;
    @Mock
    private DefaultRuleEngineBootstrap ruleEngineBootstrap;

    @Mock
    private KieSessionHelper kieSessionHelper;
    @Mock
    private MultiFlag initialisationMultiFlag;
    @Mock
    private PostRulesModuleSwappingTasksProvider postRulesModuleSwappingTasksProvider;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new PlatformRuleEngineService(ruleEngineContainerRegistry, kieSessionHelper, ruleEngineCacheService);
        service.setRuleEngineBootstrap(ruleEngineBootstrap);
        service.setPostRulesModuleSwappingTasksProvider(postRulesModuleSwappingTasksProvider);
        service.setup();
        when(ruleEngineCacheService.createKIEModuleCacheBuilder(any())).thenReturn(cache);
        //set up base configuration:
        when(kieBuilder.getResults()).thenReturn(results);
        when(kieServices.newKieModuleModel()).thenReturn(kieModuleModel);
        when(kieServices.newKieFileSystem()).thenReturn(kieFileSystem);
        when(kieServices.getRepository()).thenReturn(kieRepository);
        when(kieServices.newReleaseId(anyString(), anyString(), anyString())).thenReturn(releaseId);
        when(kieServices.newKieBuilder(any(KieFileSystem.class))).thenReturn(kieBuilder);

        when(kieBuilder.getKieModule()).thenReturn(kieModule);
        when(kieModule.getMemoryFileSystem()).thenReturn(memoryFileSystem);
    }


    @Test
    public void initialize() {
        final DroolsKIEModuleModel module = mock(DroolsKIEModuleModel.class);
        when(module.getName()).thenReturn("myModuleName");
        when(initialisationMultiFlag.compareAndSet(anyString(), anyBoolean(), anyBoolean())).thenReturn(true);
        service.initialize(Arrays.asList(module), true, new ExecutionContext());

        verify(ruleEngineBootstrap).switchKieModuleAsync(anyString(), any(KieContainerListener.class),
                anyList(), any(Supplier.class), any(LinkedList.class), anyBoolean(), any(RuleEngineActionResult.class));
    }

}
