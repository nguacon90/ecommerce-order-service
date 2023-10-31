package com.vctek.orderservice.promotionengine.promotionservice.strategy.impl;

import com.vctek.orderservice.dto.PriceData;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.ItemModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionResultModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.RuleBasedFixedPriceProductActionModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionActionService;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.AbstractRuleActionRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.FixedPriceProductRAO;
import com.vctek.orderservice.promotionengine.ruleengineservice.rao.OrderEntryRAO;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.ModelService;
import com.vctek.orderservice.service.ProductService;
import com.vctek.orderservice.util.PriceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component("defaultFixedPriceProductActionStrategy")
public class DefaultFixedPriceProductActionStrategy extends AbstractRuleActionStrategy<RuleBasedFixedPriceProductActionModel> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFixedPriceProductActionStrategy.class);
    private ProductService productService;

    public DefaultFixedPriceProductActionStrategy(ModelService modelService,
                                                  PromotionActionService promotionActionService,
                                                  CalculationService calculationService) {
        super(modelService, promotionActionService, calculationService);
    }

    @Override
    public Class<RuleBasedFixedPriceProductActionModel> forClass() {
        return RuleBasedFixedPriceProductActionModel.class;
    }

    @Override
    public List apply(AbstractRuleActionRAO action) {
        if(!(action instanceof FixedPriceProductRAO)) {
            LOGGER.warn("cannot apply {}, action is not of type FixedPriceProductRAO, but {}", this.getClass().getSimpleName(), action);
            return Collections.emptyList();
        }

        FixedPriceProductRAO fixedPriceProductRAO = (FixedPriceProductRAO) action;
        if (!(fixedPriceProductRAO.getAppliedToObject() instanceof OrderEntryRAO)) {
            LOGGER.warn("cannot apply {}, appliedToObject is not of type OrderEntryRAO, but {}", this.getClass().getSimpleName(), action.getAppliedToObject());
            return Collections.emptyList();
        }

        PromotionResultModel promoResult = this.getPromotionActionService().createPromotionResult(action);
        if (promoResult == null) {
            LOGGER.warn("cannot apply {}, promotionResult could not be created.", this.getClass().getSimpleName());
            return Collections.emptyList();
        }

        AbstractOrderEntryModel orderEntry = this.getPromotionActionService().getOrderEntry(action);
        if(orderEntry == null) {
            LOGGER.warn("cannot apply {}, entry not found: {}", this.getClass().getSimpleName(), orderEntry);
            return Collections.emptyList();
        }

        orderEntry.setBasePrice(fixedPriceProductRAO.getFixedPrice().doubleValue());
        orderEntry.setOriginBasePrice(fixedPriceProductRAO.getFixedPrice().doubleValue());
        orderEntry.setFixedPrice(true);

        RuleBasedFixedPriceProductActionModel fixedPriceProductActionModel = this.createPromotionAction(promoResult, action);
        fixedPriceProductActionModel.setProductId(orderEntry.getProductId());
        fixedPriceProductActionModel.setPromotionResult(promoResult);
        fixedPriceProductActionModel.setOrderEntryQuantity(this.getConsumedQuantity(promoResult, orderEntry));
        this.getModelService().saveAll(new Object[]{promoResult, orderEntry, fixedPriceProductActionModel});
        return Collections.singletonList(promoResult);
    }

    @Override
    public void undo(ItemModel item) {
        if (!(item instanceof RuleBasedFixedPriceProductActionModel)) {
            return;
        }
        RuleBasedFixedPriceProductActionModel action = (RuleBasedFixedPriceProductActionModel) item;
        this.handleUndoActionMetadata(action);
        Long productId = action.getProductId();
        PromotionResultModel promotionResult = action.getPromotionResult();

        AbstractOrderModel order = promotionResult.getOrder();
        String priceType = order.getPriceType();
        for(AbstractOrderEntryModel entry : order.getEntries()) {
            if(entry.isFixedPrice() && entry.getProductId().equals(productId)) {
                if (PriceType.DISTRIBUTOR_PRICE.toString().equals(priceType)) {
                    entry.setFixedPrice(false);
                    continue;
                }
                PriceData priceData = productService.getPriceOfProduct(entry.getProductId(), 0);
                if(priceData != null) {
                    Double basePrice = PriceType.WHOLESALE_PRICE.toString().equals(priceType) ? priceData.getWholesalePrice():priceData.getPrice();
                    entry.setBasePrice(basePrice);
                    entry.setOriginBasePrice(basePrice);
                    entry.setFixedPrice(false);
                }
            }
        }

        this.getModelService().save(order);
    }


    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }
}
