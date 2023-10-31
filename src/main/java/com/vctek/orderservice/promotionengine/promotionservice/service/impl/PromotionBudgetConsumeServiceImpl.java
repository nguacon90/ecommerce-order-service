package com.vctek.orderservice.promotionengine.promotionservice.service.impl;

import com.vctek.orderservice.dto.ConsumeBudgetParam;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.mapper.ConsumeBudgetMapper;
import com.vctek.orderservice.promotionengine.promotionservice.repository.PromotionBudgetConsumeRepository;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionBudgetConsumeService;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionSourceRuleService;
import com.vctek.orderservice.promotionengine.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PromotionBudgetConsumeServiceImpl implements PromotionBudgetConsumeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionBudgetConsumeServiceImpl.class);
    private PromotionBudgetConsumeRepository promotionBudgetConsumeRepository;
    private PromotionSourceRuleService promotionSourceRuleService;

    public PromotionBudgetConsumeServiceImpl(PromotionBudgetConsumeRepository promotionBudgetConsumeRepository) {
        this.promotionBudgetConsumeRepository = promotionBudgetConsumeRepository;
    }

    @Override
    public BigDecimal calculateConsumeBudgetAmount(ConsumeBudgetParam param) {
        PromotionSourceRuleModel promotionSourceRuleModel = promotionSourceRuleService.findByCode(param.getRuleCode());
        if(promotionSourceRuleModel == null) {
            LOGGER.warn("Not found source rule for calculate consume budget: ruleCode: {}", param.getRuleCode());
            return BigDecimal.ZERO;
        }
        int month = CommonUtils.getMonth(param.getCreatedOrderDate());
        int year = CommonUtils.getYear(param.getCreatedOrderDate());
        BigDecimal bigDecimal = promotionBudgetConsumeRepository.sumAllBudgetConsume(param.getCustomerId(), promotionSourceRuleModel.getId(), month, year);
        return bigDecimal == null ? BigDecimal.ZERO : bigDecimal;
    }

    @Override
    public Map<Long, Double> calculateConsumedBudgetOfSourceRules(ConsumeBudgetParam param) {
        Map<Long, Double> consumeBudgetMap = new HashMap<>();
        int month = CommonUtils.getMonth(param.getCreatedOrderDate());
        int year = CommonUtils.getYear(param.getCreatedOrderDate());
        List<ConsumeBudgetMapper> consumeBudgetMappers = promotionBudgetConsumeRepository.sumByPromotionSourceRulesOf(param.getCustomerId(), param.getSourceRuleIds(), month, year);
        consumeBudgetMappers.forEach(b -> consumeBudgetMap.put(b.getSourceRuleId().longValue(), b.getTotalDiscountAmount()));
        return consumeBudgetMap;
    }

    @Autowired
    public void setPromotionSourceRuleService(PromotionSourceRuleService promotionSourceRuleService) {
        this.promotionSourceRuleService = promotionSourceRuleService;
    }
}
