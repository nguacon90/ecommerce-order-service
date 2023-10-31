package com.vctek.orderservice.service.impl;

import com.vctek.migration.dto.InvoiceLinkDto;
import com.vctek.orderservice.kafka.producer.InvoiceLinkProducer;
import com.vctek.orderservice.model.PaymentTransactionModel;
import com.vctek.orderservice.service.PaymentTransactionService;
import com.vctek.orderservice.service.SyncInvoiceLinkService;
import com.vctek.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SyncInvoiceLinkServiceImpl implements SyncInvoiceLinkService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncInvoiceLinkServiceImpl.class);
    private PaymentTransactionService paymentTransactionService;
    private InvoiceLinkProducer invoiceLinkProducer;

    @Override
    @Transactional
    public void processInvoiceLinkMessage(List<InvoiceLinkDto> linkDtoList) {
        List<PaymentTransactionModel> models = new ArrayList<>();
        for (InvoiceLinkDto dto : linkDtoList) {
            PaymentTransactionModel paymentTransactionModel = null;
            if (dto.getReturnExternalId() != null ) {
                paymentTransactionModel = paymentTransactionService.findByMoneySourceIdAndPaymentMethodIdAndReturnOrderExternalIdAndCompanyId(dto.getMoneySourceId(), dto.getPaymentMethodId(), dto.getReturnExternalId(), dto.getCompanyId());
                if (paymentTransactionModel == null) {
                    LOGGER.debug("Not found payment transaction model with return order externalId: {}, moneySourceId: {}, paymentMethodId: {}",
                            dto.getReturnExternalId(), dto.getMoneySourceId(), dto.getPaymentMethodId());
                    continue;
                }
                dto.setReturnOrderId(paymentTransactionModel.getReturnOrder().getId());
            } else {
                paymentTransactionModel = findPaymentForInvoiceLink(dto);
                if (paymentTransactionModel == null) {
                    LOGGER.debug("Not found payment transaction model order code: {}, moneySourceId: {}, paymentMethodId: {}",
                            dto.getOrderCode(), dto.getMoneySourceId(), dto.getPaymentMethodId());
                    continue;
                }
                dto.setPaymentTransactionId(paymentTransactionModel.getId());
                paymentTransactionModel.setInvoiceId(dto.getInvoiceId());
                models.add(paymentTransactionModel);
            }
        }
        paymentTransactionService.saveAll(models);
        invoiceLinkProducer.produce(linkDtoList);
    }

    private PaymentTransactionModel findPaymentForInvoiceLink(InvoiceLinkDto dto) {
        List<PaymentTransactionModel> paymentList = paymentTransactionService.findPaymentForInvoiceLink(dto);
        for (PaymentTransactionModel model : paymentList) {
            if (Math.ceil(CommonUtils.readValue(model.getAmount())) == Math.ceil(CommonUtils.readValue(dto.getAmount()))) {
                return model;
            }
        }
        return null;
    }

    @Autowired
    public void setPaymentTransactionService(PaymentTransactionService paymentTransactionService) {
        this.paymentTransactionService = paymentTransactionService;
    }

    @Autowired
    public void setInvoiceLinkProducer(InvoiceLinkProducer invoiceLinkProducer) {
        this.invoiceLinkProducer = invoiceLinkProducer;
    }
}