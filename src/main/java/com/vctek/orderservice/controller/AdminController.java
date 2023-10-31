package com.vctek.orderservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.OrderImageData;
import com.vctek.orderservice.dto.request.OrderPartialIndexRequest;
import com.vctek.orderservice.dto.request.OrderReportRequest;
import com.vctek.orderservice.dto.request.ReturnOrderSearchRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.*;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.repository.DefaultOrderRepository;
import com.vctek.validate.Validator;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController extends AbstractController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);
    private OrderElasticSearchFacade orderElasticSearchFacade;
    private Validator<OrderReportRequest> orderReportValidator;
    private OrderFacade orderFacade;
    private ReturnOrderFacade returnOrderFacade;
    private SyncReportFacade syncReportFacade;
    private ReturnOrderDocumentFacade returnOrderDocumentFacade;
    private DefaultOrderRepository defaultOrderRepository;
    private ObjectMapper objectMapper;

    @PostMapping("/orders/create-order-report")
    public ResponseEntity<Void> createOrderReport(@RequestBody OrderReportRequest orderReportRequest) {
        validateAdminCompanyUser();
        orderReportValidator.validate(orderReportRequest);
        orderElasticSearchFacade.createOrderReport(orderReportRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/orders/create-order-history-report")
    public ResponseEntity<Void> createOrderHistoryReport(@RequestBody OrderReportRequest orderReportRequest) {
        validateAdminCompanyUser();
        orderElasticSearchFacade.createOrderHistoryReport(orderReportRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/orders/full-index")
    public ResponseEntity fullIndex() {
        validateAdminCompanyUser();
        orderElasticSearchFacade.fullIndex();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/orders/partial-index")
    public ResponseEntity partialIndex(@RequestBody OrderPartialIndexRequest request) {
        validateAdminCompanyUser();
        orderElasticSearchFacade.partialIndex(request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/orders/full-index-order-entry")
    public ResponseEntity fullIndexOrderEntry() {
        validateAdminCompanyUser();
        orderElasticSearchFacade.fullIndexOrderEntry();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/orders/full-index-order-payment")
    public ResponseEntity fullIndexOrderPaymentData(@RequestParam("companyId") Long companyId) {
        validateAdminCompanyUser();
        orderElasticSearchFacade.fullIndexOrderPaymentData(companyId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/orders/full-index-sell-signal")
    public ResponseEntity fullIndexOrderSellSignal(@RequestParam("companyId") Long companyId) {
        validateAdminCompanyUser();
        orderElasticSearchFacade.fullIndexOrderSellSignal(companyId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/orders/update-origin-base-price")
    public ResponseEntity updateOriginBasePrice(@RequestBody OrderReportRequest orderReportRequest) {
        validateAdminCompanyUser();
        syncReportFacade.updateOriginBasePrice(orderReportRequest);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/migration/orders/link-to-bill")
    public ResponseEntity linkOrderToBill(@RequestParam("companyId") Long companyId) {
        validateAdminCompanyUser();
        orderFacade.linkOrderToBill(companyId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/orders/create-revenue-return-order")
    public ResponseEntity<Void> createRevenueReturnOrder(@RequestBody ReturnOrderSearchRequest request) {
        validateAdminCompanyUser();

        if(request.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        returnOrderFacade.createRevenueReturnOrder(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/report/sync-promotions")
    public ResponseEntity syncPromotionReport(@RequestParam("companyId") Long companyId,
                                              @RequestParam(value = "type", defaultValue = "PROMOTION") String type) {
        validateAdminCompanyUser();
        syncReportFacade.syncPromotion(companyId, type);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/return-orders/update-report")
    public ResponseEntity updateReturnOrderReport(@RequestBody ReturnOrderSearchRequest request) {
        validateAdminCompanyUser();
        returnOrderFacade.updateReport(request);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/return-orders/update-origin-order-bill")
    public ResponseEntity updateOriginOrderBill(@RequestParam("companyId") Long companyId) {
        validateAdminCompanyUser();
        returnOrderFacade.updateOriginOrderBill(companyId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/return-orders/partial-index")
    public ResponseEntity partial(@RequestParam("companyId") Long companyId) {
        returnOrderDocumentFacade.partialIndex(companyId);
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @PostMapping("/orders/convert-image")
    public ResponseEntity convertImage() {
        List<AbstractOrderModel> models = defaultOrderRepository.findAllByImagesIsNotNull();
        if (CollectionUtils.isNotEmpty(models)) {
            for (AbstractOrderModel model : models) {
                try {
                    String[] images = model.getImages().split(";");
                    List<String> imageUrls = Arrays.asList(images);
                    List<OrderImageData> imageData = new ArrayList<>();
                    for (String image : imageUrls) {
                        OrderImageData data = new OrderImageData();
                        data.setUrl(image);
                        data.setFinishedProduct(false);
                        imageData.add(data);
                    }
                    model.setImages(objectMapper.writeValueAsString(imageData));
                } catch (JsonProcessingException e) {
                    LOGGER.error("CANNOT WRITE IMAGE ORDER: {}", model.getCode());
                }
            }
            defaultOrderRepository.saveAll(models);
        }
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/orders/full-index-order-entry-sale-off")
    public ResponseEntity fullIndexOrderEntrySaleOff() {
        validateAdminCompanyUser();
        orderElasticSearchFacade.fullIndexOrderEntrySaleOff();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Autowired
    public void setOrderElasticSearchFacade(OrderElasticSearchFacade orderElasticSearchFacade) {
        this.orderElasticSearchFacade = orderElasticSearchFacade;
    }

    @Autowired
    @Qualifier("orderReportValidator")
    public void setOrderReportValidator(Validator<OrderReportRequest> orderReportValidator) {
        this.orderReportValidator = orderReportValidator;
    }

    @Autowired
    public void setOrderFacade(OrderFacade orderFacade) {
        this.orderFacade = orderFacade;
    }

    @Autowired
    public void setReturnOrderFacade(ReturnOrderFacade returnOrderFacade) {
        this.returnOrderFacade = returnOrderFacade;
    }

    @Autowired
    public void setSyncReportFacade(SyncReportFacade syncReportFacade) {
        this.syncReportFacade = syncReportFacade;
    }

    @Autowired
    public void setReturnOrderDocumentFacade(ReturnOrderDocumentFacade returnOrderDocumentFacade) {
        this.returnOrderDocumentFacade = returnOrderDocumentFacade;
    }

    @Autowired
    public void setDefaultOrderRepository(DefaultOrderRepository defaultOrderRepository) {
        this.defaultOrderRepository = defaultOrderRepository;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
