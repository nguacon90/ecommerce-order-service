package com.vctek.orderservice.service.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.couponservice.model.CouponCodeModel;
import com.vctek.orderservice.couponservice.model.CouponModel;
import com.vctek.orderservice.couponservice.model.CouponRedemptionModel;
import com.vctek.orderservice.dto.CommerceRedeemCouponParameter;
import com.vctek.orderservice.dto.CouponCodeData;
import com.vctek.orderservice.dto.RedeemableCouponCodeData;
import com.vctek.orderservice.dto.ValidCouponCodeData;
import com.vctek.orderservice.event.CouponCRUEvent;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.ItemModel;
import com.vctek.orderservice.model.OrderHasCouponCodeModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.AbstractPromotionActionModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionOrderEntryConsumedModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionResultModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.repository.AbstractPromotionActionRepository;
import com.vctek.orderservice.promotionengine.promotionservice.repository.PromotionOrderEntryConsumedRepository;
import com.vctek.orderservice.promotionengine.promotionservice.repository.PromotionResultRepository;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionActionService;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionEngineService;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionResultService;
import com.vctek.orderservice.repository.CouponCodeRepository;
import com.vctek.orderservice.repository.CouponRepository;
import com.vctek.orderservice.repository.OrderHasCouponRepository;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.service.event.OrderEvent;
import com.vctek.orderservice.strategy.CommerceCartCalculationStrategy;
import com.vctek.orderservice.util.SellSignal;
import org.apache.commons.collections4.CollectionUtils;
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
public class CouponServiceImpl implements CouponService {
    private CouponRepository couponRepository;
    private CouponCodeRepository couponCodeRepository;
    private CouponRedemptionService couponRedemptionService;
    private PromotionEngineService promotionEngineService;
    private ModelService modelService;
    private ApplicationEventPublisher applicationEventPublisher;
    private CommerceCartCalculationStrategy commerceCartCalculationStrategy;
    private PromotionResultService promotionResultService;
    private OrderHasCouponRepository orderHasCouponRepository;
    private PromotionResultRepository promotionResultRepository;
    private AbstractPromotionActionRepository abstractPromotionActionRepository;
    private PromotionOrderEntryConsumedRepository promotionOrderEntryConsumedRepository;
    private PromotionActionService promotionActionService;
    private CalculationService calculationService;

    private ValidateCouponService validateCouponService;

    @Override
    @Transactional
    public CouponModel save(CouponModel model) {
        CouponModel savedModel = couponRepository.save(model);
        applicationEventPublisher.publishEvent(new CouponCRUEvent(savedModel));
        return savedModel;
    }

    @Override
    public List<CouponModel> findAllForQualifyingByCompanyId(Long companyId) {
        return couponRepository.findAllForQualifyingByCompanyId(companyId);
    }

    @Override
    @Transactional
    public void updateUseForPromotion(List<Long> coupons, PromotionSourceRuleModel rule) {
        if (rule == null) {
            return;
        }

        Set<CouponModel> currentRuleCoupons = rule.getCoupons();

        List<CouponModel> modelChanges = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(currentRuleCoupons)) {
            for (CouponModel model : currentRuleCoupons) {
                model.setPromotionSourceRule(null);
                modelChanges.add(model);
            }
        }

        for (Long couponId : coupons) {
            Optional<CouponModel> modelOptional = couponRepository.findById(couponId);
            if (modelOptional.isPresent()) {
                CouponModel couponModel = modelOptional.get();
                PromotionSourceRuleModel promotionSourceRule = couponModel.getPromotionSourceRule();
                if (promotionSourceRule != null && !promotionSourceRule.getId().equals(rule.getId())) {
                    ErrorCodes err = ErrorCodes.COUPON_HAD_ASSIGNED_TO_PROMOTION;
                    throw new ServiceException(err.code(), err.message(), err.httpStatus(),
                            new Object[]{promotionSourceRule.getId()});
                }

                couponModel.setPromotionSourceRule(rule);
                modelChanges.add(couponModel);
            }
        }

        if (CollectionUtils.isNotEmpty(modelChanges)) {
            couponRepository.saveAll(modelChanges);
        }
    }

    private boolean isValidUpdateCoupons(Set<CouponModel> currentRuleCoupons) {
        for (CouponModel couponModel : currentRuleCoupons) {
            Set<CouponCodeModel> couponCodes = couponModel.getCouponCodes();
            for (CouponCodeModel codeModel : couponCodes) {
                if (couponRedemptionService.countBy(codeModel) > 0) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public Page<CouponModel> findAllBy(Long companyId, String name, Pageable pageable) {
        if (StringUtils.isNotBlank(name)) {
            return couponRepository.findAllByCompanyIdAndNameLike(companyId, name, pageable);
        }

        return couponRepository.findAllByCompanyId(companyId, pageable);
    }

    @Override
    public CouponModel findById(Long couponId, Long companyId) {
        return couponRepository.findByIdAndCompanyId(couponId, companyId);
    }

    @Override
    public void delete(CouponModel couponModel) {
        couponRepository.delete(couponModel);
    }

    @Override
    public List<CouponModel> findAllForQualifyingByCompanyIdOrSourceRule(Long companyId, Long sourceRuleId) {
        return couponRepository.findAllForQualifyingByCompanyIdOrSourceRule(companyId, sourceRuleId);
    }

    @Override
    @Transactional
    public void redeemCoupon(CommerceRedeemCouponParameter parameter) {
        String clearedCouponCode = parameter.getCouponCode().trim();
        AbstractOrderModel order = parameter.getAbstractOrderModel();

        if (containsCouponCode(clearedCouponCode, order)) {
            ErrorCodes err = ErrorCodes.EXISTED_COUPON_IN_ORDER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        CouponCodeModel validatedCoupon = this.findValidatedCouponCode(clearedCouponCode, order.getCompanyId());
        if (validatedCoupon == null) {
            ErrorCodes err = ErrorCodes.INVALID_COUPON_CODE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (containsCouponNotAllowRedemptionMultiple(order, validatedCoupon)) {
            ErrorCodes err = ErrorCodes.NOT_ALLOW_REDEMPTION_MULTIPLE_COUPON_IN_ORDER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        RedeemableCouponCodeData validateRedemptionQuantity = validateCouponService.getValidateRedemptionQuantityCouponCode(validatedCoupon, parameter.getRedemptionQuantity());

        if(!validateRedemptionQuantity.isCanRedeem()) {
            ErrorCodes err = ErrorCodes.COUPON_OVER_MAX_REDEMPTION_QUANTITY;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{validateRedemptionQuantity.getRemainRedeemQuantity()});
        }

        OrderHasCouponCodeModel orderHasCouponCodeModel = new OrderHasCouponCodeModel();
        orderHasCouponCodeModel.setCouponCode(validatedCoupon);
        orderHasCouponCodeModel.setOrder(order);
        orderHasCouponCodeModel.setRedemptionQuantity(parameter.getRedemptionQuantity() == null ? 1 :
                parameter.getRedemptionQuantity());
        order.getOrderHasCouponCodeModels().add(orderHasCouponCodeModel);
        modelService.saveAll(order, validatedCoupon);
        promotionEngineService.updatePromotions(Collections.emptyList(), order);
        this.commerceCartCalculationStrategy.splitOrderPromotionToEntries(order);
        this.calculationService.calculateVatByProductOf(order, true);
        if (order instanceof OrderModel) {
            OrderModel orderModel = (OrderModel) order;
            this.createCouponRedemption(orderModel);
            applicationEventPublisher.publishEvent(new OrderEvent(orderModel));
        }
    }

    private boolean containsCouponNotAllowRedemptionMultiple(AbstractOrderModel order,
                                                             CouponCodeModel validatedCoupon) {
        Set<OrderHasCouponCodeModel> orderHasCouponCodeModels = order.getOrderHasCouponCodeModels();
        if (CollectionUtils.isEmpty(orderHasCouponCodeModels)) {
            return false;
        }

        Set<CouponCodeModel> appliedCouponCodes = orderHasCouponCodeModels.stream()
                .map(oc -> oc.getCouponCode())
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(appliedCouponCodes)) {
            return false;
        }

        for (CouponCodeModel model : appliedCouponCodes) {
            if (!model.getCoupon().isAllowRedemptionMultipleCoupon()) {
                return true;
            }
        }

        return !validatedCoupon.getCoupon().isAllowRedemptionMultipleCoupon();
    }

    @Override
    @Transactional
    public void createCouponRedemption(OrderModel savedOrder) {
        Set<OrderHasCouponCodeModel> orderHasCouponCodeModels = savedOrder.getOrderHasCouponCodeModels();
        if (CollectionUtils.isNotEmpty(orderHasCouponCodeModels)) {
            List<CouponRedemptionModel> redemptionModels = new ArrayList<>();
            for (OrderHasCouponCodeModel orderHasCoupon : orderHasCouponCodeModels) {
                if (orderHasCoupon.getRedemptionQuantity() != null) {
                    for (int i = 0; i < orderHasCoupon.getRedemptionQuantity(); i++) {
                        redemptionModels.add(getNewCouponRedemptionModel(savedOrder, orderHasCoupon));
                    }
                } else {
                    redemptionModels.add(getNewCouponRedemptionModel(savedOrder, orderHasCoupon));
                }
            }

            couponRedemptionService.saveAll(redemptionModels);
        }
    }

    private CouponRedemptionModel getNewCouponRedemptionModel(OrderModel savedOrder, OrderHasCouponCodeModel orderHasCoupon) {
        CouponRedemptionModel redemptionModel = new CouponRedemptionModel();
        redemptionModel.setCouponCodeModel(orderHasCoupon.getCouponCode());
        redemptionModel.setCustomerId(savedOrder.getCustomerId());
        redemptionModel.setOrder(savedOrder);
        return redemptionModel;
    }

    @Override
    @Transactional
    public void releaseCoupon(CommerceRedeemCouponParameter parameter) {
        String clearedCouponCode = parameter.getCouponCode().trim();
        AbstractOrderModel order = parameter.getAbstractOrderModel();

        Optional<OrderHasCouponCodeModel> orderHasCouponCodeModelOptional = order.getOrderHasCouponCodeModels().stream()
                .filter(oc -> oc.getCouponCode().getCode().equals(clearedCouponCode))
                .findFirst();
        if (!orderHasCouponCodeModelOptional.isPresent()) {
            ErrorCodes err = ErrorCodes.INVALID_COUPON_CODE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        OrderHasCouponCodeModel orderHasCouponCodeModel = orderHasCouponCodeModelOptional.get();
        orderHasCouponCodeModel.setOrder(null);
        orderHasCouponCodeModel.setCouponCode(null);
        order.getOrderHasCouponCodeModels().remove(orderHasCouponCodeModel);

        modelService.saveAll(order);
        modelService.remove(orderHasCouponCodeModel);
        this.promotionEngineService.updatePromotions(Collections.emptyList(), order);
        commerceCartCalculationStrategy.splitOrderPromotionToEntries(order);
        this.calculationService.calculateVatByProductOf(order, true);
        if (order instanceof OrderModel) {
            OrderModel orderModel = (OrderModel) order;
            this.removeCouponRedemption(orderModel);
            applicationEventPublisher.publishEvent(new OrderEvent(orderModel));
        }
    }

    @Override
    @Transactional
    public void removeCouponRedemption(OrderModel order) {
        Set<CouponRedemptionModel> couponRedemptionModels = order.getCouponRedemptionModels();
        if (CollectionUtils.isNotEmpty(couponRedemptionModels)) {
            modelService.removeAll(couponRedemptionModels);
            order.getCouponRedemptionModels().removeAll(couponRedemptionModels);
            modelService.save(order);
        }
    }

    @Override
    @Transactional
    public void removeCouponToSourceRule(PromotionSourceRuleModel rule) {
        Set<CouponModel> currentRuleCoupons = rule.getCoupons();
        if (CollectionUtils.isNotEmpty(currentRuleCoupons) && !isValidUpdateCoupons(currentRuleCoupons)) {
            ErrorCodes err = ErrorCodes.CANNOT_CHANGE_QUALIFYING_REDEEMED_COUPON;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        currentRuleCoupons.stream().forEach(c -> c.setPromotionSourceRule(null));
        rule.getCoupons().removeAll(currentRuleCoupons);
        modelService.save(rule);
    }

    @Override
    public ValidCouponCodeData getValidatedCouponCode(AbstractOrderModel abstractOrderModel) {
        ValidCouponCodeData data = new ValidCouponCodeData();
        Set<OrderHasCouponCodeModel> orderHasCouponCodeModels = abstractOrderModel.getOrderHasCouponCodeModels();
        if (CollectionUtils.isEmpty(orderHasCouponCodeModels)) {
            data.setValid(true);
            return data;
        }

        boolean isValid = true;
        List<CouponCodeData> appliedCouponCodes = new ArrayList<>();
        CouponCodeData couponCodeData;
        for (OrderHasCouponCodeModel orderHasCouponCodeModel : orderHasCouponCodeModels) {
            CouponCodeModel couponCode = orderHasCouponCodeModel.getCouponCode();
            if (couponCode != null) {
                couponCodeData = new CouponCodeData();
                couponCodeData.setCode(couponCode.getCode());
                couponCodeData.setTotalRedemption(orderHasCouponCodeModel.getRedemptionQuantity());
                if (!populateValidatedCouponPromotion(couponCode, couponCodeData, abstractOrderModel)) {
                    isValid = false;
                }
                appliedCouponCodes.add(couponCodeData);
            }
        }
        data.setCouponData(appliedCouponCodes);
        data.setValid(isValid);
        return data;
    }

    @Override
    public List<CouponCodeModel> findAllCouponCodeBy(CouponModel source) {
        return couponCodeRepository.findAllByCoupon(source);
    }

    @Override
    public Page<CouponModel> findAllByCompanyId(Long companyId, Pageable pageable) {
        return couponRepository.findAllByCompanyId(companyId, pageable);
    }

    private boolean populateValidatedCouponPromotion(CouponCodeModel couponCode, CouponCodeData couponCodeData, AbstractOrderModel target) {
        couponCodeData.setValid(true);
        CouponModel coupon = couponCode.getCoupon();
        PromotionSourceRuleModel promotionSourceRule = coupon.getPromotionSourceRule();
        if (promotionSourceRule == null) {
            couponCodeData.setValid(false);
            return false;
        }
        if(SellSignal.ECOMMERCE_WEB.toString().equalsIgnoreCase(target.getSellSignal())) {
            RedeemableCouponCodeData validateRedemptionQuantity = validateCouponService.getValidateRedemptionQuantityCouponCode(couponCode, couponCodeData.getTotalRedemption());
            if(!validateRedemptionQuantity.isCanRedeem()) {
                couponCodeData.setOverTotalRedemption(true);
            }
        }

        couponCodeData.setPromotionId(promotionSourceRule.getId());
        Set<Long> promotionSourceRuleIds = promotionResultService.findAllPromotionSourceRulesByOrder(target).stream()
                .map(p -> p.getId()).collect(Collectors.toSet());
        target.getCouldFirePromotions().stream().forEach(cp -> promotionSourceRuleIds.add(cp.getId()));

        if (CollectionUtils.isEmpty(promotionSourceRuleIds) || !promotionSourceRuleIds.contains(promotionSourceRule.getId())) {
            couponCodeData.setValid(false);
            return false;
        }

        if(couponCodeData.isOverTotalRedemption()) {
            return false;
        }

        return true;
    }

    @Override
    public CouponCodeModel findValidatedCouponCode(String clearedCouponCode, Long companyId) {
        List<CouponCodeModel> validCoupons = couponCodeRepository.findValidCoupon(clearedCouponCode, companyId,
                Calendar.getInstance().getTime());
        if (CollectionUtils.isEmpty(validCoupons)) {
            return null;
        }

        Collections.sort(validCoupons, (o1, o2) -> o2.getId().compareTo(o1.getId()));
        for (CouponCodeModel codeModel : validCoupons) {
            int maxTotalRedemption = codeModel.getCoupon().getMaxTotalRedemption();
            Long totalRedemption = couponRedemptionService.countBy(codeModel);
            if (totalRedemption == null || totalRedemption.intValue() < maxTotalRedemption) {
                return codeModel;
            }
        }

        ErrorCodes err = ErrorCodes.COUPON_OVER_MAX_REDEMPTION;
        throw new ServiceException(err.code(), err.message(), err.httpStatus());
    }

    private boolean containsCouponCode(String clearedCouponCode, AbstractOrderModel order) {
        Set<OrderHasCouponCodeModel> orderHasCouponCodeModels = order.getOrderHasCouponCodeModels();
        if (CollectionUtils.isEmpty(orderHasCouponCodeModels)) {
            return false;
        }
        Set<CouponCodeModel> appliedCouponCodes = orderHasCouponCodeModels.stream()
                .map(oc -> oc.getCouponCode()).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(appliedCouponCodes)) {
            return false;
        }

        for (CouponCodeModel model : appliedCouponCodes) {
            if (StringUtils.isNotBlank(model.getCode()) && model.getCode().equalsIgnoreCase(clearedCouponCode)) {
                return true;
            }
        }

        return false;
    }

    @Override
    @Transactional
    public void revertAllCouponToOrder(OrderModel orderModel) {
        List<OrderHasCouponCodeModel> orderHasCouponCodeModels = orderHasCouponRepository.findAllByOrderId(orderModel.getId());
        if (CollectionUtils.isEmpty(orderHasCouponCodeModels)) return;
        List<Long> couponIds = orderHasCouponCodeModels.stream().map(m -> m.getCouponCode().getCoupon().getId())
                .collect(Collectors.toList());
        removePromotionResultOfCoupon(couponIds, orderModel);

        calculationService.recalculate(orderModel);
        commerceCartCalculationStrategy.splitOrderPromotionToEntries(orderModel);

        List<ItemModel> removedItems = new ArrayList<>();
        orderHasCouponCodeModels.stream().forEach(m -> {
            m.setCouponCode(null);
            m.setOrder(null);
        });
        removedItems.addAll(orderHasCouponCodeModels);

        List<CouponRedemptionModel> couponRedemptionModels = couponRedemptionService.findAllBy(orderModel);
        if (CollectionUtils.isNotEmpty(couponRedemptionModels)) {
            couponRedemptionModels.stream().forEach(c -> {
                c.setCouponCodeModel(null);
                c.setOrder(null);
            });
            removedItems.addAll(couponRedemptionModels);
        }
        modelService.removeAll(removedItems);
        modelService.save(orderModel);
    }

    private void removePromotionResultOfCoupon(List<Long> couponIds, AbstractOrderModel orderModel) {
        if (CollectionUtils.isEmpty(couponIds)) return;
        List<PromotionResultModel> promotionResultModels = promotionResultRepository.findAllByCouponIdInAndOrderCode(couponIds, orderModel.getCode());
        if (CollectionUtils.isNotEmpty(promotionResultModels)) {
            List<PromotionOrderEntryConsumedModel> promotionOrderEntryConsumedModels = new ArrayList<>();
            List<AbstractPromotionActionModel> promotionActionModels = new ArrayList<>();
            for(PromotionResultModel promotionResultModel : promotionResultModels) {
                List<PromotionOrderEntryConsumedModel> consumeEntries = promotionOrderEntryConsumedRepository.findAllByPromotionResult(promotionResultModel);
                if(CollectionUtils.isNotEmpty(consumeEntries)) {
                    promotionOrderEntryConsumedModels.addAll(consumeEntries);
                }

                List<AbstractPromotionActionModel> actions = abstractPromotionActionRepository.findAllByPromotionResult(promotionResultModel);
                promotionActionModels.addAll(actions);
            }
            if(CollectionUtils.isNotEmpty(promotionOrderEntryConsumedModels)) {
                promotionOrderEntryConsumedModels.forEach(ce -> ce.setPromotionResult(null));
                modelService.removeAll(promotionOrderEntryConsumedModels);
            }
            if(CollectionUtils.isNotEmpty(promotionActionModels)) {
                List<String> actionGuids = promotionActionModels.stream().map(AbstractPromotionActionModel::getGuid).collect(Collectors.toList());
                promotionActionService.removeDiscountValueBy(actionGuids, orderModel);
                promotionActionModels.stream().forEach(p -> p.setPromotionResult(null));
                modelService.removeAll(promotionActionModels);
            }
            modelService.removeAll(promotionResultModels);
        }
    }

    @Autowired
    public void setCouponRepository(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Autowired
    public void setCouponRedemptionService(CouponRedemptionService couponRedemptionService) {
        this.couponRedemptionService = couponRedemptionService;
    }

    @Autowired
    public void setCouponCodeRepository(CouponCodeRepository couponCodeRepository) {
        this.couponCodeRepository = couponCodeRepository;
    }

    @Autowired
    public void setPromotionEngineService(PromotionEngineService promotionEngineService) {
        this.promotionEngineService = promotionEngineService;
    }

    @Autowired
    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Autowired
    public void setPromotionResultService(PromotionResultService promotionResultService) {
        this.promotionResultService = promotionResultService;
    }

    @Autowired
    public void setCommerceCartCalculationStrategy(CommerceCartCalculationStrategy commerceCartCalculationStrategy) {
        this.commerceCartCalculationStrategy = commerceCartCalculationStrategy;
    }

    @Autowired
    public void setOrderHasCouponRepository(OrderHasCouponRepository orderHasCouponRepository) {
        this.orderHasCouponRepository = orderHasCouponRepository;
    }

    @Autowired
    public void setPromotionResultRepository(PromotionResultRepository promotionResultRepository) {
        this.promotionResultRepository = promotionResultRepository;
    }

    @Autowired
    public void setAbstractPromotionActionRepository(AbstractPromotionActionRepository abstractPromotionActionRepository) {
        this.abstractPromotionActionRepository = abstractPromotionActionRepository;
    }

    @Autowired
    public void setPromotionOrderEntryConsumedRepository(PromotionOrderEntryConsumedRepository promotionOrderEntryConsumedRepository) {
        this.promotionOrderEntryConsumedRepository = promotionOrderEntryConsumedRepository;
    }

    @Autowired
    public void setPromotionActionService(PromotionActionService promotionActionService) {
        this.promotionActionService = promotionActionService;
    }

    @Autowired
    public void setCalculationService(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @Autowired
    public void setValidateCouponService(ValidateCouponService validateCouponService) {
        this.validateCouponService = validateCouponService;
    }
}
