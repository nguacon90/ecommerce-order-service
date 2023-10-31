package com.vctek.orderservice.promotionengine.ruleengine.init.impl;

import org.drools.compiler.kproject.ReleaseIdImpl;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieContainer;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;


public class DefaultRuleEngineContainerRegistryUnitTest
{
	@Mock
	private KieContainer kieContainer;

	private static final String RELEASEID_GROUPID = "test-group";
	private static final String RELEASEID_ARTIFACTID = "test-artifact";
	private static final String RELEASEID_VERSION = "test-version";
	
	private DefaultRuleEngineContainerRegistry ruleEngineContainerRegistry;

	@Before
	public void setUp()
	{
	    MockitoAnnotations.initMocks(this);
		ruleEngineContainerRegistry = new DefaultRuleEngineContainerRegistry();
		ruleEngineContainerRegistry.setup();
	}

	@Test
	public void testSetGetActiveContainer()
	{
		final ReleaseIdImpl releaseId = new ReleaseIdImpl(RELEASEID_GROUPID, RELEASEID_ARTIFACTID, RELEASEID_VERSION);
		ruleEngineContainerRegistry.setActiveContainer(releaseId, kieContainer);

		assertThat(ruleEngineContainerRegistry.getActiveContainer(releaseId)).isEqualTo(kieContainer);
	}

	@Test
	public void testSetRemoveGetActiveContainer()
	{
		final ReleaseIdImpl releaseId = new ReleaseIdImpl(RELEASEID_GROUPID, RELEASEID_ARTIFACTID, RELEASEID_VERSION);
		ruleEngineContainerRegistry.setActiveContainer(releaseId, kieContainer);

		final KieContainer removedKieContainer = ruleEngineContainerRegistry.removeActiveContainer(releaseId);
		assertThat(removedKieContainer).isEqualTo(kieContainer);

		assertThat(ruleEngineContainerRegistry.getActiveContainer(releaseId)).isNull();
	}

	@Test
	public void  testLookupForDeployedRelease()
	{
		final ReleaseIdImpl releaseId1 = new ReleaseIdImpl("test_group1", "test_artifact1", RELEASEID_VERSION);  // NOSONAR
		final ReleaseIdImpl releaseId2 = new ReleaseIdImpl("test_group2", "test_artifact2", RELEASEID_VERSION);  // NOSONAR
		final KieContainer kieContainer1 = Mockito.mock(KieContainer.class);
		final KieContainer kieContainer2 = Mockito.mock(KieContainer.class);

		ruleEngineContainerRegistry.setActiveContainer(releaseId1, kieContainer1);
		ruleEngineContainerRegistry.setActiveContainer(releaseId2, kieContainer2);

		assertThat(ruleEngineContainerRegistry.lookupForDeployedRelease("test_group1", "test_artifact1")).isPresent().contains(releaseId1);
		assertThat(ruleEngineContainerRegistry.lookupForDeployedRelease("test_group2", "test_artifact2")).isPresent().contains(releaseId2);
		assertThat(ruleEngineContainerRegistry.lookupForDeployedRelease("test_group1", "test_artifact2")).isNotPresent();
	}

	@Test
	public void testLockReadingRegistry()
	{
		ruleEngineContainerRegistry.lockReadingRegistry();
		assertThat(ruleEngineContainerRegistry.isLockedForReading()).isTrue();

		ruleEngineContainerRegistry.unlockReadingRegistry();
		assertThat(ruleEngineContainerRegistry.isLockedForReading()).isFalse();
	}

	@Test
	public void testLockWritingRegistry()
	{
		ruleEngineContainerRegistry.lockWritingRegistry();
		assertThat(ruleEngineContainerRegistry.isLockedForWriting()).isTrue();

		ruleEngineContainerRegistry.unlockWritingRegistry();
		assertThat(ruleEngineContainerRegistry.isLockedForWriting()).isFalse();
	}

}
