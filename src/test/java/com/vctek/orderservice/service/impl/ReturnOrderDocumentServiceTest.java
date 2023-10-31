package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.elasticsearch.model.returnorder.ExchangeOrder;
import com.vctek.orderservice.elasticsearch.model.returnorder.ReturnOrderDocument;
import com.vctek.orderservice.elasticsearch.model.returnorder.ReturnOrderEntry;
import com.vctek.orderservice.elasticsearch.repository.ReturnOrderDocumentRepository;
import com.vctek.orderservice.elasticsearch.service.impl.ReturnOrderDocumentServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ReturnOrderDocumentServiceTest {
    private ReturnOrderDocumentServiceImpl service;

    @Mock
    private ReturnOrderDocumentRepository returnOrderDocumentRepository;
    @Mock
    private ElasticsearchTemplate elasticsearchTemplate;
    @Mock
    private ReturnOrderDocument doc;
    private ExchangeOrder exchangeOrder = new ExchangeOrder();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new ReturnOrderDocumentServiceImpl(elasticsearchTemplate);
        service.setReturnOrderDocumentRepository(returnOrderDocumentRepository);
        exchangeOrder.setEntries(Collections.singletonList(new ReturnOrderEntry()));
    }

    @Test
    public void save() {
        service.save(doc);
        verify(returnOrderDocumentRepository).save(doc);
    }

    @Test
    public void deleteAllDocuments() {
        service.deleteAllDocuments();
        verify(returnOrderDocumentRepository).deleteAll();
    }

    @Test
    public void findById() {
        service.findById(1L);
        verify(returnOrderDocumentRepository).findById(1L);
    }

    @Test
    public void updateExchangeOrder() {
        service.updateExchangeOrder(1L, exchangeOrder);
        verify(elasticsearchTemplate).update(any(UpdateQuery.class));
    }

}
