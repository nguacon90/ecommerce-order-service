package com.vctek.orderservice.converter.populator.migration;

import com.vctek.converter.Populator;
import com.vctek.migration.dto.*;
import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.service.OrderSourceService;
import com.vctek.orderservice.util.DiscountType;
import com.vctek.orderservice.util.PriceType;
import com.vctek.orderservice.util.ProductDType;
import com.vctek.util.DateUtil;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class SyncOrderPopulator implements Populator<MigrateBillDto, OrderModel> {
    private ProductSearchService productService;
    private OrderSourceService orderSourceService;

    @Override
    public void populate(MigrateBillDto migrateBillDto, OrderModel orderModel) {
        orderModel.setCode(migrateBillDto.getOrderCode());
        orderModel.setTotalPrice(migrateBillDto.getTotalCost());
        orderModel.setWarehouseId(migrateBillDto.getWarehouseId());
        orderModel.setType(migrateBillDto.getOrderType());
        orderModel.setCustomerId(migrateBillDto.getCustomerId());
        orderModel.setCompanyId(migrateBillDto.getCompanyId());
        if (OrderType.ONLINE.toString().equals(orderModel.getType())) {
            orderModel.setOrderStatus(migrateBillDto.getOrderStatus());
            orderModel.setCustomerNote(migrateBillDto.getCustomerNote());
            orderModel.setPriceType(PriceType.RETAIL_PRICE.toString());
        } else {
            orderModel.setOrderStatus(OrderStatus.COMPLETED.code());
            orderModel.setCustomerNote(migrateBillDto.getNote());
            populatePaymentTransaction(migrateBillDto, orderModel);
        }
        orderModel.setFinalPrice(migrateBillDto.getFinalCost());
        orderModel.setNote(migrateBillDto.getNote());
        orderModel.setShippingCompanyId(migrateBillDto.getShippingCompanyId());
        orderModel.setDiscount(migrateBillDto.getDiscount());
        orderModel.setDiscountType(migrateBillDto.getDiscountType());
        orderModel.setOrderRetailCode(migrateBillDto.getOrderRetailCode());
        orderModel.setCustomerSupportNote(migrateBillDto.getCustomerSupportNote());
        orderModel.setTotalDiscount(migrateBillDto.getTotalDiscount());
        orderModel.setDeliveryCost(migrateBillDto.getOrderShipFee());
        orderModel.setExchange(migrateBillDto.isExchange());
        orderModel.setShippingAddressId(migrateBillDto.getShippingAddressId());
        orderModel.setEmployeeId(migrateBillDto.getEmployeeId());
        if (StringUtils.isNotEmpty(migrateBillDto.getDeliveryDate())) {
            orderModel.setDeliveryDate(DateUtil.parseDate(migrateBillDto.getDeliveryDate(), DateUtil.ISO_DATE_TIME_PATTERN));
        }
        OrderSourceModel orderSourceModel = orderSourceService.findByIdAndCompanyId(migrateBillDto.getOrderSourceId(), migrateBillDto.getCompanyId());
        if (orderSourceModel != null) {
            orderModel.setOrderSourceModel(orderSourceModel);
        }
        orderModel.setVat(migrateBillDto.getVat());
        orderModel.setVatDate(migrateBillDto.getVatDate());
        orderModel.setVatNumber(migrateBillDto.getVatNumber());
        orderModel.setVatType(migrateBillDto.getVatType());
        populateEntry(migrateBillDto, orderModel);
    }

    private void populatePaymentTransaction(MigrateBillDto migrateBillDto, OrderModel orderModel) {
        Set<PaymentTransactionModel> transactionModels = new HashSet<>();
        for (PaymentTransactionData data : migrateBillDto.getPayments()) {
            PaymentTransactionModel transaction = new PaymentTransactionModel();
            transaction.setAmount(data.getAmount());
            transaction.setMoneySourceId(data.getMoneySourceId());
            transaction.setPaymentMethodId(data.getPaymentMethodId());
            transaction.setTransactionNumber(data.getTransactionNumber());
            transaction.setMoneySourceType(data.getMoneySourceType());
            transaction.setOrderModel(orderModel);
            transaction.setOrderCode(orderModel.getCode());
            transaction.setWarehouseId(orderModel.getWarehouseId());
            transactionModels.add(transaction);
        }
        orderModel.setPaymentTransactions(transactionModels);

    }

    private void populateEntry(MigrateBillDto migrateBillDto, OrderModel orderModel) {
        List<AbstractOrderEntryModel> orderEntryModels = new ArrayList<>();
        int entryNumber = 0;
        for (MigrateBillDetailDto detailDto : migrateBillDto.getDetailDtos()) {
            ProductSearchModel searchModel = populateSearchModel(detailDto.getProductId(), migrateBillDto.getCompanyId());
            if (searchModel != null) {
                OrderEntryModel orderEntryModel = new OrderEntryModel();
                orderEntryModel.setProductId(searchModel.getId());
                orderEntryModel.setBasePrice(detailDto.getPrice() != null ? detailDto.getPrice() : 0);
                orderEntryModel.setOriginBasePrice(orderEntryModel.getBasePrice());
                orderEntryModel.setQuantity(Long.valueOf(detailDto.getQuantity()));
                orderEntryModel.setTotalPrice(detailDto.getTotalPrice());
                orderEntryModel.setOrder(orderModel);
                orderEntryModel.setWarehouseId(orderModel.getWarehouseId());
                orderEntryModel.setFinalPrice(detailDto.getFinalPrice());
                orderEntryModel.setOrderCode(orderModel.getCode());
                orderEntryModel.setComboType(searchModel.getComboType());
                orderEntryModel.setEntryNumber(entryNumber++);
                orderEntryModel.setDiscount(detailDto.getDiscount());
                orderEntryModel.setDiscountType(detailDto.getDiscountType());
                populateDiscountWithOnlyCombo(migrateBillDto, orderEntryModel, orderModel, searchModel);
                populateHoldingStock(orderEntryModel, detailDto);
                populateSubOrderEntries(orderEntryModel, detailDto, orderModel.getCompanyId());
                populateTopping(orderEntryModel, detailDto.getToppingOptions(), orderModel.getCompanyId());
                orderEntryModels.add(orderEntryModel);
            }
        }
        orderModel.setEntries(orderEntryModels);
    }

    private void populateTopping(OrderEntryModel orderEntryModel, List<SyncToppingOptionData> toppingOptions, Long companyId) {
        if (CollectionUtils.isNotEmpty(toppingOptions)) {
            Set<ToppingOptionModel> toppingOptionModels = new HashSet<>();
            for (SyncToppingOptionData syncToppingOptionData : toppingOptions) {
                ToppingOptionModel toppingOptionModel = new ToppingOptionModel();
                toppingOptionModel.setOrderEntry(orderEntryModel);
                toppingOptionModel.setQuantity(syncToppingOptionData.getQuantity());
                toppingOptionModel.setSugar(syncToppingOptionData.getSugar());
                toppingOptionModel.setIce(syncToppingOptionData.getIce());
                for (SyncToppingItemData syncToppingItemData : syncToppingOptionData.getToppingItems()) {
                    ProductSearchModel searchModel = populateSearchModel(syncToppingItemData.getProductId(), companyId);
                    if (searchModel != null) {
                        ToppingItemModel toppingItemModel = new ToppingItemModel();
                        toppingItemModel.setProductId(searchModel.getId());
                        toppingItemModel.setQuantity(syncToppingItemData.getQuantity());
                        toppingItemModel.setToppingOptionModel(toppingOptionModel);
                        toppingItemModel.setBasePrice(syncToppingItemData.getPrice());
                        toppingOptionModel.getToppingItemModels().add(toppingItemModel);
                    }
                }
                toppingOptionModels.add(toppingOptionModel);
            }
            orderEntryModel.setToppingOptionModels(toppingOptionModels);
        }
    }

    private void populateDiscountWithOnlyCombo(MigrateBillDto migrateBillDto, OrderEntryModel orderEntryModel, OrderModel orderModel, ProductSearchModel searchModel) {
        if (migrateBillDto.getDetailDtos().size() == 1 && ProductDType.COMBO_MODEL.code().equals(searchModel.getDtype())) {
            orderEntryModel.setDiscount(migrateBillDto.getTotalDiscount());
            orderEntryModel.setDiscountType(DiscountType.CASH.toString());
            orderModel.setDiscount(0d);
        }
    }

    private void populateHoldingStock(OrderEntryModel orderEntryModel, MigrateBillDetailDto detailDto) {
        if (detailDto.getHolding() != null && detailDto.getHolding() == 1) {
            orderEntryModel.setHolding(true);
            orderEntryModel.setHoldingStock(Long.valueOf(detailDto.getQuantity()));
        } else {
            orderEntryModel.setPreOrder(detailDto.getPreOrder() != null && detailDto.getPreOrder() == 1 ? true : false);
        }
    }

    private ProductSearchModel populateSearchModel(Long productId, Long companyId) {
        ProductSearchModel searchModel = new ProductSearchModel();
        if (productId == null) {
            throw new IllegalArgumentException("empty productId: ");
        } else {
            searchModel = productService.findByExternalIdAndCompanyId(productId, companyId);
            if (searchModel == null) {
                throw new IllegalArgumentException("invalid productId: " + productId);
            }
        }
        return searchModel;
    }

    private void populateSubOrderEntries(OrderEntryModel entryModel, MigrateBillDetailDto detailDto, Long companyId) {
        if (CollectionUtils.isNotEmpty(detailDto.getSubOrderEntries())) {
            Set<SubOrderEntryModel> subOrderEntryModels = new HashSet<>();
            for (SyncSubOrderEntryData comboProduct : detailDto.getSubOrderEntries()) {
                ProductSearchModel searchModel = populateSearchModel(comboProduct.getProductId(), companyId);
                if (searchModel != null) {
                    SubOrderEntryModel subOrderEntryModel = new SubOrderEntryModel();
                    subOrderEntryModel.setProductId(searchModel.getId());
                    subOrderEntryModel.setOriginPrice(searchModel.getPrices().get(0).getPrice());
                    subOrderEntryModel.setOrderEntry(entryModel);
                    subOrderEntryModel.setQuantity(comboProduct.getQuantity());
                    subOrderEntryModel.setComboGroupNumber(comboProduct.getComboGroupNumber());
                    subOrderEntryModels.add(subOrderEntryModel);
                }
            }
            entryModel.setSubOrderEntries(subOrderEntryModels);
        }
    }

    @Autowired
    public void setProductService(ProductSearchService productService) {
        this.productService = productService;
    }

    @Autowired
    public void setOrderSourceService(OrderSourceService orderSourceService) {
        this.orderSourceService = orderSourceService;
    }
}
