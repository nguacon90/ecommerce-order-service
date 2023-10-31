package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.PaymentTransactionData;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.elasticsearch.index.MigratePaymentMethodRunnable;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.PaymentTransactionFacade;
import com.vctek.orderservice.kafka.producer.MigratePaymentMethodProducer;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.PaymentTransactionModel;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.service.PaymentTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class PaymentTransactionFacadeImpl implements PaymentTransactionFacade {
    private PaymentTransactionService paymentTransactionService;
    private Converter<PaymentTransactionModel, PaymentTransactionData> paymentTransactionDataConverter;
    private OrderService orderService;
    private MigratePaymentMethodProducer migratePaymentMethodProducer;

    public PaymentTransactionFacadeImpl(PaymentTransactionService paymentTransactionService, Converter<PaymentTransactionModel, PaymentTransactionData> paymentTransactionDataConverter, OrderService orderService) {
        this.paymentTransactionService = paymentTransactionService;
        this.paymentTransactionDataConverter = paymentTransactionDataConverter;
        this.orderService = orderService;
    }

    private void populateModel(PaymentTransactionRequest request, PaymentTransactionModel model) {
        model.setNote(request.getNote());
        model.setAmount(request.getAmount());
        model.setMoneySourceId(request.getMoneySourceId());
        model.setPaymentMethodId(request.getPaymentMethodId());
        populateOrder(request, model);


    }

    protected void populateOrder(PaymentTransactionRequest request, PaymentTransactionModel model) {
        OrderModel orderModel = orderService.findById(request.getOrderId());
        if(orderModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        model.setOrderModel(orderModel);
    }

    @Override
    public PaymentTransactionData create(PaymentTransactionRequest request) {
        PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();
        populateModel(request , paymentTransactionModel);

        PaymentTransactionModel saveModel = paymentTransactionService.save(paymentTransactionModel);
        return paymentTransactionDataConverter.convert(saveModel);
    }

    @Override
    public PaymentTransactionData update(PaymentTransactionRequest request) {
        PaymentTransactionModel model = paymentTransactionService.findById(request.getId());
        populateModel(request, model);
        PaymentTransactionModel savedModel = paymentTransactionService.save(model);
        return paymentTransactionDataConverter.convert(savedModel);
    }

    @Override
    public void delete(Long paymentMethodId) {
        PaymentTransactionModel model = paymentTransactionService.findById(paymentMethodId);
        if (model == null) {
            ErrorCodes err = ErrorCodes.INVALID_PAYMENT_TRANSACTION_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        paymentTransactionService.delete(model);
    }

    @Override
    public List<PaymentTransactionData> findAll() {
        List<PaymentTransactionModel> models = paymentTransactionService.findAll();
        return paymentTransactionDataConverter.convertAll(models);
    }

    @Override
    public PaymentTransactionData findById(Long paymentMethodId) {
        PaymentTransactionModel model = paymentTransactionService.findById(paymentMethodId);
        if (model == null) {
            ErrorCodes err = ErrorCodes.INVALID_PAYMENT_TRANSACTION_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        return paymentTransactionDataConverter.convert(model);
    }

    @Override
    public void migratePaymentForInvoice() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for(int i = 0; i < 5; i++) {
            MigratePaymentMethodRunnable methodRunnable = new MigratePaymentMethodRunnable(authentication, i, 100, 5);
            methodRunnable.setMigratePaymentMethodProducer(migratePaymentMethodProducer);
            methodRunnable.setPaymentTransactionService(paymentTransactionService);
            executorService.execute(methodRunnable);
        }
    }

    @Autowired
    public void setMigratePaymentMethodProducer(MigratePaymentMethodProducer migratePaymentMethodProducer) {
        this.migratePaymentMethodProducer = migratePaymentMethodProducer;
    }
}
