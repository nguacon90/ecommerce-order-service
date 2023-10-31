package com.vctek.orderservice.service.impl;

import com.vctek.converter.Populator;
import com.vctek.dto.request.AssignCardParameter;
import com.vctek.dto.request.CheckValidCardParameter;
import com.vctek.exception.ServiceException;
import com.vctek.kafka.data.loyalty.TransactionData;
import com.vctek.kafka.data.loyalty.TransactionRequest;
import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.orderservice.dto.AvailablePointAmountData;
import com.vctek.orderservice.dto.AwardLoyaltyData;
import com.vctek.orderservice.dto.PaymentMethodData;
import com.vctek.orderservice.dto.ProductCanRewardDto;
import com.vctek.orderservice.dto.request.AvailablePointAmountRequest;
import com.vctek.orderservice.dto.request.CustomerRequest;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.LoyaltyClient;
import com.vctek.orderservice.feignclient.dto.CustomerData;
import com.vctek.orderservice.feignclient.dto.LoyaltyCardData;
import com.vctek.orderservice.feignclient.dto.ProductSearchRequest;
import com.vctek.orderservice.feignclient.dto.RewardSettingData;
import com.vctek.orderservice.kafka.producer.OrderLoyaltyRewardRequestProducer;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionResultService;
import com.vctek.orderservice.repository.EntryRepository;
import com.vctek.orderservice.repository.SubOrderEntryRepository;
import com.vctek.orderservice.repository.ToppingItemRepository;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.util.CurrencyUtils;
import com.vctek.util.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LoyaltyServiceImpl implements LoyaltyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoyaltyServiceImpl.class);
    private LoyaltyClient loyaltyClient;
    private PromotionResultService promotionResultService;
    private LoyaltyTransactionService loyaltyTransactionService;
    private CalculationService calculationService;
    private ProductRedeemRateUseService productRedeemRateUseService;
    private CartService cartService;
    private ProductSearchService productSearchService;
    private FinanceService financeService;
    private CustomerService customerService;
    private Populator<OrderModel, TransactionRequest> transactionRequestPopulator;
    private OrderLoyaltyRewardRequestProducer orderLoyaltyRewardRequestProducer;
    private OrderService orderService;
    private EntryRepository entryRepository;
    private ToppingItemRepository toppingItemRepository;
    private SubOrderEntryRepository subOrderEntryRepository;

    @Override
    public void assignCardToCustomerIfNeed(String cardNumber, CustomerRequest customerRequest, boolean isExchange, Long warehouseId) {
        if (StringUtils.isBlank(cardNumber) || customerRequest == null || isExchange) {
            return;
        }

        LoyaltyCardData loyaltyCardData = this.findByCardNumber(cardNumber, customerRequest.getCompanyId());
        if (StringUtils.isNotBlank(loyaltyCardData.getAssignedPhone())) {
            return;
        }

        try {
            AssignCardParameter parameter = new AssignCardParameter();
            parameter.setCompanyId(customerRequest.getCompanyId());
            parameter.setPhone(customerRequest.getPhone());
            parameter.setCardNumber(cardNumber);
            parameter.setEmail(customerRequest.getEmail());
            parameter.setCustomerName(customerRequest.getName());
            parameter.setAddressId(customerRequest.getAddressId());
            parameter.setCustomerId(customerRequest.getId());
            parameter.setWarehouseId(warehouseId);
            loyaltyClient.assignCard(parameter);

        } catch (ServiceException e) {
            LOGGER.error("ASSIGN CARD ERROR: cardNumber: {}, phone: {}, companyId: {}, customerId: {}, loyalty error: {}",
                    cardNumber, customerRequest.getPhone(), customerRequest.getCompanyId(), customerRequest.getId(),
                    e.getMessage());
        } catch (RuntimeException e) {
            LOGGER.error("ASSIGN CARD ERROR: cardNumber: {}, phone: {}, companyId: {}, customerId: {}",
                    cardNumber, customerRequest.getPhone(), customerRequest.getCompanyId(), customerRequest.getId());
        }
    }

    @Override
    public boolean isValid(CheckValidCardParameter parameter) {
        Boolean validCardNumber = loyaltyClient.isValidCardNumber(parameter);
        return Boolean.TRUE.equals(validCardNumber);
    }

    @Override
    public boolean isApplied(OrderModel orderModel) {
        CheckValidCardParameter parameter = new CheckValidCardParameter();
        parameter.setCardNumber(orderModel.getCardNumber());
        parameter.setCompanyId(orderModel.getCompanyId());
        return loyaltyClient.isAppliedCardNumber(parameter);
    }

    @Override
    public List<ProductCanRewardDto> getAwardProducts(AbstractOrderModel model) {
        List<ProductCanRewardDto> results = new ArrayList<>();
        boolean isLimitedApplyPromotionAndReward = customerService.limitedApplyPromotionAndReward(model.getCustomerId(), model.getCompanyId());
        if (isLimitedApplyPromotionAndReward) {
            return results;
        }
        Set<PromotionSourceRuleModel> sourceRuleModels = promotionResultService.findAllPromotionSourceRulesAppliedToOrder(model);
        if (CollectionUtils.isNotEmpty(sourceRuleModels)) {
            for (PromotionSourceRuleModel sourceRuleModel : sourceRuleModels) {
                if (Boolean.FALSE.equals(sourceRuleModel.isAllowReward())) {
                    return results;
                }
            }
        }

        List<AbstractOrderEntryModel> entries = entryRepository.findAllByOrder(model);
        populateToppingItem(results, model);
        for (AbstractOrderEntryModel entry : entries) {
            if (cartService.isComboEntry(entry) || entry.isSaleOff()) continue;
            ProductCanRewardDto dto = new ProductCanRewardDto();
            dto.setOrderEntryId(entry.getId());
            dto.setProductId(entry.getProductId());
            dto.setFinalPrice(CommonUtils.readValue(entry.getFinalPrice()) -
                    CommonUtils.readValue(entry.getDiscountOrderToItem()));
            productShouldPopulateRewardData(entry, dto);
            results.add(dto);

        }
        List<ProductCanRewardDto> comboDto = populateComboEntries(entries, model);
        if (CollectionUtils.isNotEmpty(comboDto)) results.addAll(comboDto);
        return results;
    }

    private List<ProductCanRewardDto> populateComboEntries(List<AbstractOrderEntryModel> entries, AbstractOrderModel orderModel) {
        List<AbstractOrderEntryModel> comboEntries = entries.stream()
                .filter(e -> cartService.isComboEntry(e))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(comboEntries)) {
            return new ArrayList<>();
        }
        Set<Long> comboIds = comboEntries.stream().map(cbe -> cbe.getProductId()).collect(Collectors.toSet());
        String productIds = comboIds.stream()
                .map(productId -> String.valueOf(productId))
                .collect(Collectors.joining(","));
        ProductSearchRequest request = new ProductSearchRequest();
        request.setCompanyId(orderModel.getCompanyId());
        request.setIds(productIds);
        request.setPageSize(comboEntries.size());
        List<ProductSearchModel> productSearchData = productSearchService.findAllByCompanyId(request);
        Map<Long, Boolean> productSearchAllowReward = productSearchData.stream()
                .collect(Collectors.toMap(p -> p.getId(), p -> p.isAllowReward()));
        List<ProductCanRewardDto> list = new ArrayList<>();

        for (AbstractOrderEntryModel entry : comboEntries) {
            if (!productSearchAllowReward.containsKey(entry.getProductId()) || !productSearchAllowReward.get(entry.getProductId()))
                continue;
            ProductCanRewardDto dto = new ProductCanRewardDto();
            dto.setProductId(entry.getProductId());
            dto.setOrderEntryId(entry.getId());
            dto.setFinalPrice(CommonUtils.readValue(entry.getFinalPrice()) - CommonUtils.readValue(entry.getDiscountOrderToItem()));
            populateSubOrderEntry(entry, dto);
            list.add(dto);
        }
        return list;
    }

    private void populateSubOrderEntry(AbstractOrderEntryModel entryModel, ProductCanRewardDto productCanRewardDto) {
        List<ProductCanRewardDto> list = new ArrayList<>();
        List<SubOrderEntryModel> subOrderEntries = subOrderEntryRepository.findAllByOrderEntry(entryModel);
        for (SubOrderEntryModel subOrderEntry : subOrderEntries) {
            ProductCanRewardDto dto = new ProductCanRewardDto();
            dto.setOrderEntryId(productCanRewardDto.getOrderEntryId());
            dto.setProductId(subOrderEntry.getProductId());
            dto.setSubOrderEntryId(subOrderEntry.getId());
            dto.setFinalPrice(CommonUtils.readValue(subOrderEntry.getFinalPrice()));
            list.add(dto);
        }
        productCanRewardDto.setSubOrderEntries(list);
    }

    protected void productShouldPopulateRewardData(AbstractOrderEntryModel entry, ProductCanRewardDto dto) {
        Set<PromotionSourceRuleModel> sourceRuleModels = promotionResultService.findAllPromotionSourceRulesAppliedToOrderEntry(entry);
        Optional<PromotionSourceRuleModel> ruleModelOptional = sourceRuleModels.stream()
                .filter(i -> Boolean.FALSE.equals(i.isAllowReward())).findFirst();
        if (ruleModelOptional.isPresent()) {
            Long quantity = entry.getQuantity();
            Long qty = promotionResultService.getTotalAppliedQuantityOf(entry);
            quantity -= CommonUtils.readValue(qty);
            if (quantity <= 0) {
                dto.setFinalPrice(0d);
                return;
            }

            double totalDiscount = CommonUtils.readValue(entry.getFixedDiscount()) + CommonUtils.readValue(entry.getDiscountOrderToItem());
            double finalPrice = (quantity * entry.getBasePrice()) - (totalDiscount / entry.getQuantity() * quantity);
            dto.setFinalPrice(finalPrice);
        }
    }

    private void populateToppingItem(List<ProductCanRewardDto> results, AbstractOrderModel model) {
        Set<ToppingItemModel> toppingItemModels = toppingItemRepository.findAllByOrderId(model.getId());
        for (ToppingItemModel item : toppingItemModels) {
            ToppingOptionModel optionModel = item.getToppingOptionModel();
            int optionQty = CommonUtils.readValue(optionModel.getQuantity());
            ProductCanRewardDto dto = new ProductCanRewardDto();
            dto.setToppingOptionId(optionModel.getId());
            dto.setOrderEntryId(optionModel.getOrderEntry().getId());
            dto.setProductId(item.getProductId());
            dto.setToppingItemId(item.getId());
            dto.setFinalPrice(computeFinalPriceOfToppingItem(item, optionQty, true));
            results.add(dto);
        }
    }

    private double computeFinalPriceOfToppingItem(ToppingItemModel item, int optionQty, boolean isReward) {
        Long totalQty = CommonUtils.readValue(item.getQuantity()) * Long.valueOf(optionQty);
        double discount = 0d;
        if (isReward) {
            discount = CommonUtils.readValue(item.getDiscountOrderToItem());
        }
        double totalPrice = CommonUtils.readValue(item.getBasePrice()) * totalQty;
        double totalDiscount = discount + CurrencyUtils.computeValue(item.getDiscount(), item.getDiscountType(), totalPrice);
        return totalPrice - totalDiscount;
    }

    @Override
    public void reward(OrderModel orderModel) {

        if (StringUtils.isBlank(orderModel.getCardNumber()) && orderModel.getCustomerId() == null) {
            return;
        }

        boolean isLimitedApplyPromotionAndReward = customerService.limitedApplyPromotionAndReward(orderModel.getCustomerId(), orderModel.getCompanyId());
        if (isLimitedApplyPromotionAndReward) return;

        if (StringUtils.isNotBlank(orderModel.getCardNumber())) {
            rewardByCardNumber(orderModel);
            return;
        }
        rewardByPhone(orderModel);
    }

    private void rewardByPhone(OrderModel orderModel) {
        if (orderModel.getTotalRewardAmount() == null || orderModel.getTotalRewardAmount() <= 0) {
            return;
        }
        Long customerId = orderModel.getCustomerId();
        CustomerData customer = customerService.getBasicCustomerInfo(customerId, orderModel.getCompanyId());
        if (customer == null || StringUtils.isBlank(customer.getPhone())) {
            LOGGER.warn("CANNOT REWARD INVALID CUSTOMER INFO: id: {}", customerId);
            return;
        }
        TransactionRequest transactionRequest = populateTransactionRequest(orderModel, customer);
        orderLoyaltyRewardRequestProducer.sendLoyaltyRewardRequestKafka(transactionRequest, KafkaMessageType.REWARD_BY_PHONE);
    }

    private void rewardByCardNumber(OrderModel orderModel) {
        if (orderModel.getTotalRewardAmount() == null || orderModel.getTotalRewardAmount() <= 0) {
            return;
        }

        TransactionRequest transactionRequest = populateTransactionRequest(orderModel, null);
        orderLoyaltyRewardRequestProducer.sendLoyaltyRewardRequestKafka(transactionRequest, KafkaMessageType.REWARD_BY_CARD_NUMBER);
    }

    private TransactionRequest populateTransactionRequest(OrderModel orderModel, CustomerData customer) {
        TransactionRequest transactionRequest = createBasicTransactionRequest(orderModel, orderModel.getTotalRewardAmount());
        populateTransactionRequestRewardOrRedeem(orderModel, transactionRequest);
        if (customer != null) {
            transactionRequest.setCustomerId(customer.getId());
            transactionRequest.setCustomerName(customer.getName());
            transactionRequest.setPhone(customer.getPhone());
            transactionRequest.setEmail(customer.getEmail());
        }
        return transactionRequest;
    }

    @Override
    public TransactionData callReward(OrderModel orderModel, Double awardMount) {
        TransactionRequest transactionRequest = createBasicTransactionRequest(orderModel, awardMount);
        populateTransactionRequestRewardOrRedeem(orderModel, transactionRequest);
        return loyaltyClient.reward(transactionRequest);
    }

    @Override
    public LoyaltyCardData findByCardNumber(String cardNumber, Long companyId) {
        return loyaltyClient.getDetailByCardNumber(cardNumber, companyId);
    }

    @Override
    public TransactionData redeem(OrderModel orderModel, Double amount) {
        if (amount <= 0) return null;
        TransactionRequest transactionRequest = createBasicTransactionRequest(orderModel, amount);
        populateTransactionRequestRewardOrRedeem(orderModel, transactionRequest);
        TransactionData transactionData = loyaltyClient.redeem(transactionRequest);
        createLoyaltyTransaction(orderModel.getCode(), transactionData.getInvoiceNumber(), TransactionType.REDEEM.name(), transactionData.getConversionRate(), null);
        return transactionData;
    }

    @Override
    public AvailablePointAmountData computeAvailablePointAmountOf(AvailablePointAmountRequest request) {
        AbstractOrderModel orderModel = cartService.findByOrderCodeAndCompanyId(request.getOrderCode(), request.getCompanyId());
        validateOrderModel(orderModel);
        LoyaltyCardData cardData = this.findByCardNumber(request.getCardNumber(), request.getCompanyId());
        double totalPriceEntries = computeOrderEntry(orderModel.getEntries(), orderModel.getCompanyId());
        double totalDiscount = CommonUtils.readValue(orderModel.getTotalDiscount());
        Double totalPriceOfEntry = Math.min((totalPriceEntries - totalDiscount), CommonUtils.readValue(orderModel.getFinalPrice()));
        double point = Math.ceil((totalPriceOfEntry / CommonUtils.readValue(cardData.getConversionRate())));
        AvailablePointAmountData dto = new AvailablePointAmountData();
        dto.setOriginAvailableAmount(CommonUtils.readValue(cardData.getPointAmount()));
        dto.setAvailableAmount(Math.floor(CommonUtils.readValue(cardData.getPointAmount())));
        dto.setPointAmount(point);
        dto.setConversionRate(cardData.getConversionRate());
        return dto;
    }

    protected void validateOrderModel(AbstractOrderModel orderModel) {
        if (orderModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_CODE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    private double computeOrderEntry(List<AbstractOrderEntryModel> entries, Long companyId) {
        if (CollectionUtils.isEmpty(entries)) return 0;
        Map<Long, Boolean> canRedeem = checkIfProductCanRedeem(entries, companyId);
        double totalPriceOfEntry = entries.stream()
                .filter(entry -> canRedeem.containsKey(entry.getProductId()) && canRedeem.get(entry.getProductId()))
                .mapToDouble(entry -> CommonUtils.readValue(entry.getFinalPrice())).sum();
        Double totalPriceOfToppingItem;
        List<AbstractOrderEntryModel> abstractToppingEntries = entries.stream()
                .filter(entry -> CollectionUtils.isNotEmpty(entry.getToppingOptionModels()))
                .collect(Collectors.toList());
        for (AbstractOrderEntryModel entry : abstractToppingEntries) {
            Set<ToppingOptionModel> toppingOptionModels = entry.getToppingOptionModels();
            for (ToppingOptionModel toppingOptionModel : toppingOptionModels) {
                Set<ToppingItemModel> toppingItemModels = toppingOptionModel.getToppingItemModels();
                if (CollectionUtils.isEmpty(toppingItemModels)) continue;
                int optionQty = CommonUtils.readValue(toppingOptionModel.getQuantity());
                totalPriceOfToppingItem = toppingItemModels.stream()
                        .filter(item -> canRedeem.containsKey(item.getProductId()) && canRedeem.get(item.getProductId()))
                        .mapToDouble(item -> computeFinalPriceOfToppingItem(item, optionQty, false))
                        .sum();
                totalPriceOfEntry += CommonUtils.readValue(totalPriceOfToppingItem);
            }
        }
        return totalPriceOfEntry;
    }

    protected Map<Long, Boolean> checkIfProductCanRedeem(List<AbstractOrderEntryModel> entries, Long companyId) {
        Set<Long> setProductOfEntry = entries.stream().map(AbstractOrderEntryModel::getProductId).collect(Collectors.toSet());
        for (AbstractOrderEntryModel entry : entries) {
            if (CollectionUtils.isNotEmpty(entry.getToppingOptionModels())) {
                Set<Long> listProductOfItemTopping = entry.getToppingOptionModels().stream()
                        .flatMap(option -> option.getToppingItemModels().stream())
                        .map(ToppingItemModel::getProductId)
                        .collect(Collectors.toSet());
                setProductOfEntry.addAll(listProductOfItemTopping);
            }
        }
        List<Long> listProductOfEntry = setProductOfEntry.stream().collect(Collectors.toList());
        return productRedeemRateUseService.productCanRedeem(companyId, listProductOfEntry);
    }

    @Override
    public TransactionData refund(OrderModel orderModel, ReturnOrderModel returnOrderModel, Double amount) {
        LoyaltyTransactionModel loyaltyTransactionModel = loyaltyTransactionService.findLastByOrderCodeAndListType(orderModel.getCode(),
                Arrays.asList(TransactionType.REDEEM.name(), TransactionType.REFUND.name()));
        if (loyaltyTransactionModel == null) {
            ErrorCodes err = ErrorCodes.HAS_NOT_EXISTED_REFER_INVOICE_FOR_REFUND;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        TransactionRequest transactionRequest = createBasicTransactionRequest(orderModel, amount);
        populateTransactionRequestRevertOrRefund(orderModel, returnOrderModel, loyaltyTransactionModel.getInvoiceNumber(), transactionRequest);
        TransactionData transactionData = loyaltyClient.refund(transactionRequest);
        createLoyaltyTransaction(orderModel.getCode(), transactionData.getInvoiceNumber(), TransactionType.REFUND.name(), transactionData.getConversionRate(), returnOrderModel != null ? returnOrderModel.getId() : null);
        return transactionData;
    }

    @Override
    public TransactionData revert(OrderModel orderModel, ReturnOrderModel returnOrderModel, Double amount) {
        LoyaltyTransactionModel loyaltyTransactionModel = loyaltyTransactionService.findLastByOrderCodeAndListType(orderModel.getCode(),
                Arrays.asList(TransactionType.AWARD.name(), TransactionType.REVERT.name()));
        if (loyaltyTransactionModel == null) {
            ErrorCodes err = ErrorCodes.HAS_NOT_EXISTED_REFER_INVOICE_FOR_REVERT;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        TransactionRequest transactionRequest = createBasicTransactionRequest(orderModel, amount);
        populateTransactionRequestRevertOrRefund(orderModel, returnOrderModel, loyaltyTransactionModel.getInvoiceNumber(), transactionRequest);
        TransactionData transactionData = loyaltyClient.revert(transactionRequest);
        createLoyaltyTransaction(orderModel.getCode(), transactionData.getInvoiceNumber(), TransactionType.REVERT.name(), transactionData.getConversionRate(), returnOrderModel != null ? returnOrderModel.getId() : null);
        return transactionData;
    }

    @Override
    public TransactionData updateReward(OrderModel orderModel) {
        LoyaltyTransactionModel loyaltyTransactionModel = loyaltyTransactionService.findLastByOrderCodeAndListType(orderModel.getCode(),
                Arrays.asList(TransactionType.AWARD.name(), TransactionType.REVERT.name()));
        if (loyaltyTransactionModel == null) {
            if (orderModel.getTotalRewardAmount() != null && orderModel.getTotalRewardAmount() > 0) {
                TransactionData transactionData = this.callReward(orderModel, orderModel.getTotalRewardAmount());
                createLoyaltyTransaction(orderModel.getCode(), transactionData.getInvoiceNumber(), TransactionType.AWARD.name(), transactionData.getConversionRate(), null);
                return transactionData;
            }
            return null;
        }
        TransactionRequest transactionRequest = createBasicTransactionRequest(orderModel, orderModel.getTotalRewardAmount());
        transactionRequest.setInvoiceNumber(loyaltyTransactionModel.getInvoiceNumber());
        TransactionData transactionData = loyaltyClient.updateReward(transactionRequest, loyaltyTransactionModel.getInvoiceNumber());
        loyaltyTransactionModel.setConversionRate(transactionData.getConversionRate());

        loyaltyTransactionService.save(loyaltyTransactionModel);
        return transactionData;
    }

    @Override
    public TransactionData updateRedeem(OrderModel orderModel) {
        LoyaltyTransactionModel loyaltyTransactionModel = loyaltyTransactionService.findLastByOrderCodeAndListType(orderModel.getCode(),
                Arrays.asList(TransactionType.REDEEM.name(), TransactionType.REFUND.name()));

        double redeemAmount = 0d;
        Set<PaymentTransactionModel> paymentTransactions = orderModel.getPaymentTransactions();
        PaymentMethodData paymentMethodData = financeService.getPaymentMethodByCode(PaymentMethodType.LOYALTY_POINT.code());
        for (PaymentTransactionModel payment : paymentTransactions) {
            if (!payment.isDeleted() && paymentMethodData.getId().equals(payment.getPaymentMethodId())) {
                redeemAmount = payment.getAmount();
                break;
            }
        }
        if (loyaltyTransactionModel == null) {
            return this.redeem(orderModel, redeemAmount);
        }
        TransactionRequest transactionRequest = createBasicTransactionRequest(orderModel, redeemAmount);
        transactionRequest.setInvoiceNumber(loyaltyTransactionModel.getInvoiceNumber());
        TransactionData transactionData = loyaltyClient.updateRedeem(transactionRequest, loyaltyTransactionModel.getInvoiceNumber());
        loyaltyTransactionModel.setConversionRate(transactionData.getConversionRate());
        loyaltyTransactionService.save(loyaltyTransactionModel);
        return transactionData;
    }

    @Override
    public OrderModel recalculateRewardAmount(OrderModel orderModel) {
        List<ProductCanRewardDto> productCanRewardDtoList = this.getAwardProducts(orderModel);
        double loyaltyAmount = calculationService.calculateLoyaltyAmount(productCanRewardDtoList, orderModel.getCompanyId());
        return (OrderModel) calculationService.saveRewardAmountToEntries(orderModel, loyaltyAmount, loyaltyAmount, productCanRewardDtoList, true);
    }

    @Override
    public TransactionData updateRefund(ReturnOrderModel returnOrderModel) {
        OrderModel originOrder = returnOrderModel.getOriginOrder();
        LoyaltyTransactionModel loyaltyTransactionModel = loyaltyTransactionService.findLastByOrderCodeAndListType(originOrder.getCode(),
                Collections.singletonList(TransactionType.REFUND.name()));

        if (loyaltyTransactionModel == null) {
            ErrorCodes err = ErrorCodes.HAS_NOT_EXISTED_REFER_INVOICE_FOR_REFUND;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        TransactionRequest transactionRequest = createBasicTransactionRequest(originOrder, returnOrderModel.getRefundAmount());
        transactionRequest.setInvoiceNumber(loyaltyTransactionModel.getInvoiceNumber());
        TransactionData transactionData = loyaltyClient.updateRefund(transactionRequest, loyaltyTransactionModel.getInvoiceNumber());
        loyaltyTransactionModel.setConversionRate(transactionData.getConversionRate());
        loyaltyTransactionService.save(loyaltyTransactionModel);
        return transactionData;
    }

    @Override
    public void updateRewardRedeemForOrder(OrderModel orderToUpdate) {
        if (StringUtils.isBlank(orderToUpdate.getCardNumber())) {
            return;
        }

        if (OrderType.ONLINE.toString().equals(orderToUpdate.getType()) &&
                !OrderStatus.COMPLETED.code().equals(orderToUpdate.getOrderStatus())) {
            //Ignore reward, redeem for not completed online order
            return;
        }

        orderToUpdate = this.recalculateRewardAmount(orderToUpdate);
        TransactionData rewardTransactionData = this.updateReward(orderToUpdate);
        if (rewardTransactionData != null) {
            orderToUpdate.setRewardPoint(rewardTransactionData.getPoint());
        }

        TransactionData redeemTransactionData = this.updateRedeem(orderToUpdate);
        if (redeemTransactionData != null) {
            orderToUpdate.setRedeemAmount(redeemTransactionData.getRedeemAmount());
        }
    }

    @Override
    public TransactionData findByInvoiceNumberAndCompanyIdAndType(TransactionRequest transactionRequest) {
        return loyaltyClient.findByInvoiceNumberAndCompanyIdAndType(transactionRequest, transactionRequest.getInvoiceNumber());
    }

    @Override
    public AwardLoyaltyData getLoyaltyPointsOf(AbstractOrderModel abstractOrderModel) {
        AwardLoyaltyData data = new AwardLoyaltyData();
        boolean isLimitedApplyPromotionAndReward = customerService.limitedApplyPromotionAndReward(abstractOrderModel.getCustomerId(), abstractOrderModel.getCompanyId());
        if (isLimitedApplyPromotionAndReward) {
            return data;
        }
        List<ProductCanRewardDto> productCanRewardDtoList = this.getAwardProducts(abstractOrderModel);
        if (CollectionUtils.isEmpty(productCanRewardDtoList)) {
            return data;
        }

        calculationService.calculateLoyaltyAmount(productCanRewardDtoList, abstractOrderModel.getCompanyId());
        RewardSettingData rewardUnit = loyaltyClient.findRewardUnit(abstractOrderModel.getCompanyId());
        if (rewardUnit == null) {
            return data;
        }
        double conversionRate = rewardUnit.getConversionRate();
        if (conversionRate == 0) {
            return data;
        }

        Map<Long, Double> toppingPoints = data.getToppingPoints();
        Map<Long, Double> entryPoints = data.getEntryPoints();
        for (ProductCanRewardDto e : productCanRewardDtoList) {
            double awardPoint = e.getAwardAmount() / conversionRate;
            double point = calculationService.round(awardPoint, 2);
            if (e.getToppingItemId() != null) {
                toppingPoints.put(e.getToppingItemId(), point);
            } else {
                entryPoints.put(e.getOrderEntryId(), point);
            }
        }
        return data;
    }

    private TransactionRequest createBasicTransactionRequest(OrderModel orderModel, double amount) {
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequestPopulator.populate(orderModel, transactionRequest);
        transactionRequest.setAmount(amount);
        return transactionRequest;
    }

    private TransactionRequest populateTransactionRequestRewardOrRedeem(OrderModel orderModel, TransactionRequest transactionRequest) {
        transactionRequest.setReferCode(orderModel.getCode());
        transactionRequest.setReferType(orderModel.getType());
        Date transactionDate = OrderType.ONLINE.toString().equals(orderModel.getType()) ? Calendar.getInstance().getTime() : orderModel.getCreatedTime();
        transactionRequest.setTransactionDate(transactionDate);
        return transactionRequest;
    }

    private TransactionRequest populateTransactionRequestRevertOrRefund(OrderModel orderModel, ReturnOrderModel returnOrderModel, String referInvoice, TransactionRequest transactionRequest) {
        if (returnOrderModel != null) {
            transactionRequest.setReferCode(returnOrderModel.getId().toString());
            transactionRequest.setReferType(OrderType.RETURN_ORDER.name());
            transactionRequest.setTransactionDate(returnOrderModel.getCreatedTime());
        } else {
            transactionRequest.setReferCode(orderModel.getCode());
            transactionRequest.setReferType(OrderType.ONLINE.name());
            transactionRequest.setTransactionDate(Calendar.getInstance().getTime());
        }
        transactionRequest.setReferInvoiceNumber(referInvoice);
        return transactionRequest;
    }

    @Override
    public LoyaltyTransactionModel createLoyaltyTransaction(String orderCode, String invoiceNumber, String transactionType, Double conversionRate, Long returnOrderId) {
        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        loyaltyTransactionModel.setOrderCode(orderCode);
        loyaltyTransactionModel.setInvoiceNumber(invoiceNumber);
        loyaltyTransactionModel.setType(transactionType);
        loyaltyTransactionModel.setConversionRate(conversionRate);
        loyaltyTransactionModel.setReturnOrderId(returnOrderId);
        return loyaltyTransactionService.save(loyaltyTransactionModel);
    }

    @Override
    public double convertAmountToPoint(double amount, Long companyId) {
        TransactionRequest request = new TransactionRequest();
        request.setCompanyId(companyId);
        request.setAmount(amount);
        return loyaltyClient.convertAmountToPoint(request);
    }

    @Override
    @Transactional
    public void splitRewardAmountToEntriesAndCreateLoyaltyTransaction(TransactionData transactionData) {
        OrderModel orderModel = orderService.findByCodeAndCompanyId(transactionData.getReferCode(), transactionData.getCompanyId());
        if (orderModel == null) {
            LOGGER.error("NOT FOUND ORDER CODE FOR SAVE REWARD TRANSACTION: orderCode: {}, invoiceNumber: {}", transactionData.getReferCode(), transactionData.getInvoiceNumber());
            return;
        }
        orderModel.setCardNumber(transactionData.getCardNumber());
        orderService.save(orderModel);
        List<ProductCanRewardDto> productCanRewardDtoList = this.getAwardProducts(orderModel);
        calculationService.calculateLoyaltyAmount(productCanRewardDtoList, orderModel.getCompanyId());
        calculationService.saveRewardAmountToEntries(orderModel, transactionData.getPoint(), transactionData.getAwardAmount(), productCanRewardDtoList, false);
        createLoyaltyTransaction(transactionData.getReferCode(), transactionData.getInvoiceNumber(), TransactionType.AWARD.name(), transactionData.getConversionRate(), null);
    }

    @Override
    public TransactionData createRedeemPending(OrderModel orderModel, Double amount) {
        if (amount <= 0) return null;
        TransactionRequest transactionRequest = createBasicTransactionRequest(orderModel, amount);
        populateTransactionRequestRewardOrRedeem(orderModel, transactionRequest);
        TransactionData transactionData = loyaltyClient.createRedeemPending(transactionRequest);

        createLoyaltyTransaction(orderModel.getCode(), transactionData.getInvoiceNumber(), TransactionType.REDEEM.name(), transactionData.getConversionRate(), null);
        return transactionData;
    }

    @Override
    @Transactional
    public void cancelPendingRedeem(OrderModel model) {
        LoyaltyTransactionModel transactionModel = loyaltyTransactionService.findLastByOrderCodeAndListType(model.getCode(),
                Arrays.asList(TransactionType.REDEEM.name()));
        if (transactionModel == null) {
            ErrorCodes err = ErrorCodes.HAS_NOT_EXISTED_REFER_INVOICE_FOR_REDEEM;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        resetPaymentForLoyalty(model);
        TransactionRequest transactionRequest = createBasicTransactionRequest(model, model.getRedeemAmount());
        transactionRequest.setInvoiceNumber(transactionModel.getInvoiceNumber());
        loyaltyClient.cancelRedeem(transactionRequest, transactionModel.getInvoiceNumber());
        transactionModel.setType(TransactionType.CANCEL_PENDING_REDEEM.toString());
        loyaltyTransactionService.save(transactionModel);
        orderService.save(model);
    }

    @Override
    @Transactional
    public void resetPaymentForLoyalty(OrderModel model) {
        PaymentMethodData paymentMethodData = financeService.getPaymentMethodByCode(PaymentMethodType.LOYALTY_POINT.code());
        Set<PaymentTransactionModel> paymentTransactions = model.getPaymentTransactions();
        for (PaymentTransactionModel payment : paymentTransactions) {
            if (!payment.isDeleted() && paymentMethodData.getId().equals(payment.getPaymentMethodId())) {
                payment.setDeleted(true);
            }
        }
    }

    @Override
    @Transactional
    public void cancelPendingRedeemForCancelOrder(OrderModel model) {
        if (model.getRedeemAmount() == null) return;
        LoyaltyTransactionModel transactionModel = loyaltyTransactionService.findLastByOrderCodeAndListType(model.getCode(),
                Arrays.asList(TransactionType.REDEEM.name()));
        if (transactionModel == null) {
            return;
        }
        TransactionRequest transactionRequest = createBasicTransactionRequest(model, model.getRedeemAmount());
        transactionRequest.setInvoiceNumber(transactionModel.getInvoiceNumber());
        loyaltyClient.cancelRedeem(transactionRequest, transactionModel.getInvoiceNumber());
        transactionModel.setType(TransactionType.CANCEL_PENDING_REDEEM.toString());
        loyaltyTransactionService.save(transactionModel);
    }

    @Override
    public TransactionData updatePendingRedeem(OrderModel model, PaymentTransactionRequest request) {
        LoyaltyTransactionModel loyaltyTransactionModel = loyaltyTransactionService.findLastByOrderCodeAndListType(model.getCode(),
                Arrays.asList(TransactionType.REDEEM.name(), TransactionType.REFUND.name()));

        if (loyaltyTransactionModel == null) {
            LOGGER.warn("Not found redeem transaction online: {}", model.getCode());
            return null;
        }
        if(TransactionType.REFUND.toString().equals(loyaltyTransactionModel.getType())) {
            LOGGER.warn("Found refund transaction latest online: {}", model.getCode());
            return null;
        }
        TransactionRequest transactionRequest = createBasicTransactionRequest(model, request.getAmount());
        transactionRequest.setInvoiceNumber(loyaltyTransactionModel.getInvoiceNumber());
        TransactionData transactionData = loyaltyClient.updatePendingRedeem(transactionRequest, loyaltyTransactionModel.getInvoiceNumber());
        loyaltyTransactionModel.setConversionRate(transactionData.getConversionRate());
        loyaltyTransactionService.save(loyaltyTransactionModel);
        PaymentMethodData paymentMethodData = financeService.getPaymentMethodByCode(PaymentMethodType.LOYALTY_POINT.code());
        Set<PaymentTransactionModel> payments = model.getPaymentTransactions();
        for (PaymentTransactionModel payment : payments) {
            if (!payment.isDeleted() && paymentMethodData.getId().equals(payment.getPaymentMethodId())) {
                payment.setAmount(transactionData.getRedeemAmount());
                break;
            }
        }
        return transactionData;
    }

    @Override
    public void completeRedeemLoyaltyForOnline(OrderModel order) {
        LoyaltyTransactionModel loyaltyTransactionModel = loyaltyTransactionService.findLastByOrderCodeAndListType(order.getCode(),
                Arrays.asList(TransactionType.REDEEM.name(), TransactionType.REFUND.name()));
        if(loyaltyTransactionModel == null ||
                TransactionType.REFUND.toString().equals(loyaltyTransactionModel.getType())) {
            return;
        }
        TransactionRequest transactionRequest = new TransactionRequest();
        String invoiceNumber = loyaltyTransactionModel.getInvoiceNumber();
        transactionRequest.setInvoiceNumber(invoiceNumber);
        transactionRequest.setReferCode(order.getCode());
        transactionRequest.setReferType(order.getType());
        transactionRequest.setCompanyId(order.getCompanyId());
        this.loyaltyClient.completePendingRedeem(transactionRequest, invoiceNumber);
    }

    @Override
    public void revertOnlineOrderReward(OrderModel order, Double amount) {
        LoyaltyTransactionModel loyaltyTransactionModel = loyaltyTransactionService.findLastByOrderCodeAndListType(order.getCode(),
                Arrays.asList(TransactionType.AWARD.name(), TransactionType.REVERT.name()));
        if (loyaltyTransactionModel == null) {
            LOGGER.warn("Not found award transaction: orderCode: {}, awardAmount: {}", order.getCode(), amount);
            return;
        }
        TransactionRequest transactionRequest = createBasicTransactionRequest(order, amount);
        populateTransactionRequestRevertOrRefund(order, null, loyaltyTransactionModel.getInvoiceNumber(), transactionRequest);
        TransactionData transactionData = loyaltyClient.revert(transactionRequest);
        createLoyaltyTransaction(order.getCode(), transactionData.getInvoiceNumber(), TransactionType.REVERT.name(), transactionData.getConversionRate(), null);
    }

    @Override
    public TransactionRequest populateRedeemKafkaForOnlineOrder(OrderModel orderModel, Double amount) {
        if (amount <= 0) return null;
        TransactionRequest transactionRequest = createBasicTransactionRequest(orderModel, amount);
        populateTransactionRequestRewardOrRedeem(orderModel, transactionRequest);
        return transactionRequest;
    }

    @Override
    public TransactionRequest populateRefundKafkaForOnlineOrder(OrderModel orderModel, Double amount) {
        LoyaltyTransactionModel loyaltyTransactionModel = loyaltyTransactionService.findLastByOrderCodeAndListType(orderModel.getCode(),
                Arrays.asList(TransactionType.REDEEM.name(), TransactionType.REFUND.name()));
        if (loyaltyTransactionModel == null) {
            ErrorCodes err = ErrorCodes.HAS_NOT_EXISTED_REFER_INVOICE_FOR_REFUND;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        TransactionRequest transactionRequest = createBasicTransactionRequest(orderModel, amount);
        populateTransactionRequestRevertOrRefund(orderModel, null, loyaltyTransactionModel.getInvoiceNumber(), transactionRequest);
        return transactionRequest;
    }

    @Override
    public TransactionRequest populateRevertKafkaForOnlineOrder(OrderModel orderModel, Double amount) {
        LoyaltyTransactionModel loyaltyTransactionModel = loyaltyTransactionService.findLastByOrderCodeAndListType(orderModel.getCode(),
                Arrays.asList(TransactionType.AWARD.name(), TransactionType.REVERT.name()));
        if (loyaltyTransactionModel == null) {
            ErrorCodes err = ErrorCodes.HAS_NOT_EXISTED_REFER_INVOICE_FOR_REVERT;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        TransactionRequest transactionRequest = createBasicTransactionRequest(orderModel, amount);
        populateTransactionRequestRevertOrRefund(orderModel, null, loyaltyTransactionModel.getInvoiceNumber(), transactionRequest);
        return transactionRequest;
    }

    @Override
    public TransactionRequest populateCompleteRedeemKafkaForOnlineOrder(OrderModel orderModel) {
        LoyaltyTransactionModel loyaltyTransactionModel = loyaltyTransactionService.findLastByOrderCodeAndListType(orderModel.getCode(),
                Arrays.asList(TransactionType.REDEEM.name(), TransactionType.REFUND.name()));
        if(loyaltyTransactionModel == null ||
                TransactionType.REFUND.toString().equals(loyaltyTransactionModel.getType())) {
            return null;
        }
        TransactionRequest transactionRequest = new TransactionRequest();
        String invoiceNumber = loyaltyTransactionModel.getInvoiceNumber();
        transactionRequest.setInvoiceNumber(invoiceNumber);
        transactionRequest.setReferCode(orderModel.getCode());
        transactionRequest.setReferType(orderModel.getType());
        transactionRequest.setCompanyId(orderModel.getCompanyId());
        return transactionRequest;
    }

    @Override
    public TransactionRequest populateCancelRedeemKafkaForOnlineOrder(OrderModel orderModel) {
        LoyaltyTransactionModel transactionModel = loyaltyTransactionService.findLastByOrderCodeAndListType(orderModel.getCode(),
                Arrays.asList(TransactionType.REDEEM.name()));
        if (transactionModel == null) {
            ErrorCodes err = ErrorCodes.HAS_NOT_EXISTED_REFER_INVOICE_FOR_REDEEM;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        transactionModel.setType(TransactionType.CANCEL_PENDING_REDEEM.toString());
        loyaltyTransactionService.save(transactionModel);
        TransactionRequest transactionRequest = createBasicTransactionRequest(orderModel, orderModel.getRedeemAmount());
        transactionRequest.setInvoiceNumber(transactionModel.getInvoiceNumber());
        return transactionRequest;
    }

    @Autowired
    public void setLoyaltyClient(LoyaltyClient loyaltyClient) {
        this.loyaltyClient = loyaltyClient;
    }

    @Autowired
    public void setPromotionResultService(PromotionResultService promotionResultService) {
        this.promotionResultService = promotionResultService;
    }

    @Autowired
    public void setLoyaltyTransactionService(LoyaltyTransactionService loyaltyTransactionService) {
        this.loyaltyTransactionService = loyaltyTransactionService;
    }

    @Autowired
    public void setCalculationService(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @Autowired
    public void setProductRedeemRateUseService(ProductRedeemRateUseService productRedeemRateUseService) {
        this.productRedeemRateUseService = productRedeemRateUseService;
    }

    @Autowired
    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }

    @Autowired
    public void setFinanceService(FinanceService financeService) {
        this.financeService = financeService;
    }

    @Autowired
    public void setTransactionRequestPopulator(Populator<OrderModel, TransactionRequest> transactionRequestPopulator) {
        this.transactionRequestPopulator = transactionRequestPopulator;
    }

    @Autowired
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Autowired
    public void setOrderLoyaltyRewardRequestProducer(OrderLoyaltyRewardRequestProducer orderLoyaltyRewardRequestProducer) {
        this.orderLoyaltyRewardRequestProducer = orderLoyaltyRewardRequestProducer;
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    public void setProductSearchService(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @Autowired
    public void setEntryRepository(EntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    @Autowired
    public void setSubOrderEntryRepository(SubOrderEntryRepository subOrderEntryRepository) {
        this.subOrderEntryRepository = subOrderEntryRepository;
    }

    @Autowired
    public void setToppingItemRepository(ToppingItemRepository toppingItemRepository) {
        this.toppingItemRepository = toppingItemRepository;
    }
}
