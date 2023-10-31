package com.vctek.orderservice.promotionengine.ruleengineservice.evaluate.impl;

import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.promotionengine.ruleengine.model.eveluation.RuleActionContext;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.AbstractRuleActionRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.CartRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.FreeProductRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.RuleEngineResultRAO;
import com.vctek.orderservice.service.ProductService;
import com.vctek.redis.ProductData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component("ruleFreeGiftRAOAction")
public class RuleFreeGiftRAOAction extends AbstractRuleExecutableSupport {
    private static final Logger LOG = LoggerFactory.getLogger(RuleFreeGiftRAOAction.class);
    private ProductSearchService productSearchService;
    private RuleOrderEntryPercentageDiscountRAOAction ruleOrderEntryPercentageDiscountRAOAction;

    public boolean performActionInternal(RuleActionContext context) {
        Long productId = context.getParameter("product", Long.class);
        Integer quantity = context.getParameter("quantity", Integer.class);
        return this.performAction(context, productId, quantity);
    }

    protected boolean performAction(RuleActionContext context, Long productId, Integer quantity) {
        CartRAO cartRao = context.getCartRao();
        ProductData product = this.findProduct(productId, context);
        if (Objects.nonNull(product)) {
            FreeProductRAO freeProductRAO = this.getRuleEngineCalculationService().addFreeProductsToCart(cartRao, product, quantity);
            int availableFreeProducts = quantity - this.getRuleEngineCalculationService()
                    .getConsumedQuantityForOrderEntry(freeProductRAO.getAddedOrderEntry());
            if (availableFreeProducts > 0) {
                this.setRAOMetaData(context, new AbstractRuleActionRAO[]{freeProductRAO});
                RuleEngineResultRAO result = context.getRuleEngineResultRao();
                result.getActions().add(freeProductRAO);
                context.scheduleForUpdate(new Object[]{cartRao, result});
                context.insertFacts(new Object[]{freeProductRAO});
                return true;
            }
        }

        return false;
    }

    protected ProductData findProduct(Long productId, RuleActionContext context) {
        ProductData product = null;

        try {
            ProductSearchModel productSearchModel = this.productSearchService.findById(productId);
            if(productSearchModel != null) {
                product = new ProductData();
                product.setId(productSearchModel.getId());
                product.setdType(productSearchModel.getDtype());
            }
        } catch (Exception var5) {
            LOG.error("no product found for ID {} in rule {}, cannot apply rule action.",
                    new Object[]{productId, this.getRuleCode(context), var5});
        }

        return product;
    }


    @Autowired
    public void setRuleOrderEntryPercentageDiscountRAOAction(RuleOrderEntryPercentageDiscountRAOAction ruleOrderEntryPercentageDiscountRAOAction) {
        this.ruleOrderEntryPercentageDiscountRAOAction = ruleOrderEntryPercentageDiscountRAOAction;
    }

    @Autowired
    public void setProductSearchService(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }
}
