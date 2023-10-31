package com.vctek.orderservice.strategy.impl;

import com.vctek.orderservice.dto.CommerceAbstractOrderParameter;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.service.CalculationService;
import com.vctek.orderservice.service.ModelService;
import com.vctek.orderservice.strategy.CommerceUpdateCartStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DefaultCommerceUpdateCartStrategy extends AbstractCommerceCartStrategy implements CommerceUpdateCartStrategy {
    private CalculationService calculationService;

    @Override
    public void updateCartDiscount(CommerceAbstractOrderParameter commerceAbtractOrderParameter) {
        AbstractOrderModel cart = commerceAbtractOrderParameter.getOrder();
        cart.setCalculated(false);
        cart.setDiscountType(commerceAbtractOrderParameter.getDiscountType());
        cart.setDiscount(commerceAbtractOrderParameter.getDiscount());
        commerceAbtractOrderParameter.setRecalculateVat(true);
        commerceCartCalculationStrategy.calculateCart(commerceAbtractOrderParameter);
    }

    @Override
    @Transactional
    public void updateVat(CommerceAbstractOrderParameter commerceAbtractOrderParameter) {
        AbstractOrderModel cart = commerceAbtractOrderParameter.getOrder();
        cart.setVat(commerceAbtractOrderParameter.getVat());
        cart.setVatType(commerceAbtractOrderParameter.getVatType());
        calculationService.calculateVat(cart);
    }

    @Autowired
    public void setCalculationService(CalculationService calculationService) {
        this.calculationService = calculationService;
    }

    @Autowired
    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }
}
