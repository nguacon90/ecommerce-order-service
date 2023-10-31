package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.MoneySourceData;
import com.vctek.orderservice.dto.PaymentMethodData;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.FinanceClient;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.PaymentTransactionModel;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.service.PaymentTransactionService;
import com.vctek.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentTransactionRequestValidator implements Validator<PaymentTransactionRequest> {
    private PaymentTransactionService paymentTransactionService;
    private OrderService orderService;
    private FinanceClient financeClient;

    @Autowired
    public PaymentTransactionRequestValidator(PaymentTransactionService paymentTransactionService, OrderService orderService, FinanceClient financeClient) {
        this.paymentTransactionService = paymentTransactionService;
        this.orderService = orderService;
        this.financeClient = financeClient;
    }


    @Override
    public void validate(PaymentTransactionRequest request) throws ServiceException {
        if (request.getId() != null) {
            PaymentTransactionModel model = paymentTransactionService.findById(request.getId());
            if (model == null) {
                ErrorCodes err = ErrorCodes.INVALID_PAYMENT_TRANSACTION_ID;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }

        if (request.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (request.getOrderId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_ORDER_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        OrderModel orderModel = orderService.findById(request.getOrderId());
        if (orderModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (request.getMoneySourceId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_MONEY_SOURCE_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        MoneySourceData moneySourceData = financeClient.getMoneySource(request.getMoneySourceId(), request.getCompanyId());
        if (moneySourceData == null) {
            ErrorCodes err = ErrorCodes.INVALID_MONEY_SOURCE_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (request.getPaymentMethodId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_PAYMENT_METHOD_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        PaymentMethodData paymentMethodData = financeClient.getPaymentMethodData(request.getPaymentMethodId());
        if (paymentMethodData == null) {
            ErrorCodes err = ErrorCodes.INVALID_PAYMENT_METHOD_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }
}
