package com.vctek.orderservice.elasticsearch.index;

import com.vctek.converter.Converter;
import com.vctek.orderservice.elasticsearch.model.returnorder.ReturnOrderDocument;
import com.vctek.orderservice.elasticsearch.service.ReturnOrderDocumentService;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.ReturnOrderService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

public class ReturnOrderIndexRunnable implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReturnOrderIndexRunnable.class);
    private Authentication authentication;
    private ReturnOrderService returnOrderService;
    private int indexOfThread;
    private int pageSize;
    private int numOfThread;
    private Converter<ReturnOrderModel, ReturnOrderDocument> returnOrderDocumentConverter;
    private ReturnOrderDocumentService returnOrderDocumentService;
    private Long companyId;

    public ReturnOrderIndexRunnable(Authentication authentication, int indexOfThread, int pageSize, int numOfThread) {
        this.authentication = authentication;
        this.indexOfThread = indexOfThread;
        this.pageSize = pageSize;
        this.numOfThread = numOfThread;
    }

    @Override
    public void run() {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        int index = this.indexOfThread;
        Pageable pageable = PageRequest.of(index, this.pageSize);
        while (true) {
            Page<ReturnOrderModel> returnOrderModels = returnOrderService.findAllByCompanyId(companyId, pageable);
            List<ReturnOrderDocument> returnOrderDocuments = this.index(returnOrderModels);
            if (CollectionUtils.isEmpty(returnOrderDocuments)) {
                LOGGER.info("Index done!");
                break;
            }
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("Indexed: success {} items / {} pages/ {} totalItems", returnOrderDocuments.size(),
                        index, returnOrderModels.getTotalElements());
            }
            index += this.numOfThread;
            pageable = PageRequest.of(index, this.pageSize);
        }
    }

    protected List<ReturnOrderDocument> index(Page<ReturnOrderModel> data) {
        List<ReturnOrderModel> returnOrderModels = data.getContent();
        if(CollectionUtils.isEmpty(returnOrderModels)) {
            return new ArrayList<>();
        }

        List<ReturnOrderDocument> returnOrderDocuments = new ArrayList<>();
        for(ReturnOrderModel order : returnOrderModels) {
            try {
                returnOrderDocuments.add(getConvertReturnOrderDocument(order));
            } catch (RuntimeException e) {
                LOGGER.error("Convert error: OrderId: {}, message: {}", order.getId(), e.getMessage(), e);
            }
        }

        returnOrderDocumentService.bulkIndex(returnOrderDocuments);
        return returnOrderDocuments;
    }

    protected ReturnOrderDocument getConvertReturnOrderDocument(ReturnOrderModel order) {
        return returnOrderDocumentConverter.convert(order);
    }

    public void setReturnOrderService(ReturnOrderService returnOrderService) {
        this.returnOrderService = returnOrderService;
    }

    public void setReturnOrderDocumentConverter(Converter<ReturnOrderModel, ReturnOrderDocument> returnOrderDocumentConverter) {
        this.returnOrderDocumentConverter = returnOrderDocumentConverter;
    }

    public void setReturnOrderDocumentService(ReturnOrderDocumentService returnOrderDocumentService) {
        this.returnOrderDocumentService = returnOrderDocumentService;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }
}
