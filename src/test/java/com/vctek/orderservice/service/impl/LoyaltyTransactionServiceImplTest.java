package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.model.LoyaltyTransactionModel;
import com.vctek.orderservice.repository.LoyaltyTransactionRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class LoyaltyTransactionServiceImplTest {

    @Mock
    private LoyaltyTransactionRepository repository;
    @Mock
    private ArgumentCaptor<List<LoyaltyTransactionModel>> captor;

    private LoyaltyTransactionServiceImpl service;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        captor = ArgumentCaptor.forClass(List.class);
        service = new LoyaltyTransactionServiceImpl(repository);
    }

    @Test
    public void save(){
        LoyaltyTransactionModel loyaltyTransactionModel = new LoyaltyTransactionModel();
        service.save(loyaltyTransactionModel);
        verify(repository).save(loyaltyTransactionModel);
    }

    @Test
    public void findByOrderCodeAndInvoiceNumber(){
        service.findByOrderCodeAndInvoiceNumber("abc","abcd");
        verify(repository).findByOrderCodeAndInvoiceNumber("abc", "abcd");
    }

    @Test
    public void findAllByOrderCode(){
        service.findByAllOrderCode("abc");
        verify(repository).findAllByOrderCode("abc");
    }

    @Test
    public void findLastByOrderCode(){
        service.findLastByOrderCode("abc");
        verify(repository).findLastByOrderCode("abc");
    }

    @Test
    public void findLastByOrderCodeAndListType(){
        service.findLastByOrderCodeAndListType("abc", Arrays.asList("test"));
        verify(repository).findLastByOrderCodeAndListType("abc",Arrays.asList("test"));
    }

    @Test
    public void cloneRedeemLoyaltyTransaction(){
        LoyaltyTransactionModel model = new LoyaltyTransactionModel();
        model.setOrderCode("code");
        when(repository.findLastByOrderCodeAndListType(anyString(), anyList())).thenReturn(Optional.of(model)).thenReturn(Optional.empty());
        service.cloneAwardRedeemLoyaltyTransaction("abc", "test");
        verify(repository, times(2)).findLastByOrderCodeAndListType(anyString() ,anyList());
        verify(repository).saveAll(captor.capture());
        List<LoyaltyTransactionModel> modelSaved = captor.getValue();
        assertEquals("test", modelSaved.get(0).getOrderCode());
    }

    @Test
    public void cloneAwardLoyaltyTransaction(){
        LoyaltyTransactionModel model = new LoyaltyTransactionModel();
        model.setOrderCode("code");
        when(repository.findLastByOrderCodeAndListType(anyString(), anyList())).thenReturn(Optional.empty()).thenReturn(Optional.of(model));
        service.cloneAwardRedeemLoyaltyTransaction("abc", "test");
        verify(repository, times(2)).findLastByOrderCodeAndListType(anyString() ,anyList());
        verify(repository).saveAll(captor.capture());
        List<LoyaltyTransactionModel> modelSaved = captor.getValue();
        assertEquals("test", modelSaved.get(0).getOrderCode());
    }

}
