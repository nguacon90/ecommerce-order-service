package com.vctek.orderservice.service.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.request.*;
import com.vctek.orderservice.dto.request.storefront.ProductPromotionRequest;
import com.vctek.orderservice.dto.request.storefront.StoreFrontSubOrderEntryRequest;
import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.OrderSettingDiscountFacade;
import com.vctek.orderservice.facade.OrderSettingFacade;
import com.vctek.orderservice.feignclient.dto.*;
import com.vctek.orderservice.kafka.producer.UpdateProductInventoryProducer;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.promotionengine.util.CommonUtils;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.service.event.OrderEvent;
import com.vctek.orderservice.strategy.*;
import com.vctek.orderservice.util.CurrencyUtils;
import com.vctek.orderservice.util.EventType;
import com.vctek.orderservice.util.PriceType;
import com.vctek.orderservice.util.SellSignal;
import com.vctek.redis.elastic.ProductSearchData;
import com.vctek.service.UserService;
import com.vctek.util.ComboType;
import com.vctek.util.CurrencyType;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DefaultCommerceCartService implements CommerceCartService {
    private AddToCartStrategy addToCartStrategy;
    private CommerceCartCalculationStrategy commerceCartCalculationStrategy;
    private CommerceRemoveEntriesStrategy commerceRemoveEntriesStrategy;
    private CommerceUpdateCartEntryStrategy commerceUpdateCartEntryStrategy;
    private CommerceUpdateCartStrategy commerceUpdateCartStrategy;
    private CommercePlaceOrderStrategy commercePlaceOrderStrategy;
    private CommerceCartMergingStrategy commerceCartMergingStrategy;
    private ModelService modelService;
    private ToppingOptionService toppingOptionService;
    private ToppingItemService toppingItemService;
    private ApplicationEventPublisher applicationEventPublisher;
    private InventoryService inventoryService;
    private BillService billService;
    private CalculationService calculationService;
    private LoyaltyService loyaltyService;
    private InvoiceService invoiceService;
    private ProductService productService;
    private OrderSettingDiscountFacade orderSettingDiscountFacade;
    private OrderSettingFacade orderSettingFacade;
    private LogisticService logisticService;
    private UpdateProductInventoryProducer updateProductInventoryProducer;
    private OrderStorefrontSetupService orderStorefrontSetupService;
    private CartService cartService;
    private UserService userService;
    private EntryMergeStrategy entryMergeStrategy;

    private CouponService couponService;
    private OrderSourceService orderSourceService;

    public DefaultCommerceCartService(AddToCartStrategy addToCartStrategy,
                                      CommerceCartCalculationStrategy commerceCartCalculationStrategy) {
        this.addToCartStrategy = addToCartStrategy;
        this.commerceCartCalculationStrategy = commerceCartCalculationStrategy;
    }

    @Override
    @Transactional
    public CommerceCartModification addToCart(CommerceAbstractOrderParameter parameter) {
        return addToCartStrategy.addToCart(parameter);
    }

    @Override
    @Transactional
    public CommerceCartModification addEntryToOrder(CommerceAbstractOrderParameter parameter) {
        CommerceCartModification commerceCartModification = addToCartStrategy.addEntryToOrder(parameter);
        commerceUpdateCartEntryStrategy.handleUpdateEntryStockHoldingOnline(parameter.getOrder(), commerceCartModification.getEntry(), parameter.getQuantity());
        OrderModel order = (OrderModel) parameter.getOrder();
        loyaltyService.updateRewardRedeemForOrder(order);
        OrderModel savedOrder = modelService.save(order);

        if (shouldUpdateOrderBill(savedOrder)) {
            commercePlaceOrderStrategy.updateProductInReturnBillWithOrder(savedOrder, commerceCartModification);
            commercePlaceOrderStrategy.updatePriceAndDiscountBillOf(savedOrder);
        }

        commerceCartModification.setOrder(savedOrder);
        applicationEventPublisher.publishEvent(new OrderEvent(savedOrder));
        return commerceCartModification;
    }

    @Override
    @Transactional
    public void removeAllEntries(CommerceAbstractOrderParameter parameter) {
        commerceRemoveEntriesStrategy.removeAllEntries(parameter);
        parameter.setRecalculateVat(true);
        commerceCartCalculationStrategy.recalculateCart(parameter);
    }

    @Override
    @Transactional
    public CommerceCartModification updateQuantityForCartEntry(CommerceAbstractOrderParameter parameter) {
        return commerceUpdateCartEntryStrategy.updateQuantityForCartEntry(parameter);
    }

    @Override
    @Transactional
    public void updateDiscountForCartEntry(CommerceAbstractOrderParameter parameter) {
        commerceUpdateCartEntryStrategy.updateDiscountForCartEntry(parameter);
    }

    @Override
    @Transactional
    public void updateDiscountForOrderEntry(CommerceAbstractOrderParameter parameter) {
        OrderModel order = (OrderModel) parameter.getOrder();
        commerceUpdateCartEntryStrategy.updateDiscountForCartEntry(parameter);
        loyaltyService.updateRewardRedeemForOrder(order);
        order = modelService.save(order);

        commercePlaceOrderStrategy.updatePriceAndDiscountBillOf(order);
        applicationEventPublisher.publishEvent(new OrderEvent(order));
    }

    @Override
    @Transactional
    public void updateDiscountForCart(CommerceAbstractOrderParameter parameter) {
        commerceUpdateCartStrategy.updateCartDiscount(parameter);
    }

    @Override
    @Transactional
    public void updateDiscountForOrder(CommerceAbstractOrderParameter parameter) {
        OrderModel order = (OrderModel) parameter.getOrder();
        commerceUpdateCartStrategy.updateCartDiscount(parameter);
        OrderModel savedOrder = modelService.save(order);
        commercePlaceOrderStrategy.updatePriceAndDiscountBillOf(savedOrder);
        applicationEventPublisher.publishEvent(new OrderEvent(savedOrder));
    }

    @Override
    @Transactional
    public void updateVatForCart(CommerceAbstractOrderParameter parameter) {
        commerceUpdateCartStrategy.updateVat(parameter);
        AbstractOrderModel abstractOrderModel = parameter.getOrder();
        if (abstractOrderModel instanceof OrderModel) {
            applicationEventPublisher.publishEvent(new OrderEvent((OrderModel) abstractOrderModel));
        }
    }

    @Override
    @Transactional
    public void updatePriceForCartEntry(CommerceAbstractOrderParameter parameter) {
        commerceUpdateCartEntryStrategy.updatePriceForCartEntry(parameter);
        AbstractOrderModel order = parameter.getOrder();
        if (order instanceof OrderModel) {
            commercePlaceOrderStrategy.updatePriceAndDiscountBillOf((OrderModel) order);
            loyaltyService.updateRewardRedeemForOrder((OrderModel) order);
            modelService.save(order);
            applicationEventPublisher.publishEvent(new OrderEvent((OrderModel) order));

        }
    }

    @Override
    @Transactional
    public void updatePriceForCartEntries(AbstractOrderModel orderModel) {
        Map<Long, Double> productPrice = getPriceProductOfPriceType(orderModel);

        for (AbstractOrderEntryModel entry : orderModel.getEntries()) {
            Double basePrice = productPrice.get(entry.getProductId());
            entry.setBasePrice(basePrice);
            entry.setOriginBasePrice(basePrice);
            if (!PriceType.DISTRIBUTOR_PRICE.toString().equals(orderModel.getPriceType())) {
                entry.setRecommendedRetailPrice(null);
            }
        }
        validatePriceCombo(orderModel);

        modelService.save(orderModel);
        recalculate(orderModel, true);

        if (orderModel instanceof OrderModel) {
            commercePlaceOrderStrategy.updatePriceAndDiscountBillOf((OrderModel) orderModel);
            loyaltyService.updateRewardRedeemForOrder((OrderModel) orderModel);
            modelService.save(orderModel);
            applicationEventPublisher.publishEvent(new OrderEvent((OrderModel) orderModel));
        }
    }

    private void validatePriceCombo(AbstractOrderModel model) {
        List<AbstractOrderEntryModel> entryModels = model.getEntries().stream()
                .filter(i -> StringUtils.isNotBlank(i.getComboType())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(entryModels)) return;
        List<Object> errorMap = new ArrayList<>();
        for (AbstractOrderEntryModel entryModel : entryModels) {
            try {
                commerceUpdateCartEntryStrategy.validatePriceForCartEntry(entryModel, model);
            } catch (ServiceException e) {
                errorMap.add(entryModel.getProductId());
            }
        }
        if (CollectionUtils.isNotEmpty(errorMap)) {
            ErrorCodes err = ErrorCodes.CHANGE_PRICE_TYPE_INVALID_COMBO_PRICE_LARGER_THAN_OR_LESS_THAN;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{errorMap});
        }
    }

    private Map<Long, Double> getPriceProductOfPriceType(AbstractOrderModel orderModel) {
        if (!PriceType.WHOLESALE_PRICE.toString().equals(orderModel.getPriceType())) {
            return getPriceProductTypeNotWholesale(orderModel);
        }
        List<String> stringProductId = orderModel.getEntries().stream().map(i -> i.getProductId().toString()).collect(Collectors.toList());
        String stringProductIds = String.join(",", stringProductId);

        ProductSearchRequest searchRequest = new ProductSearchRequest();
        searchRequest.setIds(stringProductIds);
        searchRequest.setCompanyId(orderModel.getCompanyId());
        searchRequest.setPageSize(stringProductId.size());
        List<ProductSearchData> productSearchData = productService.search(searchRequest);
        if (CollectionUtils.isEmpty(productSearchData)) {
            ErrorCodes err = ErrorCodes.PRODUCT_HAS_NOT_WHOLESALE_PRICE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        Map<Long, ProductSearchData> productSearchDataMap = productSearchData.stream()
                .collect(Collectors.toMap(ProductSearchData::getId, Function.identity(), (item1, item2) -> item2));
        List<AbstractOrderEntryModel> entries = orderModel.getEntries();
        Map<Long, Double> wholeSalePrices = new HashMap<>();
        for (AbstractOrderEntryModel entry : entries) {
            ProductSearchData data = productSearchDataMap.get(entry.getProductId());
            if (isEmptyWholesalePriceProduct(entry, data)) {
                ErrorCodes err = ErrorCodes.PRODUCT_HAS_NOT_WHOLESALE_PRICE;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }

            if (data.getWholesalePrice() != null) {
                wholeSalePrices.put(entry.getProductId(), data.getWholesalePrice());
                continue;
            }

            if (entry.isGiveAway() && CollectionUtils.isNotEmpty(data.getPrices())) {
                wholeSalePrices.put(entry.getProductId(), data.getPrices().get(0).getPrice());
            }
        }

        return wholeSalePrices;
    }

    private Map<Long, Double> getPriceProductTypeNotWholesale(AbstractOrderModel orderModel) {
        String priceType = orderModel.getPriceType();
        List<Long> productIds = new ArrayList<>();
        Map<Long, Double> priceMap = new HashMap<>();
        for (AbstractOrderEntryModel entry : orderModel.getEntries()) {
            if (StringUtils.isNotBlank(entry.getComboType())) {
                PriceData priceData = productService.getPriceOfProduct(entry.getProductId(), entry.getQuantity().intValue());
                priceMap.put(entry.getProductId(), priceData.getPrice());
                continue;
            }
            productIds.add(entry.getProductId());
        }
        if (CollectionUtils.isNotEmpty(productIds)) {
            String stringProductIds = StringUtils.join(productIds, ",");
            Map<Long, Double> originPrice = getPriceProductOfRetailPriceType(stringProductIds);
            priceMap.putAll(originPrice);
        }

        if (PriceType.DISTRIBUTOR_PRICE.name().equals(priceType)) {
            return getPriceProductOfDistributorPriceType(orderModel, priceMap);
        }

        return priceMap;
    }

    private Map<Long, Double> getPriceProductOfRetailPriceType(String productIds) {
        List<PriceData> priceData = productService.getListPriceOfProductIds(productIds);
        return priceData.stream().collect(Collectors.toMap(PriceData::getProductId, PriceData::getPrice));
    }

    private Map<Long, Double> getPriceProductOfDistributorPriceType(AbstractOrderModel model, Map<Long, Double> productPriceMap) {
        List<Long> productIds = model.getEntries().stream().map(i -> i.getProductId()).collect(Collectors.toList());
        Map<Long, DistributorSetingPriceData> priceDataMap = logisticService.getProductPriceSetting(
                model.getDistributorId(), model.getCompanyId(), productIds);
        Map<Long, Double> price = new HashMap<>();
        for (AbstractOrderEntryModel entry : model.getEntries()) {
            Double originBasePrice = productPriceMap.get(entry.getProductId());
            entry.setRecommendedRetailPrice(originBasePrice);
            entry.setOriginBasePrice(originBasePrice);
            if (priceDataMap.containsKey(entry.getProductId())) {
                DistributorSetingPriceData setingPriceData = priceDataMap.get(entry.getProductId());
                if (setingPriceData.getRecommendedRetailPrice() != null) {
                    entry.setRecommendedRetailPrice(setingPriceData.getRecommendedRetailPrice());
                }
                double basePrice = logisticService.calculateDistributorSettingPrice(setingPriceData, entry.getRecommendedRetailPrice());
                price.put(entry.getProductId(), basePrice);
                continue;
            }
            price.put(entry.getProductId(), originBasePrice);
        }
        return price;
    }

    private boolean isEmptyWholesalePriceProduct(AbstractOrderEntryModel entry, ProductSearchData data) {
        return data == null || (!entry.isGiveAway() && data.getWholesalePrice() == null);
    }

    @Override
    @Transactional
    public void recalculate(AbstractOrderModel abstractOrderModel, boolean recalculateVat) {
        CommerceAbstractOrderParameter parameter = new CommerceAbstractOrderParameter();
        parameter.setOrder(abstractOrderModel);
        parameter.setRecalculateVat(recalculateVat);
        commerceCartCalculationStrategy.recalculateCart(parameter);
    }

    @Override
    @Transactional
    public void updateWeightForOrderEntry(CommerceAbstractOrderParameter parameter) {
        commerceUpdateCartEntryStrategy.updateWeightForOrderEntry(parameter);
        AbstractOrderModel abstractOrderModel = parameter.getOrder();
        if (abstractOrderModel instanceof OrderModel) {
            applicationEventPublisher.publishEvent(new OrderEvent((OrderModel) abstractOrderModel));
        }
    }

    @Override
    @Transactional
    public void changeOrderEntryToComboEntry(CommerceAbstractOrderParameter cartParameter) {
        addToCartStrategy.changeOrderEntryToComboEntry(cartParameter);
        AbstractOrderModel abstractOrderModel = cartParameter.getOrder();
        if (abstractOrderModel instanceof OrderModel) {
            applicationEventPublisher.publishEvent(new OrderEvent((OrderModel) abstractOrderModel));
        }
    }

    @Override
    @Transactional
    public void updateSubOrderEntry(AbstractOrderEntryModel entryModel) {
        commerceUpdateCartEntryStrategy.updateSubOrderEntry(entryModel);
    }

    @Override
    @Transactional
    public CommerceCartModification updateOrderEntry(CommerceAbstractOrderParameter parameter) {
        OrderModel order = (OrderModel) parameter.getOrder();
        CommerceCartModification commerceCartModification = commerceUpdateCartEntryStrategy.updateQuantityForCartEntry(parameter);
        AbstractOrderEntryModel entryModel = commerceCartModification.getEntry();
        if (!commerceCartModification.isDeletedEntry()) {
            commerceUpdateCartEntryStrategy.updateSubOrderEntry(entryModel);
        }

        loyaltyService.updateRewardRedeemForOrder(order);
        OrderModel savedOrder = modelService.save(order);

        commerceCartModification.setOrder(savedOrder);
        if (!shouldUpdateOrderBill(savedOrder)) {
            applicationEventPublisher.publishEvent(new OrderEvent(savedOrder));
            return commerceCartModification;
        }

        if (commerceCartModification.isDeletedEntry()) {
            deleteToppingInReturnBillWithOrder(savedOrder, entryModel);
            commercePlaceOrderStrategy.deleteProductInReturnBillWithOrder(savedOrder, commerceCartModification);
        } else {
            commercePlaceOrderStrategy.updateProductInReturnBillWithOrder(savedOrder, commerceCartModification);
        }

        commercePlaceOrderStrategy.updatePriceAndDiscountBillOf(savedOrder);
        applicationEventPublisher.publishEvent(new OrderEvent(savedOrder));
        return commerceCartModification;
    }

    private void deleteToppingInReturnBillWithOrder(OrderModel savedOrder, AbstractOrderEntryModel entryModel) {
        Set<ToppingOptionModel> toppingOptionModels = entryModel.getToppingOptionModels();
        if (CollectionUtils.isNotEmpty(toppingOptionModels)) {
            updateToppingInReturnBillWithOrder(entryModel, savedOrder,
                    toppingOptionModels.stream().collect(Collectors.toList()), true);
        }
    }

    @Override
    public boolean shouldUpdateOrderBill(OrderModel orderModel) {
        return billService.shouldUpdateBillOf(orderModel);
    }

    @Override
    public CommerceCartModification addProductToCombo(CommerceAbstractOrderEntryParameter parameter) {
        CommerceCartModification modification = new CommerceCartModification();
        AbstractOrderEntryModel orderEntryModel = parameter.getOrderEntryModel();
        AbstractOrderModel orderModel = parameter.getOrderModel();
        ProductInComboData productInComboData = parameter.getProductInComboData();
        ComboData comboData = parameter.getComboData();

        modification.setOrder(orderModel);
        modification.setProductId(productInComboData.getId());

        SubOrderEntryModel subOrderEntryModel = findExistedSubEntryOf(orderEntryModel, productInComboData.getId());
        int orderEntryQty = orderEntryModel.getQuantity().intValue();
        if (subOrderEntryModel != null && comboData.isDuplicateSaleProduct()) {
            int productInComboQuantity = productInComboData.getQuantity();
            int newSubOrderEntryQty = subOrderEntryModel.getQuantity() + orderEntryQty * productInComboQuantity;
            int qtyAdd;
            if (productInComboData.isUpdateQuantity()) {
                newSubOrderEntryQty = orderEntryQty * productInComboQuantity;
                qtyAdd = newSubOrderEntryQty - subOrderEntryModel.getQuantity();
            } else {
                qtyAdd = productInComboQuantity * orderEntryQty;
            }
            modification.setQuantityAdded((long) qtyAdd);
            subOrderEntryModel.setQuantity(newSubOrderEntryQty);
        } else {
            subOrderEntryModel = populateSubOrderEntry(orderModel, orderEntryModel, productInComboData);
            modification.setQuantityAdded((long) productInComboData.getQuantity() * orderEntryQty);
        }

        commerceUpdateCartEntryStrategy.validatePriceForCartEntry(orderEntryModel, orderModel);
        AbstractOrderEntryModel saveEntry = this.modelService.save(orderEntryModel);
        modification.setEntry(saveEntry);
        return modification;
    }

    protected void updateToppingInReturnBillWithOrder(AbstractOrderEntryModel entryModel, OrderModel orderModel,
                                                      List<ToppingOptionModel> optionModels, boolean isDeleted) {
        List<OrderBillRequest> orderBillRequests = new ArrayList<>();
        OrderBillRequest orderBillRequest;
        for (ToppingOptionModel toppingOptionModel : optionModels) {
            Set<ToppingItemModel> toppingItemModels = toppingOptionModel.getToppingItemModels();
            for (ToppingItemModel toppingItem : toppingItemModels) {
                orderBillRequest = populateOrderBillRequest(entryModel, orderModel, toppingItem, toppingOptionModel);
                if (isDeleted) {
                    orderBillRequest.setQuantity(0);
                }
                orderBillRequests.add(orderBillRequest);
            }
        }

        if (CollectionUtils.isNotEmpty(orderBillRequests)) {
            commercePlaceOrderStrategy.updateOrDeleteToppingInReturnBillWithOrder(orderModel, orderBillRequests);
        }
    }

    protected void deleteOrUpdateToppingItemInBill(AbstractOrderEntryModel entryModel, OrderModel orderModel,
                                                   ToppingOptionModel toppingOptionModel, ToppingItemModel toppingItem,
                                                   boolean isDeleted) {
        OrderBillRequest orderBillRequest = populateOrderBillRequest(entryModel, orderModel, toppingItem, toppingOptionModel);
        if (isDeleted) {
            orderBillRequest.setQuantity(0);
        }
        commercePlaceOrderStrategy.updateOrDeleteToppingInReturnBillWithOrder(orderModel, Arrays.asList(orderBillRequest));
    }

    @Override
    @Transactional
    public void updateOrderToppingOption(ToppingOptionParameter parameter) {
        OrderModel orderModel = (OrderModel) parameter.getAbstractOrderModel();
        AbstractOrderEntryModel abstractOrderEntryModel = parameter.getAbstractOrderEntryModel();
        ToppingOptionModification modification = this.updateToppingOption(parameter);
        if (shouldUpdateOrderBill(orderModel)) {
            this.updateToppingInReturnBillWithOrder(abstractOrderEntryModel, orderModel,
                    Arrays.asList(modification.getToppingOptionModel()), modification.isDeleted());
        } else {
            commerceUpdateCartEntryStrategy.updateStockHoldingToppingOptionWithOrder(orderModel, modification.getToppingOptionModel(), modification.getQuantityAdd());
        }
        applicationEventPublisher.publishEvent(new OrderEvent(orderModel));
    }

    @Override
    @Transactional
    public void updateOrderToppingItem(ToppingItemParameter parameter) {
        OrderModel orderModel = (OrderModel) parameter.getAbstractOrderModel();
        ToppingItemModification modification = this.updateToppingItem(parameter);
        ToppingOptionModel toppingOptionModel = parameter.getToppingOptionModel();
        int optQty = CommonUtils.getIntValue(toppingOptionModel.getQuantity());
        int quantity = (modification.getQuantity() - modification.getOldQuantity()) * optQty;
        commerceUpdateCartEntryStrategy.handleUpdateToppingItemStockHoldingOnline(orderModel, modification.getProductId(), quantity);
        if (this.shouldUpdateOrderBill(orderModel)) {
            AbstractOrderEntryModel abstractOrderEntryModel = parameter.getAbstractOrderEntryModel();
            ToppingItemModel toppingItemModel = modification.getToppingItemModel();
            this.deleteOrUpdateToppingItemInBill(abstractOrderEntryModel, orderModel, toppingOptionModel,
                    toppingItemModel, modification.isDeleted());
        }

        applicationEventPublisher.publishEvent(new OrderEvent(orderModel));
    }

    @Override
    @Transactional
    public CommerceCartModification addProductToComboInOrder(CommerceAbstractOrderEntryParameter parameter) {
        OrderModel order = (OrderModel) parameter.getOrderModel();
        CommerceCartModification modification = this.addProductToCombo(parameter);
        this.updateSubOrderEntry(parameter.getOrderEntryModel());
        calculationService.calculateVatByProductOf(order, true);
        OrderEntryModel entry = (OrderEntryModel) parameter.getOrderEntryModel();
        addHoldingStockOrPreOrder(order, entry, modification.getProductId(), modification.getQuantityAdded());

        if (this.shouldUpdateOrderBill(order)) {
            commercePlaceOrderStrategy.updateComboInReturnBillWithOrder(order, modification.getEntry());
        }
        applicationEventPublisher.publishEvent(new OrderEvent(order));
        return modification;
    }

    private void addHoldingStockOrPreOrder(OrderModel orderModel, OrderEntryModel entry, Long productId, Long quantity) {
        if (!OrderType.ONLINE.name().equals(orderModel.getType())) return;

        UpdateProductInventoryDetailData data = new UpdateProductInventoryDetailData();
        data.setValue(Math.abs(quantity));
        data.setProductId(productId);
        OrderStatus orderStatus = OrderStatus.findByCode(orderModel.getOrderStatus());
        boolean holding = entry.isHolding();
        boolean preOrder = entry.isPreOrder();
        if (OrderStatus.CONFIRMED.value() <= orderStatus.value() && orderStatus.value() < OrderStatus.SHIPPING.value()) {
            holding = true;
        }
        if (holding) {
            if (quantity < 0) {
                holding = false;
            }
            inventoryService.updateStockHoldingProductOfList(orderModel, Arrays.asList(data), holding);
        }

        if (preOrder) {
            if (quantity < 0) {
                preOrder = false;
            }
            inventoryService.updatePreOrderProductOfList(orderModel, Arrays.asList(data), preOrder);
        }
    }

    @Override
    @Transactional
    public void updateHolingProduct(OrderModel order, OrderEntryModel entryModel, HoldingData holdingData) {
        inventoryService.updateHoldingProductOf(order, entryModel, holdingData);
        modelService.save(entryModel);
        applicationEventPublisher.publishEvent(new OrderEvent(order));
    }

    @Override
    @Transactional
    public void updateNoteInOrder(OrderModel order, NoteRequest noteRequest) {
        order.setCustomerNote(com.vctek.orderservice.util.CommonUtils.escapeSpecialSymbols(noteRequest.getCustomerNote()));
        order.setNote(com.vctek.orderservice.util.CommonUtils.escapeSpecialSymbols(noteRequest.getNote()));
        order.setCustomerSupportNote(com.vctek.orderservice.util.CommonUtils.escapeSpecialSymbols(noteRequest.getCustomerSupportNote()));
        order = modelService.save(order);
        applicationEventPublisher.publishEvent(new OrderEvent(order));
    }

    @Override
    @Transactional
    public void removeOrder(OrderModel ordermodel) {
        ordermodel.setDeleted(true);
        Set<PaymentTransactionModel> paymentTransactions = ordermodel.getPaymentTransactions();
        for (PaymentTransactionModel transactionModel : paymentTransactions) {
            transactionModel.setAmount(0d);
            transactionModel.setDeleted(true);
        }
        OrderModel savedModel = modelService.save(ordermodel);
        invoiceService.saveInvoices(ordermodel, ordermodel.getCustomerId());
        OrderEvent orderEvent = new OrderEvent(savedModel);
        orderEvent.setEventType(EventType.DELETE);
        applicationEventPublisher.publishEvent(orderEvent);
    }

    @Override
    @Transactional
    public void holdingProductOfOrder(HoldingProductRequest request, OrderModel orderModel) {
        inventoryService.holdingProducts(request, orderModel);
        modelService.save(orderModel);
        applicationEventPublisher.publishEvent(new OrderEvent(orderModel));
    }

    @Override
    @Transactional
    public void updateDiscountForToppingItem(ToppingItemParameter parameter) {
        ToppingOptionModel toppingOptionModel = parameter.getToppingOptionModel();
        ToppingItemModel toppingItem = toppingItemService.findByIdAndToppingOption(parameter.getToppingItemId(), toppingOptionModel);
        if (toppingItem == null) {
            ErrorCodes err = ErrorCodes.INVALID_TOPPING_ITEM_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        toppingItem.setDiscountType(parameter.getDiscountType());
        toppingItem.setDiscount(parameter.getDiscount());
        modelService.save(toppingItem);
        AbstractOrderModel abstractOrderModel = parameter.getAbstractOrderModel();
        recalculate(abstractOrderModel, true);
        if (abstractOrderModel instanceof OrderModel) {
            applicationEventPublisher.publishEvent(new OrderEvent((OrderModel) abstractOrderModel));
        }
    }

    @Override
    @Transactional
    public void updateListOrderEntry(AbstractOrderModel model, EntryRequest request) {
        CommerceCartModification commerceCartModification = commerceUpdateCartEntryStrategy.removeListCartEntry(model, request);
        AbstractOrderModel abstractOrderModel = commerceCartModification.getOrder();
        if (abstractOrderModel instanceof OrderModel) {
            applicationEventPublisher.publishEvent(new OrderEvent((OrderModel) abstractOrderModel));
        }

    }

    @Override
    public void calculateComboEntryPrices(CartEntryModel entryModel) {
        calculationService.calculateSubEntryPriceWithCombo(entryModel);
        AbstractOrderModel abstractOrderModel = entryModel.getOrder();
        calculationService.calculateVatByProductOf(abstractOrderModel, true);
        modelService.save(entryModel);
    }

    @Override
    public void clearComboEntryPrices(CartEntryModel entryModel) {
        calculationService.clearComboEntryPrices(entryModel);
    }

    @Override
    @Transactional
    public void updateShippingFee(CommerceAbstractOrderParameter parameter) {
        calculationService.calculateVat(parameter.getOrder());
        AbstractOrderModel abstractOrderModel = parameter.getOrder();
        if (abstractOrderModel instanceof OrderModel) {
            applicationEventPublisher.publishEvent(new OrderEvent((OrderModel) abstractOrderModel));
        }
    }

    @Override
    @Transactional
    public void updateDefaultSettingCustomer(CommerceAbstractOrderParameter parameter) {
        AbstractOrderModel abstractOrderModel = parameter.getOrder();
        modelService.save(abstractOrderModel);
        if (abstractOrderModel instanceof OrderModel) {
            applicationEventPublisher.publishEvent(new OrderEvent((OrderModel) abstractOrderModel));
        }
    }

    @Override
    @Transactional
    public void updateAllDiscountForCart(CommerceAbstractOrderParameter parameter, UpdateAllDiscountRequest request) {
        final AbstractOrderModel abstractOrderModel = parameter.getOrder();
        Map<Long, OrderSettingDiscountData> productSettingDiscount = orderSettingDiscountFacade.getDiscountProduct(request.getCompanyId(), request.getProductIds());
        Set<ToppingItemModel> toppingItemModels = toppingItemService.findAllByOrderId(abstractOrderModel.getId());
        if (CollectionUtils.isNotEmpty(toppingItemModels)) {
            for (ToppingItemModel toppingItemModel : toppingItemModels) {
                OrderSettingDiscountData settingDiscountData = productSettingDiscount.get(toppingItemModel.getProductId());
                if (settingDiscountData != null) {
                    int quantity = toppingItemModel.getQuantity() / toppingItemModel.getToppingOptionModel().getQuantity();
                    double discount = CurrencyUtils.computeValue(settingDiscountData.getDiscount(),
                            settingDiscountData.getDiscountType(), toppingItemModel.getBasePrice());
                    toppingItemModel.setDiscount(discount * quantity);
                    toppingItemModel.setDiscountType(CurrencyType.CASH.toString());
                }
            }
            modelService.saveAll(toppingItemModels);
        }

        for (AbstractOrderEntryModel entryModel : abstractOrderModel.getEntries()) {
            OrderSettingDiscountData settingDiscountData = productSettingDiscount.get(entryModel.getProductId());
            if (settingDiscountData != null) {
                double discount = CurrencyUtils.computeValue(settingDiscountData.getDiscount(),
                        settingDiscountData.getDiscountType(), entryModel.getOriginBasePrice());
                entryModel.setDiscount(discount * entryModel.getQuantity());
                entryModel.setDiscountType(CurrencyType.CASH.toString());
            }
        }
        AbstractOrderModel orderModel = modelService.save(abstractOrderModel);
        recalculate(orderModel, true);
    }

    @Override
    @Transactional
    public void addToppingOption(ToppingOptionParameter parameter) {
        AbstractOrderEntryModel abstractOrderEntryModel = parameter.getAbstractOrderEntryModel();
        validateToppingOptionLimit(abstractOrderEntryModel, parameter.getQuantity());
        ToppingOptionModel model = new ToppingOptionModel();
        model.setQuantity(parameter.getQuantity());
        model.setIce(parameter.getIce());
        model.setSugar(parameter.getSugar());
        model.setOrderEntry(abstractOrderEntryModel);
        abstractOrderEntryModel.getToppingOptionModels().add(model);
        modelService.save(abstractOrderEntryModel);
        AbstractOrderModel abstractOrderModel = parameter.getAbstractOrderModel();
        if (abstractOrderModel instanceof OrderModel) {
            applicationEventPublisher.publishEvent(new OrderEvent((OrderModel) abstractOrderModel));
        }
    }

    private void validateToppingOptionLimit(AbstractOrderEntryModel abstractOrderEntryModel, int addedOptionQty) {
        Set<ToppingOptionModel> toppingOptionModels = abstractOrderEntryModel.getToppingOptionModels();
        if (CollectionUtils.isNotEmpty(toppingOptionModels)) {
            Long entryQty = abstractOrderEntryModel.getQuantity();
            int totalCurrentOptionQty = toppingOptionModels.stream().filter(opt -> opt.getQuantity() != null)
                    .mapToInt(ToppingOptionModel::getQuantity).sum();
            if (totalCurrentOptionQty + addedOptionQty > entryQty) {
                ErrorCodes err = ErrorCodes.INVALID_QUANTITY_IN_TOPPING_OPTION;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }
    }

    @Override
    @Transactional
    public ToppingOptionModification updateToppingOption(ToppingOptionParameter parameter) {
        ToppingOptionModification modification = new ToppingOptionModification();
        AbstractOrderEntryModel abstractOrderEntryModel = parameter.getAbstractOrderEntryModel();
        ToppingOptionModel toppingOptionModel = toppingOptionService.findByIdAndOrderEntry(parameter.getId(), abstractOrderEntryModel);
        validateToppingOption(toppingOptionModel);

        boolean shouldRecalculateCart = CollectionUtils.isNotEmpty(toppingOptionModel.getToppingItemModels());
        if (parameter.getQuantity() == null || parameter.getQuantity() <= 0) {
            abstractOrderEntryModel.getToppingOptionModels().remove(toppingOptionModel);
            modification.setDeleted(true);
            modification.setQuantityAdd(-toppingOptionModel.getQuantity());
        } else {
            int currentQty = toppingOptionModel.getQuantity() == null ? 0 : toppingOptionModel.getQuantity();
            int addedQty = parameter.getQuantity() - currentQty;
            validateToppingOptionLimit(abstractOrderEntryModel, addedQty);
            toppingOptionModel.setQuantity(parameter.getQuantity());
            toppingOptionModel.setSugar(parameter.getSugar());
            toppingOptionModel.setIce(parameter.getIce());
            modification.setQuantityAdd(addedQty);
        }
        modelService.save(abstractOrderEntryModel);
        if (shouldRecalculateCart) {
            AbstractOrderModel abstractOrderModel = parameter.getAbstractOrderModel();
            recalculate(abstractOrderModel, true);
        }
        modification.setModifiedToppingOptionId(parameter.getId());
        modification.setToppingOptionModel(toppingOptionModel);
        return modification;
    }

    @Override
    @Transactional
    public ToppingItemModification updateToppingItem(ToppingItemParameter parameter) {
        ToppingOptionModel toppingOptionModel = parameter.getToppingOptionModel();
        ToppingItemModel toppingItem = toppingItemService.findByIdAndToppingOption(parameter.getToppingItemId(), toppingOptionModel);
        if (toppingItem == null) {
            ErrorCodes err = ErrorCodes.INVALID_TOPPING_ITEM_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        Integer oldQty = toppingItem.getQuantity();
        ToppingItemModification modification = new ToppingItemModification();
        if (parameter.getQuantity() == null || parameter.getQuantity() <= 0) {
            toppingOptionModel.getToppingItemModels().remove(toppingItem);
            modification.setDeleted(true);
        } else {
            toppingItem.setQuantity(parameter.getQuantity());
        }
        modelService.save(toppingOptionModel);
        modification.setToppingItemModel(toppingItem);
        modification.setToppingItemId(parameter.getToppingItemId());
        modification.setProductId(toppingItem.getProductId());
        modification.setQuantity(toppingItem.getQuantity());
        modification.setOldQuantity(oldQty);
        AbstractOrderModel abstractOrderModel = parameter.getAbstractOrderModel();
        recalculate(abstractOrderModel, true);
        return modification;
    }

    @Override
    @Transactional
    public void deleteToppingOptionInOrder(ToppingOptionParameter parameter) {
        AbstractOrderEntryModel abstractOrderEntryModel = parameter.getAbstractOrderEntryModel();
        ToppingOptionModel toppingOptionModel = toppingOptionService.findByIdAndOrderEntry(parameter.getId(), abstractOrderEntryModel);
        validateToppingOption(toppingOptionModel);
        abstractOrderEntryModel.getToppingOptionModels().remove(toppingOptionModel);
        OrderModel orderModel = (OrderModel) parameter.getAbstractOrderModel();
        if (shouldUpdateOrderBill(orderModel)) {
            this.updateToppingInReturnBillWithOrder(abstractOrderEntryModel, orderModel, Arrays.asList(toppingOptionModel), true);
        } else {
            commerceUpdateCartEntryStrategy.addOrRemoveStockHoldingToppingWithOrder(orderModel, Arrays.asList(toppingOptionModel), true);
        }
        modelService.save(abstractOrderEntryModel);
        applicationEventPublisher.publishEvent(new OrderEvent(orderModel));
    }

    private OrderBillRequest populateOrderBillRequest(AbstractOrderEntryModel entryModel,
                                                      OrderModel orderModel,
                                                      ToppingItemModel toppingItemModel, ToppingOptionModel optionModel) {
        OrderBillRequest orderBillRequest = new OrderBillRequest();
        orderBillRequest.setProductId(toppingItemModel.getProductId());
        orderBillRequest.setCompanyId(orderModel.getCompanyId());
        int optQty = CommonUtils.getIntValue(optionModel.getQuantity());
        int quantity = CommonUtils.getIntValue(toppingItemModel.getQuantity()) * optQty;
        orderBillRequest.setQuantity(quantity);
        orderBillRequest.setPrice(toppingItemModel.getBasePrice());
        orderBillRequest.setBillId(orderModel.getBillId());
        orderBillRequest.setOrderCode(orderModel.getCode());
        orderBillRequest.setOrderEntryId(entryModel.getId());
        orderBillRequest.setSaleOff(entryModel.isSaleOff());
        orderBillRequest.setToppingOptionId(optionModel.getId());
        orderBillRequest.setOrderStatus(orderModel.getOrderStatus());
        return orderBillRequest;
    }

    @Override
    @Transactional
    public void addToppingItem(ToppingItemParameter parameter) {
        ToppingItemModel toppingModel = new ToppingItemModel();
        toppingModel.setBasePrice(parameter.getPrice());
        toppingModel.setProductId(parameter.getProductId());
        toppingModel.setQuantity(parameter.getQuantity());
        toppingModel.setDiscount(parameter.getDiscount());
        toppingModel.setDiscountType(parameter.getDiscountType());
        ToppingOptionModel toppingOptionModel = parameter.getToppingOptionModel();
        validateToppingOption(toppingOptionModel);
        toppingModel.setToppingOptionModel(toppingOptionModel);
        toppingOptionModel.getToppingItemModels().add(toppingModel);
        modelService.save(toppingOptionModel);
        AbstractOrderModel abstractOrderModel = parameter.getAbstractOrderModel();
        recalculate(abstractOrderModel, true);
        if (abstractOrderModel instanceof OrderModel) {
            OrderModel orderModel = (OrderModel) abstractOrderModel;
            AbstractOrderEntryModel abstractOrderEntryModel = parameter.getAbstractOrderEntryModel();
            if (this.shouldUpdateOrderBill(orderModel)) {
                this.updateToppingInReturnBillWithOrder(abstractOrderEntryModel, orderModel,
                        Arrays.asList(toppingOptionModel), false);
            } else {
                commerceUpdateCartEntryStrategy.handleUpdateToppingItemStockHoldingOnline(orderModel, toppingModel.getProductId(), toppingModel.getQuantity() * toppingOptionModel.getQuantity());
            }
            applicationEventPublisher.publishEvent(new OrderEvent(orderModel));
        }
    }

    protected void validateToppingOption(ToppingOptionModel model) {
        if (model == null) {
            ErrorCodes err = ErrorCodes.INVALID_TOPPING_OPTION_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }


    protected SubOrderEntryModel populateSubOrderEntry(AbstractOrderModel orderModel, AbstractOrderEntryModel entryCombo, ProductInComboData productData) {
        SubOrderEntryModel subOrderEntryModel = new SubOrderEntryModel();
        subOrderEntryModel.setProductId(productData.getId());
        subOrderEntryModel.setOriginPrice(productData.getPrice());
        subOrderEntryModel.setOrderEntry(entryCombo);
        subOrderEntryModel.setComboGroupNumber(productData.getComboGroupNumber());
        if(!SellSignal.ECOMMERCE_WEB.toString().equalsIgnoreCase(orderModel.getSellSignal())) {
            subOrderEntryModel.setQuantity(Math.toIntExact(entryCombo.getQuantity()));
        } else {
            subOrderEntryModel.setQuantity(productData.getQuantity() * Math.toIntExact(entryCombo.getQuantity()));
        }
        entryCombo.getSubOrderEntries().add(subOrderEntryModel);
        return subOrderEntryModel;
    }

    private SubOrderEntryModel findExistedSubEntryOf(AbstractOrderEntryModel orderEntryModel, Long productId) {
        Set<SubOrderEntryModel> subOrderEntries = orderEntryModel.getSubOrderEntries();
        if (CollectionUtils.isEmpty(subOrderEntries)) {
            return null;
        }

        Optional<SubOrderEntryModel> firstSubEntryOptional = subOrderEntries.stream()
                .filter(soe -> soe.getProductId() != null && soe.getProductId().equals(productId))
                .findFirst();
        if (firstSubEntryOptional.isPresent()) {
            return firstSubEntryOptional.get();
        }

        return null;
    }

    @Override
    public List<OrderSettingDiscountData> checkDiscountMaximumOrder(AbstractOrderModel abstractOrderModel) {
        List<OrderSettingDiscountData> dataList = new ArrayList<>();
        if (!checkApplyOrderType(abstractOrderModel)) return dataList;

        List<AbstractOrderEntryModel> entryModels = abstractOrderModel.getEntries().stream()
                .filter(e -> !e.isGiveAway() && e.getDiscount() != null && e.getDiscount() > 0).collect(Collectors.toList());
        List<Long> productIds = entryModels.stream().map(e -> e.getProductId()).collect(Collectors.toList());
        Set<ToppingItemModel> toppingItemModels = toppingItemService.findAllByOrderId(abstractOrderModel.getId());
        toppingItemModels = toppingItemModels.stream().filter(i -> i.getDiscount() != null && i.getDiscount() > 0).collect(Collectors.toSet());
        List<Long> productToppingIds = toppingItemModels.stream().map(i -> i.getProductId()).collect(Collectors.toList());
        productIds.addAll(productToppingIds);

        Map<Long, OrderSettingDiscountData> settingDiscountProduct = orderSettingDiscountFacade.getDiscountProduct(abstractOrderModel.getCompanyId(), productIds);

        checkDiscountMaximunOrderEntries(dataList, entryModels, settingDiscountProduct);
        checkDiscountMaximunOrderToppingItem(dataList, toppingItemModels, settingDiscountProduct);

        return dataList;
    }

    @Override
    public Map<Long, OrderSettingDiscountData> checkDiscountMaximumProduct(AbstractOrderModel abstractOrderModel, Long productId) {
        if (!checkApplyOrderType(abstractOrderModel)) return new HashMap<>();

        return orderSettingDiscountFacade.getDiscountProduct(abstractOrderModel.getCompanyId(), Arrays.asList(productId));
    }

    private boolean checkApplyOrderType(AbstractOrderModel abstractOrderModel) {
        OrderSettingData orderSettingData = orderSettingFacade.getOrderMaximumDiscount(abstractOrderModel.getCompanyId());
        if (orderSettingData == null) return false;

        if (StringUtils.isNotBlank(orderSettingData.getOrderTypes())) {
            List<String> settingDiscountOrderType = Arrays.asList(orderSettingData.getOrderTypes().split(","));
            if (!settingDiscountOrderType.contains(abstractOrderModel.getType())) {
                return false;
            }
        }

        return true;
    }

    private void checkDiscountMaximunOrderToppingItem(List<OrderSettingDiscountData> dataList, Set<ToppingItemModel> toppingItemModels,
                                                      Map<Long, OrderSettingDiscountData> settingDiscountProduct) {
        for (ToppingItemModel toppingItemModel : toppingItemModels) {
            if (settingDiscountProduct.containsKey(toppingItemModel.getProductId())) {
                OrderSettingDiscountData data = settingDiscountProduct.get(toppingItemModel.getProductId());
                data.setOrderDiscount(toppingItemModel.getDiscount());
                data.setOrderDiscountType(toppingItemModel.getDiscountType());
                long quantity = toppingItemModel.getQuantity() / toppingItemModel.getToppingOptionModel().getQuantity();
                double totalPrice = toppingItemModel.getBasePrice() * quantity;
                if (validateMaximumDiscountProduct(data, totalPrice, quantity, toppingItemModel.getBasePrice())) {
                    OrderSettingDiscountData discountData = new OrderSettingDiscountData();
                    discountData.setProductId(toppingItemModel.getProductId());
                    discountData.setCategoryCode(data.getCategoryCode());
                    discountData.setOrderDiscount(toppingItemModel.getDiscount());
                    discountData.setOrderDiscountType(toppingItemModel.getDiscountType());
                    discountData.setDiscount(data.getDiscount());
                    discountData.setDiscountType(data.getDiscountType());
                    discountData.setToppingItemId(toppingItemModel.getId());
                    dataList.add(discountData);
                }
            }
        }
    }

    private void checkDiscountMaximunOrderEntries(List<OrderSettingDiscountData> dataList, List<AbstractOrderEntryModel> entryModels,
                                                  Map<Long, OrderSettingDiscountData> settingDiscountProduct) {
        for (AbstractOrderEntryModel entryModel : entryModels) {
            if (settingDiscountProduct.containsKey(entryModel.getProductId())) {
                OrderSettingDiscountData data = settingDiscountProduct.get(entryModel.getProductId());
                data.setOrderDiscount(entryModel.getDiscount());
                data.setOrderDiscountType(entryModel.getDiscountType());
                if (validateMaximumDiscountProduct(data, entryModel.getTotalPrice(), entryModel.getQuantity(), entryModel.getOriginBasePrice())) {
                    OrderSettingDiscountData discountData = new OrderSettingDiscountData();
                    discountData.setProductId(entryModel.getProductId());
                    discountData.setCategoryCode(data.getCategoryCode());
                    discountData.setDiscount(data.getDiscount());
                    discountData.setDiscountType(data.getDiscountType());
                    discountData.setOrderDiscount(entryModel.getDiscount());
                    discountData.setOrderDiscountType(entryModel.getDiscountType());
                    discountData.setEntryId(entryModel.getId());

                    dataList.add(discountData);
                }
            }
        }
    }

    private boolean validateMaximumDiscountProduct(OrderSettingDiscountData data, double totalPrice, Long quantity, double price) {
        double maximumDiscount = data.getDiscount();
        double orderDiscount = data.getOrderDiscount() / quantity;
        if (CurrencyType.PERCENT.toString().equals(data.getDiscountType())) {
            maximumDiscount = CurrencyUtils.computeValue(data.getDiscount(), data.getDiscountType(), price);
        }
        if (CurrencyType.PERCENT.toString().equals(data.getOrderDiscountType())) {
            orderDiscount = CurrencyUtils.computeValue(data.getOrderDiscount(), data.getOrderDiscountType(), totalPrice / quantity);
        }
        return orderDiscount > maximumDiscount;
    }

    @Override
    @Transactional
    public boolean updateRecommendedRetailPriceForCartEntry(CommerceAbstractOrderParameter parameter) {
        boolean updateBasePrice = commerceUpdateCartEntryStrategy.updateRecommendedRetailPriceForCartEntry(parameter);
        AbstractOrderModel order = parameter.getOrder();
        if (updateBasePrice && order instanceof OrderModel) {
            commercePlaceOrderStrategy.updatePriceAndDiscountBillOf((OrderModel) order);
            loyaltyService.updateRewardRedeemForOrder((OrderModel) order);
            modelService.save(order);
            applicationEventPublisher.publishEvent(new OrderEvent((OrderModel) order));
        }
        return updateBasePrice;
    }

    @Override
    @Transactional
    public void markEntrySaleOff(CommerceAbstractOrderParameter parameter) {
        AbstractOrderEntryModel entryToUpdate = commerceUpdateCartEntryStrategy.markEntrySaleOff(parameter);
        AbstractOrderModel abstractOrderModel = parameter.getOrder();
        if (abstractOrderModel instanceof OrderModel) {
            OrderModel order = (OrderModel) abstractOrderModel;
            commercePlaceOrderStrategy.updatePriceAndDiscountBillOf(order);
            loyaltyService.updateRewardRedeemForOrder(order);
            modelService.save(order);
            updateProductInventoryProducer.sendUpdateStockEntries(order, Arrays.asList(entryToUpdate));
            applicationEventPublisher.publishEvent(new OrderEvent(order));
        }
    }

    @Override
    @Transactional
    public AbstractOrderModel updateCustomer(UpdateCustomerRequest request, AbstractOrderModel abstractOrderModel) {
        CustomerRequest customerRequest = request.getCustomer();
        abstractOrderModel.setCustomerId(customerRequest.getId());
        abstractOrderModel.setCardNumber(request.getCardNumber());
        AddressRequest shippingAddress = customerRequest.getShippingAddress();
        if (shippingAddress != null) {
            abstractOrderModel.setShippingCustomerName(shippingAddress.getCustomerName());
            abstractOrderModel.setShippingCustomerPhone(shippingAddress.getPhone1());
            abstractOrderModel.setShippingAddressId(shippingAddress.getId());
            abstractOrderModel.setShippingProvinceId(shippingAddress.getProvinceId());
            abstractOrderModel.setShippingDistrictId(shippingAddress.getDistrictId());
            abstractOrderModel.setShippingWardId(shippingAddress.getWardId());
            abstractOrderModel.setShippingAddressDetail(shippingAddress.getAddressDetail());
        }
        modelService.save(abstractOrderModel);
        recalculate(abstractOrderModel, false);
        if (abstractOrderModel instanceof OrderModel) {
            OrderModel order = (OrderModel) abstractOrderModel;
            loyaltyService.updateRewardRedeemForOrder(order);
            applicationEventPublisher.publishEvent(new OrderEvent(order));
        }
        return abstractOrderModel;
    }

    @Override
    @Transactional
    public AbstractOrderModel addVatOf(AbstractOrderModel abstractOrderModel, Boolean addVat) {
        if (Boolean.TRUE.equals(addVat)) {
            abstractOrderModel.setHasGotVat(true);
            calculationService.calculateVatByProductOf(abstractOrderModel, true);
        } else {
            calculationService.resetVatOf(abstractOrderModel);
            calculationService.calculateVat(abstractOrderModel);
        }

        if (abstractOrderModel instanceof OrderModel) {
            OrderModel order = (OrderModel) abstractOrderModel;
            applicationEventPublisher.publishEvent(new OrderEvent(order));
        }
        return abstractOrderModel;
    }

    @Override
    public Map<Long, Double> getDiscountPriceFor(ProductPromotionRequest request) {
        OrderStorefrontSetupModel setupModel = orderStorefrontSetupService.findByCompanyId(request.getCompanyId());
        if (setupModel == null || setupModel.getWarehouseId() == null) {
            return new HashMap<>();
        }
        CartModel cartModel = new CartModel();
        cartModel.setType(OrderType.ONLINE.toString());
        cartModel.setOrderStatus(OrderStatus.NEW.code());
        cartModel.setWarehouseId(setupModel.getWarehouseId());
        cartModel.setCompanyId(request.getCompanyId());
        cartModel.setPriceType(PriceType.RETAIL_PRICE.toString());
        cartModel.setOrderSourceModel(orderSourceService.findByIdAndCompanyId(setupModel.getOrderSourceId(), request.getCompanyId()));
        List<AbstractOrderEntryModel> orderEntryModels = new ArrayList<>();
        List<ProductSearchModel> productSearchModels = request.getProductSearchModels();
        Map<Long, Integer> qtyMap = new HashMap<>();
        request.getProductList().forEach(p -> qtyMap.put(p.getProductId(), p.getQuantity() == null ? 1 : p.getQuantity()));
        int size = productSearchModels.size();
        for (int i = 0; i < size; i++) {
            ProductSearchModel p = productSearchModels.get(i);
            CartEntryModel cartEntryModel = new CartEntryModel();
            cartEntryModel.setId(i + 1l);
            cartEntryModel.setProductId(p.getId());
            cartEntryModel.setEntryNumber(i);
            List<com.vctek.redis.PriceData> prices = p.getPrices();
            if (CollectionUtils.isNotEmpty(prices)) {
                cartEntryModel.setBasePrice(prices.get(0).getPrice());
                Integer qty = qtyMap.get(p.getId());
                cartEntryModel.setQuantity(qty == null ? 1l : qty);
                cartEntryModel.setOrder(cartModel);
                orderEntryModels.add(cartEntryModel);
            }
        }
        cartModel.setEntries(orderEntryModels);
        return commerceCartCalculationStrategy.doAppliedCartTemp(cartModel);
    }

    @Override
    public CartModel getStorefrontCart(String cartCode, Long companyId) {
        CartInfoParameter cartInfoParameter = new CartInfoParameter();
        cartInfoParameter.setCode(cartCode);
        cartInfoParameter.setGuid(cartCode);
        cartInfoParameter.setCompanyId(companyId);
        Long currentUserId = userService.getCurrentUserId();
        if (currentUserId == null) {
            return cartService.getCartByGuid(cartInfoParameter);
        }

        return cartService.findByCodeAndUserIdAndCompanyId(cartInfoParameter.getCode(), currentUserId, cartInfoParameter.getCompanyId());
    }

    @Override
    public CartModel getOrCreateNewStorefrontCart(CartInfoParameter parameter) {
        Long currentUserId = userService.getCurrentUserId();
        parameter.setUserId(currentUserId);
        parameter.setGuid(UUID.randomUUID().toString());
        OrderSourceModel orderSourceModel = orderSourceService.findByIdAndCompanyId(parameter.getOrderSourceId(), parameter.getCompanyId());
        parameter.setOrderSourceModel(orderSourceModel);
        if (currentUserId == null) {
            return cartService.getOrCreateNewCart(parameter);
        }

        CartModel existedCart = cartService.findByUserIdAndCompanyIdAndSellSignal(parameter);
        if (existedCart != null) {
            existedCart.setWarehouseId(parameter.getWarehouseId());
            return cartService.save(existedCart);
        }

        return cartService.getOrCreateNewCart(parameter);
    }

    @Override
    public CartModel getByCompanyIdAndGuid(Long companyId, String guid) {
        CartInfoParameter param = new CartInfoParameter();
        param.setCompanyId(companyId);
        param.setGuid(guid);
        return cartService.getCartByGuid(param);
    }

    @Override
    public void mergeCarts(CartModel fromCart, CartModel toCart) {
        commerceCartMergingStrategy.mergeCarts(fromCart, toCart);
    }

    @Override
    public AbstractOrderEntryModel getExistedEntry(CommerceAbstractOrderParameter cartParameter) {
        List<AbstractOrderEntryModel> entries = cartParameter.getOrder().getEntries();
        AbstractOrderEntryModel entryModel = new AbstractOrderEntryModel();
        entryModel.setProductId(cartParameter.getProductId());
        entryModel.setBasePrice(cartParameter.getBasePrice());
        entryModel.setComboType(cartParameter.getComboType());
        entryModel.setQuantity(cartParameter.getQuantity());
        if (CollectionUtils.isNotEmpty(cartParameter.getSubEntries())) {
            Set<SubOrderEntryModel> subOrderEntryModels = new HashSet<>();
            cartParameter.getSubEntries().forEach(se -> {
                SubOrderEntryModel subOrderEntryModel = new SubOrderEntryModel();
                subOrderEntryModel.setComboGroupNumber(se.getComboGroupNumber());
                subOrderEntryModel.setQuantity((int) cartParameter.getQuantity() * se.getQuantity());
                subOrderEntryModel.setProductId(se.getProductId());
                subOrderEntryModels.add(subOrderEntryModel);
            });
            entryModel.setSubOrderEntries(subOrderEntryModels);
        }
        return entryMergeStrategy.getEntryToMerge(entries, entryModel);
    }

    @Override
    @Transactional
    public CommerceCartModification updateLatestPriceForEntries(CartModel cartModel) {
        CommerceCartModification cartModification = new CommerceCartModification();
        List<AbstractOrderEntryModel> entries = cartModel.getEntries();
        if(CollectionUtils.isEmpty(entries)) {
            return cartModification;
        }
        Map<Long, Double> priceMap = getPriceOfNormalProductList(entries);
        boolean shouldUpdate = false;
        for (AbstractOrderEntryModel e : entries) {
            if(e.isFixedPrice()) {
                continue;
            }

            Double updatePrice = priceMap.get(e.getProductId());
            if(cartService.isComboEntry(e)) {
                PriceData priceData = productService.getPriceOfProduct(e.getProductId(), e.getQuantity().intValue());
                updatePrice = priceData.getPrice();
            }

            if(shouldUpdatePrice(e.getBasePrice(), updatePrice)) {
                e.setBasePrice(updatePrice);
                e.setOriginBasePrice(updatePrice);
                shouldUpdate = true;
            }

            for (SubOrderEntryModel subOrderEntryModel : e.getSubOrderEntries()) {
                Double newPrice = priceMap.get(subOrderEntryModel.getProductId());
                if (shouldUpdatePrice(subOrderEntryModel.getOriginPrice(), newPrice)) {
                    subOrderEntryModel.setOriginPrice(newPrice);
                    shouldUpdate = true;
                }
            }
        }

        if (shouldUpdate) {
            cartModification.setUpdatePrice(true);
            cartService.save(cartModel);
            this.recalculate(cartModel, false);
        }
        return cartModification;
    }

    private Map<Long, Double> getPriceOfNormalProductList(List<AbstractOrderEntryModel> entries) {
        List<PriceRequest> priceRequests = populateNormalPriceRequestList(entries);
        if (CollectionUtils.isEmpty(priceRequests)) {
            return new HashMap<>();
        }

        PriceProductRequest request = new PriceProductRequest();
        request.setPriceRequestList(priceRequests);
        return productService.getPriceOfProductList(request);
    }

    @Override
    public CommerceCartValidateData validate(CommerceCartValidateParam param) {
        CartModel cartModel = param.getCartModel();
        CommerceCartValidateData commerceCartValidateData = new CommerceCartValidateData();
        List<AbstractOrderEntryModel> entries = cartModel.getEntries();
        if (CollectionUtils.isEmpty(entries)) {
            return commerceCartValidateData;
        }
        Long companyId = cartModel.getCompanyId();
        List<Long> productIds = populateProductIdsForCheckingStock(cartModel);
        Map<Long, Integer> productAvailableStock = inventoryService.getStoreFrontAvailableStockOfProductList(companyId, productIds);
        Map<Long, Long> totalBuyQuantityMap = getTotalProductQuantity(cartModel);

        for (AbstractOrderEntryModel entry : entries) {
            if(entry.isGiveAway()) {
                continue;
            }
            Long productId = entry.getProductId();
            CommerceEntryError entryError = validateProductInOrder(productId, companyId, productAvailableStock, totalBuyQuantityMap, false);
            validateSubOrderEntries(companyId, productId, entry, productAvailableStock, totalBuyQuantityMap, entryError);
            if (entryError.isHasError()) {
                commerceCartValidateData.getEntryErrors().put(entry.getId(), entryError);
            }
        }
        if(MapUtils.isNotEmpty(commerceCartValidateData.getEntryErrors())) {
            commerceCartValidateData.setHasError(true);
        }

        if(param.isValidateCoupon()) {
            validateAppliedCouponOfCart(commerceCartValidateData, cartModel);
        }
        return commerceCartValidateData;
    }

    private void validateAppliedCouponOfCart(CommerceCartValidateData commerceCartValidateData, CartModel cartModel) {
        Set<OrderHasCouponCodeModel> orderHasCouponCodeModels = cartModel.getOrderHasCouponCodeModels();
        if(CollectionUtils.isEmpty(orderHasCouponCodeModels)) {
            return;
        }
        ValidCouponCodeData validatedCouponCode = couponService.getValidatedCouponCode(cartModel);
        if(!validatedCouponCode.isValid()) {
            commerceCartValidateData.setHasError(true);
            Optional<CouponCodeData> overRedemptionQtyOptional = validatedCouponCode.getCouponData().stream().filter(c -> c.isOverTotalRedemption())
                    .findFirst();
            if(overRedemptionQtyOptional.isPresent()) {
                commerceCartValidateData.getOrderErrorCodes().add(ErrorCodes.COUPON_OVER_MAX_REDEMPTION_QUANTITY.code());
                return;
            }

            Optional<CouponCodeData> notValidAppliedCouponOptional = validatedCouponCode.getCouponData().stream().filter(c -> !c.isValid()).findFirst();
            if(notValidAppliedCouponOptional.isPresent()) {
                commerceCartValidateData.getOrderErrorCodes().add(ErrorCodes.COUPON_NOT_APPLIED.code());
            }
        }

    }

    private void validateSubOrderEntries(Long companyId, Long productId, AbstractOrderEntryModel entry,
                                         Map<Long, Integer> productAvailableStock, Map<Long, Long> totalBuyQuantityMap,
                                         CommerceEntryError entryError) {
        Set<SubOrderEntryModel> subOrderEntries = entry.getSubOrderEntries();
        if (CollectionUtils.isEmpty(subOrderEntries)) {
            return;
        }
        ComboData comboInfo = productService.getCombo(productId, companyId);
        if(!entry.getComboType().equalsIgnoreCase(comboInfo.getComboType())) {
            entryError.setHasError(true);
            entryError.setErrorCode(ErrorCodes.OFF_SITE_PRODUCT.code());
            return;
        }

        int totalItemInCombo = comboInfo.getTotalItemQuantity() * entry.getQuantity().intValue();
        int totalItemSubEntry = 0;
        for (SubOrderEntryModel subEntry : subOrderEntries) {
            totalItemSubEntry += subEntry.getQuantity();
            CommerceEntryError subEntryError = validateProductInOrder(subEntry.getProductId(), companyId, productAvailableStock, totalBuyQuantityMap, true);
            if (subEntryError.isHasError()) {
                entryError.setHasError(true);
                entryError.getSubEntryErrors().put(subEntry.getId(), subEntryError);
                continue;
            }

            try {
                productService.validateProductInCombo(entry.getProductId(), companyId, subEntry.getProductId().toString());
            } catch (ServiceException e) {
                subEntryError.setHasError(true);
                subEntryError.setErrorCode(e.getCode());
                entryError.setHasError(true);
                entryError.getSubEntryErrors().put(subEntry.getId(), subEntryError);
            }
        }

        if (totalItemInCombo != totalItemSubEntry && StringUtils.isBlank(entryError.getErrorCode())) {
            entryError.setHasError(true);
            entryError.setErrorCode(ErrorCodes.INVALID_TOTAL_ITEM_IN_COMBO.code());
        }
    }

    @Override
    @Transactional
    public CartModel changeProductInCombo(StoreFrontSubOrderEntryRequest subOrderEntryRequest) {
        CartModel cartModel = this.getStorefrontCart(subOrderEntryRequest.getOrderCode(), subOrderEntryRequest.getCompanyId());
        CartEntryModel cartEntry = cartService.findEntryBy(subOrderEntryRequest.getEntryId(), cartModel);
        SubOrderEntryModel subEntry = cartEntry.getSubOrderEntries().stream()
                .filter(soe -> soe.getId().equals(subOrderEntryRequest.getSubEntryId()))
                .findFirst().orElse(null);
        if (subEntry == null) {
            return cartModel;
        }

        Long newProductId = subOrderEntryRequest.getProductId();
        if (subEntry.getProductId().equals(newProductId)) {
            return cartModel;
        }

        PriceData priceData = productService.getPriceOfProduct(newProductId, subEntry.getQuantity());
        if (priceData == null || priceData.getPrice() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_PRODUCT_PRICE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        ComboData combo = productService.getCombo(cartEntry.getProductId(), cartModel.getCompanyId());
        if (ComboType.ONE_GROUP.toString().equalsIgnoreCase(combo.getComboType()) && combo.isDuplicateSaleProduct()) {
            SubOrderEntryModel mergedSubEntry = cartEntry.getSubOrderEntries().stream()
                    .filter(soe -> soe.getProductId().equals(newProductId))
                    .findFirst().orElse(null);
            if (mergedSubEntry != null) {
                mergedSubEntry.setQuantity(mergedSubEntry.getQuantity() + subEntry.getQuantity());
                cartEntry.getSubOrderEntries().remove(subEntry);
            }
        }
        subEntry.setProductId(newProductId);
        subEntry.setOriginPrice(priceData.getPrice());
        cartModel.setCalculated(false);
        cartService.saveEntry(cartEntry);
        CommerceAbstractOrderParameter param = new CommerceAbstractOrderParameter();
        param.setOrder(cartModel);
        commerceCartCalculationStrategy.recalculateCart(param);
        return cartModel;
    }

    @Override
    @Transactional
    public AbstractOrderModel changeOrderSource(AbstractOrderModel abstractOrderModel, Long orderSourceId) {
        CommerceAbstractOrderParameter param = new CommerceAbstractOrderParameter();
        param.setOrder(abstractOrderModel);
        if(orderSourceId == null) {
            abstractOrderModel.setOrderSourceModel(null);
            commerceCartCalculationStrategy.recalculateCart(param);
            return abstractOrderModel;
        }
        OrderSourceModel orderSource = this.orderSourceService.findByIdAndCompanyId(orderSourceId, abstractOrderModel.getCompanyId());
        if(orderSource == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_SOURCE_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        abstractOrderModel.setOrderSourceModel(orderSource);
        commerceCartCalculationStrategy.recalculateCart(param);

        if (abstractOrderModel instanceof OrderModel) {
            OrderModel order = (OrderModel) abstractOrderModel;
            loyaltyService.updateRewardRedeemForOrder(order);
            modelService.save(order);
            commercePlaceOrderStrategy.updatePriceAndDiscountBillOf(order);

            applicationEventPublisher.publishEvent(new OrderEvent(order));
        }

        return abstractOrderModel;
    }

    private Map<Long, Long> getTotalProductQuantity(CartModel cartModel) {
        Map<Long, Long> quantityMap = new HashMap<>();
        cartModel.getEntries().forEach(e -> {
            Long qty = quantityMap.get(e.getProductId());
            long sellQty = com.vctek.util.CommonUtils.readValue(qty) + com.vctek.util.CommonUtils.readValue(e.getQuantity());
            quantityMap.put(e.getProductId(), sellQty);
            e.getSubOrderEntries().forEach(soe -> {
                Long soeQty = quantityMap.get(soe.getProductId());
                long totalSellQty = com.vctek.util.CommonUtils.readValue(soeQty) +
                        com.vctek.util.CommonUtils.readValue(soe.getQuantity());
                quantityMap.put(soe.getProductId(), totalSellQty);
            });
        });
        return quantityMap;
    }

    private CommerceEntryError validateProductInOrder(Long productId, Long companyId, Map<Long, Integer> productAvailableStock,
                                                      Map<Long, Long> totalBuyQuantityMap, boolean ignoreValidateOffsite) {
        CommerceEntryError commerceEntryError = new CommerceEntryError();
        if (!ignoreValidateOffsite) {
            boolean onsite = productService.isOnsite(productId, companyId);
            if(!onsite) {
                commerceEntryError.setHasError(true);
                commerceEntryError.setErrorCode(ErrorCodes.OFF_SITE_PRODUCT.code());
                return commerceEntryError;
            }
        }

        Integer availableStock = productAvailableStock.get(productId);
        if (availableStock == null || availableStock <= 0) {
            commerceEntryError.setHasError(true);
            commerceEntryError.setErrorCode(ErrorCodes.PRODUCT_OUT_OF_STOCK.code());
            return commerceEntryError;
        }

        Long totalSell = totalBuyQuantityMap.get(productId);
        if (totalSell > availableStock) {
            commerceEntryError.setHasError(true);
            commerceEntryError.setErrorCode(ErrorCodes.NOT_ENOUGH_STOCK.code());
            commerceEntryError.setAvailableStock(availableStock);
        }
        return commerceEntryError;
    }

    private List<Long> populateProductIdsForCheckingStock(CartModel cartModel) {
        Set<Long> productIds = new HashSet<>();
        cartModel.getEntries().forEach(e -> {
            productIds.add(e.getProductId());
            e.getSubOrderEntries().forEach(soe -> productIds.add(soe.getProductId()));
        });
        return productIds.stream().collect(Collectors.toList());
    }

    private boolean shouldUpdatePrice(Double oldPrice, Double updatePrice) {
        return updatePrice != null && !updatePrice.equals(oldPrice);
    }

    private List<PriceRequest> populateNormalPriceRequestList(List<AbstractOrderEntryModel> entries) {
        List<PriceRequest> priceRequests = new ArrayList<>();
        Set<Long> productIds = new HashSet<>();
        entries.forEach(e -> {
            boolean comboEntry = cartService.isComboEntry(e);
            if(comboEntry) {
                e.getSubOrderEntries().forEach(soe -> {
                    Long productId = soe.getProductId();
                    PriceRequest pr = new PriceRequest();
                    pr.setProductId(productId);
                    if (!productIds.contains(e.getProductId())) {
                        priceRequests.add(pr);
                    }
                    productIds.add(productId);
                });
            } else {
                PriceRequest priceRequest = new PriceRequest();
                priceRequest.setProductId(e.getProductId());
                if (!productIds.contains(e.getProductId())) {
                    priceRequests.add(priceRequest);
                }
                productIds.add(e.getProductId());
            }
        });
        return priceRequests;
    }

    @Autowired
    public void setCommerceRemoveEntriesStrategy(CommerceRemoveEntriesStrategy commerceRemoveEntriesStrategy) {
        this.commerceRemoveEntriesStrategy = commerceRemoveEntriesStrategy;
    }

    @Autowired
    public void setCommerceUpdateCartEntryStrategy(CommerceUpdateCartEntryStrategy commerceUpdateCartEntryStrategy) {
        this.commerceUpdateCartEntryStrategy = commerceUpdateCartEntryStrategy;
    }

    @Autowired
    public void setCommerceUpdateCartStrategy(CommerceUpdateCartStrategy commerceUpdateCartStrategy) {
        this.commerceUpdateCartStrategy = commerceUpdateCartStrategy;
    }

    @Autowired
    public void setCommercePlaceOrderStrategy(CommercePlaceOrderStrategy commercePlaceOrderStrategy) {
        this.commercePlaceOrderStrategy = commercePlaceOrderStrategy;
    }

    @Autowired
    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    @Autowired
    public void setToppingOptionService(ToppingOptionService toppingOptionService) {
        this.toppingOptionService = toppingOptionService;
    }

    @Autowired
    public void setToppingItemService(ToppingItemService toppingItemService) {
        this.toppingItemService = toppingItemService;
    }

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Autowired
    public void setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Autowired
    public void setBillService(BillService billService) {
        this.billService = billService;
    }

    @Autowired
    public void setLoyaltyService(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    @Autowired
    public void setCalculationService(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @Autowired
    public void setInvoiceService(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    @Autowired
    public void setOrderSettingDiscountFacade(OrderSettingDiscountFacade orderSettingDiscountFacade) {
        this.orderSettingDiscountFacade = orderSettingDiscountFacade;
    }

    @Autowired
    public void setOrderSettingFacade(OrderSettingFacade orderSettingFacade) {
        this.orderSettingFacade = orderSettingFacade;
    }

    @Autowired
    public void setLogisticService(LogisticService logisticService) {
        this.logisticService = logisticService;
    }

    @Autowired
    public void setUpdateProductInventoryProducer(UpdateProductInventoryProducer updateProductInventoryProducer) {
        this.updateProductInventoryProducer = updateProductInventoryProducer;
    }

    @Autowired
    public void setOrderStorefrontSetupService(OrderStorefrontSetupService orderStorefrontSetupService) {
        this.orderStorefrontSetupService = orderStorefrontSetupService;
    }

    @Autowired
    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setCommerceCartMergingStrategy(CommerceCartMergingStrategy commerceCartMergingStrategy) {
        this.commerceCartMergingStrategy = commerceCartMergingStrategy;
    }

    @Autowired
    public void setEntryMergeStrategy(EntryMergeStrategy entryMergeStrategy) {
        this.entryMergeStrategy = entryMergeStrategy;
    }

    @Autowired
    public void setOrderSourceService(OrderSourceService orderSourceService) {
        this.orderSourceService = orderSourceService;
    }

    @Autowired
    public void setCouponService(CouponService couponService) {
        this.couponService = couponService;
    }
}
