package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Converter;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.model.LoyaltyTransactionModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.model.TagModel;
import com.vctek.orderservice.service.LoyaltyTransactionService;
import com.vctek.orderservice.service.ReturnOrderService;
import com.vctek.orderservice.service.TagService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderPopulator<T extends OrderData> extends AbstractOrderPopulator<OrderModel, T> {
    private ReturnOrderService returnOrderService;
    private LoyaltyTransactionService loyaltyTransactionService;
    private TagService tagService;
    private Converter<TagModel, TagData> tagDataConverter;

    @Override
    public void populate(OrderModel source, T target) {
        addCommon(source, target);
        addEntries(source, target);
        addPaymentTransactions(source, target);
        addPromotions(source, target);
        populateCouldFirePromotion(source, target);
        populateCouponCode(source, target);
        target.setBillId(source.getBillId());
        target.setShippingCompanyId(source.getShippingCompanyId());
        target.setDeliveryCost(source.getDeliveryCost());
        target.setCompanyShippingFee(source.getCompanyShippingFee());
        target.setCollaboratorShippingFee(source.getCollaboratorShippingFee());
        target.setGender(source.getGender());
        target.setAge(source.getAge());
        target.setRedeemAmount(source.getRedeemAmount());
        target.setRefundAmount(source.getRefundAmount());
        target.setRewardPoint(source.getRewardPoint());
        target.setPriceType(source.getPriceType());
        target.setDistributorId(source.getDistributorId());
        target.setConfirmDiscountBy(source.getConfirmDiscountBy());
        if (source.getOrderSourceModel() != null) {
            target.setOrderSourceId(source.getOrderSourceModel().getId());
        }
        ReturnOrderModel returnOrder = source.getReturnOrder();
        if (returnOrder != null) {
            target.setReturnOrderId(returnOrder.getId());
        }

        List<ReturnOrderModel> returnOrders = returnOrderService.findAllByOriginOrder(source);
        if (CollectionUtils.isNotEmpty(returnOrders)) {
            List<Long> returnOrderIds = returnOrders.stream().map(o -> o.getId()).collect(Collectors.toList());
            target.setReturnOrderIds(returnOrderIds);
        }
        populateAwardPoint(target);
        target.setSettingCustomerOptionIds(source.getOrderSettingCustomerOptionModels().stream().map(o -> o.getId()).collect(Collectors.toList()));
        populateTags(source, target);
    }

    private void populateTags(OrderModel source, T target) {
        List<TagModel> tags = tagService.findAllByOrder(source);
        if(CollectionUtils.isNotEmpty(tags)) {
            List<TagData> tagDataList = tagDataConverter.convertAll(tags);
            target.setTags(tagDataList);
        }
    }

    private void populateAwardPoint(T target) {
        LoyaltyTransactionModel transaction = loyaltyTransactionService.findLastByOrderCode(target.getCode());
        if (transaction == null) {
            return;
        }
        Double conversionRate = transaction.getConversionRate();
        if (conversionRate == null) {
            return;
        }
        List<OrderEntryData> entries = target.getEntries();
        double totalPoint = 0;
        for (OrderEntryData e : entries) {
            if (e.getRewardAmount() != null) {
                Double point = calculationService.round(e.getRewardAmount() / conversionRate, 2);
                e.setAwardPoint(point);
                totalPoint += point;
            }

            if (CollectionUtils.isEmpty(e.getToppingOptions())) {
                continue;
            }

            totalPoint += populateToppingItemAwardPoint(conversionRate, e);
        }

        target.setTotalAwardPoint(totalPoint);
    }

    private double populateToppingItemAwardPoint(Double conversionRate, OrderEntryData orderEntryData) {
        double totalItemPoint = 0;
        for (ToppingOptionData option : orderEntryData.getToppingOptions()) {
            List<ToppingItemData> toppingItems = option.getToppingItems();
            if (CollectionUtils.isEmpty(toppingItems)) {
                continue;
            }

            for (ToppingItemData item : toppingItems) {
                if (item.getRewardAmount() != null) {
                    Double point = calculationService.round(item.getRewardAmount() / conversionRate, 2);
                    item.setAwardPoint(point);
                    totalItemPoint += point;
                }
            }
        }
        return totalItemPoint;
    }

    @Autowired
    public void setReturnOrderService(ReturnOrderService returnOrderService) {
        this.returnOrderService = returnOrderService;
    }

    @Autowired
    public void setLoyaltyTransactionService(LoyaltyTransactionService loyaltyTransactionService) {
        this.loyaltyTransactionService = loyaltyTransactionService;
    }

    @Autowired
    public void setTagService(TagService tagService) {
        this.tagService = tagService;
    }

    @Autowired
    public void setTagDataConverter(Converter<TagModel, TagData> tagDataConverter) {
        this.tagDataConverter = tagDataConverter;
    }
}
