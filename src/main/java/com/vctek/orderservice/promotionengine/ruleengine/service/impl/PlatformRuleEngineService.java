package com.vctek.orderservice.promotionengine.ruleengine.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.vctek.orderservice.promotionengine.ruleengine.*;
import com.vctek.orderservice.promotionengine.ruleengine.cache.KIEModuleCacheBuilder;
import com.vctek.orderservice.promotionengine.ruleengine.cache.RuleEngineCacheService;
import com.vctek.orderservice.promotionengine.ruleengine.drools.KieSessionHelper;
import com.vctek.orderservice.promotionengine.ruleengine.enums.KIESessionType;
import com.vctek.orderservice.promotionengine.ruleengine.exception.DroolsInitializationException;
import com.vctek.orderservice.promotionengine.ruleengine.init.InitializationFuture;
import com.vctek.orderservice.promotionengine.ruleengine.init.MultiFlag;
import com.vctek.orderservice.promotionengine.ruleengine.init.RuleEngineBootstrap;
import com.vctek.orderservice.promotionengine.ruleengine.init.RuleEngineContainerRegistry;
import com.vctek.orderservice.promotionengine.ruleengine.init.task.PostRulesModuleSwappingTasksProvider;
import com.vctek.orderservice.promotionengine.ruleengine.model.AbstractRuleModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleEngineContextModel;
import com.vctek.orderservice.promotionengine.ruleengine.service.RuleEngineService;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.kie.api.builder.ReleaseId;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.internal.command.CommandFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Supplier;

@Service("platformRuleEngineService")
public class PlatformRuleEngineService implements RuleEngineService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformRuleEngineService.class);
    private RuleEngineContainerRegistry<ReleaseId, KieContainer> ruleEngineContainerRegistry;
    private KieSessionHelper kieSessionHelper;
    private RuleEngineCacheService ruleEngineCacheService;
    private RuleEngineBootstrap ruleEngineBootstrap;
    private MultiFlag initialisationMultiFlag;
    private PostRulesModuleSwappingTasksProvider postRulesModuleSwappingTasksProvider;

    public PlatformRuleEngineService(RuleEngineContainerRegistry<ReleaseId, KieContainer> ruleEngineContainerRegistry,
                                     KieSessionHelper kieSessionHelper,
                                     @Qualifier("commerceRuleEngineCacheService") RuleEngineCacheService ruleEngineCacheService) {
        this.ruleEngineContainerRegistry = ruleEngineContainerRegistry;
        this.kieSessionHelper = kieSessionHelper;
        this.ruleEngineCacheService = ruleEngineCacheService;
    }

    @Override
    public RuleEvaluationResult evaluate(RuleEvaluationContext context) {
        try {
            ruleEngineContainerRegistry.lockReadingRegistry();
            RuleEvaluationResult result = new RuleEvaluationResult();
            ReleaseId deployedReleaseId = this.kieSessionHelper.getDeployedKieModuleReleaseId(context);
            KieContainer kContainer = this.ruleEngineContainerRegistry.getActiveContainer(deployedReleaseId);
            if (Objects.isNull(kContainer)) {
                LOGGER.info("KieContainer with releaseId [{}] was not found. " +
                        "Trying to look up for closest matching version...", deployedReleaseId);
                String[] releaseTokens = {deployedReleaseId.getGroupId(), deployedReleaseId.getArtifactId()};
                ReleaseId tryDeployedReleaseId = this.ruleEngineContainerRegistry.lookupForDeployedRelease(releaseTokens)
                        .orElseThrow(() -> new DroolsInitializationException("Cannot complete the evaluation: " +
                                "rule engine was not initialized for releaseId [" + deployedReleaseId + "]"));
                LOGGER.info("Found KieContainer with releaseId [{}]", tryDeployedReleaseId);
                kContainer = this.ruleEngineContainerRegistry.getActiveContainer(tryDeployedReleaseId);
            }

            this.ruleEngineCacheService.provideCachedEntities(context);
            List<Command> commands = new ArrayList<>();
            commands.add(CommandFactory.newInsertElements(context.getFacts()));
            FireAllRulesCommand fireAllRulesCommand = new FireAllRulesCommand();
//            LOGGER.info("Adding command [{}]", fireAllRulesCommand);
            commands.add(fireAllRulesCommand);
            BatchExecutionCommand command = CommandFactory.newBatchExecution(commands);
            DroolsRuleEngineContextModel ruleEngineContext = (DroolsRuleEngineContextModel) context.getRuleEngineContext();
            KIESessionType sessionType = KIESessionType.getValueOf(ruleEngineContext.getKieSession().getSessionType());
            Supplier<ExecutionResults> executionResultsSupplier = KIESessionType.STATEFUL.equals(sessionType) ?
                    this.executionResultsSupplierWithStatefulSession(kContainer, command, context) :
                    this.executionResultsSupplierWithStatelessSession(kContainer, command, context);
            result.setExecutionResult(executionResultsSupplier.get());
            return result;
        } finally {
            ruleEngineContainerRegistry.unlockReadingRegistry();
        }
    }

    @Override
    public InitializationFuture initialize(List<AbstractRuleModuleModel> modules, boolean enableIncrementalUpdate,
                                           ExecutionContext executionContext) {
        InitializationFuture initializationFuture = InitializationFuture.of(this.ruleEngineBootstrap);

        Iterator var7 = modules.iterator();

        while(var7.hasNext()) {
            AbstractRuleModuleModel module = (AbstractRuleModuleModel)var7.next();
            RuleEngineActionResult result = new RuleEngineActionResult();
            result.setExecutionContext(executionContext);
            this.initialize(module, null, enableIncrementalUpdate, result);
            initializationFuture.getResults().add(result);
        }

        return initializationFuture;
    }

    protected void initialize(AbstractRuleModuleModel abstractModule, String deployedMvnVersion, boolean enableIncrementalUpdate, RuleEngineActionResult result) {
        this.initializeNonBlocking(abstractModule, deployedMvnVersion, enableIncrementalUpdate, result);
    }

    protected void initializeNonBlocking(AbstractRuleModuleModel abstractModule, final String deployedMvnVersion,
                                      boolean enableIncrementalUpdate, final RuleEngineActionResult result) {
            Preconditions.checkArgument(Objects.nonNull(abstractModule), "module must not be null");
            Preconditions.checkState(abstractModule instanceof DroolsKIEModuleModel,
                    "module %s is not a DroolsKIEModule. this is not supported.", abstractModule.getName());
            LOGGER.debug("Drools Engine Service initialization for '{}' module triggered...", abstractModule.getName());
            final DroolsKIEModuleModel moduleModel = (DroolsKIEModuleModel)abstractModule;
            final Optional<ReleaseId> oldDeployedReleaseId = this.ruleEngineBootstrap.getDeployedReleaseId(moduleModel, deployedMvnVersion);
            final String oldVersion = oldDeployedReleaseId.map(ReleaseId::getVersion).orElse("NONE");
            result.setOldVersion(oldVersion);
            this.switchKieModule(moduleModel, new KieContainerListener() {
                public void onSuccess(KieContainer kieContainer, KIEModuleCacheBuilder cache) {
                    PlatformRuleEngineService.this.doSwapKieContainers(kieContainer, cache, result, moduleModel, deployedMvnVersion);
                }

                public void onFailure(RuleEngineActionResult resultx) {
                    PlatformRuleEngineService.LOGGER.error("Kie Containers initialisation/swapping failed: {}",
                            resultx.getMessagesAsString(MessageLevel.ERROR));
                    resultx.setDeployedVersion(oldVersion);
                }
            }, enableIncrementalUpdate, result, this.postRulesModuleSwappingTasksProvider.getTasks(result));

    }

    private void doSwapKieContainers(KieContainer kieContainer, KIEModuleCacheBuilder cache, RuleEngineActionResult result,
                                     DroolsKIEModuleModel moduleModel, String deployedMvnVersion) {
        this.ruleEngineContainerRegistry.lockWritingRegistry();

        try {
            this.ruleEngineBootstrap.activateNewRuleEngineContainer(kieContainer, cache, result, moduleModel, deployedMvnVersion);
        } finally {
            this.ruleEngineContainerRegistry.unlockWritingRegistry();
        }

        LOGGER.info("Swapping to a newly created container [{}] is finished successfully", kieContainer.getReleaseId());
    }

    protected void switchKieModule(DroolsKIEModuleModel module, KieContainerListener listener,
                                   boolean enableIncrementalUpdate, RuleEngineActionResult result,
                                   Collection<Supplier<Object>> chainOfPostTasks) {
        String moduleName = module.getName();
        if (this.initialisationMultiFlag.compareAndSet(moduleName, false, true)) {
            Supplier resetFlagSupplier = () -> this.initialisationMultiFlag.compareAndSet(moduleName, true, false);

            try {
                List<Object> resultAccumulator = Lists.newArrayList();
                LinkedList<Supplier<Object>> postTaskList = Lists.newLinkedList();
                if (Objects.nonNull(chainOfPostTasks)) {
                    postTaskList.addAll(chainOfPostTasks);
                }

                postTaskList.addLast(resetFlagSupplier);
                this.ruleEngineBootstrap.switchKieModuleAsync(moduleName, listener, resultAccumulator,
                        resetFlagSupplier, postTaskList, enableIncrementalUpdate, result);
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                resetFlagSupplier.get();
                listener.onFailure(result);
            }

        } else {
            LOGGER.error("Kie containers swapping is in progress, no rules updates are possible at this time");
            throw new DroolsInitializationException("Kie containers swapping is in progress, " +
                    "no rules updates are possible at this time");
        }
    }

    protected Supplier<ExecutionResults> executionResultsSupplierWithStatefulSession(KieContainer kContainer,
                                                                                     BatchExecutionCommand command, RuleEvaluationContext context) {
        return () -> {
            KieSession kieSession = (KieSession) this.kieSessionHelper.initializeSession(KieSession.class, context, kContainer);
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("Executing KieSession.execute for releaseId [{}]", kContainer.getReleaseId());
            }

            ExecutionResults executionResults;
            try {
                executionResults = kieSession.execute(command);
            } finally {
                LOGGER.debug("Disposing the session: {}", kieSession);
                kieSession.dispose();
            }

            return executionResults;
        };
    }

    protected Supplier<ExecutionResults> executionResultsSupplierWithStatelessSession(KieContainer kContainer,
                                                                                      BatchExecutionCommand command, RuleEvaluationContext context) {
        return () -> {
            StatelessKieSession statelessKieSession = (StatelessKieSession)this.kieSessionHelper.initializeSession(StatelessKieSession.class, context, kContainer);
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("Executing StatelessKieSession.execute for releaseId [{}]", kContainer.getReleaseId());
            }
            return (ExecutionResults)statelessKieSession.execute(command);
        };
    }

    @PostConstruct
    public void setup() {
        this.initialisationMultiFlag = new MultiFlag();
    }

    @Autowired
    public void setRuleEngineBootstrap(RuleEngineBootstrap ruleEngineBootstrap) {
        this.ruleEngineBootstrap = ruleEngineBootstrap;
    }

    @Autowired
    public void setPostRulesModuleSwappingTasksProvider(PostRulesModuleSwappingTasksProvider postRulesModuleSwappingTasksProvider) {
        this.postRulesModuleSwappingTasksProvider = postRulesModuleSwappingTasksProvider;
    }
}
