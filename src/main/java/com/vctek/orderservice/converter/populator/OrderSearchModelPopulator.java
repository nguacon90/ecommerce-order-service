package com.vctek.orderservice.converter.populator;

import com.google.common.collect.Lists;
import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.dto.redis.DistrictData;
import com.vctek.dto.redis.ProvinceData;
import com.vctek.dto.redis.WardData;
import com.vctek.orderservice.dto.OrderSettingCustomerOptionData;
import com.vctek.orderservice.dto.TagData;
import com.vctek.orderservice.dto.UserData;
import com.vctek.orderservice.elasticsearch.model.OrderSearchModel;
import com.vctek.orderservice.elasticsearch.model.PaymentTransactionData;
import com.vctek.orderservice.feignclient.dto.CustomerData;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.service.*;
import com.vctek.util.CommonUtils;
import com.vctek.util.OrderType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component("orderSearchModelPopulator")
public class OrderSearchModelPopulator extends AbstractOrderSearchModelPopulator implements Populator<OrderModel, OrderSearchModel> {
    private AuthService authService;
    private CRMService crmService;
    private PaymentTransactionService paymentTransactionService;
    private ReturnOrderService returnOrderService;
    private Converter<PaymentTransactionModel, PaymentTransactionData> orderPaymentTransactionConverter;
    private OrderSettingCustomerOptionService settingCustomerOptionService;
    private TagService tagService;
    private Converter<TagModel, TagData> tagDataConverter;

    @Autowired
    public OrderSearchModelPopulator(AuthService authService, CRMService crmService,
                                     PaymentTransactionService paymentTransactionService) {
        this.authService = authService;
        this.crmService = crmService;
        this.paymentTransactionService = paymentTransactionService;
    }


    @Override
    public void populate(OrderModel source, OrderSearchModel target) {
        target.setId(source.getCode());
        target.setCode(source.getCode());
        target.setSellSignal(source.getSellSignal());
        target.setTotalPrice(source.getTotalPrice());
        target.setTotalTax(source.getTotalTax());
        target.setWarehouseId(source.getWarehouseId());
        target.setGuid(source.getGuid());
        target.setOrderType(source.getType());
        target.setPriceType(source.getPriceType());
        target.setGlobalDiscountValues(source.getGlobalDiscountValues());
        target.setSubTotal(source.getSubTotal());
        target.setCompanyId(source.getCompanyId());
        target.setDeleted(source.isDeleted());
        populateUserAndEmployee(source, target);
        if (source.getCustomerId() != null) {
            populateCustomer(source, target);
        }
        target.setVat(source.getVat());
        target.setVatType(source.getVatType());
        target.setOrderStatus(source.getOrderStatus());
        target.setFinalPrice(source.getFinalPrice());
        target.setDiscount(source.getDiscount());
        target.setDiscountType(source.getDiscountType());
        target.setDeliveryCost(source.getDeliveryCost());
        target.setCompanyShippingFee(source.getCompanyShippingFee());
        target.setCollaboratorShippingFee(source.getCollaboratorShippingFee());
        target.setPaymentCost(source.getPaymentCost());
        target.setFixedDiscount(source.getFixedDiscount());
        target.setNote(com.vctek.orderservice.util.CommonUtils.unescapeSpecialSymbols(source.getNote()));
        target.setCreatedTime(source.getCreatedTime());
        target.setBillId(source.getBillId());
        target.setShippingCompanyId(source.getShippingCompanyId());
        target.setOrderRetailCode(source.getOrderRetailCode());
        if(StringUtils.isNotBlank(source.getCustomerNote())) {
            target.setCustomerNote(com.vctek.orderservice.util.CommonUtils.unescapeSpecialSymbols(source.getCustomerNote()));
        }
        target.setCustomerSupportNote(com.vctek.orderservice.util.CommonUtils.unescapeSpecialSymbols(source.getCustomerSupportNote()));
        target.setExchange(source.isExchange());
        target.setAge(source.getAge());
        target.setGender(source.getGender());
        target.setPaidAmount(source.getPaidAmount());
        target.setTotalRewardAmount(source.getTotalRewardAmount());
        target.setRewardPoint(source.getRewardPoint());
        target.setRedeemAmount(source.getRedeemAmount());
        target.setRefundAmount(source.getRefundAmount());
        target.setModifiedTimeLastStatus(source.getCreatedTime());
        target.setDeliveryDate(source.getDeliveryDate());
        target.setDistributorId(source.getDistributorId());

        populateImages(source, target);
        if (source.getOrderSourceModel() != null) {
            target.setOrderSourceId(source.getOrderSourceModel().getId());
        }
        populateProduct(target, source);
        populatePaymentMethod(target, source);
        populateOrderHistory(target, source);
        double subTotalDiscount = source.getSubTotalDiscount() == null ? 0d : source.getSubTotalDiscount();
        double totalDiscount = source.getTotalDiscount() == null ? 0d : source.getTotalDiscount();
        target.setTotalDiscount(subTotalDiscount + totalDiscount + CommonUtils.readValue(source.getTotalToppingDiscount()));

        populateReturnOrderIdOfExchangeOrder(target, source);

        populateReturnOrderListOf(target, source);

        populateSettingCustomerOption(target, source);

        populateTags(source, target);

    }

    private void populateTags(OrderModel source, OrderSearchModel target) {
        List<TagModel> tags = tagService.findAllByOrder(source);
        if(CollectionUtils.isNotEmpty(tags)) {
            List<TagData> tagData = tagDataConverter.convertAll(tags);
            target.setTags(tagData);
        }
    }

    private void populateSettingCustomerOption(OrderSearchModel target, OrderModel source) {
        List<OrderSettingCustomerOptionData> optionData = new ArrayList<>();
        List<OrderSettingCustomerOptionModel> optionModels = settingCustomerOptionService.findAllByOrderId(source.getId());
        for (OrderSettingCustomerOptionModel optionModel : optionModels) {
            OrderSettingCustomerOptionData data = new OrderSettingCustomerOptionData();
            data.setId(optionModel.getId());
            data.setName(optionModel.getName());
            optionData.add(data);
        }
        target.setSettingCustomerOptionData(optionData);
    }

    private void populateReturnOrderListOf(OrderSearchModel target, OrderModel source) {
        List<ReturnOrderModel> returnOrders = returnOrderService.findAllByOriginOrder(source);
        if (CollectionUtils.isNotEmpty(returnOrders)) {
            List<Long> returnOrderIds = returnOrders.stream().map(ro -> ro.getId()).collect(Collectors.toList());
            target.setReturnOrderIds(returnOrderIds);
        } else {
            target.setReturnOrderIds(Collections.emptyList());
        }
    }

    private void populateReturnOrderIdOfExchangeOrder(OrderSearchModel target, OrderModel source) {
        ReturnOrderModel returnOrder = source.getReturnOrder();
        if (returnOrder != null) {
            target.setReturnOrderId(returnOrder.getId());
        }
    }

    protected void populateUserAndEmployee(OrderModel source, OrderSearchModel target) {
        Long userId = source.getCreateByUser();
        if (userId != null) {
            UserData userData = authService.getUserById(userId);
            target.setCreatedBy(userId);
            target.setCreatedName(userData.getName());
        }

        if (source.getEmployeeId() != null) {
            UserData userData = authService.getUserById(source.getEmployeeId());
            target.setEmployeeId(source.getEmployeeId());
            target.setEmployeeName(userData.getName());
        }
    }

    protected void populateCustomer(OrderModel source, OrderSearchModel target) {
        Long customerId = source.getCustomerId();
        CustomerData customerData = crmService.getBasicCustomerInfo(customerId, source.getCompanyId());
        if (customerData == null) {
            return;
        }

        target.setCustomerId(customerId);
        target.setCustomerName(customerData.getName());
        target.setCustomerPhone(customerData.getPhone());
        populateAddress(target, source);
    }

    protected void populateAddress(OrderSearchModel target, OrderModel source) {
        target.setAddress(source.getShippingAddressDetail());
        if (source.getShippingProvinceId() != null) {
            ProvinceData provinceData = crmService.getProvinceById(source.getShippingProvinceId());
            if (provinceData != null) {
                target.setProvinceId(provinceData.getId());
                target.setProvinceName(provinceData.getName());
            }
        }
        if (source.getShippingDistrictId() != null) {
            DistrictData districtData = crmService.getDistrictById(source.getShippingDistrictId());
            if (districtData != null) {
                target.setDistrictId(districtData.getId());
                target.setDistrictName(districtData.getName());
            }
        }
        if (source.getShippingWardId() != null) {
            WardData wardData = crmService.getWardById(source.getShippingWardId());
            if (wardData != null) {
                target.setWardId(wardData.getId());
                target.setWardName(wardData.getName());
            }
        }
    }

    protected void populatePaymentMethod(OrderSearchModel target, OrderModel source) {
        Set<PaymentTransactionData> paymentTransactionDataSet = new HashSet<>();
        List<PaymentTransactionModel> paymentTransactionModels = paymentTransactionService.findAllByOrderCode(source.getCode());
        List<PaymentTransactionData> paymentTransactionData = orderPaymentTransactionConverter.convertAll(paymentTransactionModels);
        if (CollectionUtils.isNotEmpty(paymentTransactionData)) {
            paymentTransactionDataSet.addAll(paymentTransactionData);
        }
        if (OrderType.ONLINE.toString().equals(source.getType())) {
            List<PaymentTransactionData> paymentTransaction = paymentTransactionService.findAllPaymentInvoiceOrder(source);
            if (CollectionUtils.isNotEmpty(paymentTransaction)) {
                paymentTransactionDataSet.addAll(paymentTransaction);
            }
        }
        target.setPaymentTransactionData(Lists.newArrayList(paymentTransactionDataSet));

    }

    @Autowired
    public void setReturnOrderService(ReturnOrderService returnOrderService) {
        this.returnOrderService = returnOrderService;
    }

    @Autowired
    @Qualifier("orderPaymentTransactionConverter")
    public void setOrderPaymentTransactionConverter(Converter<PaymentTransactionModel, PaymentTransactionData> orderPaymentTransactionConverter) {
        this.orderPaymentTransactionConverter = orderPaymentTransactionConverter;
    }

    @Autowired
    public void setSettingCustomerOptionService(OrderSettingCustomerOptionService settingCustomerOptionService) {
        this.settingCustomerOptionService = settingCustomerOptionService;
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
