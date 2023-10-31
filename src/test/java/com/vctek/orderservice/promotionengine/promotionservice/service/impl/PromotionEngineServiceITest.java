package com.vctek.orderservice.promotionengine.promotionservice.service.impl;

import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionResultModel;
import com.vctek.orderservice.promotionengine.promotionservice.result.PromotionOrderResults;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionEngineService;
import com.vctek.orderservice.service.CartService;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

@Ignore
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@TestPropertySource(properties={"spring.cloud.discovery.enabled=false", "eureka.client.enabled=false"})
@EnableAutoConfiguration(exclude = {EurekaClientAutoConfiguration.class})
public class PromotionEngineServiceITest {

    @Autowired
    private CartService cartService;

    @Autowired
    private PromotionEngineService promotionEngineService;

    @BeforeClass
    public static void setUp() {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    @Test
    @Transactional
    @Ignore
    public void applyPromotion() {
        CartModel order = cartService.findByCodeAndUserIdAndCompanyId("321060719", 1l, 1l);
        PromotionOrderResults results = promotionEngineService.updatePromotions(Collections.emptyList(), order);
        List<PromotionResultModel> promotionResults = results.getApplyAllActionsPromotionResults();
        assertEquals(2, promotionResults.size());
    }

}
