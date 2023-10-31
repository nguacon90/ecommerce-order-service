package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.LoyaltyTransactionModel;
import com.vctek.orderservice.repository.LoyaltyTransactionRepository;
import com.vctek.orderservice.service.LoyaltyTransactionService;
import com.vctek.util.TransactionType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class LoyaltyTransactionServiceImpl implements LoyaltyTransactionService {
    private LoyaltyTransactionRepository loyaltyTransactionRepository;

    public LoyaltyTransactionServiceImpl(LoyaltyTransactionRepository loyaltyTransactionRepository) {
        this.loyaltyTransactionRepository = loyaltyTransactionRepository;
    }

    @Override
    @Transactional
    public LoyaltyTransactionModel save(LoyaltyTransactionModel loyaltyTransactionModel) {
        return loyaltyTransactionRepository.save(loyaltyTransactionModel);
    }

    @Override
    public LoyaltyTransactionModel findByOrderCodeAndInvoiceNumber(String orderCode, String invoiceNumber) {
        return loyaltyTransactionRepository.findByOrderCodeAndInvoiceNumber(orderCode, invoiceNumber);
    }

    @Override
    public List<LoyaltyTransactionModel> findByAllOrderCode(String orderCode) {
        return loyaltyTransactionRepository.findAllByOrderCode(orderCode);
    }

    @Override
    public LoyaltyTransactionModel findLastByOrderCode(String code) {
        Optional<LoyaltyTransactionModel> optionalModel = loyaltyTransactionRepository.findLastByOrderCode(code);
        return optionalModel.isPresent() ? optionalModel.get() : null;
    }

    @Override
    public LoyaltyTransactionModel findLastByOrderCodeAndListType(String code, List<String>types) {
        Optional<LoyaltyTransactionModel> optionalModel = loyaltyTransactionRepository.findLastByOrderCodeAndListType(code,types);
        return optionalModel.isPresent() ? optionalModel.get() : null;
    }

    @Override
    @Transactional
    public void cloneAwardRedeemLoyaltyTransaction(String code, String retailOrderCode) {
        LoyaltyTransactionModel redemModel = findLastByOrderCodeAndListType(code, Arrays.asList(TransactionType.REDEEM.toString()));
        List<LoyaltyTransactionModel> models = new ArrayList<>();
        if (redemModel != null) {
            LoyaltyTransactionModel cloneRedeemModel = SerializationUtils.clone(redemModel);
            cloneRedeemModel.setId(null);
            cloneRedeemModel.setOrderCode(retailOrderCode);
            models.add(cloneRedeemModel);
        }
        LoyaltyTransactionModel awardModel = findLastByOrderCodeAndListType(code, Arrays.asList(TransactionType.AWARD.toString()));
        if (awardModel != null) {
            LoyaltyTransactionModel cloneAwardModel = SerializationUtils.clone(awardModel);
            cloneAwardModel.setId(null);
            cloneAwardModel.setOrderCode(retailOrderCode);
            models.add(cloneAwardModel);
        }

        if (CollectionUtils.isNotEmpty(models)) {
            loyaltyTransactionRepository.saveAll(models);
        }
    }
}
