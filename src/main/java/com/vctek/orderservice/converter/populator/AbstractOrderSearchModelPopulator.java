package com.vctek.orderservice.converter.populator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vctek.orderservice.dto.OrderImageData;
import com.vctek.orderservice.dto.ProductImageData;
import com.vctek.orderservice.elasticsearch.model.OrderEntryData;
import com.vctek.orderservice.elasticsearch.model.OrderHistory;
import com.vctek.orderservice.elasticsearch.model.OrderSearchModel;
import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.promotionengine.util.CommonUtils;
import com.vctek.orderservice.repository.OrderEntryRepository;
import com.vctek.orderservice.repository.SubOrderEntryRepository;
import com.vctek.orderservice.repository.ToppingItemRepository;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.OrderHistoryService;
import com.vctek.orderservice.util.CurrencyUtils;
import com.vctek.util.VNCharacterUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class AbstractOrderSearchModelPopulator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOrderSearchModelPopulator.class);
    protected ProductSearchService productSearchService;
    protected OrderEntryRepository orderEntryRepository;
    protected CalculationService calculationService;
    protected ToppingItemRepository toppingItemRepository;
    protected SubOrderEntryRepository subOrderEntryRepository;
    protected OrderHistoryService orderHistoryService;
    protected ObjectMapper objectMapper;

    protected void populateProduct(OrderSearchModel target, OrderModel source) {
        List<OrderEntryModel> entryModels = orderEntryRepository.findAllByOrderCode(source.getCode());
        List<OrderEntryData> entries = new ArrayList<>();
        OrderEntryData entryData;
        boolean hasSaleOff = false;
        for (OrderEntryModel orderEntryModel : entryModels) {
            entryData = new OrderEntryData();
            populateProductInfo(orderEntryModel.getProductId(), entryData, source.getCode(), source.getCompanyId());
            entryData.setQuantity(orderEntryModel.getQuantity());
            entryData.setPrice(orderEntryModel.getBasePrice());
            entryData.setTotalPrice(orderEntryModel.getTotalPrice());
            entryData.setGiveAway(orderEntryModel.isGiveAway());
            entryData.setFinalDiscount(calculationService.calculateFinalDiscountOfEntry(orderEntryModel));
            entryData.setReturnQuantity(orderEntryModel.getReturnQuantity());
            entryData.setHolding(orderEntryModel.isHolding());
            entryData.setPreOrder(orderEntryModel.isPreOrder());
            entryData.setSaleOff(orderEntryModel.isSaleOff());
            entryData.setVat(orderEntryModel.getVat());
            entryData.setVatType(orderEntryModel.getVatType());
            populateProductOfCombo(orderEntryModel, entryData, source.getCode(), source.getCompanyId());
            if (orderEntryModel.isSaleOff()) {
                hasSaleOff = true;
            }
            entries.add(entryData);
        }
        target.setHasSaleOff(hasSaleOff);

        List<OrderEntryData> toppingEntries = getToppingEntries(source);
        if (CollectionUtils.isNotEmpty(toppingEntries)) {
            entries.addAll(toppingEntries);
        }

        target.setOrderEntries(entries);
    }

    protected void populateProductOfCombo(OrderEntryModel orderEntryModel, OrderEntryData entryData, final String orderCode, Long companyId) {
        List<OrderEntryData> dataList = new ArrayList<>();
        List<SubOrderEntryModel> subOrderEntries = subOrderEntryRepository.findAllByOrderEntry(orderEntryModel);
        if (CollectionUtils.isNotEmpty(subOrderEntries)) {
            subOrderEntries.forEach(subOrderEntryModel -> {
                OrderEntryData orderEntryData = new OrderEntryData();
                populateProductInfo(subOrderEntryModel.getProductId(), orderEntryData, orderCode, companyId);
                orderEntryData.setQuantity((long) subOrderEntryModel.getQuantity());
                orderEntryData.setPrice(subOrderEntryModel.getPrice());
                orderEntryData.setTotalPrice(subOrderEntryModel.getTotalPrice());
                orderEntryData.setFinalDiscount(subOrderEntryModel.getDiscountValue());
                orderEntryData.setVat(subOrderEntryModel.getVat());
                orderEntryData.setVatType(subOrderEntryModel.getVatType());
                dataList.add(orderEntryData);
            });
        }

        entryData.setSubOrderEntries(dataList);
    }

    protected OrderEntryData populateProductInfo(Long productId, OrderEntryData entryData, String orderCode, Long companyId) {
        entryData.setId(productId);
        try {
            ProductSearchModel productDetailData = productSearchService.findByIdAndCompanyId(productId, companyId);
            if (StringUtils.isNotBlank(productDetailData.getDefaultImageUrl())) {
                entryData.setImage(productDetailData.getDefaultImageUrl());
            }
            entryData.setBarcode(productDetailData.getBarcode());
            entryData.setName(productDetailData.getName());
            entryData.setSupplierProductName(productDetailData.getSupplierProductName());
            String name = VNCharacterUtils.removeAccent(entryData.getName());
            entryData.setStringName(name);
            entryData.setSku(productDetailData.getSku());
            entryData.setdType(productDetailData.getDtype());
        } catch (RuntimeException e) {
            LOGGER.error("POPULATE PRODUCT ERROR: productId: {}, orderCode: {} ", productId, orderCode);
            LOGGER.error(e.getMessage(), e);
        }
        return entryData;
    }

    protected List<OrderEntryData> getToppingEntries(OrderModel orderModel) {
        Set<ToppingItemModel> toppingItemModels = toppingItemRepository.findAllByOrderId(orderModel.getId());
        if (CollectionUtils.isEmpty(toppingItemModels)) {
            return new ArrayList<>();
        }
        Map<Long, OrderEntryData> toppingEntryMap = new HashMap<>();
        for (ToppingItemModel toppingItemModel : toppingItemModels) {
            Long productId = toppingItemModel.getProductId();
            if (productId == null) {
                continue;
            }
            OrderEntryData orderEntryData = toppingEntryMap.get(productId);
            ToppingOptionModel optionModel = toppingItemModel.getToppingOptionModel();
            int optQty = CommonUtils.getIntValue(optionModel.getQuantity());
            double price = CommonUtils.getDoubleValue(toppingItemModel.getBasePrice());
            int itemQty = CommonUtils.getIntValue(toppingItemModel.getQuantity());
            int totalQty = itemQty * optQty;
            double discount = CommonUtils.getDoubleValue(toppingItemModel.getDiscountOrderToItem());
            double totalDiscount = discount + CurrencyUtils.computeValue(toppingItemModel.getDiscount(), toppingItemModel.getDiscountType(), price * totalQty);
            if (orderEntryData == null) {
                orderEntryData = new OrderEntryData();
                populateProductInfo(productId, orderEntryData, orderModel.getCode(), orderModel.getCompanyId());
                orderEntryData.setQuantity((long) totalQty);
                orderEntryData.setPrice(price);
                orderEntryData.setTotalPrice(price * totalQty);
                orderEntryData.setFinalDiscount(totalDiscount);
                orderEntryData.setVat(toppingItemModel.getVat());
                orderEntryData.setVatType(toppingItemModel.getVatType());
                toppingEntryMap.put(productId, orderEntryData);
            } else {
                orderEntryData.setQuantity(orderEntryData.getQuantity() + totalQty);
                orderEntryData.setTotalPrice(orderEntryData.getTotalPrice() + price * totalQty);
                orderEntryData.setFinalDiscount(orderEntryData.getFinalDiscount() + totalDiscount);
            }
        }

        return toppingEntryMap.values().stream().collect(Collectors.toList());
    }

    protected void populateOrderHistory(OrderSearchModel target, OrderModel source) {
        List<OrderHistoryModel> orderHistoryModel = orderHistoryService.findAllByOrderId(source.getId());

        List<OrderHistory> orderHistoryData = new ArrayList<>();
        for (OrderHistoryModel historyModel : orderHistoryModel) {
            OrderHistory orderHistory = new OrderHistory();
            orderHistory.setCurrentStatus(historyModel.getCurrentStatus());
            orderHistory.setPreviousStatus(historyModel.getPreviousStatus());
            orderHistory.setModifiedTimeStatus(historyModel.getModifiedTime());
            orderHistory.setExtraData(historyModel.getExtraData());
            orderHistoryData.add(orderHistory);
            if (orderHistory.getCurrentStatus().equals(source.getOrderStatus())
                    && (target.getModifiedTimeLastStatus() == null
                    || target.getModifiedTimeLastStatus().before(historyModel.getModifiedTime()))) {
                target.setModifiedTimeLastStatus(historyModel.getModifiedTime());
                target.setCancelReason(historyModel.getExtraData());
            }
        }
        target.setOrderHistoryData(orderHistoryData);
    }

    protected void populateImages(OrderModel source, OrderSearchModel target) {
        target.setImages(source.getImages());
        boolean isFinishedProduct = false;
        if (StringUtils.isNotBlank(source.getImages())) {
            try {
                List<OrderImageData> imageData = objectMapper.readValue(source.getImages(), new TypeReference<List<OrderImageData>>() {
                });
                Optional<OrderImageData> finishedProduct = imageData.stream().filter(i -> Boolean.TRUE.equals(i.isFinishedProduct())).findFirst();
                if (finishedProduct.isPresent()) {
                    isFinishedProduct = true;
                }

            } catch (IOException e) {
                LOGGER.error("CANNOT READ ORDER IMAGE: {}", source.getCode());
            }
        }
        target.setFinishedProduct(isFinishedProduct);
    }

    @Autowired
    public void setToppingItemRepository(ToppingItemRepository toppingItemRepository) {
        this.toppingItemRepository = toppingItemRepository;
    }

    @Autowired
    public void setSubOrderEntryRepository(SubOrderEntryRepository subOrderEntryRepository) {
        this.subOrderEntryRepository = subOrderEntryRepository;
    }

    @Autowired
    public void setOrderEntryRepository(OrderEntryRepository orderEntryRepository) {
        this.orderEntryRepository = orderEntryRepository;
    }

    @Autowired
    public void setCalculationService(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @Autowired
    public void setOrderHistoryService(OrderHistoryService orderHistoryService) {
        this.orderHistoryService = orderHistoryService;
    }

    @Autowired
    public void setProductSearchService(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
