package com.vctek.orderservice.elasticsearch.index;

import com.vctek.orderservice.kafka.producer.MigratePaymentMethodProducer;
import com.vctek.orderservice.model.PaymentTransactionModel;
import com.vctek.orderservice.service.PaymentTransactionService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public class MigratePaymentMethodRunnable implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MigratePaymentMethodRunnable.class);
    private Authentication authentication;
    private int indexOfThread;
    private int pageSize;
    private int numOfThread;
    private MigratePaymentMethodProducer migratePaymentMethodProducer;
    private PaymentTransactionService paymentTransactionService;

    public MigratePaymentMethodRunnable(Authentication authentication, int indexOfThread, int pageSize, int numOfThread) {
        this.authentication = authentication;
        this.indexOfThread = indexOfThread;
        this.pageSize = pageSize;
        this.numOfThread = numOfThread;
    }

    @Override
    public void run() {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        int index = this.indexOfThread;
        Pageable pageable = PageRequest.of(index, this.pageSize, new Sort(Sort.Direction.ASC, "id"));
        while (true) {
            Page<PaymentTransactionModel> page = paymentTransactionService.findAllForMigratePaymentMethod(pageable);
            List<PaymentTransactionModel> paymentTransactionModels = page.getContent();
            if (CollectionUtils.isEmpty(paymentTransactionModels)) {
                LOGGER.info("Run migrate done!: {} totalItems", page.getTotalElements());
                break;
            }

            migratePaymentMethodProducer.sendMigratePaymentMethodMessage(paymentTransactionModels);

            index += this.numOfThread;
            pageable = PageRequest.of(index, this.pageSize);
        }
    }

    public void setMigratePaymentMethodProducer(MigratePaymentMethodProducer migratePaymentMethodProducer) {
        this.migratePaymentMethodProducer = migratePaymentMethodProducer;
    }

    public void setPaymentTransactionService(PaymentTransactionService paymentTransactionService) {
        this.paymentTransactionService = paymentTransactionService;
    }
}
