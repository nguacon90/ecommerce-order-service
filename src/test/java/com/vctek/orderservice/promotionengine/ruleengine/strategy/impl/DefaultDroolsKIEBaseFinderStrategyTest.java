package com.vctek.orderservice.promotionengine.ruleengine.strategy.impl;

import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEBaseModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.repository.DroolKIEBaseRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public class DefaultDroolsKIEBaseFinderStrategyTest {
    private DefaultDroolsKIEBaseFinderStrategy strategy;
    @Mock
    private DroolKIEBaseRepository kieBaseRepository;
    @Mock
    private DroolsKIEModuleModel kieModuleModel;
    @Mock
    private DroolsKIEBaseModel kieBase;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        strategy = new DefaultDroolsKIEBaseFinderStrategy(kieBaseRepository);
    }

    @Test
    public void getKIEBaseForKIEModule_ReturnNull() {
        when(kieBaseRepository.findAllByDroolsKIEModule(kieModuleModel)).thenReturn(new ArrayList<>());
        DroolsKIEBaseModel actual = strategy.getKIEBaseForKIEModule(kieModuleModel);
        assertNull(actual);
    }

    @Test
    public void getKIEBaseForKIEModule() {
        when(kieBaseRepository.findAllByDroolsKIEModule(kieModuleModel)).thenReturn(Arrays.asList(kieBase));
        DroolsKIEBaseModel actual = strategy.getKIEBaseForKIEModule(kieModuleModel);
        assertEquals(kieBase, actual);
    }
}
