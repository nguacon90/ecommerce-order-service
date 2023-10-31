package com.vctek.orderservice.promotionengine.ruleengine.init.impl;

import com.google.common.base.Preconditions;
import com.vctek.orderservice.promotionengine.ruleengine.init.RuleEngineContainerRegistry;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class DefaultRuleEngineContainerRegistry implements RuleEngineContainerRegistry<ReleaseId, KieContainer> {
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock;
    private final Lock writeLock;

    @Value("${ruleengine.kiemodule.swapping.workers.initialcapacity:3}")
    private int initialCapacity = 3;

    @Value("${ruleengine.kiemodule.swapping.workers.loadfactor:0.75f}")
    private float loadFactor = 0.75f;

    @Value("${ruleengine.kiemodule.swapping.workers.concurrencylevel:2}")
    private int concurrencyLevel = 2;

    private Map<ReleaseId, KieContainer> kieContainerMap;

    public DefaultRuleEngineContainerRegistry() {
        this.readLock = this.readWriteLock.readLock();
        this.writeLock = this.readWriteLock.writeLock();
    }

    @PostConstruct
    public void setup() {
        this.kieContainerMap = new ConcurrentHashMap(initialCapacity, loadFactor, concurrencyLevel);
    }

    @Override
    public void lockReadingRegistry() {
        this.readLock.lock();
    }

    @Override
    public void unlockReadingRegistry() {
        this.readLock.unlock();
    }

    @Override
    public KieContainer getActiveContainer(ReleaseId deployedReleaseId) {
        return this.kieContainerMap.get(deployedReleaseId);
    }

    @Override
    public Optional<ReleaseId> lookupForDeployedRelease(String... releaseTokens) {
        Preconditions.checkArgument(Objects.nonNull(releaseTokens), "Lookup release tokens should be provided");
        return releaseTokens.length == 2 ? this.kieContainerMap.keySet().stream()
                .filter((rid) -> rid.getGroupId().equals(releaseTokens[0]) && rid.getArtifactId().equals(releaseTokens[1]))
                .findFirst() : Optional.empty();
    }

    public void setActiveContainer(ReleaseId releaseId, KieContainer rulesContainer) {
        this.kieContainerMap.put(releaseId, rulesContainer);
    }

    public KieContainer removeActiveContainer(ReleaseId releaseHolder) {
        return this.kieContainerMap.remove(releaseHolder);
    }

    public boolean isLockedForReading() {
        return this.readWriteLock.getReadLockCount() > 0;
    }

    public boolean isLockedForWriting() {
        return this.readWriteLock.isWriteLocked();
    }

    public void lockWritingRegistry() {
        this.writeLock.lock();
    }

    public void unlockWritingRegistry() {
        this.writeLock.unlock();
    }

}
