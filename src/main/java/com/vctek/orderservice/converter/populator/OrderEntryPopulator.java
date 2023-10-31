package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.OrderEntryData;
import com.vctek.orderservice.dto.SubOrderEntryData;
import com.vctek.orderservice.dto.ToppingOptionData;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.CartEntryModel;
import com.vctek.orderservice.model.SubOrderEntryModel;
import com.vctek.orderservice.model.ToppingOptionModel;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.ProductService;
import com.vctek.redis.ProductData;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class OrderEntryPopulator implements Populator<AbstractOrderEntryModel, OrderEntryData> {
    private CalculationService calculationService;
    private ProductService productService;
    private Converter<ToppingOptionModel, ToppingOptionData> toppingOptionDataConverter;

    public OrderEntryPopulator(CalculationService calculationService, ProductService productService) {
        this.calculationService = calculationService;
        this.productService = productService;
    }

    @Override
    public void populate(AbstractOrderEntryModel source, OrderEntryData target) {
        target.setId(source.getId());
        target.setEntryNumber(source.getEntryNumber());
        target.setPrice(source.getBasePrice());
        target.setRecommendedRetailPrice(source.getRecommendedRetailPrice());
        target.setDiscount(source.getDiscount());
        target.setDiscountType(source.getDiscountType());
        target.setProductId(source.getProductId());
        target.setQuantity(source.getQuantity());
        target.setTotalPrice(source.getTotalPrice());
        target.setFixedDiscount(source.getFixedDiscount());
        target.setTotalPriceWithoutDiscount(source.getBasePrice() * source.getQuantity());
        target.setFinalPrice(source.getFinalPrice());
        target.setWeight(source.getWeight());
        target.setTotalDiscount(source.getTotalDiscount());
        target.setDiscountOrderToItem(source.getDiscountOrderToItem());
        target.setFinalDiscount(calculationService.calculateFinalDiscountOfEntry(source));
        target.setPreOrder(source.isPreOrder());
        target.setHolding(source.isHolding());
        target.setHoldingStock(source.getHoldingStock());
        target.setComboType(source.getComboType());
        target.setGiveAway(source.isGiveAway());
        target.setFixedPrice(source.isFixedPrice());
        target.setSaleOff(source.isSaleOff());
        target.setRewardAmount(source.getRewardAmount());
        target.setReturnQuantity(source.getReturnQuantity());
        target.setOriginBasePrice(source.getOriginBasePrice());
        target.setVat(source.getVat());
        target.setVatType(source.getVatType());
        target.setDiscountValues(source.getDiscountValues());
        populateSubOrderEntry(target, source);
        populateOrderEntryTopping(target, source);
    }

    private void populateOrderEntryTopping(OrderEntryData target, AbstractOrderEntryModel source) {
        Set<ToppingOptionModel> toppingOptionModels = source.getToppingOptionModels();
        if (CollectionUtils.isNotEmpty(toppingOptionModels)) {
            target.setToppingOptions(toppingOptionDataConverter.convertAll(toppingOptionModels));
        }
    }

    private void populateSubOrderEntry(OrderEntryData target, AbstractOrderEntryModel source) {
        List<SubOrderEntryData> subOrderEntry = new ArrayList<>();
        for (SubOrderEntryModel subOrderEntryModel : source.getSubOrderEntries()) {
            SubOrderEntryData subOrderEntryData = new SubOrderEntryData();
            subOrderEntryData.setId(subOrderEntryModel.getId());
            subOrderEntryData.setProductId(subOrderEntryModel.getProductId());
            ProductData productData = productService.getBasicProductDetail(subOrderEntryModel.getProductId());
            subOrderEntryData.setProductName(productData.getName());
            subOrderEntryData.setProductSku(productData.getSku());
            subOrderEntryData.setDiscountValue(subOrderEntryModel.getDiscountValue());
            subOrderEntryData.setFinalPrice(subOrderEntryModel.getFinalPrice());
            subOrderEntryData.setOriginPrice(subOrderEntryModel.getOriginPrice());
            subOrderEntryData.setQuantity(subOrderEntryModel.getQuantity());
            subOrderEntryData.setTotalPrice(subOrderEntryModel.getTotalPrice());
            subOrderEntryData.setPrice(subOrderEntryModel.getPrice());
            subOrderEntryData.setRewardAmount(subOrderEntryModel.getRewardAmount());
            subOrderEntryData.setComboGroupNumber(subOrderEntryModel.getComboGroupNumber());
            if(source instanceof CartEntryModel) {
                subOrderEntryData.setVat(productData.getVat());
                subOrderEntryData.setVatType(productData.getVatType());
            } else {
                subOrderEntryData.setVat(subOrderEntryModel.getVat());
                subOrderEntryData.setVatType(subOrderEntryModel.getVatType());
            }
            subOrderEntry.add(subOrderEntryData);
        }
        target.setSubOrderEntries(subOrderEntry);
    }

    @Autowired
    public void setToppingOptionDataConverter(Converter<ToppingOptionModel, ToppingOptionData> toppingOptionDataConverter) {
        this.toppingOptionDataConverter = toppingOptionDataConverter;
    }
}
