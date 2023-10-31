package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.OrderData;
import com.vctek.orderservice.dto.ReturnOrderData;
import com.vctek.orderservice.dto.ToppingItemData;
import com.vctek.orderservice.dto.ToppingOptionData;
import com.vctek.orderservice.feignclient.dto.BillDetailData;
import com.vctek.orderservice.feignclient.dto.ReturnOrderBillData;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.repository.OrderEntryRepository;
import com.vctek.orderservice.service.BillService;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.util.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("returnOrderDetailPopulator")
public class ReturnOrderDetailPopulator implements Populator<ReturnOrderModel, ReturnOrderData> {
    private Converter<OrderModel, OrderData> orderDataConverter;
    private BillService billService;
    private OrderEntryRepository orderEntryRepository;
    private CalculationService calculationService;

    @Override
    public void populate(ReturnOrderModel returnOrderModel, ReturnOrderData returnOrderData) {
        OrderModel exchangeOrder = returnOrderModel.getExchangeOrder();
        if (exchangeOrder != null) {
            returnOrderData.setExchangeOrder(orderDataConverter.convert(exchangeOrder));
        }

        ReturnOrderBillData returnOrderBillData = billService.getReturnOrderBill(returnOrderModel.getBillId(),
                returnOrderModel.getCompanyId(), returnOrderModel.getId());
        OrderModel originOrder = returnOrderModel.getOriginOrder();
        populateToppingOfComboEntries(originOrder, returnOrderBillData);
        returnOrderData.setBillData(returnOrderBillData);

        populateOriginQuantity(returnOrderModel, returnOrderBillData);

        List<BillDetailData> entries = returnOrderBillData.getEntries();
        populateOriginDiscountOf(returnOrderModel.getOriginOrder(), entries);
    }

    private void populateOriginDiscountOf(OrderModel originOrder, List<BillDetailData> entries) {
        for(BillDetailData data : entries) {
            Long orderEntryId = data.getOrderEntryId();
            OrderEntryModel entryModel = orderEntryRepository.findByOrderAndId(originOrder, orderEntryId);
            if(entryModel != null) {
                double finalDiscount = calculationService.calculateFinalDiscountOfEntry(entryModel);
                double originFinalDiscount = Math.round(finalDiscount / entryModel.getQuantity() * data.getQuantity());
                data.setOriginFinalDiscount(originFinalDiscount);
            }
        }
    }

    private void populateToppingOfComboEntries(OrderModel originOrder, ReturnOrderBillData returnOrderBillData) {
        List<BillDetailData> newBillDetailList = new ArrayList<>();

        List<ToppingOptionModel> toppingOptionModels = originOrder.getEntries().stream()
                .flatMap(option -> option.getToppingOptionModels().stream())
                .collect(Collectors.toList());
        Map<Long, ToppingOptionModel> originOptionMap = toppingOptionModels.stream().collect(
                Collectors.toMap(ToppingOptionModel::getId, option -> option));

        Map<Long, List<BillDetailData>> detailDataMap = returnOrderBillData.getEntries().stream()
                .filter(data -> data.getOrderEntryId() != null)
                .collect(Collectors.groupingBy(BillDetailData::getOrderEntryId, Collectors.toList()));

        detailDataMap.forEach((entryId, oldBillDetailList) ->
                populateEntry(newBillDetailList, oldBillDetailList, originOptionMap));
        returnOrderBillData.setEntries(newBillDetailList);
    }

    private void populateEntry(List<BillDetailData> newBillDetailList, List<BillDetailData> oldBillDetailList, Map<Long, ToppingOptionModel> originOptionMap) {

        List<BillDetailData> toppingEntries = oldBillDetailList.stream().filter(topping -> topping.getToppingOptionId() != null).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(toppingEntries)) {
            newBillDetailList.add(populateToppingEntries(oldBillDetailList, toppingEntries, originOptionMap));
            return;
        }

        List<BillDetailData> comboEntries = oldBillDetailList.stream().filter(bd -> bd.getComboId() != null).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(comboEntries)) {
            newBillDetailList.add( populateComboEntry(comboEntries));
            return;
        }

        newBillDetailList.addAll(oldBillDetailList);
    }

    private BillDetailData populateComboEntry(List<BillDetailData> oldBillDetailList) {
        BillDetailData combo = new BillDetailData();
        combo.setProductId(oldBillDetailList.get(0).getComboId());
        combo.setQuantity(oldBillDetailList.get(0).getComboQuantity());
        combo.setOrderEntryId(oldBillDetailList.get(0).getOrderEntryId());
        double comboPrice = 0;
        double finalDiscount = 0;
        List<BillDetailData> subOrderEntries = new ArrayList<>();
        BillDetailData comboEntry;
        for (BillDetailData billDetailData : oldBillDetailList) {
            comboEntry = new BillDetailData();
            comboEntry.setId(billDetailData.getId());
            comboEntry.setProductId(billDetailData.getProductId());
            comboEntry.setPrice(billDetailData.getPrice());
            comboEntry.setQuantity(billDetailData.getQuantity());
            comboEntry.setDiscount(billDetailData.getDiscount());
            comboEntry.setDiscountValue(billDetailData.getDiscountValue());
            comboEntry.setFinalPrice(billDetailData.getFinalPrice());
            comboEntry.setFinalPrice(billDetailData.getTotalPrice());
            comboEntry.setComboId(billDetailData.getComboId());
            comboEntry.setSubOrderEntryId(billDetailData.getSubOrderEntryId());
            comboEntry.setOrderEntryId(billDetailData.getOrderEntryId());

            comboPrice += CommonUtils.readValue(billDetailData.getPrice()) * ((double) comboEntry.getQuantity() / combo.getQuantity());
            finalDiscount += CommonUtils.readValue(billDetailData.getDiscount());
            subOrderEntries.add(comboEntry);
        }
        combo.setPrice(comboPrice);
        combo.setDiscount(finalDiscount);
        combo.setSubOrderEntries(subOrderEntries);
        return combo;
    }

    private BillDetailData populateToppingEntries(List<BillDetailData> oldBillDetailList, List<BillDetailData> toppingEntries, Map<Long, ToppingOptionModel> originOptionMap) {
        BillDetailData entryData = oldBillDetailList.stream().filter(topping -> topping.getToppingOptionId() == null)
                .findFirst().orElse(new BillDetailData());
        Map<Long, ToppingOptionData> optionDataMap = new HashMap<>();
        for (BillDetailData billDetailData : toppingEntries) {
            ToppingOptionData optionData = optionDataMap.get(billDetailData.getToppingOptionId());
            int optionQuantity = 1;
            if (optionData == null) {
                ToppingOptionModel originOptionModel = originOptionMap.get(billDetailData.getToppingOptionId());
                optionQuantity = originOptionModel.getQuantity();
                optionData = new ToppingOptionData();
                optionData.setIce(originOptionModel.getIce());
                optionData.setSugar(originOptionModel.getSugar());
                optionData.setQuantity(optionQuantity);
                optionDataMap.put(billDetailData.getToppingOptionId(), optionData);
            }
            ToppingItemData itemData = new ToppingItemData();
            itemData.setProductId(billDetailData.getProductId());
            itemData.setId(billDetailData.getId());
            itemData.setDiscountOrderToItem(CommonUtils.readValue(billDetailData.getDiscount()));
            itemData.setPrice(billDetailData.getPrice());
            itemData.setTotalQuantity(billDetailData.getQuantity());
            itemData.setQuantity(billDetailData.getQuantity() / optionQuantity);
            itemData.setTotalPrice(billDetailData.getTotalPrice());
            optionData.getToppingItems().add(itemData);

        }
        entryData.setToppingOptions(new ArrayList<>(optionDataMap.values()));
        return entryData;
    }

    protected void populateOriginQuantity(ReturnOrderModel returnOrderModel, ReturnOrderBillData returnOrderBillData) {
        List<BillDetailData> returnEntries = returnOrderBillData.getEntries();
        if (CollectionUtils.isNotEmpty(returnEntries)) {
            OrderModel originOrder = returnOrderModel.getOriginOrder();
            List<AbstractOrderEntryModel> entries = originOrder.getEntries();
            Map<Long, AbstractOrderEntryModel> originEntriesMap = entries.stream()
                    .collect(Collectors.toMap(AbstractOrderEntryModel::getId, i -> i));
            for (BillDetailData detailData : returnEntries) {
                AbstractOrderEntryModel originEntry = originEntriesMap.get(detailData.getOrderEntryId());
                detailData.setGiveAway(originEntry.isGiveAway());
                detailData.setOriginQuantity(originEntry.getQuantity() != null ? originEntry.getQuantity().intValue() : 0);
            }
        }
    }

    @Autowired
    public void setOrderDataConverter(Converter<OrderModel, OrderData> orderDataConverter) {
        this.orderDataConverter = orderDataConverter;
    }

    @Autowired
    public void setBillService(BillService billService) {
        this.billService = billService;
    }

    @Autowired
    public void setOrderEntryRepository(OrderEntryRepository orderEntryRepository) {
        this.orderEntryRepository = orderEntryRepository;
    }

    @Autowired
    public void setCalculationService(CalculationService calculationService) {
        this.calculationService = calculationService;
    }
}