package com.vctek.orderservice.converter.storefront.populator;

import com.vctek.orderservice.converter.populator.AbstractOrderPopulator;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.OrderHistoryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.AbstractPromotionActionModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.AbstractRuleBasedPromotionActionModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.repository.AbstractPromotionActionRepository;
import com.vctek.orderservice.promotionengine.promotionservice.util.DiscountValue;
import com.vctek.orderservice.promotionengine.ruleengine.enums.RuleType;
import com.vctek.orderservice.promotionengine.ruleengineservice.converter.RuleActionsConverter;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleActionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleConditionDefinitionData;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.RuleParameterData;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleActionsRegistry;
import com.vctek.orderservice.repository.OrderHistoryRepository;
import com.vctek.orderservice.util.ConditionDefinitionParameter;
import com.vctek.orderservice.util.PromotionDefinitionCode;
import com.vctek.redis.PriceData;
import com.vctek.util.OrderStatus;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CommerceCartDataPopulator extends AbstractOrderPopulator<AbstractOrderModel, CommerceCartData> {
    private ProductSearchService productSearchService;
    private AbstractPromotionActionRepository abstractPromotionActionRepository;
    private RuleActionsConverter ruleActionsConverter;
    private RuleActionsRegistry ruleActionsRegistry;
    private OrderHistoryRepository orderHistoryRepository;

    @Override
    public void populate(AbstractOrderModel source, CommerceCartData target) {
        addCommon(source, target);
        addEntries(source, target);
        addPromotions(source, target);
        populateCouldFirePromotion(source, target);
        populateCouponCode(source, target);
        populateProductInfoOfEntry(target);
        populatePromotionForEntry(target);
        populateOrder(source, target);
        populateOrderCancel(source, target);
    }

    @Override
    public void populateCouldFirePromotion(final AbstractOrderModel source, final AbstractOrderData target) {
        Set<PromotionSourceRuleModel> couldFirePromotions = source.getCouldFirePromotions();
        List<PromotionResultData> couldFirePromotionData = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(couldFirePromotions)) {
            PromotionResultData data;
            Map<String, RuleConditionDefinitionData> conditionDefinitions = this.ruleConditionsRegistry.getConditionDefinitionsForRuleTypeAsMap(RuleType.PROMOTION);
            Map<String, RuleActionDefinitionData> actionDefinitionData = ruleActionsRegistry.getActionDefinitionsForRuleTypeAsMap(RuleType.PROMOTION);
            for (PromotionSourceRuleModel sourceRuleModel : couldFirePromotions) {
                data = new PromotionResultData();
                double minOrderValue = ruleConditionsService.getMinOrderTotalValueCondition(conditionDefinitions, sourceRuleModel);
                data.setMinValue(minOrderValue);
                data.setPromotionId(sourceRuleModel.getId());
                data.setMessageFired(sourceRuleModel.getMessageFired());
                populateFreeProductAction(data, sourceRuleModel, actionDefinitionData);
                couldFirePromotionData.add(data);
            }

            Collections.sort(couldFirePromotionData, (o1, o2) -> o2.getMinValue().compareTo(o1.getMinValue()));
            target.setCouldFirePromotions(couldFirePromotionData);
        }
    }

    private void populateFreeProductAction(PromotionResultData data, PromotionSourceRuleModel sourceRuleModel, Map<String, RuleActionDefinitionData> actionDefinitionData) {
        List<RuleActionData> ruleActions = ruleActionsConverter.fromString(sourceRuleModel.getActions(), actionDefinitionData);
        Long freeProductId = ruleActions.stream()
                .filter(action -> PromotionDefinitionCode.VCTEK_FREE_GIFT_ACTION.code().equalsIgnoreCase(action.getDefinitionId())
                        && action.getParameters().get(ConditionDefinitionParameter.FREE_PRODUCT.code()) != null)
                .map(action -> {
                    RuleParameterData ruleParameterData = action.getParameters().get(ConditionDefinitionParameter.FREE_PRODUCT.code());
                    return (Long) ruleParameterData.getValue();
                }).findFirst().orElse(null);
        if(freeProductId == null) {
            return;
        }
        ProductSearchModel productSearchModel = productSearchService.findById(freeProductId);
        if(productSearchModel == null) {
            return;
        }
        data.setProductId(productSearchModel.getId());
        data.setProductName(productSearchModel.getName());
        data.setProductSku(productSearchModel.getSku());
        data.setProductImage(getImageUrl(productSearchModel));
        List<PriceData> prices = productSearchModel.getPrices();
        if(CollectionUtils.isNotEmpty(prices)) {
            data.setProductPrice(prices.get(0).getPrice());
        }
    }

    private void populatePromotionForEntry(CommerceCartData target) {
        Map<String, RuleActionDefinitionData> actionDefinitionData = ruleActionsRegistry.getActionDefinitionsForRuleTypeAsMap(RuleType.PROMOTION);
        for (OrderEntryData entry : target.getEntries()) {
            List<DiscountValue> discountValues = entry.getDiscountValues();
            if (CollectionUtils.isEmpty(discountValues)) {
                continue;
            }

            populateAppliedPartner(actionDefinitionData, entry, discountValues);
        }
    }

    private void populateAppliedPartner(Map<String, RuleActionDefinitionData> actionDefinitionData, OrderEntryData entry, List<DiscountValue> discountValues) {
        List<String> actionCodes = discountValues.stream().map(DiscountValue::getCode).collect(Collectors.toList());
        List<AbstractPromotionActionModel> actions = abstractPromotionActionRepository.findAllByGuidIn(actionCodes);
        for (AbstractPromotionActionModel action : actions) {
            if (!(action instanceof AbstractRuleBasedPromotionActionModel)) {
                continue;
            }

            AbstractRuleBasedPromotionActionModel ruleAction = (AbstractRuleBasedPromotionActionModel) action;
            if (ruleAction.getRule() == null || ruleAction.getRule().getPromotionSourceRule() == null) {
                continue;
            }

            String actionStr = ruleAction.getRule().getPromotionSourceRule().getActions();
            List<RuleActionData> ruleActions = ruleActionsConverter.fromString(actionStr, actionDefinitionData);
            for (RuleActionData ra : ruleActions) {
                if (PromotionDefinitionCode.PARTNER_ORDER_PERCENTAGE_DISCOUNT_ACTION.code().equalsIgnoreCase(ra.getDefinitionId())) {
                    entry.setAppliedPartnerDiscount(true);
                    break;
                }
            }
        }
    }

    private void populateProductInfoOfEntry(CommerceCartData target) {
        Set<Long> productIds = new HashSet<>();
        target.getEntries().forEach(e -> {
            productIds.add(e.getProductId());
            e.getSubOrderEntries().forEach(soe -> productIds.add(soe.getProductId()));
        });

        List<ProductSearchModel> productSearchModels = productSearchService.findAllByIdIn(productIds.stream().collect(Collectors.toList()));
        Map<Long, ProductSearchModel> productSearchModelMap = new HashMap<>();
        productSearchModels.forEach(p -> productSearchModelMap.put(p.getId(), p));
        target.getEntries().forEach(e -> {
            ProductSearchModel productSearchModel = productSearchModelMap.get(e.getProductId());
            if (productSearchModel != null) {
                e.setProductName(productSearchModel.getName());
                e.setProductSku(productSearchModel.getSku());
                populateParent(productSearchModel, e);
                e.setProductImage(getImageUrl(productSearchModel));
            }
            e.getSubOrderEntries().forEach(soe -> {
                ProductSearchModel model = productSearchModelMap.get(soe.getProductId());
                if (model != null) {
                    soe.setProductImage(getImageUrl(model));
                }
            });

            Collections.sort(e.getSubOrderEntries(), Comparator.comparing(SubOrderEntryData::getId));
        });
    }

    private void populateParent(ProductSearchModel model, OrderEntryData data) {
        data.setParentProductId(model.getParentId());
        if (model.getParentId() != null) {
            ProductSearchModel parentModel = productSearchService.findById(model.getParentId());
            if (parentModel != null) {
                data.setParentProductName(parentModel.getName());
            }
        }
    }

    private String getImageUrl(ProductSearchModel productSearchModel) {
        List<String> onsiteImageUrls = productSearchModel.getOnsiteImageUrls();
        if (CollectionUtils.isEmpty(onsiteImageUrls)) {
            return null;
        }
        Optional<String> defaultImageOnsite = onsiteImageUrls.stream()
                .filter(i -> i.equals(productSearchModel.getDefaultImageUrl())).findFirst();
        if (defaultImageOnsite.isPresent()) {
            return defaultImageOnsite.get();
        }

        return onsiteImageUrls.get(0);
    }

    private void populateOrder(AbstractOrderModel source, CommerceCartData target) {
        if (!(source instanceof OrderModel)) return;
        OrderModel orderModel = (OrderModel) source;
        target.setShippingCompanyId(orderModel.getShippingCompanyId());
        target.setShippingFeeSettingId(orderModel.getShippingFeeSettingId());
    }

    private void populateOrderCancel(AbstractOrderModel source, CommerceCartData target) {
        if (!(source instanceof OrderModel)) return;
        OrderModel orderModel = (OrderModel) source;
        List<String> cancelStatus = Arrays.asList(OrderStatus.SYSTEM_CANCEL.toString(), OrderStatus.CUSTOMER_CANCEL.toString());
        if (!cancelStatus.contains(orderModel.getOrderStatus())) return;
        Optional<OrderHistoryModel> optional = orderHistoryRepository.findLastHistoryByOrderIdAndStatus(orderModel.getId(), orderModel.getOrderStatus());
        if (optional.isPresent()) {
            target.setCancelText(optional.get().getExtraData());
        }
    }

    @Autowired
    public void setProductSearchService(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @Autowired
    public void setRuleActionsConverter(RuleActionsConverter ruleActionsConverter) {
        this.ruleActionsConverter = ruleActionsConverter;
    }

    @Autowired
    public void setRuleActionsRegistry(RuleActionsRegistry ruleActionsRegistry) {
        this.ruleActionsRegistry = ruleActionsRegistry;
    }

    @Autowired
    public void setAbstractPromotionActionRepository(AbstractPromotionActionRepository abstractPromotionActionRepository) {
        this.abstractPromotionActionRepository = abstractPromotionActionRepository;
    }

    @Autowired
    public void setOrderHistoryRepository(OrderHistoryRepository orderHistoryRepository) {
        this.orderHistoryRepository = orderHistoryRepository;
    }
}
