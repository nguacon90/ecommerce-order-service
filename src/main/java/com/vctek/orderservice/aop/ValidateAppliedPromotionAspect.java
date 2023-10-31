package com.vctek.orderservice.aop;

import com.vctek.dto.promotion.ActionDTO;
import com.vctek.dto.promotion.ParameterDTO;
import com.vctek.dto.promotion.PromotionSourceRuleDTO;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.PromotionSourceRuleFacade;
import com.vctek.orderservice.service.ProductService;
import com.vctek.orderservice.util.ConditionDefinitionParameter;
import com.vctek.orderservice.util.PromotionDefinitionCode;
import com.vctek.redis.ProductData;
import com.vctek.util.ProductTypeSell;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class ValidateAppliedPromotionAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateAppliedPromotionAspect.class);
    private PromotionSourceRuleFacade promotionSourceRuleFacade;
    private ProductService productService;

    public ValidateAppliedPromotionAspect(PromotionSourceRuleFacade promotionSourceRuleFacade) {
        this.promotionSourceRuleFacade = promotionSourceRuleFacade;
    }

    @Before("execution(* com.vctek.orderservice.facade.impl.CartFacadeImpl.appliedPromotion(..)) && args(cartCode, companyId, promotionSourceRuleId)" +
            "|| execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.appliedPromotion(..)) && args(cartCode, companyId, promotionSourceRuleId)")
    public void validateAddCartEntry(String cartCode, Long companyId, Long promotionSourceRuleId) {
        PromotionSourceRuleDTO sourceRuleDTO = promotionSourceRuleFacade.findById(promotionSourceRuleId, companyId);
        if(sourceRuleDTO == null) {
            return;
        }
        ActionDTO freeGiftAction = sourceRuleDTO.getActions().stream()
                .filter(a -> PromotionDefinitionCode.VCTEK_FREE_GIFT_ACTION.code().equalsIgnoreCase(a.getDefinitionId()))
                .findFirst().orElse(null);
        if(freeGiftAction == null) {
            return;
        }

        ParameterDTO parameterDTO = freeGiftAction.getParameters().get(ConditionDefinitionParameter.FREE_PRODUCT.code());
        if(parameterDTO == null || parameterDTO.getValue() == null) {
            return;
        }
        Long productId = (Long) parameterDTO.getValue();
        ProductData basicProductDetail = productService.getBasicProductDetail(productId);
        if(ProductTypeSell.STOP_SELLING.toString().equalsIgnoreCase(basicProductDetail.getTypeSell())) {
            ErrorCodes err = ErrorCodes.INVALID_FREE_GIFT_PRODUCT;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        try {
            ParameterDTO quantityDTO = freeGiftAction.getParameters().get(ConditionDefinitionParameter.QUANTITY.code());
            int qty = getQty(quantityDTO);
            productService.checkIsCombo(productId, companyId, qty);
        } catch (ServiceException e) {
            LOGGER.error(e.getMessage());
            ErrorCodes err = ErrorCodes.INVALID_FREE_GIFT_PRODUCT;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
            ErrorCodes err = ErrorCodes.INTERNAL_SERVER_ERROR;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    private Integer getQty(ParameterDTO quantityDTO) {
        if(quantityDTO == null || quantityDTO.getValue() == null || !(quantityDTO.getValue() instanceof Integer)) {
            return 1;
        }
        Integer qty = (Integer) quantityDTO.getValue();
        return qty != null ? qty : 1;
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }
}
