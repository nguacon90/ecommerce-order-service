package com.vctek.orderservice.promotionengine.ruleengine.service.impl;

import com.vctek.orderservice.promotionengine.ruleengine.RuleEvaluationContext;
import com.vctek.orderservice.promotionengine.ruleengine.cache.KIEModuleCacheBuilder;
import com.vctek.orderservice.promotionengine.ruleengine.cache.RuleEngineCacheService;
import com.vctek.orderservice.promotionengine.ruleengine.drools.impl.DefaultKieSessionHelper;
import com.vctek.orderservice.promotionengine.ruleengine.init.impl.DefaultRuleEngineBootstrap;
import com.vctek.orderservice.promotionengine.ruleengine.init.impl.DefaultRuleEngineContainerRegistry;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIESessionModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleEngineContextModel;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.compiler.kie.builder.impl.MemoryKieModule;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.AgendaFilter;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static com.vctek.orderservice.promotionengine.ruleengine.drools.impl.DefaultModuleReleaseIdAware.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class PlatformRuleEngineServiceEvaluateTest
{
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private static final String MODULE_NAME = "MODULE_NAME";
	private static final String MODULE_MVN_VERSION = "MODULE_MVN_VERSION";
	private static final String KIE_SESSION_NAME = "KIE_SESSION_NAME";

	@Mock
	private KieServices kieServices;

	@InjectMocks
	private PlatformRuleEngineService service;
	@Mock
	private DroolsKIEModuleModel droolsModule;
	@Mock
	private KieFileSystem kieFileSystem;
	@Mock
	private KieModuleModel kieModuleModel;
	@Mock
	private KieRepository kieRepository;
	@Mock
	private ReleaseId releaseId;
	@Mock
	private ReleaseId newReleaseId;
	@Mock
	private KieBuilder kieBuilder;
	@Mock
	private Results results;
	@Mock
	private KieModule oldKieModule;
	@Mock
	private KieContainer oldKieContainer;
	@Mock
	private KieContainer newKieContainer;
	@Mock
	private RuleEvaluationContext ruleEvaluationContext;
	@Mock
	private DroolsRuleEngineContextModel ruleEngineContext;
	@Mock
	private DroolsKIESessionModel kieSessionModel;
	@Mock
	private DroolsKIEBaseModel kieBase;
	@Mock
	private KieSession session;
	@Mock
	private RuleEngineCacheService ruleEngineCacheService;
	@Mock
	private KIEModuleCacheBuilder cache;
	@Mock
	private MemoryKieModule newKieModule;
	@Mock
	private AgendaFilter agendaFilter;

	@InjectMocks
	private DefaultRuleEngineBootstrap ruleEngineBootstrap;

	@Mock
	private DefaultRuleEngineContainerRegistry ruleEngineContainerRegistry;

	@Mock
	private DefaultKieSessionHelper kieSessionHelper;

	@Mock
	private MemoryFileSystem memoryFileSystem;

	@Before
	public void setUp()
	{
        MockitoAnnotations.initMocks(this);
		ruleEngineBootstrap.setRuleEngineContainerRegistry(ruleEngineContainerRegistry);
		when(kieBuilder.getResults()).thenReturn(results);
		when(kieBuilder.getKieModule()).thenReturn(newKieModule);
		when(kieServices.newKieModuleModel()).thenReturn(kieModuleModel);
		when(kieServices.newKieFileSystem()).thenReturn(kieFileSystem);
		when(kieRepository.getKieModule(any(ReleaseId.class))).thenReturn(oldKieModule);
		when(kieServices.getRepository()).thenReturn(kieRepository);
		when(kieServices.newReleaseId(anyString(), anyString(), eq(MODULE_MVN_VERSION + ".0"))).thenReturn(releaseId);
		when(kieServices.newReleaseId(anyString(), anyString(), eq(MODULE_MVN_VERSION + ".1"))).thenReturn(newReleaseId);
		when(kieServices.newKieBuilder(any(KieFileSystem.class))).thenReturn(kieBuilder);

		when(kieServices.newKieContainer(newReleaseId)).thenReturn(newKieContainer);

		when(newKieContainer.getReleaseId()).thenReturn(newReleaseId);
		when(droolsModule.getName()).thenReturn(MODULE_NAME);
		when(droolsModule.getDeployedMvnVersion()).thenReturn(MODULE_MVN_VERSION + ".0");
		when(newKieModule.getReleaseId()).thenReturn(newReleaseId);
		when(releaseId.getVersion()).thenReturn(MODULE_MVN_VERSION + ".0");
		when(newReleaseId.getVersion()).thenReturn(MODULE_MVN_VERSION + ".1");
		when(kieSessionModel.getName()).thenReturn(KIE_SESSION_NAME);
		when(ruleEvaluationContext.getRuleEngineContext()).thenReturn(ruleEngineContext);
		when(ruleEvaluationContext.getFilter()).thenReturn(agendaFilter);
		when(ruleEngineContext.getKieSession()).thenReturn(kieSessionModel);
		when(kieSessionModel.getSessionType()).thenReturn(KieSessionModel.KieSessionType.STATEFUL.toString());
		when(kieSessionModel.getDroolsKIEBase()).thenReturn(kieBase);
		when(kieBase.getDroolsKIEModule()).thenReturn(droolsModule);
		when(newKieContainer.newKieSession(anyString())).thenReturn(session);
		when(newKieModule.getMemoryFileSystem()).thenReturn(memoryFileSystem);

		when(droolsModule.getMvnVersion()).thenReturn(MODULE_MVN_VERSION);
		when(kieSessionHelper.getDeployedKieModuleReleaseId(ruleEvaluationContext))
                .thenReturn(new ReleaseIdImpl(DUMMY_GROUP, DUMMY_ARTIFACT, DUMMY_VERSION));

	}

	private void setUpOldContainer()
	{
		when(droolsModule.getDeployedMvnVersion()).thenReturn(null);
		when(kieServices.newReleaseId(anyString(), anyString(), eq(null))).thenReturn(null);
		when(kieServices.newKieContainer(releaseId)).thenReturn(oldKieContainer);
		when(oldKieContainer.getReleaseId()).thenReturn(releaseId);
	}

	private void setUpInitializedContainer()
	{
        when(ruleEngineContainerRegistry.lookupForDeployedRelease(any())).thenReturn(Optional.of(releaseId));
        when(ruleEngineCacheService.createKIEModuleCacheBuilder(droolsModule)).thenReturn(cache);
        //set up base configuration:
        when(ruleEngineContainerRegistry.getActiveContainer(releaseId)).thenReturn(oldKieContainer);
        when(kieSessionHelper.initializeSession(eq(KieSession.class),
                any(RuleEvaluationContext.class), any(KieContainer.class))).thenReturn(session);
		when(droolsModule.getDeployedMvnVersion()).thenReturn(MODULE_MVN_VERSION + ".0");
		when(kieServices.newReleaseId(anyString(), anyString(), eq(MODULE_MVN_VERSION + ".0"))).thenReturn(releaseId);
		when(kieServices.newKieContainer(releaseId)).thenReturn(oldKieContainer);
		when(oldKieContainer.getReleaseId()).thenReturn(releaseId);
		when(oldKieContainer.newKieSession(anyString())).thenReturn(session);
	}

	private void setUpNewContainer()
	{
		when(kieServices.newReleaseId(anyString(), anyString(), eq(MODULE_MVN_VERSION + ".1"))).thenReturn(newReleaseId);
		when(kieServices.newKieContainer(newReleaseId)).thenReturn(newKieContainer);
		when(newKieContainer.getReleaseId()).thenReturn(newReleaseId);
		when(newKieContainer.newKieSession(anyString())).thenReturn(session);
	}

	@Test
	public void testEvaluateNoInitialization()
	{
		expectedException.expectMessage(
				"Cannot complete the evaluation: rule engine was not initialized for releaseId [DUMMY_GROUP:DUMMY_ARTIFACT:DUMMY_VERSION]");
		setUpOldContainer();
		service.evaluate(ruleEvaluationContext);
	}

	@Test
	public void testEvaluateAfterInitialization()
	{
		setUpOldContainer();
		setUpInitializedContainer();
		service.evaluate(ruleEvaluationContext);
		verify(ruleEngineContainerRegistry).unlockReadingRegistry();
	}

	@Test
	public void testEvaluateAfterSecondInitialization()
	{
		setUpOldContainer();
		setUpInitializedContainer();
		when(droolsModule.getDeployedMvnVersion()).thenReturn(MODULE_MVN_VERSION + ".1");
		setUpNewContainer();
		service.evaluate(ruleEvaluationContext);
        verify(ruleEngineContainerRegistry).unlockReadingRegistry();
	}

}
