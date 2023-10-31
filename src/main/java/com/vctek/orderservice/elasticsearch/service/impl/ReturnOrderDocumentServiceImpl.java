package com.vctek.orderservice.elasticsearch.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vctek.orderservice.elasticsearch.model.returnorder.ExchangeOrder;
import com.vctek.orderservice.elasticsearch.model.returnorder.ReturnOrderDocument;
import com.vctek.orderservice.elasticsearch.repository.ReturnOrderDocumentRepository;
import com.vctek.orderservice.elasticsearch.service.ReturnOrderDocumentService;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.util.ElasticSearchIndex;
import com.vctek.util.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;

@Service
public class ReturnOrderDocumentServiceImpl extends BulkIndexElasticServiceImpl implements ReturnOrderDocumentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReturnOrderDocumentServiceImpl.class);
    private ReturnOrderDocumentRepository returnOrderDocumentRepository;
    private ObjectMapper objectMapper;

    public ReturnOrderDocumentServiceImpl(ElasticsearchTemplate elasticsearchTemplate) {
        super(elasticsearchTemplate);
        objectMapper = new ObjectMapper();
    }

    @Override
    protected Class getClassIndex() {
        return ReturnOrderDocument.class;
    }

    @Override
    public String getIndexName() {
        return ElasticSearchIndex.RETURN_ORDER_INDEX;
    }

    @Override
    public ReturnOrderDocument save(ReturnOrderDocument document) {
        return returnOrderDocumentRepository.save(document);
    }

    @Override
    public void deleteAllDocuments() {
        returnOrderDocumentRepository.deleteAll();
    }

    @Override
    public ReturnOrderDocument findById(Long id) {
        Optional<ReturnOrderDocument> documentOptional = returnOrderDocumentRepository.findById(id);
        return documentOptional.isPresent() ? documentOptional.get() : null;
    }

    @Override
    public void updateExchangeOrder(Long returnOrderId, ExchangeOrder exchangeOrderDoc) {
        Map<String, Object> updateFields = new HashMap<>();
        updateFields.put("exchangeOrder", exchangeOrderDoc);
        if (CollectionUtils.isNotEmpty(exchangeOrderDoc.getEntries())) {
            updateFields.put("exchangeExchangeWarehouseId", exchangeOrderDoc.getWarehouseId());
        }
        UpdateQuery updateQuery = createUpdateQuery(getIndexName(), updateFields, returnOrderId);
        this.elasticsearchTemplate.update(updateQuery);
        this.elasticsearchTemplate.refresh(this.getIndexName());
    }

    @Override
    public Page<ReturnOrderDocument> search(SearchQuery query) {
        return returnOrderDocumentRepository.search(query);
    }

    @Override
    public void updateReturnOrderInfo(ReturnOrderModel returnOrder) {
        Map<String, Object> updateFields = new HashMap<>();
        updateFields.put("note", returnOrder.getNote());
        if (returnOrder.getShippingFee() != null) {
            updateFields.put("shippingFee", returnOrder.getShippingFee());
        }
        if (returnOrder.getVat() != null) {
            updateFields.put("vat", returnOrder.getVat());
        }

        double amount = getAmountReturnOrder(returnOrder);
        updateFields.put("amount", amount);

        UpdateQuery updateQuery = createUpdateQuery(getIndexName(), updateFields, returnOrder.getId());
        this.elasticsearchTemplate.update(updateQuery);
        this.elasticsearchTemplate.refresh(this.getIndexName());
    }

    private double getAmountReturnOrder(ReturnOrderModel returnOrder) {
        ReturnOrderDocument document = returnOrderDocumentRepository.findByIdAndCompanyId(returnOrder.getId(), returnOrder.getCompanyId());
        ExchangeOrder exchangeOrder = document.getExchangeOrder();
        double exchangeCost = (exchangeOrder != null && exchangeOrder.getFinalPrice() != null) ?
                exchangeOrder.getFinalPrice() : 0;
        double returnCost = document.getBill() != null && document.getBill().getFinalPrice() != null ?
                document.getBill().getFinalPrice() : 0;

        double shippingFee = CommonUtils.readValue(returnOrder.getShippingFee()) ;
        double returnVat = CommonUtils.readValue(returnOrder.getVat()) ;
        double newAmount = exchangeCost - (returnCost + shippingFee + returnVat);
        return newAmount;
    }

    @Override
    public ReturnOrderDocument findByIdAndCompanyId(Long returnOrderId, Long companyId) {
        return returnOrderDocumentRepository.findByIdAndCompanyId(returnOrderId, companyId);
    }

    @Override
    public void saveAll(List<ReturnOrderDocument> returnOrderDocuments) {
        returnOrderDocumentRepository.saveAll(returnOrderDocuments);
    }

    private UpdateQuery createUpdateQuery(String indexName, Map<String, Object> updateFields, Long returnOrderId) {
        UpdateQuery updateQuery = new UpdateQuery();
        updateQuery.setClazz(ReturnOrderDocument.class);
        updateQuery.setIndexName(indexName);
        updateQuery.setDoUpsert(true);
        updateQuery.setId(String.valueOf(returnOrderId));
        UpdateRequest updateRequest = new UpdateRequest(updateQuery.getIndexName(), updateQuery.getType(), updateQuery.getId());
        updateRequest.retryOnConflict(5);
        try {
            updateRequest.doc(buildSourceFrom(updateFields), XContentType.JSON);
        } catch (JsonProcessingException e) {
            LOGGER.error(MessageFormat.format("INDEX RETURN ORDER ERROR: id ({0}), error ({1})", returnOrderId,
                    e.getMessage()), e);
        }
        updateQuery.setUpdateRequest(updateRequest);
        return updateQuery;
    }

    private String buildSourceFrom(Map<String, Object> fields) throws JsonProcessingException {
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> stringObjectEntry : fields.entrySet()) {
            map.put(stringObjectEntry.getKey(), stringObjectEntry.getValue());
        }
        return objectMapper.writeValueAsString(map);
    }

    @Autowired
    public void setReturnOrderDocumentRepository(ReturnOrderDocumentRepository returnOrderDocumentRepository) {
        this.returnOrderDocumentRepository = returnOrderDocumentRepository;
    }
}
