package com.vctek.orderservice.validator;

import com.vctek.dto.promotion.ActionDTO;
import com.vctek.dto.promotion.ConditionDTO;
import com.vctek.dto.promotion.ParameterDTO;
import com.vctek.dto.promotion.PromotionSourceRuleDTO;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.util.PromotionDefinitionCode;
import com.vctek.validate.Validator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class PromotionSourceRuleValidator implements Validator<PromotionSourceRuleDTO> {

    @Override
    public void validate(PromotionSourceRuleDTO promotionSourceRuleDTO) throws ServiceException {
        if (promotionSourceRuleDTO.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (StringUtils.isBlank(promotionSourceRuleDTO.getMessageFired())) {
            ErrorCodes err = ErrorCodes.EMPTY_PROMOTION_MESSAGE_FIRED;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (CollectionUtils.isEmpty(promotionSourceRuleDTO.getConditions())) {
            ErrorCodes err = ErrorCodes.EMPTY_PROMOTION_CONDITIONS;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (CollectionUtils.isEmpty(promotionSourceRuleDTO.getActions())) {
            ErrorCodes err = ErrorCodes.EMPTY_PROMOTION_ACTIONS;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        validatePartnerProductPromotion(promotionSourceRuleDTO);
        validateEmployeeOrderPercentageDiscount(promotionSourceRuleDTO);
    }

    private void validateEmployeeOrderPercentageDiscount(PromotionSourceRuleDTO promotionSourceRuleDTO) {
        ActionDTO actionDTO = promotionSourceRuleDTO.getActions().stream()
                .filter(a -> PromotionDefinitionCode.VCTEK_EMPLOYEE_ORDER_DISCOUNT_ACTION.code().equalsIgnoreCase(a.getDefinitionId()))
                .findFirst().orElse(null);
        if(actionDTO == null) {
            return;
        }
        Optional<ConditionDTO> couponConditionOptional = promotionSourceRuleDTO.getConditions().stream()
                .filter(c -> PromotionDefinitionCode.QUALIFIER_COUPONS.code().equals(c.getDefinitionId()))
                .findFirst();
        if(!couponConditionOptional.isPresent()) {
            ErrorCodes err = ErrorCodes.EMPTY_COUPON_CONDITION;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    private void validatePartnerProductPromotion(PromotionSourceRuleDTO promotionSourceRuleDTO) {
        List<ConditionDTO> conditions = promotionSourceRuleDTO.getConditions();
        List<ConditionDTO> containerConditions = conditions.stream().filter(c -> PromotionDefinitionCode.CONTAINER.code().equals(c.getDefinitionId()))
                .collect(Collectors.toList());

        for (ConditionDTO container : containerConditions) {
            List<ConditionDTO> children = container.getChildren();
            if (CollectionUtils.isEmpty(children)) {
                continue;
            }
            ParameterDTO containerId = container.getParameters().get(PromotionSourceRuleDataValidator.ID_CONTAINER);
            for (ConditionDTO group : children) {
                if (!PromotionDefinitionCode.GROUP.code().equals(group.getDefinitionId())) {
                    continue;
                }

                if (containerId != null && "CONTAINER_X".equals(containerId.getValue()) && CollectionUtils.isEmpty(group.getChildren())) {
                    ErrorCodes err = ErrorCodes.EMPTY_PROMOTION_CONDITIONS;
                    throw new ServiceException(err.code(), err.message(), err.httpStatus());
                }
            }
        }
    }

}
