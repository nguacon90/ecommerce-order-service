package com.vctek.orderservice.event.listener;

import com.vctek.converter.Converter;
import com.vctek.orderservice.elasticsearch.model.LoyaltyRewardRateSearchModel;
import com.vctek.orderservice.elasticsearch.service.LoyaltyRewardRateSearchService;
import com.vctek.orderservice.event.ProductLoyaltyRewardRateEvent;
import com.vctek.orderservice.model.ProductLoyaltyRewardRateModel;
import com.vctek.orderservice.util.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ProductLoyaltyRewardRateEventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductLoyaltyRewardRateEventListener.class);
    private Converter<ProductLoyaltyRewardRateModel, LoyaltyRewardRateSearchModel> loyaltyRewardRateSearchConverter;
    private LoyaltyRewardRateSearchService searchService;

    public ProductLoyaltyRewardRateEventListener(Converter<ProductLoyaltyRewardRateModel, LoyaltyRewardRateSearchModel> loyaltyRewardRateSearchConverter, LoyaltyRewardRateSearchService searchService) {
        this.loyaltyRewardRateSearchConverter = loyaltyRewardRateSearchConverter;
        this.searchService = searchService;
    }

    @EventListener
    public void handleProductLoyaltyRewardRateEvent(ProductLoyaltyRewardRateEvent event) {
        try {
            ProductLoyaltyRewardRateModel model = event.getProductLoyaltyRewardRateModel();
            if (EventType.CREATE.toString().equals(event.getType())) {
                LoyaltyRewardRateSearchModel loyaltyRewardRateSearchModel = loyaltyRewardRateSearchConverter.convert(model);
                searchService.save(loyaltyRewardRateSearchModel);
            } else {
                searchService.deleteById(model.getId());
            }

        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
