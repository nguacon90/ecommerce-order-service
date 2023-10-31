package com.vctek.orderservice.promotionengine.promotionservice.service;

import com.vctek.orderservice.dto.ConsumeBudgetParam;

import java.math.BigDecimal;
import java.util.Map;

public interface PromotionBudgetConsumeService {
    BigDecimal calculateConsumeBudgetAmount(ConsumeBudgetParam param);

    Map<Long, Double> calculateConsumedBudgetOfSourceRules(ConsumeBudgetParam param);
}
