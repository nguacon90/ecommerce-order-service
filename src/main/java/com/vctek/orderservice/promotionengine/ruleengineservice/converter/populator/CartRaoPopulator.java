package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.orderservice.dto.ConsumeBudgetParam;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionBudgetConsumeService;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.PromotionBudgetRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.UserRAO;
import com.vctek.orderservice.service.PromotionBudgetService;
import com.vctek.util.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component("cartRaoPopulator")
public class CartRaoPopulator extends AbstractOrderRaoPopulator<AbstractOrderModel, CartRAO> {
    private PromotionBudgetService promotionBudgetService;
    private PromotionBudgetConsumeService promotionBudgetConsumeService;

    @Override
    public void populate(AbstractOrderModel source, CartRAO target) {
        super.populate(source, target);
        target.setActions(new LinkedHashSet());
        target.setOriginalTotal(target.getTotal());
        populateBudget(target);
    }

    private void populateBudget(CartRAO target) {
        UserRAO user = target.getUser();
        if(user == null || CollectionUtils.isEmpty(user.getGroups())) {
            return;
        }
        List<Long> userGroupIds = user.getGroups().stream().map(g -> g.getId()).collect(Collectors.toList());
        List<PromotionBudgetRAO> promotionBudgetRAOList = promotionBudgetService.findAllOf(userGroupIds);
        if(CollectionUtils.isEmpty(promotionBudgetRAOList)) {
            return;
        }
        Set<Long> sourceRuleIds = promotionBudgetRAOList.stream().map(p -> p.getSourceRuleId()).collect(Collectors.toSet());
        ConsumeBudgetParam param = new ConsumeBudgetParam();
        param.setSourceRuleIds(sourceRuleIds);
        param.setCustomerId(user.getId());
        param.setCreatedOrderDate(target.getCreatedDate());

        Map<Long, Double> remainDiscountMap = promotionBudgetConsumeService.calculateConsumedBudgetOfSourceRules(param);
        promotionBudgetRAOList.forEach(b -> {
            double remain = CommonUtils.readValue(b.getRemainDiscount()) - CommonUtils.readValue(remainDiscountMap.get(b.getSourceRuleId()));
            b.setRemainDiscount(remain);
        });
        target.setPromotionBudgetList(promotionBudgetRAOList);
    }

    @Autowired
    public void setPromotionBudgetService(PromotionBudgetService promotionBudgetService) {
        this.promotionBudgetService = promotionBudgetService;
    }

    @Autowired
    public void setPromotionBudgetConsumeService(PromotionBudgetConsumeService promotionBudgetConsumeService) {
        this.promotionBudgetConsumeService = promotionBudgetConsumeService;
    }
}
