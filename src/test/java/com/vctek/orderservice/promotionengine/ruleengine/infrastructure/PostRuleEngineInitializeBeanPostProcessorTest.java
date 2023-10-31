package com.vctek.orderservice.promotionengine.ruleengine.infrastructure;

import com.vctek.orderservice.promotionengine.ruleengine.cache.impl.DefaultRuleGlobalsBeanProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PostRuleEngineInitializeBeanPostProcessorTest {
    private PostRuleEngineInitializeBeanPostProcessor processor;

    @Mock
    private ConfigurableListableBeanFactory beanFactory;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        processor = new PostRuleEngineInitializeBeanPostProcessor();
        processor.setBeanFactory(beanFactory);
    }

    @Test
    public void postProcessAfterInitialization() {
        processor.postProcessBeforeInitialization(new DefaultRuleGlobalsBeanProvider(), "defaultRuleGlobalsBeanProvider");
        assertEquals(1, processor.getRuleGlobalsAwareBeans().size());
        assertEquals(1, processor.getRuleGlobalsRetrievalMethods().size());

        Object bean = processor.postProcessAfterInitialization(new DefaultRuleGlobalsBeanProvider(),
                "defaultRuleGlobalsBeanProvider");
        assertNotNull(bean);
    }

}
