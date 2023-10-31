package com.vctek.orderservice.converter.populator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.PaymentTransactionModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.CampaignModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionResultService;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionsRegistry;
import com.vctek.orderservice.promotionengine.ruleengineservice.service.RuleConditionsService;
import com.vctek.orderservice.promotionengine.util.CommonUtils;
import com.vctek.orderservice.repository.EntryRepository;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.CouponService;
import com.vctek.orderservice.service.PaymentTransactionService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractOrderPopulator<SOURCE extends AbstractOrderModel, TARGET extends AbstractOrderData>
        implements Populator<SOURCE, TARGET> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOrderPopulator.class);
    protected PromotionResultService promotionResultService;
    protected Converter<AbstractOrderEntryModel, OrderEntryData> orderEntryConverter;
    protected Converter<PaymentTransactionModel, PaymentTransactionData> paymentTransactionDataConverter;
    protected PaymentTransactionService paymentTransactionService;
    protected RuleConditionsService ruleConditionsService;
    protected RuleConditionsRegistry ruleConditionsRegistry;
    protected CalculationService calculationService;
    protected EntryRepository entryRepository;
    protected CouponService couponService;
    protected ObjectMapper objectMapper;

    protected void addCommon(final AbstractOrderModel source, final AbstractOrderData target) {
        Double totalDiscount = source.getTotalDiscount() != null ? source.getTotalDiscount() : 0;
        Double subTotalDiscount = source.getSubTotalDiscount() != null ? source.getSubTotalDiscount() : 0;
        target.setId(source.getId());
        target.setCompanyId(source.getCompanyId());
        target.setWarehouseId(source.getWarehouseId());
        target.setCreateByUser(source.getCreateByUser());
        target.setEmployeeId(source.getEmployeeId());
        target.setCustomerId(source.getCustomerId());
        target.setCode(source.getCode());
        target.setOrderStatus(source.getOrderStatus());
        target.setGuid(source.getGuid());
        target.setCreatedTime(source.getCreatedTime());
        target.setDiscount(source.getDiscount());
        target.setDiscountType(source.getDiscountType());
        target.setType(source.getType());
        target.setDeliveryCost(source.getDeliveryCost());
        target.setCompanyShippingFee(source.getCompanyShippingFee());
        target.setCollaboratorShippingFee(source.getCollaboratorShippingFee());
        target.setPaymentCost(source.getPaymentCost());
        target.setTotalPrice(source.getTotalPrice());
        target.setFinalPrice(source.getFinalPrice());
        target.setTotalDiscount(totalDiscount);
        target.setSubTotal(source.getSubTotal());
        target.setSubTotalDiscount(subTotalDiscount);
        target.setFixedDiscount(source.getFixedDiscount());
        target.setPromotionDiscount(computePromotionDiscount(source));
        target.setTotalTax(source.getTotalTax());
        target.setVat(source.getVat());
        target.setVatType(source.getVatType());
        target.setVatDate(source.getVatDate());
        target.setVatNumber(source.getVatNumber());
        target.setDeliveryDate(source.getDeliveryDate());
        if(StringUtils.isNotBlank(source.getCustomerNote())) {
            target.setCustomerNote(com.vctek.orderservice.util.CommonUtils.unescapeSpecialSymbols(source.getCustomerNote()));
        }
        target.setCustomerSupportNote(com.vctek.orderservice.util.CommonUtils.unescapeSpecialSymbols(source.getCustomerSupportNote()));
        target.setExchange(source.isExchange());
        target.setShippingCustomerName(source.getShippingCustomerName());
        target.setShippingCustomerPhone(source.getShippingCustomerPhone());
        target.setShippingAddressId(source.getShippingAddressId());
        target.setShippingProvinceId(source.getShippingProvinceId());
        target.setShippingDistrictId(source.getShippingDistrictId());
        target.setShippingWardId(source.getShippingWardId());
        target.setShippingAddressDetail(source.getShippingAddressDetail());
        target.setPaidAmount(source.getPaidAmount());
        target.setTotalRewardAmount(source.getTotalRewardAmount());
        target.setRewardPoint(source.getRewardPoint());
        target.setFinalDiscount(totalDiscount + subTotalDiscount +
                CommonUtils.getDoubleValue(source.getTotalToppingDiscount()));
        target.setCardNumber(source.getCardNumber());
        target.setPriceType(source.getPriceType());
        target.setSellSignal(source.getSellSignal());
        target.setExternalId(source.getExternalId());
        target.setExternalCode(source.getExternalCode());
        target.setDistributorId(source.getDistributorId());
        target.setHasGotVat(source.isHasGotVat());
        target.setHasChangeGift(source.isHasChangeGift());
        populateImages(source, target);
    }

    private void populateImages(AbstractOrderModel source, AbstractOrderData target) {
        if (StringUtils.isBlank(source.getImages())) return;
        try {
            List<OrderImageData> imageData = objectMapper.readValue(source.getImages(), new TypeReference<List<OrderImageData>>() {
            });
            target.setImages(imageData);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }


    private Double computePromotionDiscount(AbstractOrderModel source) {
        Double totalDiscount = source.getTotalDiscount();
        if (totalDiscount == null) {
            return 0d;
        }
        double fixedDiscount = source.getFixedDiscount() == null ? 0d : source.getFixedDiscount();
        return totalDiscount - fixedDiscount;
    }

    protected void addEntries(final AbstractOrderModel source, final AbstractOrderData prototype) {
        List<AbstractOrderEntryModel> entries = entryRepository.findAllByOrder(source);
        Collections.sort(entries, Comparator.comparing(AbstractOrderEntryModel::getEntryNumber));
        prototype.setEntries(getOrderEntryConverter().convertAll(entries));
        long totalQuantity = 0;
        double totalFixedDiscount = 0d;
        for (AbstractOrderEntryModel entry : entries) {
            totalQuantity += entry.getQuantity();
            totalFixedDiscount += entry.getFixedDiscount() == null ? 0 : entry.getFixedDiscount();
        }
        double totalDiscountTopping = calculationService.totalDiscountTopping(source);
        prototype.setSubTotalFixedDiscount(totalFixedDiscount + totalDiscountTopping);
        prototype.setTotalQuantity(totalQuantity);

    }

    protected void addPaymentTransactions(final AbstractOrderModel source, final AbstractOrderData target) {
        List<PaymentTransactionModel> paymentTransactions = paymentTransactionService.findAllByOrderCode(source.getCode());
        if (CollectionUtils.isNotEmpty(paymentTransactions)) {
            List<PaymentTransactionModel> payments = paymentTransactions.stream().filter(i -> !i.isDeleted()).collect(Collectors.toList());
            target.setPayments(paymentTransactionDataConverter.convertAll(payments));
        }
    }

    protected void addPromotions(final AbstractOrderModel source, final AbstractOrderData prototype) {
        List<PromotionResultData> promotionResultData = new ArrayList<>();
        Set<PromotionSourceRuleModel> promotionSourceRules = promotionResultService.findAllPromotionSourceRulesByOrder(source);
        if (CollectionUtils.isEmpty(promotionSourceRules)) {
            prototype.setPotentialOrderPromotions(promotionResultData);
            return;
        }

        for (PromotionSourceRuleModel model : promotionSourceRules) {
            PromotionResultData data = new PromotionResultData();
            data.setMessageFired(model.getMessageFired());
            data.setPromotionId(model.getId());
            data.setAppliedOnlyOne(model.isAppliedOnlyOne());
            populateCampaignName(data, model);
            promotionResultData.add(data);
        }
        prototype.setPotentialOrderPromotions(promotionResultData);
    }

    protected void populateCouldFirePromotion(final AbstractOrderModel source, final AbstractOrderData target) {
        Set<PromotionSourceRuleModel> couldFirePromotions = source.getCouldFirePromotions();
        if (CollectionUtils.isNotEmpty(couldFirePromotions)) {
            List<PromotionResultData> couldFirePromotionData = ruleConditionsService.sortSourceRulesByOrderTotalCondition(couldFirePromotions);
            target.setCouldFirePromotions(couldFirePromotionData);
        }
    }

    private void populateCampaignName(PromotionResultData data, PromotionSourceRuleModel promotionSourceRule) {
        Set<CampaignModel> campaigns = promotionSourceRule.getCampaigns();
        if (CollectionUtils.isNotEmpty(campaigns)) {
            data.setCampaignName(campaigns.iterator().next().getName());
        }
    }

    protected void populateCouponCode(final AbstractOrderModel source, final AbstractOrderData target) {
        ValidCouponCodeData validatedCouponData = couponService.getValidatedCouponCode(source);
        target.setCouponCodes(validatedCouponData.getCouponData());
    }

    public Converter<AbstractOrderEntryModel, OrderEntryData> getOrderEntryConverter() {
        return orderEntryConverter;
    }

    @Autowired
    public void setOrderEntryConverter(Converter<AbstractOrderEntryModel, OrderEntryData> orderEntryConverter) {
        this.orderEntryConverter = orderEntryConverter;
    }

    @Autowired
    public void setPaymentTransactionDataConverter(Converter<PaymentTransactionModel, PaymentTransactionData> paymentTransactionDataConverter) {
        this.paymentTransactionDataConverter = paymentTransactionDataConverter;
    }

    @Autowired
    public void setPromotionResultService(PromotionResultService promotionResultService) {
        this.promotionResultService = promotionResultService;
    }

    @Autowired
    public void setPaymentTransactionService(PaymentTransactionService paymentTransactionService) {
        this.paymentTransactionService = paymentTransactionService;
    }

    @Autowired
    public void setRuleConditionsService(RuleConditionsService ruleConditionsService) {
        this.ruleConditionsService = ruleConditionsService;
    }

    @Autowired
    public void setRuleConditionsRegistry(RuleConditionsRegistry ruleConditionsRegistry) {
        this.ruleConditionsRegistry = ruleConditionsRegistry;
    }

    @Autowired
    public void setCalculationService(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @Autowired
    public void setEntryRepository(EntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    @Autowired
    public void setCouponService(CouponService couponService) {
        this.couponService = couponService;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
