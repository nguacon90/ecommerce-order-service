package com.vctek.orderservice.promotionengine.promotionservice.service;


import com.vctek.dto.promotion.PromotionSourceRuleDTO;
import com.vctek.orderservice.dto.PromotionRuleSearchParam;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

public interface PromotionSourceRuleService {
    PromotionSourceRuleModel findById(Long id);

    PromotionSourceRuleModel save(PromotionSourceRuleModel model);

    PromotionSourceRuleModel findByCode(String code);

    List<PromotionSourceRuleModel> findAllByAppliedOnlyOneAndCodeIn(boolean appliedOnlyOne, List<String> codes);

    void updateAllActiveRuleStatus(String moduleName, RuleStatus published);

    void updateAllExpiredRuleToInActive(String moduleName);

    Page<PromotionSourceRuleModel> findAll(PromotionRuleSearchParam param);

    PromotionSourceRuleModel findByIdAndCompanyId(Long promotionId, Long companyId);

    boolean isExpired(PromotionSourceRuleModel sourceRuleModel);

    boolean hasCondition(PromotionSourceRuleDTO sourceRuleDTO, String conditionDefinitionId);

    Page<PromotionSourceRuleModel> findAllByCompanyId(Long companyId, Pageable pageable);

    List<PromotionSourceRuleModel> findAllByIdIn(List<Long> promotionSourceRuleIds);

    boolean isValidToAppliedForCart(PromotionSourceRuleModel sr, CartModel model);

    List<PromotionSourceRuleModel> findAllActiveOf(Long companyId, Date date);
}
