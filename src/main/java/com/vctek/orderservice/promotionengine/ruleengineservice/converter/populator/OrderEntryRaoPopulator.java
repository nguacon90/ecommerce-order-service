package com.vctek.orderservice.promotionengine.ruleengineservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CategoryRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.OrderEntryRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.ProductRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.SupplierRAO;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Component
public class OrderEntryRaoPopulator implements Populator<AbstractOrderEntryModel, OrderEntryRAO> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderEntryRaoPopulator.class);
    private ProductSearchService productSearchService;

    @Override
    public void populate(AbstractOrderEntryModel source, OrderEntryRAO target) {
        target.setId(source.getId());
        target.setFixedDiscount(source.getFixedDiscount() == null ? BigDecimal.ZERO : BigDecimal.valueOf(source.getFixedDiscount()));
        if (source.getProductId() != null) {
            ProductRAO productRAO = this.populateProductRAO(source.getProductId());
            target.setProduct(productRAO);
        }

        if (Objects.nonNull(source.getQuantity())) {
            target.setQuantity(source.getQuantity().intValue());
        }

        Double basePrice = source.getBasePrice();
        if (Objects.nonNull(basePrice)) {
            target.setBasePrice(BigDecimal.valueOf(basePrice));
            AbstractOrderModel order = source.getOrder();
            if (Objects.nonNull(order) && Objects.nonNull(order.getCurrencyCode())) {
                target.setCurrencyIsoCode(order.getCurrencyCode());
            } else {
                LOGGER.warn("Order is NULL or the order currency is not set correctly");
            }
        }

        if (Objects.nonNull(source.getEntryNumber())) {
            target.setEntryNumber(source.getEntryNumber());
        }
    }


    private ProductRAO populateProductRAO(Long productId) {
        ProductRAO productRAO = new ProductRAO();
        productRAO.setId(productId);
        ProductSearchModel model = productSearchService.findById(productId);
        if(model != null) {
            Set<CategoryRAO> categoryRAOSet = getCategoryOf(model);
            productRAO.setCategories(categoryRAOSet);
            SupplierRAO supplierRAO = getSupplierOf(model);
            productRAO.setSupplier(supplierRAO);
        }
        return productRAO;
    }

    private SupplierRAO getSupplierOf(ProductSearchModel model) {
        SupplierRAO supplierRAO = new SupplierRAO();
        supplierRAO.setSupplierId(model.getSupplierId());
        return supplierRAO;
    }

    private Set<CategoryRAO> getCategoryOf(ProductSearchModel model) {
        Set<CategoryRAO> categoryRAOSet = new HashSet<>();
        if(CollectionUtils.isNotEmpty(model.getFullCategoryIds())) {
            model.getFullCategoryIds().forEach(cId -> {
                CategoryRAO categoryRAO = new CategoryRAO();
                categoryRAO.setCode(cId);
                categoryRAOSet.add(categoryRAO);
            });
        }
        return categoryRAOSet;
    }

    @Autowired
    public void setProductSearchService(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }
}
