package com.vctek.orderservice.promotionengine.promotionservice.service.impl;

import com.vctek.dto.promotion.ConditionDTO;
import com.vctek.dto.promotion.PromotionSourceRuleDTO;
import com.vctek.orderservice.dto.PromotionRuleSearchParam;
import com.vctek.orderservice.event.PromotionSourceRuleCRUEvent;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.model.OrderSourceModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.repository.PromotionSourceRuleRepository;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionSourceRuleService;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleStatus;
import com.vctek.orderservice.service.specification.PromotionSourceRuleSpecification;
import com.vctek.util.CommonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DefaultPromotionSourceRuleService implements PromotionSourceRuleService {
    private PromotionSourceRuleRepository promotionSourceRuleRepository;
    private ApplicationEventPublisher applicationEventPublisher;
    public DefaultPromotionSourceRuleService(PromotionSourceRuleRepository promotionSourceRuleRepository) {
        this.promotionSourceRuleRepository = promotionSourceRuleRepository;
    }

    @Override
    public PromotionSourceRuleModel findById(Long id) {
        Optional<PromotionSourceRuleModel> modelOptional = promotionSourceRuleRepository.findById(id);
        return modelOptional.isPresent() ? modelOptional.get() : null;
    }

    @Override
    @Transactional
    public PromotionSourceRuleModel save(PromotionSourceRuleModel model) {
        PromotionSourceRuleModel savedModel = promotionSourceRuleRepository.save(model);
        applicationEventPublisher.publishEvent(new PromotionSourceRuleCRUEvent(savedModel));
        return savedModel;
    }

    @Override
    public PromotionSourceRuleModel findByCode(String code) {
        return promotionSourceRuleRepository.findByCode(code);
    }

    @Override
    public List<PromotionSourceRuleModel> findAllByAppliedOnlyOneAndCodeIn(boolean appliedOnlyOne, List<String> codes) {
        return promotionSourceRuleRepository.findAllByAppliedOnlyOneAndCodeIn(appliedOnlyOne, codes);
    }

    @Override
    public void updateAllActiveRuleStatus(String moduleName, RuleStatus published) {
        promotionSourceRuleRepository.updateAllActiveRuleStatus(Calendar.getInstance().getTime(), moduleName, published.toString());
    }

    @Override
    public void updateAllExpiredRuleToInActive(String moduleName) {
        promotionSourceRuleRepository.updateAllExpiredRuleToInActive(Calendar.getInstance().getTime(), moduleName,
                RuleStatus.INACTIVE.toString(), RuleStatus.PUBLISHED.toString());
    }

    @Override
    public Page<PromotionSourceRuleModel> findAll(PromotionRuleSearchParam param) {
        return promotionSourceRuleRepository.findAll(new PromotionSourceRuleSpecification(param), param.getPageable());
    }

    @Override
    public PromotionSourceRuleModel findByIdAndCompanyId(Long promotionId, Long companyId) {
        return promotionSourceRuleRepository.findByIdAndCompanyId(promotionId, companyId);
    }

    @Override
    public boolean isExpired(PromotionSourceRuleModel sourceRuleModel) {
        return sourceRuleModel.getEndDate() != null &&
                sourceRuleModel.getEndDate().before(Calendar.getInstance().getTime());
    }

    @Override
    public boolean hasCondition(PromotionSourceRuleDTO sourceRuleDTO, String conditionDefinitionId) {
        List<ConditionDTO> conditions = sourceRuleDTO.getConditions();
        if (CollectionUtils.isEmpty(conditions)) {
            return false;
        }

        Optional<ConditionDTO> foundCondition = conditions.stream()
                .filter(c -> c.getDefinitionId().equalsIgnoreCase(conditionDefinitionId))
                .findFirst();
        return foundCondition.isPresent();
    }

    @Override
    public Page<PromotionSourceRuleModel> findAllByCompanyId(Long companyId, Pageable pageable) {
        return promotionSourceRuleRepository.findAllByCompanyId(companyId, pageable);
    }

    @Override
    public List<PromotionSourceRuleModel> findAllByIdIn(List<Long> promotionSourceRuleIds) {
        return promotionSourceRuleRepository.findAllByIdIn(promotionSourceRuleIds);
    }

    @Override
    public boolean isValidToAppliedForCart(PromotionSourceRuleModel sr, CartModel model) {
        if (!sr.isActive() || isExpired(sr)) {
            return false;
        }

        if (!isValidOrderType(sr, model)) {
            return false;
        }

        if(!isValidAppliedWarehouse(sr, model)) {
            return false;
        }

        if(!validAppliedPriceTypes(sr, model)) {
            return false;
        }

        if(!validAppliedOrderSource(sr, model)) {
            return false;
        }

        return true;
    }

    @Override
    public List<PromotionSourceRuleModel> findAllActiveOf(Long companyId, Date date) {
        return promotionSourceRuleRepository.findAllActiveOf(companyId, date);
    }

    private boolean validAppliedOrderSource(PromotionSourceRuleModel sr, CartModel model) {
        String excludeOrderSources = sr.getExcludeOrderSources();
        OrderSourceModel orderSourceModel = model.getOrderSourceModel();
        if(StringUtils.isBlank(excludeOrderSources) || orderSourceModel == null) {
            return true;
        }
        String[] excludeOrderSourceList = CommonUtils.splitByComma(excludeOrderSources);
        List<Long> excludeOrderSourceIds = Arrays.stream(excludeOrderSourceList).map(id -> Long.valueOf(id))
                .collect(Collectors.toList());
        if(excludeOrderSourceIds.contains(orderSourceModel.getId())) {
            return false;
        }

        return true;
    }

    private boolean validAppliedPriceTypes(PromotionSourceRuleModel sr, CartModel model) {
        String appliedPriceTypes = sr.getAppliedPriceTypes();
        if (StringUtils.isBlank(appliedPriceTypes)) {
            return true;
        }
        String[] priceTypeArr = CommonUtils.splitByComma(appliedPriceTypes);
        List<String> priceTypes = Arrays.stream(priceTypeArr).collect(Collectors.toList());
        return priceTypes.contains(model.getPriceType());
    }

    private boolean isValidAppliedWarehouse(PromotionSourceRuleModel sr, CartModel model) {
        String appliedWarehouseIds = sr.getAppliedWarehouseIds();
        if(StringUtils.isBlank(appliedWarehouseIds)) {
            return true;
        }
        String[] warehouseIdList = CommonUtils.splitByComma(appliedWarehouseIds);
        List<Long> warehouseIds = Arrays.stream(warehouseIdList).map(id -> Long.valueOf(id))
                .collect(Collectors.toList());
        return warehouseIds.contains(model.getWarehouseId());
    }

    private boolean isValidOrderType(PromotionSourceRuleModel sr, CartModel model) {
        if (StringUtils.isBlank(sr.getAppliedOrderTypes())) {
            return true;
        }
        String[] orderTypeArr = CommonUtils.splitByComma(sr.getAppliedOrderTypes());
        List<String> orderTypes = Arrays.stream(orderTypeArr).collect(Collectors.toList());
        return orderTypes.contains(model.getType());
    }

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

}
