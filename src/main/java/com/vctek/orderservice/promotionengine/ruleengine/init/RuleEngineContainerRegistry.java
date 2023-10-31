package com.vctek.orderservice.promotionengine.ruleengine.init;

import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;

import java.util.Optional;

public interface RuleEngineContainerRegistry<RELEASEHOLDER, CONTAINER> {
    void lockReadingRegistry();

    void unlockReadingRegistry();

    CONTAINER getActiveContainer(RELEASEHOLDER deployedReleaseId);

    Optional<RELEASEHOLDER> lookupForDeployedRelease(String... releaseTokens);

    void setActiveContainer(RELEASEHOLDER releaseId, CONTAINER kieContainer);

    KieContainer removeActiveContainer(ReleaseId releaseId);

    void lockWritingRegistry();

    void unlockWritingRegistry();

}
