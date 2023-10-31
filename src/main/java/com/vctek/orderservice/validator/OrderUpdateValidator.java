package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.AvailablePointAmountData;
import com.vctek.orderservice.dto.PaymentMethodData;
import com.vctek.orderservice.dto.request.AvailablePointAmountRequest;
import com.vctek.orderservice.dto.request.OrderRequest;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.service.OrderService;
import com.vctek.util.CommonUtils;
import com.vctek.util.OrderType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component("orderUpdateValidator")
public class OrderUpdateValidator extends AbstractOrderRequestValidator {
    private OrderService orderService;

    public OrderUpdateValidator(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    protected AbstractOrderModel getOrder(OrderRequest orderRequest) {
        OrderModel orderModel = orderService.findByCodeAndCompanyId(orderRequest.getCode(), orderRequest.getCompanyId());
        populateCard(orderModel, orderRequest);
        return orderModel;
    }

    @Override
    protected void validateLoyaltyPoint(OrderRequest orderRequest, PaymentTransactionRequest payment, PaymentMethodData paymentMethodData) {
        if(paymentMethodData != null && paymentMethodData.getId().equals(payment.getPaymentMethodId())) {
            double paymentAmount = CommonUtils.readValue(payment.getAmount());
            if(paymentAmount <= 0) {
                return;
            }

            if(StringUtils.isBlank(orderRequest.getCardNumber()) && paymentAmount > 0) {
                ErrorCodes err = ErrorCodes.EMPTY_LOYALTY_CARD_NUMBER;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }

            AvailablePointAmountRequest pointAmountRequest = new AvailablePointAmountRequest();
            pointAmountRequest.setCompanyId(orderRequest.getCompanyId());
            pointAmountRequest.setOrderCode(orderRequest.getCode());
            pointAmountRequest.setCardNumber(orderRequest.getCardNumber());
            AvailablePointAmountData pointAmountData = loyaltyService.computeAvailablePointAmountOf(pointAmountRequest);
            double availablePoint = CommonUtils.readValue(pointAmountData.getAvailableAmount());
            double maximumRedeemPoint = CommonUtils.readValue(pointAmountData.getPointAmount());
            double redeemPoint = paymentAmount / pointAmountData.getConversionRate();

            OrderModel orderModel = orderService.findByCodeAndCompanyId(orderRequest.getCode(), orderRequest.getCompanyId());
            double oldRedeemPoint = CommonUtils.readValue(orderModel.getRedeemAmount()) / pointAmountData.getConversionRate();


            if(redeemPoint - oldRedeemPoint > availablePoint) {
                ErrorCodes err = ErrorCodes.CANNOT_EXCEED_AVAILABLE_POINTS;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }

            if(redeemPoint > maximumRedeemPoint) {
                ErrorCodes err = ErrorCodes.INVALID_POINT_FOR_ORDER;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }

            validatePointInteger(redeemPoint);
        }
    }

    private void populateCard(OrderModel orderModel, OrderRequest orderRequest) {
        if(OrderType.ONLINE.toString().equals(orderModel.getType())) {
            //NOSONAR Ignore validate update loyalty card for online, FIXME when online order has redeem amount
            return;
        }

        if ((orderModel.getTotalRewardAmount() != null || orderModel.getRedeemAmount() != null) &&
                StringUtils.isNotBlank(orderModel.getCardNumber()) && !orderModel.getCardNumber().equals(orderRequest.getCardNumber())) {
            ErrorCodes err = ErrorCodes.CANNOT_UPDATE_LOYALTY_CARD_INFO;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }
}
