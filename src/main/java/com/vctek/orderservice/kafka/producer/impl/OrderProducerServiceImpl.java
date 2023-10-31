package com.vctek.orderservice.kafka.producer.impl;

import com.vctek.dto.redis.AddressData;
import com.vctek.kafka.data.*;
import com.vctek.kafka.message.KafkaMessage;
import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.kafka.service.KafkaProducerService;
import com.vctek.kafka.stream.OrderKafkaOutStream;
import com.vctek.kafka.stream.OrderStatusKafkaOutStream;
import com.vctek.kafka.stream.RecalculateOrderReportOutStream;
import com.vctek.migration.dto.PaymentTransactionData;
import com.vctek.orderservice.couponservice.model.CouponCodeModel;
import com.vctek.orderservice.couponservice.model.CouponModel;
import com.vctek.orderservice.couponservice.model.CouponRedemptionModel;
import com.vctek.orderservice.kafka.producer.LoyaltyTransactionProducerService;
import com.vctek.orderservice.kafka.producer.OrderProducerService;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.promotionengine.promotionservice.model.*;
import com.vctek.orderservice.promotionengine.promotionservice.repository.AbstractPromotionActionRepository;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionResultService;
import com.vctek.orderservice.promotionengine.promotionservice.util.DiscountValue;
import com.vctek.orderservice.promotionengine.ruleengineservice.calculation.domain.Currency;
import com.vctek.orderservice.promotionengine.util.CommonUtils;
import com.vctek.orderservice.repository.*;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.service.event.OrderEvent;
import com.vctek.orderservice.util.CurrencyUtils;
import com.vctek.orderservice.util.EventType;
import com.vctek.service.UserService;
import com.vctek.util.MoneySourceType;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import com.vctek.util.TransactionType;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderProducerServiceImpl implements OrderProducerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderProducerServiceImpl.class);
    protected KafkaProducerService kafkaProducerService;
    protected OrderKafkaOutStream orderKafkaOutStream;
    private OrderStatusKafkaOutStream orderStatusKafkaOutStream;
    private UserService userService;
    private CRMService crmService;
    private PaymentTransactionService paymentTransactionService;
    private OrderEntryRepository orderEntryRepository;
    private SubOrderEntryRepository subOrderEntryRepository;
    private ToppingOptionRepository optionRepository;
    private ToppingItemRepository toppingItemRepository;
    private OrderHistoryRepository orderHistoryRepository;
    private PromotionResultService promotionResultService;
    private AbstractPromotionActionRepository abstractPromotionActionRepository;
    private CouponRedemptionService couponRedemptionService;
    private RecalculateOrderReportOutStream recalculateOrderReportOutStream;
    private OrderSettingCustomerOptionService customerOptionService;
    private LoyaltyTransactionProducerService loyaltyTransactionProducerService;
    private TagService tagService;

    @Autowired
    public OrderProducerServiceImpl(KafkaProducerService kafkaProducerService,
                                    OrderKafkaOutStream orderKafkaOutStream,
                                    OrderStatusKafkaOutStream orderStatusKafkaOutStream) {
        this.kafkaProducerService = kafkaProducerService;
        this.orderKafkaOutStream = orderKafkaOutStream;
        this.orderStatusKafkaOutStream = orderStatusKafkaOutStream;
    }

    @Override
    public void sendOrderKafka(OrderEvent event) {
        OrderModel orderModel = event.getOrderModel();
        OrderStatus oldStatus = event.getOldOrderStatus();
        OrderData orderData = populateOrder(orderModel, event.getOldEntries(), oldStatus);
        if (orderData == null) {
            LOGGER.error("NOT SEND ORDER TO KAFKA: orderCode: {}", orderModel.getCode());
            return;
        }

        populateCurrentUser(orderData, event.getCurrentUserId());
        orderData.setImportDetailId(event.getImportDetailId());
        KafkaMessage<OrderData> kafkaMessage = new KafkaMessage<>();
        kafkaMessage.setContent(orderData);
        KafkaMessageType kafkaMessageType = getKafkaMessageType(event);
        kafkaMessage.setType(kafkaMessageType);
        kafkaProducerService.send(kafkaMessage, orderKafkaOutStream.produceOrderTopic());
        sendKafkaCompletedLoyaltyRedeem(event);
    }

    private KafkaMessageType getKafkaMessageType(OrderEvent event) {
        if (EventType.IMPORT_CHANGE_STATUS_ONLINE.equals(event.getEventType())) {
            return KafkaMessageType.CHANGE_ORDER_STATUS;
        }

        if(EventType.CREATE.equals(event.getEventType()) && event.isEcommerceOrder()) {
            return KafkaMessageType.HOLDING_AVAILABLE_STOCK;
        }

        return KafkaMessageType.ORDERS;
    }

    private void populateCurrentUser(OrderData orderData, Long currentUserId) {
        if (currentUserId == null) {
            currentUserId = userService.getCurrentUserId();
        }
        orderData.setCurrentUserId(currentUserId);
    }

    @Override
    public void recalculateOrderReport(OrderModel orderModel, KafkaMessageType kafkaMessageType, Long currentUserId) {
        OrderData orderData = populateOrder(orderModel, new ArrayList<>(), null);
        populateCurrentUser(orderData, currentUserId);
        if (orderData != null) {
            KafkaMessage<OrderData> kafkaMessage = new KafkaMessage<>();
            kafkaMessage.setContent(orderData);
            kafkaMessage.setType(kafkaMessageType);
            kafkaProducerService.send(kafkaMessage, recalculateOrderReportOutStream.produceTopic());
        }
    }

    @Override
    public void sendChangeStatusKafka(OrderHistoryModel orderHistoryModel) {
        OrderStatusData orderStatusData = new OrderStatusData();
        OrderStatus newStatus = OrderStatus.findByCode(orderHistoryModel.getCurrentStatus());
        if (newStatus == null) return;
        switch (newStatus) {
            case CONFIRMED:
                orderStatusData.setConfirmedDate(orderHistoryModel.getModifiedTime());
                orderStatusData.setModifiedDate(orderHistoryModel.getModifiedTime());
                break;
            case CHANGE_TO_RETAIL:
            case COMPLETED:
                orderStatusData.setSuccessDimDate(orderHistoryModel.getModifiedTime());
                orderStatusData.setModifiedDate(orderHistoryModel.getModifiedTime());
                break;
            default:
                orderStatusData.setModifiedDate(orderHistoryModel.getModifiedTime());
                break;
        }
        orderStatusData.setOrderHistoryId(orderHistoryModel.getId());
        AbstractOrderModel orderModel = orderHistoryModel.getOrder();
        orderStatusData.setOrderCode(orderModel.getCode());
        orderStatusData.setCompanyId(orderModel.getCompanyId());
        orderStatusData.setWarehouseId(orderModel.getWarehouseId());
        List<OrderEntryModel> entryModels = orderEntryRepository.findAllByOrderCode(orderModel.getCode());
        List<OrderEntryData> orderEntryData = new ArrayList<>();
        for (AbstractOrderEntryModel entryModel : entryModels) {
            List<SubOrderEntryModel> subOrderEntries = subOrderEntryRepository.findAllByOrderEntry(entryModel);

            if (CollectionUtils.isNotEmpty(subOrderEntries)) {
                populateCombo(subOrderEntries, orderEntryData);
            }
            List<ToppingOptionModel> toppingOptionModels = optionRepository.findAllByOrderEntry(entryModel);

            if (CollectionUtils.isNotEmpty(toppingOptionModels)) {
                populateTopping(toppingOptionModels, orderEntryData);
            }

            if (CollectionUtils.isEmpty(subOrderEntries)) {
                OrderEntryData data = new OrderEntryData();
                data.setProductId(entryModel.getProductId());
                data.setQuantity(entryModel.getQuantity().intValue());
                orderEntryData.add(data);
            }
        }
        orderStatusData.setOrderEntryData(orderEntryData);
        orderStatusData.setPreviousStatus(orderHistoryModel.getPreviousStatus());
        orderStatusData.setCurrentStatus(orderHistoryModel.getCurrentStatus());
        if(orderModel instanceof OrderModel) {
            orderStatusData.setBillId(((OrderModel) orderModel).getBillId());
        }
        KafkaMessage<OrderStatusData> kafkaMessage = new KafkaMessage<>();
        kafkaMessage.setContent(orderStatusData);
        kafkaMessage.setType(KafkaMessageType.ORDERS_STATUS);
        kafkaProducerService.send(kafkaMessage, orderStatusKafkaOutStream.produceOrderTopic());
    }

    @Override
    public void produceUpdateOrderTag(OrderModel orderModel) {
        OrderData orderData = populateOrder(orderModel, new ArrayList<>(), null);
        if(orderData == null) {
            return;
        }
        populateCurrentUser(orderData, null);
        KafkaMessage<OrderData> kafkaMessage = new KafkaMessage<>();
        kafkaMessage.setContent(orderData);
        kafkaMessage.setType(KafkaMessageType.ORDER_TAG);
        kafkaProducerService.send(kafkaMessage, orderKafkaOutStream.produceOrderTopic());
    }

    private void populateTopping(List<ToppingOptionModel> toppingOptionModels, List<OrderEntryData> orderEntryData) {
        for (ToppingOptionModel toppingOptionModel : toppingOptionModels) {
            int optionQty = CommonUtils.getIntValue(toppingOptionModel.getQuantity());
            List<ToppingItemModel> toppingItemModels = toppingItemRepository.findAllByToppingOptionModel(toppingOptionModel);
            for (ToppingItemModel toppingItemModel : toppingItemModels) {
                int itemQty = CommonUtils.getIntValue(toppingItemModel.getQuantity());
                OrderEntryData data = new OrderEntryData();
                data.setProductId(toppingItemModel.getProductId());
                data.setQuantity(itemQty * optionQty);
                orderEntryData.add(data);
            }
        }
    }

    private void populateCombo(List<SubOrderEntryModel> subOrderEntries, List<OrderEntryData> orderEntryData) {
        for (SubOrderEntryModel subOrderEntryModel : subOrderEntries) {
            OrderEntryData data = new OrderEntryData();
            data.setProductId(subOrderEntryModel.getProductId());
            data.setQuantity(subOrderEntryModel.getQuantity());
            orderEntryData.add(data);
        }
    }

    protected OrderData populateOrder(OrderModel orderModel, List<com.vctek.orderservice.dto.OrderEntryData> oldOrderEntries, OrderStatus oldStatus) {
        if (isNotAbleSend(orderModel)) return null;
        OrderData orderData = new OrderData();
        orderData.setOrderCode(orderModel.getCode());
        orderData.setCompanyId(orderModel.getCompanyId());
        orderData.setDimCustomerData(populateCustomerData(orderModel));
        orderData.setWarehouseId(orderModel.getWarehouseId());
        orderData.setOrderType(orderModel.getType());
        orderData.setOrderDeleted(orderModel.isDeleted());
        orderData.setOrderStatus(orderModel.getOrderStatus());
        orderData.setShippingCompanyId(orderModel.getShippingCompanyId());
        orderData.setDeliveryFee(orderModel.getDeliveryCost());
        orderData.setCompanyShippingFee(orderModel.getCompanyShippingFee());
        orderData.setCollaboratorShippingFee(orderModel.getCollaboratorShippingFee());
        orderData.setPriceType(orderModel.getPriceType());
        orderData.setVat(orderModel.getTotalTax());
        orderData.setOrderVat(orderModel.getVat());
        orderData.setOrderVatType(orderModel.getVatType());
        orderData.setDiscount(orderModel.getDiscount());
        orderData.setDiscountType(orderModel.getDiscountType());
        orderData.setTotalPrice(orderModel.getTotalPrice());
        orderData.setFinalPrice(orderModel.getFinalPrice());
        orderData.setExchange(orderModel.isExchange());
        double totalDiscount = orderModel.getTotalDiscount() == null ? 0 : orderModel.getTotalDiscount();
        double subTotalDiscount = orderModel.getSubTotalDiscount() == null ? 0 : orderModel.getSubTotalDiscount();
        double totalToppingDiscount = CommonUtils.getDoubleValue(orderModel.getTotalToppingDiscount());
        orderData.setTotalDiscount(totalDiscount);
        orderData.setSubTotalDiscount(subTotalDiscount);
        orderData.setFinalDiscount(totalDiscount + subTotalDiscount + totalToppingDiscount);
        orderData.setDeliveryDate(orderModel.getDeliveryDate());
        orderData.setCreatedDate(orderModel.getCreatedTime());
        orderData.setOrderRetailCode(orderModel.getOrderRetailCode());
        orderData.setModifiedTime(orderModel.getModifiedTime());
        orderData.setModifiedBy(orderModel.getModifiedBy());
        orderData.setAge(orderModel.getAge());
        orderData.setGender(orderModel.getGender());
        orderData.setCustomerNote(orderModel.getCustomerNote());
        orderData.setCustomerSupportNote(orderModel.getCustomerSupportNote());
        orderData.setRewardAmount(orderModel.getTotalRewardAmount());
        OrderSourceModel orderSourceModel = orderModel.getOrderSourceModel();
        if (orderSourceModel != null) {
            orderData.setOrderSourceId(orderSourceModel.getId());
            orderData.setOrderSourceName(orderSourceModel.getName());
        }
        if (oldStatus != null) {
            orderData.setOldOrderStatus(oldStatus.code());
        }
        if (orderModel.getShippingAddressId() != null) {
            orderData.setAddressDto(populateRegionData(orderModel));
        }
        List<PaymentTransactionModel> paymentTransactions = paymentTransactionService.findAllByOrderCode(orderModel.getCode());
        populatePaymentTransactionData(orderData, paymentTransactions);
        if (CollectionUtils.isNotEmpty(paymentTransactions)) {
            populateMoney(orderData, paymentTransactions);
        }
        List<OrderEntryModel> entryModels = orderEntryRepository.findAllByOrder(orderModel);

        orderData.setTotalQuantity(0L);
        setEntryAndTotalPrice(orderData, entryModels, orderModel.getCurrencyCode(), oldOrderEntries, oldStatus);

        Long employeeId = OrderType.ONLINE.toString().equals(orderModel.getType()) &&
                orderModel.getEmployeeId() != null ? orderModel.getEmployeeId() : orderModel.getCreateByUser();
        orderData.setEmployeeId(employeeId);

        if (orderModel.getCustomerId() != null) {
            CustomerDto customerDto = new CustomerDto();
            customerDto.setCustomerId(orderModel.getCustomerId());
            orderData.setDimCustomerData(customerDto);
        }
        populateSuccessDate(orderData, orderModel);
        populatePromotionResult(orderData, orderModel, entryModels);
        populateCouponRedemption(orderData, orderModel);
        populateSettingCustomerOption(orderData, orderModel);
        populateOrderTag(orderData, orderModel);
        return orderData;
    }

    private void populateOrderTag(OrderData orderData, OrderModel orderModel) {
        List<TagModel> tagModels = tagService.findAllByOrder(orderModel);
        if(CollectionUtils.isEmpty(tagModels)) {
            return;
        }
        List<TagData> tagDataList = new ArrayList<>();
        for(TagModel tagModel : tagModels) {
            TagData tagData = new TagData();
            tagData.setId(tagModel.getId());
            tagData.setName(tagModel.getName());
            tagDataList.add(tagData);
        }
        orderData.setTags(tagDataList);
    }

    private void populatePaymentTransactionData(OrderData orderData, List<PaymentTransactionModel> paymentTransactions) {
        List<PaymentTransactionData> paymentTransactionData = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(paymentTransactions)) {
            for (PaymentTransactionModel payment : paymentTransactions) {
                if (payment.isDeleted()) continue;

                PaymentTransactionData data = new PaymentTransactionData();
                data.setId(payment.getId());
                data.setAmount(payment.getAmount());
                data.setMoneySourceId(payment.getMoneySourceId());
                data.setMoneySourceType(payment.getMoneySourceType());
                data.setPaymentMethodId(payment.getPaymentMethodId());
                paymentTransactionData.add(data);
            }
        }
        orderData.setPaymentTransactionData(paymentTransactionData);
    }

    protected void populateSettingCustomerOption(OrderData orderData, OrderModel orderModel) {
        List<OrderSettingCustomerOptionData> orderSettingCustomerOptions = new ArrayList<>();
        List<OrderSettingCustomerOptionModel> customerOptionModels = customerOptionService.findAllByOrderId(orderModel.getId());
        if (CollectionUtils.isNotEmpty(customerOptionModels)){
            for (OrderSettingCustomerOptionModel orderSettingCustomerOptionModel : customerOptionModels) {
                OrderSettingCustomerOptionData orderSettingCustomerOptionData = new OrderSettingCustomerOptionData();
                orderSettingCustomerOptionData.setId(orderSettingCustomerOptionModel.getId());
                orderSettingCustomerOptionData.setOrderSettingCustomerId(orderSettingCustomerOptionModel.getOrderSettingCustomerModel().getId());
                orderSettingCustomerOptions.add(orderSettingCustomerOptionData);
            }
        }
        orderData.setSettingCustomerOptions(orderSettingCustomerOptions);
    }

    protected void populateCouponRedemption(OrderData orderData, OrderModel orderModel) {
        List<CouponRedemptionModel> couponRedemptionModels = couponRedemptionService.findAllBy(orderModel);
        if(CollectionUtils.isEmpty(couponRedemptionModels)) {
            return;
        }

        Map<String, CouponRedemptionDTO> mapByCodes = new HashMap<>();
        CouponRedemptionDTO dto;
        for(CouponRedemptionModel model : couponRedemptionModels) {
            CouponCodeModel couponCodeModel = model.getCouponCodeModel();
            String couponCode = couponCodeModel.getCode();
            dto = mapByCodes.get(couponCode);
            dto = dto == null ? new CouponRedemptionDTO() : dto;
            dto.setCouponCode(couponCode);
            CouponModel coupon = couponCodeModel.getCoupon();
            dto.setCouponId(coupon.getId());
            PromotionSourceRuleModel promotionSourceRule = coupon.getPromotionSourceRule();
            if(promotionSourceRule != null) {
                dto.setPromotionCode(promotionSourceRule.getCode());
            }
            dto.setRedemptionQuantity(dto.getRedemptionQuantity() + 1);
            mapByCodes.put(couponCode, dto);
        }

        List<CouponRedemptionDTO> couponRedemptionDTOList = mapByCodes.values().stream().collect(Collectors.toList());
        orderData.setCouponRedemptionList(couponRedemptionDTOList);
    }

    protected void populatePromotionResult(OrderData orderData, OrderModel orderModel, List<OrderEntryModel> entryModels) {
        List<PromotionResultModel> promotionResults = promotionResultService.findAllByOrder(orderModel);
        if(CollectionUtils.isEmpty(promotionResults)) {
            return;
        }
        List<PromotionResultDTO> promotionResultDTOS = new ArrayList<>();
        PromotionResultDTO promotionResultDTO;
        List<PromotionActionDTO> promotionActionDTOS;
        for(PromotionResultModel model : promotionResults) {
            promotionActionDTOS = new ArrayList<>();
            promotionResultDTO = new PromotionResultDTO();
            AbstractPromotionModel promotion = model.getPromotion();
            if(promotion == null) {
                continue;
            }

            promotionResultDTO.setPromotionCode(promotion.getCode());
            List<AbstractPromotionActionModel> abstractPromotionActionModels = abstractPromotionActionRepository.findAllByPromotionResult(model);
            for(AbstractPromotionActionModel actionModel : abstractPromotionActionModels) {
                PromotionActionDTO actionDTO = populatePromotionActionDTO(entryModels, actionModel);
                if(actionDTO == null) {
                    continue;
                }

                promotionActionDTOS.add(actionDTO);
            }
            promotionResultDTO.setPromotionActions(promotionActionDTOS);
            promotionResultDTOS.add(promotionResultDTO);
        }

        orderData.setPromotionResults(promotionResultDTOS);
    }

    private PromotionActionDTO populatePromotionActionDTO(List<OrderEntryModel> entryModels, AbstractPromotionActionModel actionModel) {
        PromotionActionDTO actionDTO = new PromotionActionDTO();
        actionDTO.setPromotionActionDType(actionModel.getClass().getSimpleName());
        actionDTO.setGuid(actionModel.getGuid());
        if(actionModel instanceof PromotionOrderAdjustTotalActionModel) {
            PromotionOrderAdjustTotalActionModel promotionOrderAdjustTotalActionModel = (PromotionOrderAdjustTotalActionModel) actionModel;
            actionDTO.setAmount(promotionOrderAdjustTotalActionModel.getAmount());
            return actionDTO;
        }

        if(actionModel instanceof RuleBasedFixedPriceProductActionModel) {
            RuleBasedFixedPriceProductActionModel fixedPriceProductActionModel = (RuleBasedFixedPriceProductActionModel) actionModel;
            actionDTO.setProductId(fixedPriceProductActionModel.getProductId());
            actionDTO.setOrderEntryQuantity(fixedPriceProductActionModel.getOrderEntryQuantity());
            return actionDTO;
        }

        if(actionModel instanceof RuleBasedOrderAddProductActionModel) {
            RuleBasedOrderAddProductActionModel addProductActionModel = (RuleBasedOrderAddProductActionModel) actionModel;
            actionDTO.setProductId(addProductActionModel.getProductId());
            actionDTO.setOrderEntryQuantity(Long.valueOf(addProductActionModel.getQuantity()));
            return actionDTO;
        }
        if(actionModel instanceof RuleBasedOrderAdjustTotalActionModel) {
            RuleBasedOrderAdjustTotalActionModel adjustTotalActionModel = (RuleBasedOrderAdjustTotalActionModel) actionModel;
            actionDTO.setAmount(adjustTotalActionModel.getAmount() != null ? adjustTotalActionModel.getAmount().doubleValue() : 0d);
            return actionDTO;
        }

        if(actionModel instanceof RuleBasedOrderEntryAdjustActionModel) {
            RuleBasedOrderEntryAdjustActionModel orderEntryAdjustActionModel = (RuleBasedOrderEntryAdjustActionModel) actionModel;
            actionDTO.setProductId(orderEntryAdjustActionModel.getProductId());
            actionDTO.setOrderEntryQuantity(orderEntryAdjustActionModel.getOrderEntryQuantity());
            actionDTO.setOrderEntryNumber(orderEntryAdjustActionModel.getOrderEntryNumber());
            actionDTO.setAmount(orderEntryAdjustActionModel.getAmount() != null ? orderEntryAdjustActionModel.getAmount().doubleValue() : 0d);
            actionDTO.setOrderEntryId(this.findOrderEntryIdOf(orderEntryAdjustActionModel.getProductId(), orderEntryAdjustActionModel.getOrderEntryNumber(), entryModels));
            return actionDTO;
        }

        LOGGER.error("Not found type promotion action: {}", actionModel.getClass().getSimpleName());
        return null;
    }

    private Long findOrderEntryIdOf(Long productId, Integer orderEntryNumber, List<OrderEntryModel> entryModels) {
        Optional<OrderEntryModel> entryModel = entryModels.stream()
                .filter(e -> e.getProductId().equals(productId) && e.getEntryNumber().equals(orderEntryNumber))
                .findFirst();
        return entryModel.isPresent() ? entryModel.get().getId() : null;
    }

    private void populateSuccessDate(OrderData orderData, OrderModel orderModel) {
        if (OrderType.ONLINE.name().equals(orderModel.getType())) {
            Optional<OrderHistoryModel> historyOptional = orderHistoryRepository.findLastBy(orderModel.getId());
            if (!historyOptional.isPresent()) return;
            OrderHistoryModel orderHistoryModel = historyOptional.get();
            orderData.setModifiedTime(orderHistoryModel.getModifiedTime());
            if (OrderStatus.COMPLETED.code().equals(orderHistoryModel.getCurrentStatus())
                    || OrderStatus.CHANGE_TO_RETAIL.code().equals(orderHistoryModel.getCurrentStatus())
                    || OrderStatus.ORDER_RETURN.code().equals(orderHistoryModel.getCurrentStatus())) {
                orderData.setSuccessDate(orderHistoryModel.getModifiedTime());
            } else if (OrderStatus.CONFIRMED.code().equals(orderModel.getOrderStatus())) {
                orderData.setConfirmedDate(orderHistoryModel.getModifiedTime());
            }
        }
    }

    private void populateMoney(OrderData orderData, List<PaymentTransactionModel> paymentTransactions) {
        for (PaymentTransactionModel paymentTransactionModel : paymentTransactions) {
            if (MoneySourceType.CASH.name().equals(paymentTransactionModel.getMoneySourceType())) {
                orderData.setMoneyCash(paymentTransactionModel.getAmount());
            } else if (MoneySourceType.BANK_ACCOUNT.name().equals(paymentTransactionModel.getMoneySourceType())) {
                orderData.setMoneyTransfer(paymentTransactionModel.getAmount());
            }
        }
    }

    private boolean isNotAbleSend(OrderModel orderModel) {
        return orderModel.getCompanyId() == null ||
                orderModel.getWarehouseId() == null ||
                orderModel.getType() == null ||
                orderModel.getOrderStatus() == null;
    }

    private AddressDto populateRegionData(OrderModel orderModel) {
        boolean hasOrderShippingInfo = hasOrderShippingInfo(orderModel);
        Long shippingAddressId = orderModel.getShippingAddressId();
        if(hasOrderShippingInfo) {
            AddressDto dimRegionData = new AddressDto();
            dimRegionData.setAddressId(shippingAddressId);
            dimRegionData.setProvinceId(orderModel.getShippingProvinceId());
            dimRegionData.setDistrictId(orderModel.getShippingDistrictId());
            dimRegionData.setWardId(orderModel.getShippingWardId());
            dimRegionData.setAddressDetail(orderModel.getShippingAddressDetail());
            dimRegionData.setPhone(orderModel.getShippingCustomerPhone());
            dimRegionData.setName(orderModel.getShippingCustomerName());
            return dimRegionData;
        }

        AddressData addressData = crmService.getAddress(shippingAddressId);
        if (addressData != null) {
            AddressDto dimRegionData = new AddressDto();
            dimRegionData.setAddressId(shippingAddressId);
            dimRegionData.setAddressDetail(addressData.getAddressDetail());
            dimRegionData.setPhone(addressData.getPhone1());
            dimRegionData.setName(addressData.getContact());
            if (addressData.getDistrictData() != null) {
                dimRegionData.setDistrictId(addressData.getDistrictData().getId());
            }
            if (addressData.getProvinceData() != null) {
                dimRegionData.setProvinceId(addressData.getProvinceData().getId());
            }
            if (addressData.getWardData() != null) {
                dimRegionData.setWardId(addressData.getWardData().getId());
            }
            return dimRegionData;
        }

        return null;
    }

    private boolean hasOrderShippingInfo(OrderModel orderModel) {
        return orderModel.getShippingProvinceId() != null;
    }

    private void setEntryAndTotalPrice(OrderData orderData, List<OrderEntryModel> entryModels, String currencyCode,
                                       List<com.vctek.orderservice.dto.OrderEntryData> oldOrderEntries, OrderStatus oldStatus) {
        List<OrderEntryData> entryDataList = new ArrayList<>();
        for (OrderEntryModel entry : entryModels) {
            List<SubOrderEntryModel> subOrderEntryModels = subOrderEntryRepository.findAllByOrderEntry(entry);
            List<ToppingOptionModel> optionModels = optionRepository.findAllByOrderEntry(entry);
            if (CollectionUtils.isNotEmpty(subOrderEntryModels)) {
                populateComboEntry(entryDataList, orderData, subOrderEntryModels, entry, oldOrderEntries, oldStatus);
            } else if (CollectionUtils.isNotEmpty(optionModels)) {
                populateToppingEntry(entryDataList, orderData, optionModels, entry, oldOrderEntries, oldStatus);
                populateSimpleEntry(entryDataList, orderData, entry, currencyCode, oldOrderEntries, oldStatus);
            } else {
                populateSimpleEntry(entryDataList, orderData, entry, currencyCode, oldOrderEntries, oldStatus);
            }
        }
        orderData.setTotalProduct(entryModels.size());
        orderData.setEntryDataList(entryDataList);
    }

    private void populateToppingEntry(List<OrderEntryData> entryDataList, OrderData orderData,
                                      List<ToppingOptionModel> toppingOptionModels, OrderEntryModel entry,
                                      List<com.vctek.orderservice.dto.OrderEntryData> oldOrderEntries, OrderStatus oldStatus) {
        for (ToppingOptionModel optionModel : toppingOptionModels) {
            int optQuantity = CommonUtils.getIntValue(optionModel.getQuantity());
            List<ToppingItemModel> itemModels = toppingItemRepository.findAllByToppingOptionModel(optionModel);
            for (ToppingItemModel itemModel : itemModels) {
                int productQuantity = CommonUtils.getIntValue(itemModel.getQuantity()) * optQuantity;
                double totalPriceWithoutPromotion = itemModel.getBasePrice() * productQuantity;
                OrderEntryData orderEntryData = new OrderEntryData();
                orderEntryData.setOrderEntryNumber(entry.getEntryNumber());
                orderEntryData.setOrderEntryId(entry.getId());
                orderEntryData.setToppingOptionId(optionModel.getId());
                orderEntryData.setPrice(itemModel.getBasePrice());
                orderEntryData.setProductId(itemModel.getProductId());
                orderEntryData.setDiscount(itemModel.getDiscount());
                orderEntryData.setDiscountType(itemModel.getDiscountType());
                orderEntryData.setQuantity(productQuantity);
                orderEntryData.setOrderEntryTotalPrice(totalPriceWithoutPromotion);
                double totalDiscount = CurrencyUtils.computeValue(itemModel.getDiscount(), itemModel.getDiscountType(), totalPriceWithoutPromotion);
                orderEntryData.setOrderEntryTotalDiscount(totalDiscount);
                orderEntryData.setTopping(true);
                orderEntryData.setDiscountOrderToItem(itemModel.getDiscountOrderToItem());
                if(!entry.isGiveAway()) {
                    orderEntryData.setOriginBasePrice(itemModel.getBasePrice());
                }
                orderEntryData.setGiveAway(entry.isGiveAway());

                populateHoldStockAndPreStock(orderData, entry, orderEntryData, oldOrderEntries, oldStatus);
                entryDataList.add(orderEntryData);

                long quantity = orderData.getTotalQuantity();
                quantity += productQuantity;
                orderData.setTotalQuantity(quantity);
            }
        }
    }

    private void populateComboEntry(List<OrderEntryData> entryDataList, OrderData orderData, List<SubOrderEntryModel> subOrderEntries, OrderEntryModel entry,
                                    List<com.vctek.orderservice.dto.OrderEntryData> oldOrderEntries, OrderStatus oldStatus) {
        for (SubOrderEntryModel subOrderEntryModel : subOrderEntries) {
            OrderEntryData orderEntryData = new OrderEntryData();
            orderEntryData.setOrderEntryNumber(entry.getEntryNumber());
            orderEntryData.setOrderEntryId(entry.getId());
            orderEntryData.setSubOrderEntryId(subOrderEntryModel.getId());
            orderEntryData.setPrice(subOrderEntryModel.getPrice());
            orderEntryData.setProductId(subOrderEntryModel.getProductId());
            orderEntryData.setQuantity(subOrderEntryModel.getQuantity());
            orderEntryData.setOrderEntryTotalPrice(subOrderEntryModel.getTotalPrice());
            orderEntryData.setOrderEntryTotalDiscount(subOrderEntryModel.getDiscountValue());
            orderEntryData.setComboId(entry.getProductId());
            orderEntryData.setComboQuantity(entry.getQuantity().intValue());
            orderEntryData.setHolding(entry.isHolding());
            orderEntryData.setPreOrder(entry.isPreOrder());
            orderEntryData.setSaleOff(entry.isSaleOff());
            orderEntryData.setDiscount(entry.getDiscount());
            orderEntryData.setDiscountType(entry.getDiscountType());
            if(!entry.isGiveAway()) {
                orderEntryData.setOriginBasePrice(entry.getBasePrice());
            }
            orderEntryData.setOrderEntryFixDiscount(subOrderEntryModel.getDiscountValue());
            orderEntryData.setGiveAway(entry.isGiveAway());

            populateHoldStockAndPreStock(orderData, entry, orderEntryData, oldOrderEntries, oldStatus);
            entryDataList.add(orderEntryData);
            long quantity = orderData.getTotalQuantity();
            quantity = subOrderEntryModel.getQuantity() != null ? quantity + subOrderEntryModel.getQuantity() : quantity;
            orderData.setTotalQuantity(quantity);
        }
    }

    private void populateSimpleEntry(List<OrderEntryData> entryDataList, OrderData orderData, OrderEntryModel entry, String currencyCode,
                                     List<com.vctek.orderservice.dto.OrderEntryData> oldOrderEntries, OrderStatus oldStatus) {
        OrderEntryData orderEntryData = new OrderEntryData();
        orderEntryData.setOrderEntryNumber(entry.getEntryNumber());
        orderEntryData.setOrderEntryId(entry.getId());
        orderEntryData.setPrice(entry.getBasePrice());
        orderEntryData.setProductId(entry.getProductId());
        orderEntryData.setQuantity(entry.getQuantity().intValue());
        orderEntryData.setOrderEntryTotalPrice(entry.getTotalPrice());
        orderEntryData.setOrderEntryTotalDiscount(entry.getTotalDiscount());
        orderEntryData.setOrderEntryFixDiscount(entry.getFixedDiscount());
        orderEntryData.setSaleOff(entry.isSaleOff());
        orderEntryData.setDiscount(entry.getDiscount());
        orderEntryData.setDiscountType(entry.getDiscountType());
        if(!entry.isGiveAway()) {
            orderEntryData.setOriginBasePrice(entry.getOriginBasePrice());
        }
        orderEntryData.setGiveAway(entry.isGiveAway());
        double totalPriceWithoutPromotion = entry.getBasePrice() * entry.getQuantity();
        List<DiscountValue> appliedDiscounts = DiscountValue.apply(entry.getQuantity(), totalPriceWithoutPromotion, Currency.DEFAULT_DIGITS,
                entry.getDiscountValues(), currencyCode);
        double totalPromotionDiscount = 0d;
        for (final Iterator it = appliedDiscounts.iterator(); it.hasNext(); ) {
            double appliedValue = ((DiscountValue) it.next()).getAppliedValue();
            totalPromotionDiscount += appliedValue;
        }
        orderEntryData.setOrderEntryPromotionDiscount(totalPromotionDiscount);
        orderEntryData.setDiscountOrderToItem(entry.getDiscountOrderToItem());
        orderEntryData.setHolding(entry.isHolding());
        orderEntryData.setPreOrder(entry.isPreOrder());

        populateHoldStockAndPreStock(orderData, entry, orderEntryData, oldOrderEntries, oldStatus);

        entryDataList.add(orderEntryData);

        long quantity = orderData.getTotalQuantity();
        quantity = entry.getQuantity() != null ? quantity + entry.getQuantity() : quantity;
        orderData.setTotalQuantity(quantity);
    }

    private CustomerDto populateCustomerData(OrderModel orderModel) {
        if (orderModel.getCustomerId() == null) {
            return null;
        }

        CustomerDto dimCustomerData = new CustomerDto();
        dimCustomerData.setCustomerId(orderModel.getCustomerId());
        dimCustomerData.setPhone(orderModel.getCustomerPhone());
        return dimCustomerData;
    }

    private void populateHoldStockAndPreStock(OrderData orderData, OrderEntryModel entry, OrderEntryData entryData,
                                              List<com.vctek.orderservice.dto.OrderEntryData> oldOrderEntries, OrderStatus oldOrderStatus) {
        if (oldOrderStatus == null) return;
        if (CollectionUtils.isEmpty(oldOrderEntries)) {
            if (OrderType.RETAIL.toString().equals(orderData.getOrderType())) {
                entryData.setHoldingStock(entryData.getQuantity());
            }
            return;
        }
        Map<Long, com.vctek.orderservice.dto.OrderEntryData> entryMap = oldOrderEntries.stream().collect(Collectors.toMap(i -> i.getId(), Function.identity()));
        com.vctek.orderservice.dto.OrderEntryData oldEntry = entryMap.get(entry.getId());
        if (oldEntry == null) {
            if (entryData.isTopping() || entryData.getComboId() != null || entryData.isGiveAway()) {
                entryData.setHoldingStock(entryData.getQuantity());
            }
            return;
        }
        OrderStatus newStatus = OrderStatus.findByCode(orderData.getOrderStatus());
        if (oldOrderStatus.value() < newStatus.value()) {
            changeToHighStatus(oldEntry, entryData, oldOrderStatus, newStatus);
            return;
        }

        changeToLowerStatus(oldEntry, entryData, oldOrderStatus, newStatus);
    }

    private void changeToHighStatus(com.vctek.orderservice.dto.OrderEntryData oldEntry, OrderEntryData entryData, OrderStatus oldStatus, OrderStatus newStatus) {
        if (OrderStatus.PRE_ORDER.equals(newStatus) || OrderStatus.CUSTOMER_CANCEL.equals(newStatus) || OrderStatus.SYSTEM_CANCEL.equals(newStatus)
            || (OrderStatus.CHANGE_TO_RETAIL.equals(newStatus) && OrderStatus.CONFIRMED.value() <= oldStatus.value() && oldStatus.value() < OrderStatus.SHIPPING.value())) {
            entryData.setHoldingStock(entryData.getQuantity());
            return;
        }

        if (OrderStatus.CONFIRMED.equals(newStatus)) {
            Long holdStock = com.vctek.util.CommonUtils.readValue(oldEntry.getHoldingStock());
            if (holdStock > 0 && oldEntry.isPreOrder()) {
                entryData.setPreStock(oldEntry.getHoldingStock().intValue());
            }

            if (holdStock > 0 && oldEntry.isHolding()) {
                Long quantity = oldEntry.getQuantity() - holdStock;
                entryData.setHoldingStock(quantity.intValue());
                return;
            }
            entryData.setHoldingStock(oldEntry.getQuantity().intValue());
        }
    }

    private void changeToLowerStatus(com.vctek.orderservice.dto.OrderEntryData oldEntry, OrderEntryData entryData, OrderStatus oldStatus, OrderStatus newStatus) {
        if ((OrderStatus.SHIPPING.equals(oldStatus) || OrderStatus.RETURNING.equals(oldStatus)) && newStatus.value() >= OrderStatus.CONFIRMED.value()) {
            entryData.setHoldingStock(entryData.getQuantity());
            return;
        }

        if (oldStatus.value() >= OrderStatus.CONFIRMED.value()) {
            entryData.setHoldingStock(entryData.getQuantity());
            return;
        }

        if (OrderStatus.PRE_ORDER.equals(oldStatus)) {
            Long holdStock = com.vctek.util.CommonUtils.readValue(oldEntry.getHoldingStock());
            if (oldEntry.isPreOrder() && holdStock > 0) {
                entryData.setPreStock(holdStock.intValue());
                return;
            }
            if (oldEntry.isHolding() && holdStock > 0) {
                entryData.setHoldingStock(holdStock.intValue());
            }
        }
    }

    private void sendKafkaCompletedLoyaltyRedeem(OrderEvent event) {
        OrderModel order = event.getOrderModel();
        OrderStatus oldStatus = event.getOldOrderStatus();
        if (!EventType.IMPORT_CHANGE_STATUS_ONLINE.equals(event.getEventType()) || oldStatus == null) return;
        OrderStatus newStatus = OrderStatus.findByCode(order.getOrderStatus());
        double redeemAmount = com.vctek.util.CommonUtils.readValue(order.getRedeemAmount());
        if(OrderType.ONLINE.toString().equals(order.getType()) && redeemAmount > 0) {
            if (OrderStatus.COMPLETED.toString().equals(order.getOrderStatus())) {
                loyaltyTransactionProducerService.sendLoyaltyTransactionKafka(order, redeemAmount, TransactionType.COMPLETE_PENDING_REDEEM);
                return;
            }
            if ((oldStatus.value() >= OrderStatus.CONFIRMED.value() && oldStatus.value() < OrderStatus.COMPLETED.value()
                    && isOrderStatusBetweenCompletedAndChangeToRetail(newStatus))) {
                loyaltyTransactionProducerService.sendLoyaltyTransactionKafka(order, redeemAmount, TransactionType.CANCEL_PENDING_REDEEM);
            }
            if (OrderStatus.CHANGE_TO_RETAIL.equals(newStatus)) {
                loyaltyTransactionProducerService.sendLoyaltyTransactionKafka(order, redeemAmount, TransactionType.CANCEL_PENDING_REDEEM);
            }
        }

        if(OrderType.RETAIL.toString().equals(order.getType()) && redeemAmount > 0 && OrderStatus.CHANGE_TO_RETAIL.equals(newStatus)) {
            loyaltyTransactionProducerService.sendLoyaltyTransactionKafka(order, redeemAmount, TransactionType.REDEEM);
        }
    }

    private boolean isOrderStatusBetweenCompletedAndChangeToRetail(OrderStatus orderStatus) {
        return orderStatus.value() > OrderStatus.COMPLETED.value() && orderStatus.value() < OrderStatus.CHANGE_TO_RETAIL.value();
    }

    @Autowired
    public void setPaymentTransactionService(PaymentTransactionService paymentTransactionService) {
        this.paymentTransactionService = paymentTransactionService;
    }

    @Autowired
    public void setOrderEntryRepository(OrderEntryRepository orderEntryRepository) {
        this.orderEntryRepository = orderEntryRepository;
    }

    @Autowired
    public void setSubOrderEntryRepository(SubOrderEntryRepository subOrderEntryRepository) {
        this.subOrderEntryRepository = subOrderEntryRepository;
    }

    @Autowired
    public void setOptionRepository(ToppingOptionRepository optionRepository) {
        this.optionRepository = optionRepository;
    }

    @Autowired
    public void setToppingItemRepository(ToppingItemRepository toppingItemRepository) {
        this.toppingItemRepository = toppingItemRepository;
    }

    @Autowired
    public void setCrmService(CRMService crmService) {
        this.crmService = crmService;
    }

    @Autowired
    public void setOrderHistoryRepository(OrderHistoryRepository orderHistoryRepository) {
        this.orderHistoryRepository = orderHistoryRepository;
    }

    @Autowired
    public void setPromotionResultService(PromotionResultService promotionResultService) {
        this.promotionResultService = promotionResultService;
    }

    @Autowired
    public void setAbstractPromotionActionRepository(AbstractPromotionActionRepository abstractPromotionActionRepository) {
        this.abstractPromotionActionRepository = abstractPromotionActionRepository;
    }

    @Autowired
    public void setCouponRedemptionService(CouponRedemptionService couponRedemptionService) {
        this.couponRedemptionService = couponRedemptionService;
    }

    @Autowired
    public void setRecalculateOrderReportOutStream(RecalculateOrderReportOutStream recalculateOrderReportOutStream) {
        this.recalculateOrderReportOutStream = recalculateOrderReportOutStream;
    }

    @Autowired
    public void setCustomerOptionService(OrderSettingCustomerOptionService customerOptionService) {
        this.customerOptionService = customerOptionService;
    }

    @Autowired
    public void setLoyaltyTransactionProducerService(LoyaltyTransactionProducerService loyaltyTransactionProducerService) {
        this.loyaltyTransactionProducerService = loyaltyTransactionProducerService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setTagService(TagService tagService) {
        this.tagService = tagService;
    }
}
