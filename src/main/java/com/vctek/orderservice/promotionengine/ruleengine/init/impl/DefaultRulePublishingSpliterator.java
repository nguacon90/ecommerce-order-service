package com.vctek.orderservice.promotionengine.ruleengine.init.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.vctek.orderservice.promotionengine.ruleengine.MessageLevel;
import com.vctek.orderservice.promotionengine.ruleengine.ResultItem;
import com.vctek.orderservice.promotionengine.ruleengine.RuleEngineActionResult;
import com.vctek.orderservice.promotionengine.ruleengine.cache.KIEModuleCacheBuilder;
import com.vctek.orderservice.promotionengine.ruleengine.concurrency.TaskContext;
import com.vctek.orderservice.promotionengine.ruleengine.init.RuleEngineBootstrap;
import com.vctek.orderservice.promotionengine.ruleengine.init.RulePublishingSpliterator;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.service.DroolsRuleService;
import com.vctek.orderservice.promotionengine.ruleengine.util.RuleEngineUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.drools.compiler.kie.builder.impl.ResultsImpl;
import org.kie.api.KieServices;
import org.kie.api.builder.*;
import org.kie.api.builder.model.KieModuleModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class DefaultRulePublishingSpliterator implements RulePublishingSpliterator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRulePublishingSpliterator.class);
    private DroolsRuleService droolsRuleService;
    private RuleEngineBootstrap ruleEngineBootstrap;
    private TaskContext taskContext;

    @Override
    public RulePublishingFuture publishRulesAsync(KieModuleModel kieModuleModel, ReleaseId containerReleaseId, List<String> ruleUuids, KIEModuleCacheBuilder cache) {
        List<KieBuilder> kieBuilders = new CopyOnWriteArrayList();
        List<List<String>> partitionOfRulesUuids = this.splitListByThreads(ruleUuids, taskContext.getNumberOfThreads());
        Set<Thread> builderWorkers = Sets.newHashSet();
        List<RuleEngineActionResult> ruleEngineActionResults = Lists.newCopyOnWriteArrayList();
        Iterator var10 = partitionOfRulesUuids.iterator();

        while (var10.hasNext()) {
            List<String> ruleUuidsChunk = (List) var10.next();
            builderWorkers.add(this.createNewWorker(kieBuilders, kieModuleModel, containerReleaseId,
                    ruleUuidsChunk, ruleEngineActionResults, cache));
        }

        this.startWorkers(builderWorkers);
        return new RulePublishingFuture(builderWorkers, ruleEngineActionResults, kieBuilders, taskContext.getThreadTimeout());
    }

    public <T> List<List<T>> splitListByThreads(List<T> list, int numberOfThreads) {
        Preconditions.checkArgument(numberOfThreads > 0, "Valid maximum number of threads (>0) must be provided");
        int partitionSize = this.getPartitionSize(list.size(), numberOfThreads);
        return partitionSize == 0 ? Collections.emptyList() : Lists.partition(list, partitionSize);
    }

    protected Thread createNewWorker(List<KieBuilder> kieBuilders, KieModuleModel kieModuleModel, ReleaseId releaseId,
                                     List<String> ruleUuids, List<RuleEngineActionResult> ruleEngineActionResults,
                                     KIEModuleCacheBuilder cache) {
        return taskContext.getThreadFactory().newThread(() -> {
            RuleEngineActionResult result = this.addRulesBuilder(kieBuilders, kieModuleModel,
                            releaseId, ruleUuids, cache);
            ruleEngineActionResults.add(result);
        });
    }

    protected RuleEngineActionResult addRulesBuilder(List<KieBuilder> kieBuilders, KieModuleModel kieModuleModel, ReleaseId releaseId, List<String> ruleUuids, KIEModuleCacheBuilder cache) {
        List<DroolsRuleModel> droolRules = this.droolsRuleService.getRulesByUuids(ruleUuids);
        KieFileSystem partialKieFileSystem = this.getKieServices().newKieFileSystem();
        this.writeKModuleXML(kieModuleModel, partialKieFileSystem);
        this.writePomXML(releaseId, partialKieFileSystem);
        KieBuilder partialKieBuilder = this.getKieServices().newKieBuilder(partialKieFileSystem);
        Iterator var10 = droolRules.iterator();

        while(true) {
            while(var10.hasNext()) {
                DroolsRuleModel rule = (DroolsRuleModel)var10.next();
                if (Objects.nonNull(rule.getRuleContent()) && BooleanUtils.isTrue(rule.isActive())
                        && BooleanUtils.isTrue(rule.isCurrentVersion())) {
                    cache.processRule(rule);

                    try {
                        partialKieFileSystem.write(RuleEngineUtils.getRulePath(rule), rule.getRuleContent());
                    } catch (Exception var12) {
                        return this.createNewResult(this.createKieBuilderErrorResult(rule, var12));
                    }
                }

                logWarningRuleEngine(rule);
            }

            partialKieBuilder.buildAll();
            kieBuilders.add(partialKieBuilder);
            return this.createNewResult(partialKieBuilder.getResults());
        }
    }

    private void logWarningRuleEngine(DroolsRuleModel rule) {
        if (Objects.isNull(rule.getRuleContent())) {
            LOGGER.warn("ignoring rule {}. No ruleContent set!", rule.getCode());
        } else if (BooleanUtils.isNotTrue(rule.isActive()) || BooleanUtils.isNotTrue(rule.isCurrentVersion())) {
            LOGGER.warn("ignoring rule {}. Rule is not active or current version.", rule.getCode());
        }
    }

    private KieServices getKieServices() {
        return this.ruleEngineBootstrap.getEngineServices();
    }

    protected Results createKieBuilderErrorResult(DroolsRuleModel rule, Exception e) {
        ResultsImpl results = new ResultsImpl();
        results.addMessage(Message.Level.ERROR, rule.getCode(), "exception encountered during writing of kie file system:" + e.getMessage());
        return results;
    }

    protected RuleEngineActionResult createNewResult(Results results) {
        RuleEngineActionResult ruleEngineActionResult = new RuleEngineActionResult();
        Iterator var4 = results.getMessages().iterator();

        while(var4.hasNext()) {
            Message message = (Message)var4.next();
            LOGGER.error("{} {} {}", new Object[]{message.getLevel(), message.getText(), message.getPath()});
            ResultItem item = this.addNewResultItemOf(ruleEngineActionResult, this.convertLevel(message.getLevel()), message.getText());
            item.setLine(message.getLine());
            item.setPath(message.getPath());
        }

        if (results.hasMessages(new Message.Level[]{Message.Level.ERROR})) {
            ruleEngineActionResult.setActionFailed(true);
        }

        return ruleEngineActionResult;
    }

    protected MessageLevel convertLevel(Message.Level level) {
        if (level == null) {
            return null;
        } else {
            switch(level.ordinal()) {
                case 1:
                    return MessageLevel.ERROR;
                case 2:
                    return MessageLevel.WARNING;
                case 3:
                    return MessageLevel.INFO;
                default:
                    return null;
            }
        }
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

    protected void writeKModuleXML(KieModuleModel module, KieFileSystem kfs) {
        kfs.writeKModuleXML(module.toXML());
    }

    public void writePomXML(ReleaseId releaseId, KieFileSystem kfs) {
        kfs.generateAndWritePomXML(releaseId);
    }


    private int getPartitionSize(int totalSize, int numberOfPartitions) {
        int partitionSize = totalSize;
        if (numberOfPartitions > 1 && totalSize >= numberOfPartitions * numberOfPartitions) {
            if (totalSize % numberOfPartitions == 0) {
                partitionSize = totalSize / numberOfPartitions;
            } else if (numberOfPartitions > 1) {
                partitionSize = totalSize / (numberOfPartitions - 1);
            }
        }

        return partitionSize;
    }

    protected void startWorkers(Set<Thread> workers) {
        if (CollectionUtils.isNotEmpty(workers)) {
            Iterator var3 = workers.iterator();

            while (var3.hasNext()) {
                Thread worker = (Thread) var3.next();
                worker.setName("RulePublisher-" + worker.getName());
                worker.start();
            }
        }

    }

    @Autowired
    public void setDroolsRuleService(DroolsRuleService droolsRuleService) {
        this.droolsRuleService = droolsRuleService;
    }

    @Autowired
    public void setRuleEngineBootstrap(RuleEngineBootstrap ruleEngineBootstrap) {
        this.ruleEngineBootstrap = ruleEngineBootstrap;
    }

    @Autowired
    public void setTaskContext(TaskContext taskContext) {
        this.taskContext = taskContext;
    }
}
