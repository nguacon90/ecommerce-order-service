package com.vctek.orderservice.service.impl;

import com.vctek.kafka.data.BillDto;
import com.vctek.migration.dto.OrderBillLinkDTO;
import com.vctek.migration.dto.PaidAmountOrderData;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.request.OrderReportRequest;
import com.vctek.orderservice.dto.request.OrderSearchRequest;
import com.vctek.orderservice.dto.request.SaleQuantityRequest;
import com.vctek.orderservice.dto.request.storefront.CountOrderData;
import com.vctek.orderservice.feignclient.FinanceClient;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.promotionengine.promotionservice.model.*;
import com.vctek.orderservice.promotionengine.promotionservice.repository.AbstractPromotionActionRepository;
import com.vctek.orderservice.promotionengine.promotionservice.repository.PromotionOrderEntryConsumedRepository;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionResultService;
import com.vctek.orderservice.repository.*;
import com.vctek.orderservice.repository.dao.OrderDAO;
import com.vctek.orderservice.repository.dao.OrderSaleDAO;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.service.SubOrderEntryService;
import com.vctek.orderservice.service.TagService;
import com.vctek.orderservice.service.event.OrderTagEvent;
import com.vctek.util.BillType;
import com.vctek.util.CommonUtils;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends DefaultAbstractOrderService<OrderModel, OrderEntryModel> implements OrderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);
    private OrderRepository orderRepository;
    private OrderEntryRepository orderEntryRepository;
    private FinanceClient financeClient;
    private OrderSaleDAO orderSaleDAO;
    private SubOrderEntryService subOrderEntryService;
    private ToppingOptionRepository toppingOptionRepository;
    private ToppingItemRepository toppingItemRepository;
    private OrderSettingCustomerOptionRepository settingCustomerOptionRepository;
    private OrderHasCouponRepository orderHasCouponRepository;
    private PromotionResultService promotionResultService;
    private AbstractPromotionActionRepository abstractPromotionActionRepository;
    private PromotionOrderEntryConsumedRepository promotionOrderEntryConsumedRepository;
    private TagService tagService;
    private ApplicationEventPublisher applicationEventPublisher;
    private OrderDAO orderDAO;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public OrderModel findById(Long id) {
        Optional<OrderModel> paymentMethodModel = orderRepository.findById(id);
        return paymentMethodModel.isPresent() ? paymentMethodModel.get() : null;
    }

    @Override
    public OrderModel createOrderFromCart(CartModel cartModel) {
        OrderModel orderModel = cloneOrderFormModel(cartModel);

        List<AbstractOrderEntryModel> entries = cartModel.getEntries();
        List<AbstractOrderEntryModel> orderEntries = new ArrayList<>();
        for (AbstractOrderEntryModel entry : entries) {
            OrderEntryModel orderEntry = cloneOrderEntry(orderModel, entry);
            cloneSubOrderEntries(entry, orderEntry);
            cloneToppingOptions(entry, orderEntry);
            orderEntries.add(orderEntry);
        }

        orderModel.setEntries(orderEntries);
        transferPromotionsToOrder(cartModel.getPromotionResults(), orderModel);
        transferCouponCodeToOrder(cartModel.getOrderHasCouponCodeModels(), orderModel);
        return orderModel;
    }

    @Override
    public OrderEntryModel cloneOrderEntry(OrderModel orderModel, AbstractOrderEntryModel originEntry) {
        OrderEntryModel orderEntry = new OrderEntryModel();
        super.cloneEntryProperties(originEntry, orderEntry, orderModel);
        return orderEntry;
    }

    @Override
    public void cloneToppingOptions(AbstractOrderEntryModel originEntry, AbstractOrderEntryModel cloneEntry) {
        Set<ToppingOptionModel> toppingOptionModels = originEntry.getToppingOptionModels();
        Set<ToppingOptionModel> cloneToppingOptions = new HashSet<>();
        if (CollectionUtils.isNotEmpty(toppingOptionModels)) {
            for (ToppingOptionModel toppingOptionModel : toppingOptionModels) {
                ToppingOptionModel cloneToppingOption = populateToppingOptionModel(cloneEntry, toppingOptionModel);
                cloneToppingItems(toppingOptionModel, cloneToppingOption);
                cloneToppingOptions.add(cloneToppingOption);
            }
            cloneEntry.setToppingOptionModels(cloneToppingOptions);
        }
    }

    private void cloneToppingItems(ToppingOptionModel toppingOptionModel, ToppingOptionModel cloneToppingOption) {
        Set<ToppingItemModel> toppingItemModels = toppingOptionModel.getToppingItemModels();
        Set<ToppingItemModel> cloneToppingItems = new HashSet<>();
        if (CollectionUtils.isNotEmpty(toppingItemModels)) {
            for (ToppingItemModel toppingItemModel : toppingItemModels) {
                ToppingItemModel cloneToppingItem = SerializationUtils.clone(toppingItemModel);
                cloneToppingItem.setId(null);
                cloneToppingItem.setToppingOptionModel(cloneToppingOption);
                cloneToppingItems.add(cloneToppingItem);
            }
            cloneToppingOption.setToppingItemModels(cloneToppingItems);
        }
    }

    @Override
    public void cloneSubOrderEntries(AbstractOrderEntryModel originEntry, AbstractOrderEntryModel cloneEntry) {
        super.cloneSubOrderEntries(originEntry, cloneEntry);
    }

    @Override
    public void transferCouponCodeToOrder(Set<OrderHasCouponCodeModel> appliedCouponCodes, OrderModel orderModel) {
        Set<OrderHasCouponCodeModel> orderHasCouponCodeModels = new HashSet<>();
        if (CollectionUtils.isNotEmpty(appliedCouponCodes)) {
            OrderHasCouponCodeModel orderHasCouponCodeModel;
            for (OrderHasCouponCodeModel appliedCouponCode : appliedCouponCodes) {
                orderHasCouponCodeModel = new OrderHasCouponCodeModel();
                orderHasCouponCodeModel.setCouponCode(appliedCouponCode.getCouponCode());
                orderHasCouponCodeModel.setOrder(orderModel);
                orderHasCouponCodeModel.setRedemptionQuantity(appliedCouponCode.getRedemptionQuantity());
                orderHasCouponCodeModels.add(orderHasCouponCodeModel);
            }
        }
        orderModel.setOrderHasCouponCodeModels(orderHasCouponCodeModels);
    }

    @Override
    public void transferPromotionsToOrder(Set<PromotionResultModel> promotionResults, OrderModel orderModel) {
        Set<PromotionResultModel> clonePromotionResults = new HashSet<>();
        List<PromotionResultModel> promotionResultModels = promotionResults.stream().collect(Collectors.toList());
        for (PromotionResultModel promotionResult : promotionResultModels) {
            PromotionResultModel clonePromotionResult = SerializationUtils.clone(promotionResult);
            clonePromotionResult.setId(null);
            clonePromotionResult.setOrderCode(orderModel.getCode());
            clonePromotionResult.setOrder(orderModel);

            clonePromotion(promotionResult, clonePromotionResult);

            clonePromotionConsumedBudget(promotionResult, clonePromotionResult);

            cloneActions(promotionResult, clonePromotionResult);

            cloneConsumedOrderEntries(orderModel, promotionResult, clonePromotionResult);

            clonePromotionResults.add(clonePromotionResult);
        }

        orderModel.setPromotionResults(clonePromotionResults);
    }

    private void clonePromotionConsumedBudget(PromotionResultModel promotionResult, PromotionResultModel clonePromotionResult) {
        PromotionBudgetConsumeModel budgetConsumeModel = promotionResult.getBudgetConsumeModel();
        if(budgetConsumeModel != null) {
            PromotionBudgetConsumeModel cloneBudgetConsumedModel = SerializationUtils.clone(budgetConsumeModel);
            cloneBudgetConsumedModel.setId(null);
            cloneBudgetConsumedModel.setPromotionResult(clonePromotionResult);
            clonePromotionResult.setBudgetConsumeModel(cloneBudgetConsumedModel);
        }
    }

    private void cloneConsumedOrderEntries(OrderModel orderModel, PromotionResultModel promotionResult,
                                           PromotionResultModel clonePromotionResult) {
        Set<PromotionOrderEntryConsumedModel> cloneConsumedEntries = new HashSet<>();
        List<PromotionOrderEntryConsumedModel> consumedEntries = promotionOrderEntryConsumedRepository.findAllByPromotionResult(promotionResult);
        if (CollectionUtils.isNotEmpty(consumedEntries)) {
            for (PromotionOrderEntryConsumedModel consumedEntry : consumedEntries) {
                PromotionOrderEntryConsumedModel cloneEntryConsumed = SerializationUtils.clone(consumedEntry);
                cloneEntryConsumed.setId(null);
                AbstractOrderEntryModel cloneOrderEntry = getOrderEntry(orderModel, consumedEntry);
                cloneEntryConsumed.setOrderEntry(cloneOrderEntry);
                cloneEntryConsumed.setPromotionResult(clonePromotionResult);
                cloneConsumedEntries.add(cloneEntryConsumed);
            }
        }

        clonePromotionResult.setConsumedEntries(cloneConsumedEntries);
    }

    private void cloneActions(PromotionResultModel promotionResult, PromotionResultModel clonePromotionResult) {
        Set<AbstractPromotionActionModel> cloneActions = new HashSet<>();
        List<AbstractPromotionActionModel> actions = abstractPromotionActionRepository.findAllByPromotionResult(promotionResult);
        if (CollectionUtils.isNotEmpty(actions)) {
            for (AbstractPromotionActionModel action : actions) {
                if (action instanceof AbstractRuleBasedPromotionActionModel) {
                    AbstractRuleBasedPromotionActionModel ruleBaseAction = (AbstractRuleBasedPromotionActionModel) action;
                    AbstractRuleBasedPromotionActionModel cloneAction = SerializationUtils.clone(ruleBaseAction);
                    cloneAction.setId(null);
                    cloneAction.setPromotionResult(clonePromotionResult);
                    cloneAction.setRule(ruleBaseAction.getRule());
                    cloneActions.add(cloneAction);
                } else {
                    AbstractPromotionActionModel cloneAction = SerializationUtils.clone(action);
                    cloneAction.setId(null);
                    cloneAction.setPromotionResult(clonePromotionResult);
                    cloneActions.add(cloneAction);
                }
            }
        }

        clonePromotionResult.setActions(cloneActions);
    }

    private void clonePromotion(PromotionResultModel promotionResult, PromotionResultModel clonePromotionResult) {
        AbstractPromotionModel promotion = promotionResult.getPromotion();
        promotion.setPromotionResults(new HashSet<>());
        clonePromotionResult.setPromotion(promotion);
    }

    private AbstractOrderEntryModel getOrderEntry(OrderModel orderModel, PromotionOrderEntryConsumedModel entryConsumed) {
        return orderModel.getEntries().stream().filter(entry -> entry.getEntryNumber() != null &&
                entry.getEntryNumber().equals(getOrderEntryNumberWithFallBack(entryConsumed))
        ).findFirst().orElse(null);
    }

    private Integer getOrderEntryNumberWithFallBack(PromotionOrderEntryConsumedModel entryConsumed) {
        if (entryConsumed.getOrderEntry() != null) {
            return entryConsumed.getOrderEntry().getEntryNumber();
        }

        return entryConsumed.getOrderEntryNumber();
    }

    @Override
    @Transactional
    public OrderModel save(OrderModel orderModel) {
        //Should not publish event here. Listener handle event without transaction
        return orderRepository.save(orderModel);
    }

    @Override
    @Transactional
    public void refresh(OrderModel orderModel) {
        modelService.refresh(orderModel);
    }

    @Override
    public OrderModel findByCodeAndCompanyId(String code, Long companyId) {
        Optional<OrderModel> optionalOrderModel = orderRepository.findByCodeAndCompanyIdAndDeleted(code, companyId, false);
        return optionalOrderModel.isPresent() ? optionalOrderModel.get() : null;
    }

    @Override
    public OrderEntryModel addNewEntry(OrderModel order, Long productId, long qty, boolean isImport) {
        OrderEntryModel entryModel = getInstanceOrderEntryModel(order, productId, qty);
        boolean isFnB = productService.isFnB(productId);
        if (isFnB) {
            addEntryAtLast(order, entryModel);
            if (!isImport) {
                modelService.save(entryModel);
            }
        } else {
            addEntryAtFirst(order, entryModel);
            normalizeEntryNumbers(order, isImport);
        }
        recalculateSubOrderEntryQuantity(entryModel);
        order.setCalculated(Boolean.FALSE);
        return entryModel;
    }

    @Override
    public boolean isSaleOffEntry(OrderEntryDTO orderEntryDTO) {
        Optional<OrderEntryModel> orderEntryModelOptional = orderEntryRepository.findById(orderEntryDTO.getEntryId());
        if(orderEntryModelOptional.isPresent()) {
            return orderEntryModelOptional.get().isSaleOff();
        }
        return false;
    }

    private OrderEntryModel getInstanceOrderEntryModel(OrderModel order, Long productId, long qty) {
        OrderEntryModel entryModel = new OrderEntryModel();
        entryModel.setOrder(order);
        entryModel.setProductId(productId);
        entryModel.setQuantity(qty);
        entryModel.setOrderCode(order.getCode());
        return entryModel;
    }

    @Override
    public Page<OrderModel> findAll(Pageable pageable) {
        return orderRepository.findAllByDeleted(false, pageable);
    }

    @Override
    public Page<OrderModel> findAllByFromDate(Pageable pageable, Date fromDate) {
        return orderRepository.findAllByCreatedTimeGreaterThanEqual(fromDate, pageable);
    }

    @Override
    public Page<OrderModel> findAllByAndCompanyIdFromDate(Long companyId, Date fromDate, Pageable pageable) {
        return orderRepository.findAllByCompanyIdAndCreatedTimeGreaterThanEqual(companyId, fromDate, pageable);
    }

    @Override
    public OrderModel findByCodeAndCompanyIdAndDeleted(String code, Long companyId, boolean deleted) {
        Optional<OrderModel> optionalOrderModel = orderRepository.findByCodeAndCompanyIdAndDeleted(code, companyId, deleted);
        return optionalOrderModel.isPresent() ? optionalOrderModel.get() : null;
    }

    @Override
    public OrderModel findByCodeAndCompanyIdAndOrderTypeAndDeleted(String code, Long companyId, String orderType, boolean deleted) {
        Optional<OrderModel> optionalOrderModel = orderRepository.findByCodeAndCompanyIdAndTypeAndDeleted(code, companyId, orderType, deleted);
        return optionalOrderModel.isPresent() ? optionalOrderModel.get() : null;
    }

    @Override
    public void resetPreAndHoldingStockOf(OrderModel orderModel) {
        List<AbstractOrderEntryModel> entries = orderModel.getEntries();
        for (AbstractOrderEntryModel entry : entries) {
            entry.setHoldingStock(0l);
            entry.setHolding(false);
            entry.setPreOrder(false);
        }
    }

    @Override
    public OrderEntryModel findEntryBy(OrderModel orderModel, Integer entryNumber) {
        return orderEntryRepository.findByOrderAndEntryNumber(orderModel, entryNumber);
    }

    @Override
    public OrderEntryModel saveEntry(OrderEntryModel entryModel) {
        return orderEntryRepository.save(entryModel);
    }

    @Override
    public List<SaleQuantity> findAllSaleEntryBy(SaleQuantityRequest request) {
        return orderSaleDAO.findEntrySaleQuantity(request);
    }

    @Override
    public Page<OrderModel> findAllByCompanyIdAndCreateTime(Long companyId, Date fromDate, Date toDate, Pageable pageable) {
        return orderRepository.findAllByCompanyIdAndCreatedTimeBetween(companyId, fromDate, toDate, pageable);
    }

    @Override
    public Page<OrderModel> findAllByCompanyIdAndTypeAndFromDate(Pageable pageable, Long companyId, String type, Date fromDate) {
        return orderRepository.findAllByCompanyIdAndTypeAndCreatedTimeGreaterThanEqualAndDeleted(companyId, type, fromDate, false, pageable);
    }

    @Override
    @Transactional
    public void updatePaidAmountOrder(OrderModel order) {
        if (order == null) {
            return;
        }

        Double paidAmount = financeClient.getPaidAmountOrder(order.getCode(), order.getCompanyId());
        order.setPaidAmount(paidAmount);

        save(order);
    }

    @Override
    public Page<OrderModel> findAllByCompanyIdAndType(Pageable pageable, Long companyId, String type) {
        return orderRepository.findAllByCompanyIdAndTypeAndDeleted(companyId, type, false, pageable);
    }

    @Override
    public List<SaleQuantity> findAllSaleComboEntries(SaleQuantityRequest request) {
        return orderSaleDAO.findComboEntrySaleQuantity(request);
    }

    @Override
    public List<OrderModel> findOrderCombo(Long companyId) {
        return orderRepository.findOrderCombo(companyId);
    }

    @Override
    public double updateAndCalculateDiffRevertAmountOfReturnEntries(UpdateReturnOrderBillDTO updateReturnOrderBillDTO) {
        List<UpdateReturnOrderBillDetail> entries = updateReturnOrderBillDTO.getEntries();
        if (CollectionUtils.isEmpty(entries)) {
            return 0;
        }
        OrderModel originOrder = updateReturnOrderBillDTO.getOriginOrder();
        double diffRevertAmount = 0;
        List<OrderEntryModel> updatedItems = new ArrayList<>();
        for (UpdateReturnOrderBillDetail detail : entries) {
            OrderEntryModel entryModel = orderEntryRepository.findByOrderAndId(originOrder, detail.getOrderEntryId());
            if (entryModel != null) {
                int diffQty = CommonUtils.readValue(detail.getQuantity()) - CommonUtils.readValue(detail.getOriginQuantity());
                entryModel.setReturnQuantity(entryModel.getReturnQuantity() + diffQty);
                updatedItems.add(entryModel);
                diffRevertAmount += diffQty * (CommonUtils.readValue(entryModel.getRewardAmount()) / entryModel.getQuantity());
            } else {
                LOGGER.error("CANNOT FIND ENTRY: {}, PRODUCT: {}", detail.getOrderEntryId(), detail.getProductId());
            }
        }
        orderEntryRepository.saveAll(updatedItems);
        return diffRevertAmount;
    }

    @Override
    public Page<OrderModel> findAllByCompanyId(Long companyId, Pageable pageable) {
        return orderRepository.findAllByCompanyIdOrderByIdAsc(companyId, pageable);
    }

    @Override
    @Transactional
    public void linkBillToOrder(List<OrderBillLinkDTO> orderBillLinkDTOS) {
        List<OrderModel> updatedModels = new ArrayList<>();
        for (OrderBillLinkDTO dto : orderBillLinkDTOS) {
            OrderModel orderModel = orderRepository.findByCodeAndCompanyId(dto.getOrderCode(), dto.getCompanyId());
            if (orderModel != null) {
                orderModel.setBillId(dto.getBillId());
                updatedModels.add(orderModel);
            }
        }

        orderRepository.saveAll(updatedModels);
    }

    @Override
    @Transactional
    public void updatePaidAmountOrder(List<PaidAmountOrderData> data) {
        for (PaidAmountOrderData dto : data) {
            orderRepository.updatePaidAmount(dto.getPaidAmount(), dto.getOrderCode(), dto.getCompanyId());
        }
    }

    @Override
    public Page<OrderModel> findAllByAndCompanyIdAndOrderTypes(Long companyId, List<String> orderTypes, Pageable pageable) {
        return orderRepository.findAllByCompanyIdAndTypeIn(companyId, orderTypes, pageable);
    }

    @Override
    public List<OrderModel> findByCompanyIdAndOrderCodeIn(Long companyId, List<String> orderCodes) {
        return orderRepository.findAllByCompanyIdAndCodeIn(companyId, orderCodes);
    }

    @Override
    public OrderEntryModel findEntryBy(Long entryId, OrderModel order) {
        return orderEntryRepository.findByIdAndOrder(entryId, order);
    }

    @Override
    @Transactional
    public void saveAll(List<OrderModel> updateExchangeOrders) {
        orderRepository.saveAll(updateExchangeOrders);
    }

    @Override
    @Transactional
    public List<OrderModel> updateOnlineOriginBasePrice(OrderReportRequest orderReportRequest, Pageable pageable) {
        Page<OrderModel> pageResult = this.findAllByCompanyIdAndTypeAndFromDate(pageable, orderReportRequest.getCompanyId(), OrderType.ONLINE.toString(), orderReportRequest.getFromDate());
        List<OrderModel> orderModels = pageResult.getContent();
        if (CollectionUtils.isEmpty(orderModels)) {
            return new ArrayList<>();
        }
        for (OrderModel orderModel : orderModels) {
            List<OrderEntryModel> entryModels = orderEntryRepository.findAllByOrder(orderModel);
            for (OrderEntryModel entryModel : entryModels) {
                if (entryModel.getOriginBasePrice() == null) {
                    PriceData priceData = productService.getPriceOfProduct(entryModel.getProductId(), entryModel.getQuantity().intValue());
                    LOGGER.debug("UPDATE ORIGIN BASE PRICE: productId: {}, price: {}", entryModel.getProductId(), priceData.getPrice());
                    entryModel.setOriginBasePrice(priceData.getPrice());
                }
            }
        }

        return orderRepository.saveAll(orderModels);
    }

    @Override
    public OrderModel findOrderByExternalIdAndSellSignal(CartInfoParameter cartInfoParameter) {
        List<OrderModel> orderModels = orderRepository.findByCompanyIdAndExternalIdAndSellSignal(cartInfoParameter.getCompanyId(),
                cartInfoParameter.getExternalId(), cartInfoParameter.getSellSignal());
        if(CollectionUtils.isNotEmpty(orderModels)) {
            return orderModels.get(0);
        }

        return null;
    }

    @Override
    @Transactional
    public void updateOrderBill(BillDto billDto) {
        if (StringUtils.isEmpty(billDto.getOrderCode())) {
            return;
        }
        if (!OrderType.ONLINE.toString().equals(billDto.getOrderType()) ||
                !BillType.RETURN_BILL.code().equals(billDto.getType())) {
            return;
        }
        OrderModel orderModel = findByCodeAndCompanyId(billDto.getOrderCode(), billDto.getCompanyId());
        if (orderModel == null) {
            LOGGER.info("CANNOT EXIST ORDER: orderCode: {}, billId: {}", billDto.getOrderCode(), billDto.getId());
            return;
        }
        orderRepository.updateBillToOrder(billDto.getId(), billDto.getOrderCode(), billDto.getCompanyId());
        LOGGER.debug("UPDATE BILL ID FOR ORDER: code: {}, billId: {}", billDto.getOrderCode(), billDto.getId());
    }

    @Override
    public void holdingStockAndResetPreStockOf(List<OrderEntryModel> entries) {
        for (AbstractOrderEntryModel entry : entries) {
            entry.setHoldingStock(entry.getQuantity());
            entry.setHolding(true);
            entry.setPreOrder(false);
        }
    }

    @Override
    public void resetPreAndHoldingStockOfEntries(List<OrderEntryModel> entries) {
        for (AbstractOrderEntryModel entry : entries) {
            entry.setHoldingStock(0l);
            entry.setHolding(false);
            entry.setPreOrder(false);
        }
    }

    @Override
    public void cloneSubOrderEntriesForKafkaImportOrderStatus(AbstractOrderEntryModel entryModel, OrderEntryModel cloneEntry) {
        List<SubOrderEntryModel> subOrderEntries = subOrderEntryService.findAllBy(entryModel);
        if (CollectionUtils.isEmpty(subOrderEntries))  return;
        cloneEntry.setComboType(entryModel.getComboType());
        Set<SubOrderEntryModel> cloneSubEntries = populateSubOrderEntry(cloneEntry, subOrderEntries);
        cloneEntry.setSubOrderEntries(cloneSubEntries);
    }

    private Set<SubOrderEntryModel> populateSubOrderEntry(OrderEntryModel cloneEntry, List<SubOrderEntryModel> subOrderEntries) {
        Set<SubOrderEntryModel> cloneSubEntries = new HashSet<>();
        for (SubOrderEntryModel subOrderEntryModel : subOrderEntries) {
            SubOrderEntryModel cloneSubEntry = SerializationUtils.clone(subOrderEntryModel);
            cloneSubEntry.setId(null);
            cloneSubEntry.setOrderEntry(cloneEntry);
            cloneSubEntries.add(cloneSubEntry);
        }
        return cloneSubEntries;
    }

    @Override
    public void cloneToppingOptionsForKafkaImportOrderStatus(AbstractOrderEntryModel entryModel, OrderEntryModel cloneEntry) {
        List<ToppingOptionModel> optionModels = toppingOptionRepository.findAllByOrderEntry(entryModel);
        if (CollectionUtils.isEmpty(optionModels)) return;
        Set<ToppingOptionModel> cloneToppingOptions = new HashSet<>();
        for (ToppingOptionModel optionModel : optionModels) {
            ToppingOptionModel cloneToppingOption = populateToppingOptionModel(cloneEntry, optionModel);
            cloneToppingItemsForKafkaImportOrderStatus(optionModel, cloneToppingOption);
            cloneToppingOptions.add(cloneToppingOption);
            cloneEntry.setToppingOptionModels(cloneToppingOptions);
        }
    }

    private ToppingOptionModel populateToppingOptionModel(AbstractOrderEntryModel cloneEntry, ToppingOptionModel optionModel) {
        ToppingOptionModel cloneToppingOption = new ToppingOptionModel();
        cloneToppingOption.setQuantity(optionModel.getQuantity());
        cloneToppingOption.setIce(optionModel.getIce());
        cloneToppingOption.setSugar(optionModel.getSugar());
        cloneToppingOption.setOrderEntry(cloneEntry);
        return cloneToppingOption;
    }

    private void cloneToppingItemsForKafkaImportOrderStatus(ToppingOptionModel toppingOptionModel, ToppingOptionModel cloneToppingOption) {
        List<ToppingItemModel> toppingItemModels = toppingItemRepository.findAllByToppingOptionModel(toppingOptionModel);
        if (CollectionUtils.isEmpty(toppingItemModels)) return;
        Set<ToppingItemModel> cloneToppingItems = new HashSet<>();
        for (ToppingItemModel toppingItemModel : toppingItemModels) {
            ToppingItemModel cloneToppingItem = SerializationUtils.clone(toppingItemModel);
            cloneToppingItem.setId(null);
            cloneToppingItem.setToppingOptionModel(cloneToppingOption);
            cloneToppingItems.add(cloneToppingItem);
        }
        cloneToppingOption.setToppingItemModels(cloneToppingItems);
    }

    @Override
    public void cloneSettingCustomerOption(OrderModel order, OrderModel cloneRetailOrderModel) {
        List<OrderSettingCustomerOptionModel> models = settingCustomerOptionRepository.findAllByOrderId(order.getId());
        if (CollectionUtils.isEmpty(models)) return;
        cloneRetailOrderModel.setOrderSettingCustomerOptionModels(new HashSet<>(models));
    }

    @Override
    public void transferPromotionsToOrderForKafkaImportOrderStatus(OrderModel order, OrderModel cloneRetailOrderModel) {
        List<PromotionResultModel> promotionResultModels = promotionResultService.findAllByOrder(order);
        transferPromotionsToOrder(promotionResultModels.stream().collect(Collectors.toSet()), cloneRetailOrderModel);
    }

    @Override
    public void transferCouponCodeToOrderForKafkaImportOrderStatus(OrderModel order, OrderModel cloneRetailOrderModel) {
        List<OrderHasCouponCodeModel> couponCodeModels = orderHasCouponRepository.findAllByOrderId(order.getId());
        if (CollectionUtils.isEmpty(couponCodeModels)) return;
        transferCouponCodeToOrder(couponCodeModels.stream().collect(Collectors.toSet()), cloneRetailOrderModel);
    }

    @Override
    public OrderModel cloneOrderFormModel(AbstractOrderModel model) {
        OrderModel orderModel = new OrderModel();
        orderModel.setCode(model.getCode());
        orderModel.setDiscountValues(model.getDiscountValues());
        orderModel.setFinalPrice(model.getFinalPrice());
        orderModel.setTotalTax(model.getTotalTax());
        orderModel.setDiscount(model.getDiscount());
        orderModel.setDiscountType(model.getDiscountType());
        orderModel.setFixedDiscount(model.getFixedDiscount());
        orderModel.setGlobalDiscountValues(model.getGlobalDiscountValues());
        orderModel.setSubTotal(model.getSubTotal());
        orderModel.setSubTotalDiscount(model.getSubTotalDiscount());
        orderModel.setTotalDiscount(model.getTotalDiscount());
        orderModel.setTotalToppingDiscount(model.getTotalToppingDiscount());
        orderModel.setTotalPrice(model.getTotalPrice());
        orderModel.setWarehouseId(model.getWarehouseId());
        orderModel.setVatType(model.getVatType());
        orderModel.setVat(model.getVat());
        orderModel.setVatNumber(model.getVatNumber());
        orderModel.setVatDate(model.getVatDate());
        orderModel.setType(model.getType());
        orderModel.setPaymentCost(model.getPaymentCost());
        orderModel.setOrderStatus(OrderStatus.COMPLETED.code());
        orderModel.setCompanyId(model.getCompanyId());
        orderModel.setCreateByUser(model.getCreateByUser());
        orderModel.setNote(model.getNote());
        orderModel.setDeliveryDate(model.getDeliveryDate());
        orderModel.setGuid(model.getGuid());
        orderModel.setCustomerId(model.getCustomerId());
        orderModel.setCurrencyCode(model.getCurrencyCode());
        orderModel.setDeliveryCost(model.getDeliveryCost());
        orderModel.setCompanyShippingFee(model.getCompanyShippingFee());
        orderModel.setCollaboratorShippingFee(model.getCollaboratorShippingFee());
        orderModel.setPriceType(model.getPriceType());
        orderModel.setExchange(model.isExchange());
        orderModel.setCalculated(model.isCalculated());
        orderModel.setAppliedPromotionSourceRuleId(model.getAppliedPromotionSourceRuleId());
        orderModel.setSellSignal(model.getSellSignal());
        orderModel.setExternalId(model.getExternalId());
        orderModel.setExternalCode(model.getExternalCode());
        orderModel.setImages(model.getImages());
        orderModel.setDistributorId(model.getDistributorId());
        orderModel.setHasGotVat(model.isHasGotVat());
        return orderModel;
    }

    @Override
    @Transactional
    public void updateLockOrder(OrderModel orderModel, boolean lockOrder) {
        orderRepository.updateLockOrder(orderModel.getCode(), lockOrder);
    }

    @Override
    @Transactional
    public void updateLockOrders(Long companyId, List<String> orderCodes, boolean lock) {
        orderRepository.updateLockOrders(companyId, orderCodes, lock);
    }

    @Override
    @Transactional
    public void addTag(AddTagRequest addTagRequest) {
        OrderModel model = this.findByCodeAndCompanyId(addTagRequest.getOrderCode(), addTagRequest.getCompanyId());
        TagModel tagModel = tagService.findByIdAndCompanyId(addTagRequest.getTagId(), addTagRequest.getCompanyId());
        model.getTags().add(tagModel);
        orderRepository.save(model);
        applicationEventPublisher.publishEvent(new OrderTagEvent(model));
    }

    @Override
    @Transactional
    public void removeTag(Long companyId, String orderCode, Long tagId) {
        OrderModel model = this.findByCodeAndCompanyId(orderCode, companyId);
        if(model == null) {
            return;
        }
        TagModel tagModel = tagService.findByIdAndCompanyId(tagId, companyId);
        if(tagModel == null) {
            return;
        }
        model.getTags().remove(tagModel);
        orderRepository.save(model);
        applicationEventPublisher.publishEvent(new OrderTagEvent(model));
    }

    @Override
    public Page<OrderModel> findAllByCompanyIdAndOrderStatus(Pageable pageable, Long companyId, OrderStatus orderStatus) {
        return orderRepository.findAllByCompanyIdAndOrderStatus(companyId, orderStatus.code(), pageable);
    }

    @Override
    public List<CountOrderData> storefrontCountOrderByUser(OrderSearchRequest request) {
        return orderDAO.storefrontCountOrderByUser(request);
    }

    @Autowired
    public void setOrderEntryRepository(OrderEntryRepository orderEntryRepository) {
        this.orderEntryRepository = orderEntryRepository;
    }

    @Autowired
    public void setFinanceClient(FinanceClient financeClient) {
        this.financeClient = financeClient;
    }

    @Autowired
    public void setOrderSaleDAO(OrderSaleDAO orderSaleDAO) {
        this.orderSaleDAO = orderSaleDAO;
    }

    @Autowired
    public void setSubOrderEntryService(SubOrderEntryService subOrderEntryService) {
        this.subOrderEntryService = subOrderEntryService;
    }

    @Autowired
    public void setToppingOptionRepository(ToppingOptionRepository toppingOptionRepository) {
        this.toppingOptionRepository = toppingOptionRepository;
    }

    @Autowired
    public void setToppingItemRepository(ToppingItemRepository toppingItemRepository) {
        this.toppingItemRepository = toppingItemRepository;
    }

    @Autowired
    public void setSettingCustomerOptionRepository(OrderSettingCustomerOptionRepository settingCustomerOptionRepository) {
        this.settingCustomerOptionRepository = settingCustomerOptionRepository;
    }

    @Autowired
    public void setOrderHasCouponRepository(OrderHasCouponRepository orderHasCouponRepository) {
        this.orderHasCouponRepository = orderHasCouponRepository;
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
    public void setPromotionOrderEntryConsumedRepository(PromotionOrderEntryConsumedRepository promotionOrderEntryConsumedRepository) {
        this.promotionOrderEntryConsumedRepository = promotionOrderEntryConsumedRepository;
    }

    @Autowired
    public void setTagService(TagService tagService) {
        this.tagService = tagService;
    }

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Autowired
    public void setOrderDAO(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }
}
