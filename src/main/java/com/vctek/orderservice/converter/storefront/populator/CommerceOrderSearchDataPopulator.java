package com.vctek.orderservice.converter.storefront.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.request.storefront.CommerceOrderData;
import com.vctek.orderservice.dto.request.storefront.CommerceOrderEntryData;
import com.vctek.orderservice.elasticsearch.model.OrderEntryData;
import com.vctek.orderservice.elasticsearch.model.OrderSearchModel;
import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.util.ProductDType;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CommerceOrderSearchDataPopulator implements Populator<OrderSearchModel, CommerceOrderData> {
    private ProductSearchService productSearchService;

    @Override
    public void populate(OrderSearchModel source, CommerceOrderData target) {
        target.setCode(source.getCode());
        target.setOrderType(source.getOrderType());
        target.setGlobalDiscountValues(source.getGlobalDiscountValues());
        target.setSubTotal(source.getSubTotal());
        target.setTotalDiscount(source.getTotalDiscount());
        target.setCompanyId(source.getCompanyId());
        target.setCustomerName(source.getCustomerName());
        target.setCustomerPhone(source.getCustomerPhone());
        target.setTotalPrice(source.getTotalPrice());
        target.setTotalTax(source.getTotalTax());
        target.setVat(source.getVat());
        target.setVatType(source.getVatType());
        target.setOrderStatus(source.getOrderStatus());
        target.setFinalPrice(source.getFinalPrice());
        target.setDiscount(source.getDiscount());
        target.setDiscountType(source.getDiscountType());
        target.setDeliveryCost(source.getDeliveryCost());
        target.setCompanyShippingFee(source.getCompanyShippingFee());
        target.setFixedDiscount(source.getFixedDiscount());
        target.setShippingCompanyId(source.getShippingCompanyId());
        target.setCustomerNote(source.getCustomerNote());
        target.setProvinceId(source.getProvinceId());
        target.setProvinceName(source.getProvinceName());
        target.setDistrictId(source.getDistrictId());
        target.setDistrictName(source.getDistrictName());
        target.setWardId(source.getWardId());
        target.setWardName(source.getWardName());
        target.setAddress(source.getAddress());
        target.setCreatedTime(source.getCreatedTime());
        target.setDeliveryDate(source.getDeliveryDate());
        target.setCancelReason(source.getCancelReason());
        populateEntries(target, source);
    }

    private void populateEntries(CommerceOrderData target, OrderSearchModel source) {
        List<CommerceOrderEntryData> entries = new ArrayList<>();
        List<Long> variantProductIds = new ArrayList<>();
        if (CollectionUtils.isEmpty(source.getOrderEntries())) return;
        for (OrderEntryData orderEntry : source.getOrderEntries()) {
            if (ProductDType.VARIANT_PRODUCT_MODEL.code().equals(orderEntry.getdType())) {
                variantProductIds.add(orderEntry.getId());
            }
            CommerceOrderEntryData data = new CommerceOrderEntryData();
            data.setId(orderEntry.getId());
            data.setName(orderEntry.getName());
            data.setSku(orderEntry.getSku());
            data.setSupplierProductName(orderEntry.getSupplierProductName());
            data.setBarcode(orderEntry.getBarcode());
            data.setQuantity(orderEntry.getQuantity());
            data.setPrice(orderEntry.getPrice());
            data.setImage(orderEntry.getImage());
            data.setTotalPrice(orderEntry.getTotalPrice());
            data.setGiveAway(orderEntry.isGiveAway());
            data.setFinalDiscount(orderEntry.getFinalDiscount());
            data.setdType(orderEntry.getdType());
            data.setReturnQuantity(orderEntry.getReturnQuantity());
            data.setHolding(orderEntry.isHolding());
            data.setPreOrder(orderEntry.isPreOrder());
            data.setSaleOff(orderEntry.isSaleOff());
            data.setVat(orderEntry.getVat());
            data.setVatType(orderEntry.getVatType());
            data.setSubOrderEntries(orderEntry.getSubOrderEntries());
            entries.add(data);
        }
        target.setOrderEntries(entries);
        populateInfoProduct(target, variantProductIds);
    }

    private void populateInfoProduct(CommerceOrderData target, List<Long> variantProductIds) {
        if (CollectionUtils.isEmpty(variantProductIds)) return;
        List<ProductSearchModel> productSearchModels = productSearchService.findAllByIdIn(variantProductIds);
        if (CollectionUtils.isEmpty(productSearchModels)) return;
        Map<Long, ProductSearchModel> productSearchModelMap = productSearchModels.stream().collect(Collectors.toMap(i -> i.getId(), Function.identity()));
        for (CommerceOrderEntryData entry : target.getOrderEntries()) {
            ProductSearchModel productSearchModel = productSearchModelMap.get(entry.getId());
            if (productSearchModel != null) {
                entry.setParentProductId(productSearchModel.getParentId());
                populateParent(productSearchModel, entry);
            }
        }
    }

    private void populateParent(ProductSearchModel productSearchModel, CommerceOrderEntryData entry) {
        if (productSearchModel.getParentId() == null) return;
        ProductSearchModel parentModel = productSearchService.findById(productSearchModel.getParentId());
        if (parentModel != null) {
            entry.setParentProductName(parentModel.getName());
        }
    }

    @Autowired
    public void setProductSearchService(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }
}
