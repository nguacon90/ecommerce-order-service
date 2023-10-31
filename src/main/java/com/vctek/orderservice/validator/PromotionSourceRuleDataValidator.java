package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CategoryData;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.PromotionSourceRuleData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.service.ProductService;
import com.vctek.orderservice.util.ConditionDefinitionParameter;
import com.vctek.orderservice.util.PromotionDefinitionCode;
import com.vctek.validate.Validator;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class PromotionSourceRuleDataValidator implements Validator<PromotionSourceRuleData> {
    public static final String ID_CONTAINER = "id";
    private ProductService productService;

    @Override
    public void validate(PromotionSourceRuleData promotionSourceRuleData) {
        validatePartnerProductPromotion(promotionSourceRuleData);

    }

    protected void validatePartnerProductPromotion(PromotionSourceRuleData promotionSourceRuleData) {
        List<RuleActionData> actions = promotionSourceRuleData.getActions();
        Optional<RuleActionData> actionDataOptional = actions.stream()
                .filter(a -> PromotionDefinitionCode.PARTNER_ORDER_PERCENTAGE_DISCOUNT_ACTION.code().equals(a.getDefinitionId()))
                .findFirst();
        if (!actionDataOptional.isPresent()) {
            return;
        }

        RuleActionData ruleActionData = actionDataOptional.get();
        RuleParameterData qualifyingContainers = ruleActionData.getParameters().get(ConditionDefinitionParameter.QUALIFYING_CONTAINERS.code());
        RuleParameterData targetContainers = ruleActionData.getParameters().get(ConditionDefinitionParameter.TARGET_CONTAINERS.code());
        Object qualifyContainer = qualifyingContainers.getValue();
        Object targetContainer = targetContainers.getValue();
        if (!(qualifyContainer instanceof Map) || !(targetContainer instanceof Map)) {
            return;
        }

        final Object targetContainerId = ((Map) targetContainer).keySet().iterator().next();
        final Object qualifyContainerId = ((Map) qualifyContainer).keySet().iterator().next();

        List<RuleConditionData> containerConditions = promotionSourceRuleData.getConditions().stream()
                .filter(c -> PromotionDefinitionCode.CONTAINER.code().equals(c.getDefinitionId()))
                .collect(Collectors.toList());
        Optional<RuleConditionData> qualifyingProductContainerOptional = containerConditions.stream()
                .filter(c -> qualifyContainerId != null && c.getParameters().get(ID_CONTAINER) != null &&
                        c.getParameters().get(ID_CONTAINER).getValue().equals(qualifyContainerId))
                .findFirst();

        Optional<RuleConditionData> targetProductContainerOptional = containerConditions.stream()
                .filter(c -> targetContainerId != null && c.getParameters().get(ID_CONTAINER) != null &&
                        c.getParameters().get(ID_CONTAINER).getValue().equals(targetContainerId))
                .findFirst();

        if(!qualifyingProductContainerOptional.isPresent() || !targetProductContainerOptional.isPresent()) {
            return;
        }

        RuleConditionData qualifyingProductContainer = qualifyingProductContainerOptional.get();
        List<RuleConditionData> qualifyProducts = qualifyingProductContainer.getChildren().stream()
                .filter(c -> PromotionDefinitionCode.GROUP.code().equals(c.getDefinitionId()) && CollectionUtils.isNotEmpty(c.getChildren()))
                .flatMap(c -> c.getChildren().stream())
                .collect(Collectors.toList());

        Set<Long> qualifyProductIds = new HashSet<>();
        Set<Long> qualifyCategoryIds = new HashSet<>();
        Set<Long> targetProductIds = new HashSet<>();

        populateQualifyProductAndCategories(qualifyProducts, qualifyProductIds, qualifyCategoryIds);

        RuleConditionData targetProductContainer = targetProductContainerOptional.get();
        populateTargetProductIds(targetProductContainer, targetProductIds);

        if(qualifyProductIds.containsAll(targetProductIds) && qualifyProductIds.size() == 1) {
            //Ignore the same one product with target and qualify
            return;
        }

        if(containAny(qualifyProductIds, targetProductIds)) {
            ErrorCodes err = ErrorCodes.DUPLICATE_PRODUCT_IN_PARTNER_PRODUCT_PROMOTION;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        validateProductInCategories(qualifyCategoryIds, targetProductIds);
    }

    private void populateQualifyProductAndCategories(List<RuleConditionData> qualifyProducts, Set<Long> qualifyProductIds, Set<Long> qualifyCategoryIds) {
        for(RuleConditionData qualifyProduct : qualifyProducts) {
            if(PromotionDefinitionCode.QUALIFIER_PRODUCTS.code().equals(qualifyProduct.getDefinitionId())) {
                qualifyProductIds.addAll(getCollectionOf(qualifyProduct, ConditionDefinitionParameter.QUALIFYING_PRODUCTS.code()));
            } else if(PromotionDefinitionCode.QUALIFIER_CATEGORIES.code().equals(qualifyProduct.getDefinitionId())) {
                qualifyCategoryIds.addAll(getCollectionOf(qualifyProduct, ConditionDefinitionParameter.QUALIFYING_CATEGORIES.code()));
            }
        }
    }

    private void populateTargetProductIds(RuleConditionData targetProductContainer, Set<Long> targetProductIds) {
        List<RuleConditionData> targetProducts = targetProductContainer.getChildren().stream()
                .filter(c -> PromotionDefinitionCode.GROUP.code().equals(c.getDefinitionId()) && CollectionUtils.isNotEmpty(c.getChildren()))
                .flatMap(c -> c.getChildren().stream())
                .collect(Collectors.toList());
        for(RuleConditionData targetProduct : targetProducts) {
            if(PromotionDefinitionCode.QUALIFIER_PRODUCTS.code().equals(targetProduct.getDefinitionId())) {
                targetProductIds.addAll(getCollectionOf(targetProduct, ConditionDefinitionParameter.QUALIFYING_PRODUCTS.code()));
            }
        }
    }

    private void validateProductInCategories(Set<Long> qualifyCategoryIds, Set<Long> targetProductIds) {
        if(CollectionUtils.isNotEmpty(qualifyCategoryIds)) {
            Set<Long> targetCategories = new HashSet<>();
            for(Long productId : targetProductIds) {
                List<CategoryData> allProductCategories = productService.findAllProductCategories(productId);
                targetCategories.addAll(allProductCategories.stream().map(c -> c.getId()).collect(Collectors.toList()));
            }

            if(containAny(qualifyCategoryIds, targetCategories)) {
                ErrorCodes err = ErrorCodes.DUPLICATE_PRODUCT_IN_PARTNER_PRODUCT_PROMOTION;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }
    }

    private boolean containAny(Set<Long> source, Set<Long> checkingSet) {
        for(Long checkingItem : checkingSet) {
            if(source.contains(checkingItem)) {
                return true;
            }
        }

        return false;
    }

    private List<Long> getCollectionOf(RuleConditionData ruleConditionData, String key) {
        Object products = ruleConditionData.getParameters().get(key).getValue();
        List<Long> result = new ArrayList<>();
        if(products instanceof Collection) {
            ((Collection) products).stream().forEach(p -> result.add(Long.valueOf(p.toString())));
            return result;
        }

        return result;
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }
}
