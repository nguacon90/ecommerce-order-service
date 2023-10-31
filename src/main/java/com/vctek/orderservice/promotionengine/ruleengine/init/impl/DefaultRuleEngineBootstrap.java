package com.vctek.orderservice.promotionengine.ruleengine.init.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.vctek.orderservice.promotionengine.ruleengine.KieContainerListener;
import com.vctek.orderservice.promotionengine.ruleengine.MessageLevel;
import com.vctek.orderservice.promotionengine.ruleengine.ResultItem;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEngineActionResult;
import com.vctek.orderservice.promotionengine.ruleengine.cache.KIEModuleCacheBuilder;
import com.vctek.orderservice.promotionengine.ruleengine.cache.RuleEngineCacheService;
import com.vctek.orderservice.promotionengine.ruleengine.concurrency.TaskResultState;
import com.vctek.orderservice.promotionengine.ruleengine.enums.KIESessionType;
import com.vctek.orderservice.promotionengine.ruleengine.exception.DroolsInitializationException;
import com.vctek.orderservice.promotionengine.ruleengine.init.*;
import com.vctek.orderservice.promotionengine.ruleengine.model.*;
import com.vctek.orderservice.promotionengine.ruleengine.repository.DroolKIEBaseRepository;
import com.vctek.orderservice.promotionengine.ruleengine.repository.DroolsKIEModuleRepository;
import com.vctek.orderservice.promotionengine.ruleengine.repository.DroolsKIESessionRepository;
import com.vctek.orderservice.promotionengine.ruleengine.repository.DroolsRuleRepository;
import com.vctek.orderservice.promotionengine.ruleengine.util.RuleEngineUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.drools.compiler.compiler.io.memory.MemoryFileSystem;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieFileSystemImpl;
import org.drools.compiler.kie.builder.impl.MemoryKieModule;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class DefaultRuleEngineBootstrap implements RuleEngineBootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRuleEngineBootstrap.class);
    private DroolsKIEModuleRepository droolsKIEModuleRepository;
    private DroolKIEBaseRepository droolKIEBaseRepository;
    private DroolsKIESessionRepository droolsKIESessionRepository;
    private DroolsRuleRepository droolsRuleRepository;
    private KieServices kieServices;
    private RuleEngineCacheService ruleEngineCacheService;
    private RuleEngineContainerRegistry<ReleaseId, KieContainer> ruleEngineContainerRegistry;
    private Map<String, Set<Thread>> asyncWorkers;
    private ContentMatchRulesFilter contentMatchRulesFilter;
    private long workerPreDestroyTimeout;
    private IncrementalRuleEngineUpdateStrategy incrementalRuleEngineUpdateStrategy;
    private RulePublishingSpliterator rulePublishingSpliterator;
    private ThreadFactory threadFactory;

    public DefaultRuleEngineBootstrap(DroolsKIEModuleRepository droolsKIEModuleRepository,
                                      DroolKIEBaseRepository droolKIEBaseRepository,
                                      DroolsKIESessionRepository droolsKIESessionRepository,
                                      DroolsRuleRepository droolsRuleRepository,
                                      @Qualifier("commerceRuleEngineCacheService") RuleEngineCacheService ruleEngineCacheService) {
        this.droolsKIEModuleRepository = droolsKIEModuleRepository;
        this.droolKIEBaseRepository = droolKIEBaseRepository;
        this.droolsKIESessionRepository = droolsKIESessionRepository;
        this.droolsRuleRepository = droolsRuleRepository;
        this.ruleEngineCacheService = ruleEngineCacheService;
    }

    @Override
    public KieServices getEngineServices() {
        return this.kieServices;
    }

    @Override
    public RuleEngineActionResult startup(String moduleName) {
        Preconditions.checkArgument(Objects.nonNull(moduleName), "Module name should be provided");
        DroolsKIEModuleModel ruleModule = this.droolsKIEModuleRepository.findByName(moduleName);
        return this.startup(ruleModule);
    }

    @Override
    public RuleEngineActionResult startup(DroolsKIEModuleModel ruleModule) {
        RuleEngineActionResult result = new RuleEngineActionResult();
        result.setActionFailed(true);
        if (Objects.nonNull(ruleModule)) {
            result.setActionFailed(false);
            Pair<KieModule, KIEModuleCacheBuilder> kieModuleCacheBuilderPair = this.createKieModule(ruleModule, result);
            KieContainer kieContainer = this.initializeNewKieContainer(ruleModule, kieModuleCacheBuilderPair.getLeft(), result);
            this.warmUpRuleEngineContainer(ruleModule, kieContainer);
            this.activateNewRuleEngineContainer(kieContainer, kieModuleCacheBuilderPair.getRight(), result, ruleModule, null);
        }

        return result;
    }

    @Override
    public Optional<ReleaseId> getDeployedReleaseId(DroolsKIEModuleModel module, String deployedMvnVersion) {
        String deployedReleaseIdVersion = deployedMvnVersion == null ? module.getDeployedMvnVersion() : deployedMvnVersion;
        Optional<ReleaseId> deployedReleaseId = Optional.empty();
        if (Objects.nonNull(this.getKieServices()) && Objects.nonNull(deployedReleaseIdVersion)) {
            deployedReleaseId = Optional.of(this.getKieServices().newReleaseId(module.getMvnGroupId(),
                    module.getMvnArtifactId(), deployedReleaseIdVersion));
        }
        return deployedReleaseId;
    }

    private KieContainer initializeNewKieContainer(DroolsKIEModuleModel module, KieModule kieModule, RuleEngineActionResult result) {
        ReleaseId releaseId = this.getReleaseId(module);
        result.setModuleName(module.getName());
        KieRepository kieRepository = this.getKieServices().getRepository();
        LOGGER.info("Drools Engine Service initialization for '{}' module finished. ReleaseId of the new Kie Module: '{}'",
                new Object[]{module.getName(), kieModule.getReleaseId().toExternalForm()});
        kieRepository.addKieModule(kieModule);
        KieContainer kieContainer = this.getKieServices().newKieContainer(releaseId);
        result.setDeployedVersion(kieContainer.getReleaseId().getVersion());
        return kieContainer;
    }

    public ReleaseId getReleaseId(DroolsKIEModuleModel module) {
            String moduleVersion = module.getDeployedMvnVersion();
            return this.getKieServices().newReleaseId(module.getMvnGroupId(), module.getMvnArtifactId(), moduleVersion);
    }

    protected Pair<KieModule, KIEModuleCacheBuilder> createKieModule(DroolsKIEModuleModel module, RuleEngineActionResult result) {
        Preconditions.checkArgument(module != null, "module must not be null");
        KIEModuleCacheBuilder cache = this.ruleEngineCacheService.createKIEModuleCacheBuilder(module);
        Collection<DroolsKIEBaseModel> kieBases = droolKIEBaseRepository.findAllByDroolsKIEModule(module);
        Preconditions.checkArgument(kieBases != null, "kieBases in the module must not be null");
        KieModuleModel kieModuleModel = this.getKieServices().newKieModuleModel();
        KieFileSystem kfs = this.getKieServices().newKieFileSystem();
        for(DroolsKIEBaseModel base : kieBases) {
            this.addKieBase(kieModuleModel, kfs, base, cache);
        }
        this.writeKModuleXML(kieModuleModel, kfs);
        this.writePomXML(module, kfs);
        KieBuilder kieBuilder = this.getKieServices().newKieBuilder(kfs);
        kieBuilder.buildAll();
        Results results = kieBuilder.getResults();
        if (results.hasMessages(new Message.Level[]{Message.Level.ERROR, Message.Level.WARNING, Message.Level.INFO})) {
            Iterator var9 = results.getMessages().iterator();
            while(var9.hasNext()) {
                Message message = (Message)var9.next();
                LOGGER.error("{} {} {}", new Object[]{message.getLevel(), message.getText(), message.getPath()});
                ResultItem item = this.addNewResultItemOf(result, this.convertLevel(message.getLevel()), message.getText());
                item.setLine(message.getLine());
                item.setPath(message.getPath());
                if (Message.Level.ERROR.equals(message.getLevel())) {
                    result.setActionFailed(true);
                }
            }

            if (results.hasMessages(new Message.Level[]{Message.Level.ERROR})) {
                throw new DroolsInitializationException(result.getResults(), "Drools rule engine initialization failed");
            }
        }
        return Pair.of(kieBuilder.getKieModule(), cache);
    }

    @Override
    public void waitForSwappingToFinish() {
        this.asyncWorkers.entrySet().stream()
                .flatMap(e -> ((Set)e.getValue()).stream())
                .forEach(t -> this.waitWhileWorkerIsRunning((Thread) t));
    }

    @Override
    public void switchKieModuleAsync(String moduleName, KieContainerListener listener,
                                     List<Object> resultAccumulator, Supplier resetFlagSupplier,
                                     LinkedList<Supplier<Object>> postTaskList, boolean enableIncrementalUpdate,
                                     RuleEngineActionResult result) {
        this.waitForSwappingToFinish(moduleName);
        Thread asyncWorker = threadFactory.newThread(this.switchKieModuleRunnableTask(moduleName, listener,
                resultAccumulator, resetFlagSupplier, postTaskList, enableIncrementalUpdate, result));
        asyncWorker.setName(getNextWorkerName());
        asyncWorker.start();
        this.registerWorker(moduleName, asyncWorker);
    }

    protected void registerWorker(String moduleName, Thread worker) {
        Set<Thread> workersForModule = this.asyncWorkers.get(moduleName);
        ImmutableSet updatedWorkersForModule;
        if (Objects.isNull(workersForModule)) {
            updatedWorkersForModule = ImmutableSet.of(worker);
        } else {
            Set<Thread> aliveWorkers = workersForModule.stream().filter(Thread::isAlive).collect(Collectors.toSet());
            aliveWorkers.add(worker);
            updatedWorkersForModule = ImmutableSet.copyOf(aliveWorkers);
        }

        this.asyncWorkers.put(moduleName, updatedWorkersForModule);
    }

    protected String getNextWorkerName() {
        long nextActiveOrder = 0L;
        if (MapUtils.isNotEmpty(this.asyncWorkers)) {
            nextActiveOrder = this.asyncWorkers.entrySet().stream()
                    .flatMap(e -> ((Set)e.getValue()).stream())
                    .filter(w -> Objects.nonNull(w) && ((Thread)w).isAlive()).count();
        }

        return "RuleEngine-module-swapping-" + nextActiveOrder;
    }

    protected Runnable switchKieModuleRunnableTask(String moduleName, KieContainerListener listener,
                                                   List<Object> resultsAccumulator, Supplier<Object> resetFlagSupplier,
                                                   List<Supplier<Object>> postTaskList, boolean enableIncrementalUpdate,
                                                   RuleEngineActionResult result) {
        return () -> {
            result.setModuleName(moduleName);

            try {
                DroolsKIEModuleModel module = droolsKIEModuleRepository.findByName(moduleName);
                resultsAccumulator.addAll(this.switchKieModule(module, listener, (LinkedList)postTaskList,
                        enableIncrementalUpdate, result));
            } catch (Exception var9) {
                resetFlagSupplier.get();
                this.onSwapFailed(var9, result, resetFlagSupplier);
                result.setActionFailed(true);
                listener.onFailure(result);
            }
        };
    }

    protected Object onSwapFailed(Throwable t, RuleEngineActionResult result, Supplier<Object> resetFlagSupplier) {
        LOGGER.error("Exception caught: {}", t);
        this.addNewResultItemOf(result, MessageLevel.ERROR, t.getLocalizedMessage());
        return Objects.nonNull(resetFlagSupplier) ? resetFlagSupplier.get() : null;
    }


    public List<Object> switchKieModule(DroolsKIEModuleModel module, KieContainerListener listener,
                                        LinkedList<Supplier<Object>> postTaskList, boolean enableIncrementalUpdate,
                                        RuleEngineActionResult result) {
        ArrayList resultsAccumulator = Lists.newArrayList();
        boolean var9 = true;

        try {
            this.initializeNewModule(module, listener, enableIncrementalUpdate, result);
            var9 = false;
        } finally {
            if (var9) {
                postTaskList.forEach((pt) -> resultsAccumulator.add(pt.get()));
            }
        }

        postTaskList.forEach((pt) -> {
            resultsAccumulator.add(pt.get());
        });
        return resultsAccumulator;
    }

    protected void initializeNewModule(DroolsKIEModuleModel module, KieContainerListener listener,
                                       boolean enableIncrementalUpdates, RuleEngineActionResult result) {
        try {
            Pair<KieModule, KIEModuleCacheBuilder> moduleCacheBuilderPair = this.createKieModule(module, result, enableIncrementalUpdates);
            KieModule newKieModule = moduleCacheBuilderPair.getLeft();
            KIEModuleCacheBuilder cache = moduleCacheBuilderPair.getRight();
            listener.onSuccess(this.initializeNewKieContainer(module, newKieModule, result), cache);
        } catch (DroolsInitializationException var8) {
            LOGGER.error("DroolsInitializationException occured {}", var8);
            result.setResults(var8.getResults());
            this.completeWithFailure(this.getReleaseId(module), result, listener);
        } catch (RuntimeException var9) {
            LOGGER.error("Drools Engine Service initialization Exception occured {}", var9);
            this.addNewResultItemOf(result, MessageLevel.ERROR, var9.getLocalizedMessage());
            this.completeWithFailure(this.getReleaseId(module), result, listener);
        }

    }

    protected void completeWithFailure(ReleaseId releaseId, RuleEngineActionResult result, KieContainerListener listener) {
        KieRepository kieRepository = this.getKieServices().getRepository();
        KieModule corruptedKieModule = kieRepository.getKieModule(releaseId);
        if (Objects.nonNull(corruptedKieModule)) {
            kieRepository.removeKieModule(releaseId);
        }

        result.setActionFailed(true);
        listener.onFailure(result);
    }

    protected void waitForSwappingToFinish(String moduleName) {
        this.asyncWorkers.entrySet().stream()
                .filter((e) -> e.getKey().equals(moduleName))
                .flatMap((e) -> ((Set)e.getValue()).stream())
                .forEach(t -> this.waitWhileWorkerIsRunning((Thread) t));
    }

    protected void waitWhileWorkerIsRunning(Thread worker) {
        if (Objects.nonNull(worker) && worker.isAlive()) {
            try {
                LOGGER.info("Waiting for a currently running async worker to finish the job...");
                worker.join(this.getWorkerPreDestroyTimeout());
            } catch (InterruptedException var3) {
                Thread.currentThread().interrupt();
                LOGGER.debug("Interrupted exception is caught during async Kie container swap: {}", var3);
            }
        }

    }

    @PreDestroy
    public void beforeDestroy() {
        this.waitForSwappingToFinish();
    }

    protected Pair<KieModule, KIEModuleCacheBuilder> createKieModule(DroolsKIEModuleModel module, RuleEngineActionResult result, boolean enableIncrementalUpdate) {
        Collection<DroolsKIEBaseModel> kieBases = droolKIEBaseRepository.findAllByDroolsKIEModule(module);
        KieModuleModel kieModuleModel = this.getKieServices().newKieModuleModel();
        kieBases.forEach((base) -> {
            this.addKieBase(kieModuleModel, base);
        });
        KIEModuleCacheBuilder cache = this.ruleEngineCacheService.createKIEModuleCacheBuilder(module);
        ReleaseId newReleaseId = this.getReleaseId(module);
        Optional<ReleaseId> deployedReleaseId = this.getDeployedReleaseId(module, null);
        KieModule deployedKieModule;
        if (enableIncrementalUpdate && deployedReleaseId.isPresent()) {
            ReleaseId currentReleaseId = deployedReleaseId.get();
            deployedKieModule = this.getKieServices().getRepository().getKieModule(currentReleaseId);
            if (Objects.nonNull(deployedKieModule)) {
                List<Pair<Collection<DroolsRuleModel>, Collection<DroolsRuleModel>>> rulesToUpdateList = kieBases.stream()
                        .map((kBase) -> this.prepareIncrementalUpdate(currentReleaseId, kBase))
                        .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
                List<DroolsRuleModel> rulesToAdd = (List)rulesToUpdateList.stream().flatMap((u) -> {
                    return ((Collection)u.getLeft()).stream();
                }).collect(Collectors.toList());
                List<DroolsRuleModel> rulesToRemove = (List)rulesToUpdateList.stream().flatMap((u) -> {
                    return ((Collection)u.getRight()).stream();
                }).collect(Collectors.toList());
                if (this.incrementalRuleEngineUpdateStrategy.shouldUpdateIncrementally(currentReleaseId, module.getName(), rulesToAdd, rulesToRemove)) {
                    KieModule cloneKieModule = this.cloneForIncrementalCompilation((MemoryKieModule)deployedKieModule, newReleaseId, kieModuleModel);
                    kieBases.forEach((kBase) -> {
                        kBase.getRules().forEach(cache::processRule);
                    });
                    rulesToUpdateList.forEach((u) -> {
                        this.deployRulesIncrementally(newReleaseId, kieModuleModel, cloneKieModule, u.getLeft(), u.getRight(), result);
                    });
                    KieModule kieModule = this.mergePartialKieModules(newReleaseId, kieModuleModel, cloneKieModule);
                    return Pair.of(kieModule, cache);
                }
            }
        }

        List<KieBuilder> kieBuilders = kieBases.stream().flatMap((base) ->
                this.deployRules(module, kieModuleModel, base, cache).stream()).collect(Collectors.toList());
        deployedKieModule = this.mergePartialKieModules(newReleaseId, kieModuleModel, kieBuilders);
        return Pair.of(deployedKieModule, cache);
    }

    protected Optional<Pair<Collection<DroolsRuleModel>, Collection<DroolsRuleModel>>> prepareIncrementalUpdate(ReleaseId releaseId, DroolsKIEBaseModel kieBase) {
        List<DroolsRuleModel> rules = droolsRuleRepository.findAllByKieBaseAndActiveAndDate(kieBase.getId(), Calendar.getInstance().getTime());
        Long newModuleVersion = kieBase.getDroolsKIEModule().getVersion();
        Optional<Pair<Collection<DroolsRuleModel>, Collection<DroolsRuleModel>>> rulesToDeploy = Optional.empty();
        if (CollectionUtils.isNotEmpty(rules)) {
            Pair<Collection<DroolsRuleModel>, Collection<DroolsRuleModel>> matchingRules =
                    this.contentMatchRulesFilter.apply(rules, newModuleVersion);
            return Optional.of(matchingRules);
        }

        return rulesToDeploy;
    }

    protected List<KieBuilder> deployRules(DroolsKIEModuleModel module, KieModuleModel kieModuleModel,
                                           DroolsKIEBaseModel kieBase, KIEModuleCacheBuilder cache) {
        List<DroolsRuleModel> rules = droolsRuleRepository.findAllByKieBaseAndActiveAndDate(kieBase.getId(),
                Calendar.getInstance().getTime());
        List<String> rulesUuids = rules.stream().map(AbstractRuleEngineRuleModel::getUuid).collect(Collectors.toList());
        RulePublishingFuture rulePublishingFuture = this.rulePublishingSpliterator.publishRulesAsync(kieModuleModel, this.getReleaseId(module), rulesUuids, cache);
        RuleDeploymentTaskResult ruleDeploymentResult = (RuleDeploymentTaskResult)rulePublishingFuture.getTaskResult();
        if (ruleDeploymentResult.getState().equals(TaskResultState.FAILURE)) {
            throw new DroolsInitializationException(ruleDeploymentResult.getRulePublishingResults().stream()
                    .filter((result) -> CollectionUtils.isNotEmpty(result.getResults()))
                    .flatMap((result) -> result.getResults().stream())
                    .collect(Collectors.toList()), "Initialization of rule engine failed during the deployment phase: ");
        } else {
            return rulePublishingFuture.getPartialKieBuilders();
        }
    }

    protected void deployRulesIncrementally(ReleaseId releaseId, KieModuleModel kieModuleModel, KieModule kieModule, Collection<DroolsRuleModel> rulesToAdd, Collection<DroolsRuleModel> rulesToRemove, RuleEngineActionResult result) {
        if (CollectionUtils.isNotEmpty(rulesToRemove)) {
            LOGGER.debug("Rules to remove: {}", rulesToRemove.size());
            this.deleteRulesFromKieModule((MemoryKieModule)kieModule, rulesToRemove);
        }

        if (CollectionUtils.isNotEmpty(rulesToAdd)) {
            MemoryFileSystem memoryFileSystem = ((MemoryKieModule)kieModule).getMemoryFileSystem();
            KieFileSystem kfs = new KieFileSystemImpl(memoryFileSystem);
            this.writeKModuleXML(kieModuleModel, kfs);
            kfs.generateAndWritePomXML(releaseId);
            KieBuilder kieBuilder = this.getKieServices().newKieBuilder(kfs);
            String[] ruleToAddPaths = this.getRulePaths(rulesToAdd);
            if (org.apache.commons.lang.ArrayUtils.isNotEmpty(ruleToAddPaths)) {
                LOGGER.debug("Rules to add: {}", rulesToAdd.size());
                this.writeRulesToKieFileSystem(kfs, rulesToAdd);
                KieBuilderSet kieBuilderSet = ((InternalKieBuilder)kieBuilder).createFileSet(ruleToAddPaths);
                Results kieBuilderResults = kieBuilder.getResults();
                List<Message> kieBuilderMessages = kieBuilderResults.getMessages(new Message.Level[]{Message.Level.ERROR});
                this.verifyErrors(result, kieBuilderMessages);
                IncrementalResults incrementalResults = kieBuilderSet.build();
                List<Message> messages = incrementalResults.getAddedMessages();
                this.verifyErrors(result, messages);
            }

            KieModule incrementalKieModule = kieBuilder.getKieModule();
            this.copyChanges((MemoryKieModule)kieModule, (MemoryKieModule)incrementalKieModule);
        }
    }

    protected void verifyErrors(RuleEngineActionResult result, List<Message> messages) {
        messages.stream().filter(m -> m.getLevel().equals(Message.Level.ERROR))
                .forEach(m -> this.addNewResultItemOf(result, MessageLevel.ERROR, m.getText()));
        if (messages.stream().anyMatch(m -> m.getLevel().equals(Message.Level.ERROR))) {
            throw new DroolsInitializationException(result.getResults(), "Drools rule engine initialization failed");
        }
    }

    protected void copyChanges(MemoryKieModule trgKieModule, MemoryKieModule srcKieModule) {
        MemoryFileSystem trgMemoryFileSystem = trgKieModule.getMemoryFileSystem();
        MemoryFileSystem srcMemoryFileSystem = srcKieModule.getMemoryFileSystem();
        Map<String, byte[]> compiledCode = srcMemoryFileSystem.getMap();
        Iterator var7 = compiledCode.entrySet().iterator();

        while(var7.hasNext()) {
            Map.Entry<String, byte[]> entry = (Map.Entry)var7.next();
            String path = entry.getKey();
            if (!path.startsWith(RuleEngineUtils.DROOLS_BASE_PATH)) {
                String resoursePath = RuleEngineUtils.DROOLS_BASE_PATH + path;
                String normalizedRulePath = RuleEngineUtils.getNormalizedRulePath(resoursePath);
                if (resoursePath.endsWith(".drl") && trgMemoryFileSystem.existsFile(normalizedRulePath)) {
                    LOGGER.debug("Removing file: {}", normalizedRulePath);
                    trgMemoryFileSystem.remove(normalizedRulePath);
                }

                trgMemoryFileSystem.write(path, entry.getValue());
            }
        }

        trgMemoryFileSystem.mark();
    }

    protected MemoryKieModule cloneForIncrementalCompilation(MemoryKieModule origKieModule, ReleaseId releaseId, KieModuleModel kModuleModel) {
        MemoryFileSystem newMfs = new MemoryFileSystem();
        MemoryKieModule clone = new MemoryKieModule(releaseId, kModuleModel, newMfs);
        MemoryFileSystem origMemoryFileSystem = origKieModule.getMemoryFileSystem();
        Map<String, byte[]> fileContents = origMemoryFileSystem.getMap();
        Iterator var9 = fileContents.entrySet().iterator();

        while(var9.hasNext()) {
            Map.Entry<String, byte[]> entry = (Map.Entry)var9.next();
            newMfs.write(entry.getKey(), entry.getValue());
        }

        clone.mark();
        var9 = origKieModule.getKieDependencies().values().iterator();

        InternalKieModule dependency;
        while(var9.hasNext()) {
            dependency = (InternalKieModule)var9.next();
            clone.addKieDependency(dependency);
        }

        var9 = origKieModule.getKieModuleModel().getKieBaseModels().values().iterator();

        while(var9.hasNext()) {
            KieBaseModel kBaseModel = (KieBaseModel)var9.next();
            clone.cacheKnowledgeBuilderForKieBase(kBaseModel.getName(), origKieModule.getKnowledgeBuilderForKieBase(kBaseModel.getName()));
        }

        clone.setPomModel(origKieModule.getPomModel());
        var9 = origKieModule.getKieDependencies().values().iterator();

        while(var9.hasNext()) {
            dependency = (InternalKieModule)var9.next();
            clone.addKieDependency(dependency);
        }

        clone.setUnresolvedDependencies(origKieModule.getUnresolvedDependencies());
        return clone;
    }

    protected KieModule mergePartialKieModules(ReleaseId releaseId, KieModuleModel kieModuleModel, List<KieBuilder> kieBuilders) {
        MemoryFileSystem mainMemoryFileSystem = new MemoryFileSystem();
        InternalKieModule returnKieModule = new MemoryKieModule(releaseId, kieModuleModel, mainMemoryFileSystem);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(kieBuilders)) {
            Iterator var7 = kieBuilders.iterator();

            while(var7.hasNext()) {
                KieBuilder kieBuilder = (KieBuilder)var7.next();
                KieModule partialKieModule = kieBuilder.getKieModule();
                this.mergeFileSystemToKieModule((MemoryKieModule)partialKieModule, mainMemoryFileSystem);
            }
        }

        mainMemoryFileSystem.mark();
        LOGGER.debug("Main KIE module contains [{}] files", mainMemoryFileSystem.getFileNames().size());
        return returnKieModule;
    }

    protected KieModule mergePartialKieModules(ReleaseId releaseId, KieModuleModel kieModuleModel, KieModule partialKieModule) {
        MemoryFileSystem mainMemoryFileSystem = new MemoryFileSystem();
        InternalKieModule returnKieModule = new MemoryKieModule(releaseId, kieModuleModel, mainMemoryFileSystem);
        this.mergeFileSystemToKieModule((MemoryKieModule)partialKieModule, mainMemoryFileSystem);
        mainMemoryFileSystem.mark();
        LOGGER.debug("Main KIE module contains [{}] files", mainMemoryFileSystem.getFileNames().size());
        return returnKieModule;
    }

    protected void mergeFileSystemToKieModule(MemoryKieModule partialKieModule, MemoryFileSystem mainMemoryFileSystem) {
        MemoryFileSystem partialMemoryFileSystem = partialKieModule.getMemoryFileSystem();
        Map<String, byte[]> fileContents = partialMemoryFileSystem.getMap();
        Iterator var6 = fileContents.entrySet().iterator();

        while(var6.hasNext()) {
            Map.Entry<String, byte[]> entry = (Map.Entry)var6.next();
            mainMemoryFileSystem.write(entry.getKey(), entry.getValue());
        }

    }

    private MessageLevel convertLevel(Message.Level level) {
        return MessageLevel.get(level.toString());
    }

    protected ResultItem addNewResultItemOf(RuleEngineActionResult result, MessageLevel messageLevel, String message) {
        ResultItem resultItem = new ResultItem();
        resultItem.setLevel(messageLevel);
        resultItem.setMessage(message);
        if (CollectionUtils.isEmpty(result.getResults())) {
            result.setResults(Lists.newCopyOnWriteArrayList());
        }

        result.getResults().add(resultItem);
        return resultItem;
    }

    public void writeKModuleXML(KieModuleModel module, KieFileSystem kfs) {
        kfs.writeKModuleXML(module.toXML());
    }

    public void writePomXML(DroolsKIEModuleModel module, KieFileSystem kfs) {
        ReleaseId releaseId = this.getReleaseId(module);
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Writing POM for releaseId: {}", releaseId.toExternalForm());
        }
        kfs.generateAndWritePomXML(releaseId);
    }

    public void addKieBase(KieModuleModel module, KieFileSystem kfs, DroolsKIEBaseModel base, KIEModuleCacheBuilder cache) {
        this.addKieBase(module, base);
        this.addRules(kfs, base, cache);
    }

    public void addKieBase(KieModuleModel module, DroolsKIEBaseModel base) {
        KieBaseModel kieBaseModel = module.newKieBaseModel(base.getName());
        kieBaseModel.setEqualsBehavior(EqualityBehaviorOption.EQUALITY);
        kieBaseModel.setEventProcessingMode(EventProcessingOption.STREAM);
        List<DroolsKIESessionModel> kieSessions = droolsKIESessionRepository.findAllByDroolsKIEBase(base);
        if(!CollectionUtils.isEmpty(kieSessions)) {
            kieSessions.forEach((session) -> this.addKieSession(kieBaseModel, base, session));
        }
    }

    public void addKieSession(KieBaseModel base, DroolsKIEBaseModel kieBase, DroolsKIESessionModel session) {
        KieSessionModel kieSession = base.newKieSessionModel(session.getName());
        DroolsKIESessionModel defaultKIESession = kieBase.getDefaultKieSession();
        if (Objects.nonNull(defaultKIESession)) {
            Long id = defaultKIESession.getId();
            if (Objects.nonNull(id)) {
                kieSession.setDefault(id.equals(session.getId()));
            }
        }

        KieSessionModel.KieSessionType sessionType = this.getSessionType(session.getSessionType());
        kieSession.setType(sessionType);
    }

    private KieSessionModel.KieSessionType getSessionType(String sessionType) {
        if(KIESessionType.STATEFUL.toString().equalsIgnoreCase(sessionType)) {
            return KieSessionModel.KieSessionType.STATEFUL;
        }
        return KieSessionModel.KieSessionType.STATELESS;
    }

    public void addRules(KieFileSystem kfs, DroolsKIEBaseModel base, KIEModuleCacheBuilder cache) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Drools Engine Service addRules triggered...");
        }
        List<DroolsRuleModel> rules = droolsRuleRepository.findAllByKieBaseAndActiveAndDate(base.getId(),
                Calendar.getInstance().getTime());
//        Set<DroolsRuleModel> latestRules = filterByBiggestVersion(rules);
        this.writeRulesToKieFileSystem(kfs, rules);
        rules.stream().filter(this::isRuleValid).forEach(cache::processRule);
    }

    protected Set<DroolsRuleModel> filterByBiggestVersion(List<DroolsRuleModel> rulesForVersion) {
        Set<DroolsRuleModel> rules = Sets.newHashSet();
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(rulesForVersion)) {
            Map<String, List<DroolsRuleModel>> twinRulesMap = rulesForVersion.stream().collect(Collectors.groupingBy(AbstractRuleEngineRuleModel::getCode));
            twinRulesMap.values().stream().map(Collection::stream)
                    .forEach(l -> l.max(Comparator.comparing(AbstractRuleEngineRuleModel::getVersion))
                    .ifPresent(rules::add));
        }

        return rules;
    }

    protected void writeRulesToKieFileSystem(KieFileSystem kfs, Collection<DroolsRuleModel> rules) {
        Iterator var4 = rules.iterator();

        while(var4.hasNext()) {
            DroolsRuleModel rule = (DroolsRuleModel)var4.next();
            if (this.isRuleValid(rule)) {
                String rulePath = RuleEngineUtils.getRulePath(rule);
                String drl = rule.getRuleContent();
                LOGGER.debug("{} {}", rule.getCode(), rulePath);
                LOGGER.debug("{}", drl);
                kfs.write(rulePath, drl);
            }

            if (BooleanUtils.isNotTrue(rule.isActive())) {
                LOGGER.debug("ignoring rule {}. Rule is not active.", rule.getCode());
            } else if (Objects.isNull(rule.getRuleContent())) {
                LOGGER.warn("ignoring rule {}. No ruleContent set!", rule.getCode());
            }
        }

    }

    protected void deleteRulesFromKieModule(MemoryKieModule kieModule, Collection<DroolsRuleModel> rules) {
        String[] rulePaths = this.getRulePaths(rules);
        if (ArrayUtils.isNotEmpty(rulePaths)) {
            MemoryFileSystem memoryFileSystem = kieModule.getMemoryFileSystem();
            Arrays.stream(rulePaths).map(RuleEngineUtils::stripDroolsMainResources).forEach((p) -> deleteFileIfExists(memoryFileSystem, p));
        }

    }

    private static void deleteFileIfExists(MemoryFileSystem mfs, String path) {
        if (mfs.existsFile(path)) {
            mfs.remove(path);
        }

    }

    private String[] getRulePaths(Collection<DroolsRuleModel> rules) {
        return (String[])((List)rules.stream().filter(this::isRuleValid).map(RuleEngineUtils::getRulePath)
                .collect(Collectors.toList())).toArray(new String[0]);
    }

    protected boolean isRuleValid(DroolsRuleModel rule) {
        return Objects.nonNull(rule.getRuleContent()) && BooleanUtils.isTrue(rule.isActive());
    }

    @Override
    public void warmUpRuleEngineContainer(DroolsKIEModuleModel rulesModule, KieContainer rulesContainer) {
        Preconditions.checkArgument(Objects.nonNull(rulesContainer), "rulesContainer should not be null");
        Collection<DroolsKIEBaseModel> kieBases = droolKIEBaseRepository.findAllByDroolsKIEModule(rulesModule);
        if (!CollectionUtils.isEmpty(kieBases) && kieBases.size() == 1) {
            DroolsKIEBaseModel kieBase = kieBases.iterator().next();
            DroolsKIESessionModel defaultKIESession = kieBase.getDefaultKieSession();
            if (Objects.nonNull(defaultKIESession)) {
                String kieSessionName = defaultKIESession.getName();
                if (KIESessionType.STATEFUL.toString().equals(defaultKIESession.getSessionType())) {
                    LOGGER.info("Initializing and disposing the session to optimize the tree...");
                    rulesContainer.newKieSession(kieSessionName).dispose();
                } else {
                    LOGGER.info("Initializing the stateless session to optimize the tree...");
                    rulesContainer.newStatelessKieSession(kieSessionName);
                }
            }
        }
    }

    @Override
    public void activateNewRuleEngineContainer(KieContainer kieContainer, KIEModuleCacheBuilder cache,
                                               RuleEngineActionResult ruleEngineActionResult,
                                               DroolsKIEModuleModel rulesModule, String deployedReleaseIdVersion) {
        ReleaseId releaseId = kieContainer.getReleaseId();
        Optional<ReleaseId> deployedReleaseId = this.getDeployedReleaseId(rulesModule, deployedReleaseIdVersion);
        String deployedMvnVersion = this.activateKieModule(rulesModule);
        LOGGER.info("The new module with deployedMvnVersion [{}] was activated successfully", rulesModule.getDeployedMvnVersion());
        LOGGER.info("Swapping to a new created container [{}]", releaseId);
        this.getRuleEngineContainerRegistry().setActiveContainer(releaseId, kieContainer);
        this.ruleEngineCacheService.addToCache(cache);
        deployedReleaseId.filter((r) -> !releaseId.getVersion().equals(r.getVersion()))
                .ifPresent(this.getRuleEngineContainerRegistry()::removeActiveContainer);
        ruleEngineActionResult.setDeployedVersion(deployedMvnVersion);
    }

    public String activateKieModule(DroolsKIEModuleModel module) {
        String releaseIdVersion = this.getReleaseId(module).getVersion();
        module.setDeployedMvnVersion(releaseIdVersion);
        droolsKIEModuleRepository.save(module);
        return releaseIdVersion;
    }

    @PostConstruct
    public void activeRuleModule() {
        setUpKieServices();
        this.asyncWorkers = new ConcurrentHashMap<>(3, 0.75f, 2);
        List<DroolsKIEModuleModel> kieModules = droolsKIEModuleRepository.findAll();
        if(!CollectionUtils.isEmpty(kieModules)) {
            for(DroolsKIEModuleModel module : kieModules) {
                this.startup(module);
            }
        }
    }

    private void setUpKieServices() {
        if (Objects.isNull(this.kieServices)) {
            this.kieServices = KieServices.Factory.get();
        }

    }

    public KieServices getKieServices() {
        return kieServices;
    }

    public void setKieServices(KieServices kieServices) {
        this.kieServices = kieServices;
    }

    @Autowired
    public void setRuleEngineContainerRegistry(RuleEngineContainerRegistry<ReleaseId, KieContainer> ruleEngineContainerRegistry) {
        this.ruleEngineContainerRegistry = ruleEngineContainerRegistry;
    }

    public RuleEngineContainerRegistry<ReleaseId, KieContainer> getRuleEngineContainerRegistry() {
        return ruleEngineContainerRegistry;
    }

    public long getWorkerPreDestroyTimeout() {
        return workerPreDestroyTimeout;
    }

    @Value("${vctek.config.workerPreDestroyTimeout:3600000}")
    public void setWorkerPreDestroyTimeout(long workerPreDestroyTimeout) {
        this.workerPreDestroyTimeout = workerPreDestroyTimeout;
    }

    @Autowired
    public void setContentMatchRulesFilter(ContentMatchRulesFilter contentMatchRulesFilter) {
        this.contentMatchRulesFilter = contentMatchRulesFilter;
    }

    @Autowired
    public void setIncrementalRuleEngineUpdateStrategy(IncrementalRuleEngineUpdateStrategy incrementalRuleEngineUpdateStrategy) {
        this.incrementalRuleEngineUpdateStrategy = incrementalRuleEngineUpdateStrategy;
    }

    @Autowired
    public void setRulePublishingSpliterator(RulePublishingSpliterator rulePublishingSpliterator) {
        this.rulePublishingSpliterator = rulePublishingSpliterator;
    }

    @Autowired
    @Qualifier("defaultAwareThreadFactory")
    public void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }
}
