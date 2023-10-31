package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.exception.ServiceException;
import com.vctek.kafka.data.InvoiceKafkaData;
import com.vctek.kafka.data.OrderData;
import com.vctek.kafka.producer.ProductInfoKafkaData;
import com.vctek.orderservice.dto.OrderEntryExcelData;
import com.vctek.orderservice.dto.OrderSearchExcelData;
import com.vctek.orderservice.dto.WarehouseData;
import com.vctek.orderservice.dto.request.ReturnOrderSearchRequest;
import com.vctek.orderservice.elasticsearch.index.ReturnOrderIndexRunnable;
import com.vctek.orderservice.elasticsearch.model.OrderEntryData;
import com.vctek.orderservice.elasticsearch.model.PaymentTransactionData;
import com.vctek.orderservice.elasticsearch.model.returnorder.ExchangeOrder;
import com.vctek.orderservice.elasticsearch.model.returnorder.ReturnOrderBill;
import com.vctek.orderservice.elasticsearch.model.returnorder.ReturnOrderDocument;
import com.vctek.orderservice.elasticsearch.model.returnorder.ReturnOrderEntry;
import com.vctek.orderservice.elasticsearch.query.NestedSearchQuery;
import com.vctek.orderservice.elasticsearch.service.ReturnOrderDocumentService;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.ReturnOrderDocumentFacade;
import com.vctek.orderservice.feignclient.BillClient;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.service.ReturnOrderService;
import com.vctek.util.BillStatus;
import com.vctek.util.CommonUtils;
import com.vctek.util.OrderType;
import com.vctek.util.VNCharacterUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ReturnOrderDocumentFacadeImpl extends AbstractOrderElasticSearchFacade implements ReturnOrderDocumentFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReturnOrderDocumentFacadeImpl.class);
    private int numberOfThread;
    private int bulkSize;
    private ReturnOrderService returnOrderService;
    private ReturnOrderDocumentService returnOrderDocumentService;
    private Converter<ReturnOrderModel, ReturnOrderDocument> returnOrderDocumentConverter;
    private Populator<OrderModel, ExchangeOrder> exchangeOrderDocumentPopulator;
    private BillClient billClient;
    private static final String PAYMENT_TRANSACTIONS = "paymentTransactions";
    private static final int MAX_PAGE_SIZE = 1000;
    private OrderService orderService;

    @Override
    public void partialIndex(Long companyId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThread);
        for (int i = 0; i < numberOfThread; i++) {
            ReturnOrderIndexRunnable returnOrderIndexRunnable = new ReturnOrderIndexRunnable(authentication, i, bulkSize, numberOfThread);
            returnOrderIndexRunnable.setReturnOrderService(returnOrderService);
            returnOrderIndexRunnable.setReturnOrderDocumentService(returnOrderDocumentService);
            returnOrderIndexRunnable.setReturnOrderDocumentConverter(returnOrderDocumentConverter);
            returnOrderIndexRunnable.setCompanyId(companyId);
            executorService.execute(returnOrderIndexRunnable);
        }
    }

    @Override
    public void index(ReturnOrderModel returnOrderModel) {
        ReturnOrderDocument document = returnOrderDocumentConverter.convert(returnOrderModel);
        returnOrderDocumentService.save(document);
        LOGGER.debug("Index return order id : {}", returnOrderModel.getId());
    }

    @Override
    public void updateExchangeOrder(OrderModel exchangeOrder, ReturnOrderModel returnOrder) {
        ReturnOrderDocument document = returnOrderDocumentService.findById(returnOrder.getId());

        if (document == null) {
            document = returnOrderDocumentConverter.convert(returnOrder);
            returnOrderDocumentService.save(document);
            LOGGER.debug("Index return order id: {}", returnOrder.getId());
            return;
        }
        ExchangeOrder exchangeOrderDoc = new ExchangeOrder();
        exchangeOrderDocumentPopulator.populate(exchangeOrder, exchangeOrderDoc);
        returnOrderDocumentService.updateExchangeOrder(returnOrder.getId(), exchangeOrderDoc);
    }

    @Override
    public Page<ReturnOrderDocument> search(ReturnOrderSearchRequest searchRequest, Pageable pageableRequest) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery());
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withPageable(pageableRequest);
        if (searchRequest.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        boolQueryBuilder.must(QueryBuilders.matchQuery("companyId", searchRequest.getCompanyId()));
        if (searchRequest.getId() != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("id", searchRequest.getId()));
        }

        populateSearchDate(boolQueryBuilder, searchRequest.getFromCreatedTime(), searchRequest.getToCreatedTime(), "creationTime");
        if (StringUtils.isNotBlank(searchRequest.getProduct())) {
            List<NestedSearchQuery> nestedSearchQueries = populateNestedQueries();
            populateSearchNestedObject(boolQueryBuilder, searchRequest.getProduct(), nestedSearchQueries);
        }
        if (StringUtils.isNotBlank(searchRequest.getCustomer())) {
            populateSearchLikeNestedObject(boolQueryBuilder, searchRequest.getCustomer(),
                    "originOrder", "originOrder.customerId", "originOrder.customerName");
        }
        if (StringUtils.isNotBlank(searchRequest.getOrderTypes())) {
            populateSearchOrderTypes(boolQueryBuilder, searchRequest);
        }
        if (StringUtils.isNotBlank(searchRequest.getPaymentMethods())) {
            populateSearchPaymentMethods(boolQueryBuilder, searchRequest);
        }
        if (searchRequest.getReturnWarehouseId() != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("returnWarehouseId", searchRequest.getReturnWarehouseId()));
        }
        if (searchRequest.getExchangeWarehouseId() != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("exchangeWarehouseId", searchRequest.getExchangeWarehouseId()));
        }
        populateSearchEmployee(boolQueryBuilder, searchRequest);
        populateSearchOrderSources(boolQueryBuilder, searchRequest);
        return returnOrderDocumentService.search(nativeSearchQueryBuilder.withQuery(boolQueryBuilder).build());
    }

    private void populateSearchOrderSources(BoolQueryBuilder boolQueryBuilder, ReturnOrderSearchRequest searchRequest) {
        if (CollectionUtils.isNotEmpty(searchRequest.getOriginOrderSourceIds())) {
            BoolQueryBuilder shouldQuery = new BoolQueryBuilder();
            for (Long orderSourceId : searchRequest.getOriginOrderSourceIds()) {
                shouldQuery.should(QueryBuilders.matchQuery("originOrderSourceId", orderSourceId));
            }
            shouldQuery.minimumShouldMatch(MINIMUM_SHOULD_MATCH);
            boolQueryBuilder.must(shouldQuery);
        }
    }

    private void populateSearchEmployee(BoolQueryBuilder boolQueryBuilder, ReturnOrderSearchRequest searchRequest) {
        if (searchRequest.getOriginEmployeeId() != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("employeeId", searchRequest.getOriginEmployeeId()));
        }
    }

    private void populateSearchPaymentMethods(BoolQueryBuilder boolQueryBuilder, ReturnOrderSearchRequest searchRequest) {
        String[] paymentMethods = CommonUtils.splitByComma(searchRequest.getPaymentMethods());
        BoolQueryBuilder nestedQuery = new BoolQueryBuilder();
        BoolQueryBuilder mustNotNestedQuery = new BoolQueryBuilder();
        BoolQueryBuilder shouldNestedQuery = new BoolQueryBuilder();
        for (String paymentMethodId : paymentMethods) {
            try {
                if (Long.valueOf(paymentMethodId) <= 0) {
                    BoolQueryBuilder mustNot = new BoolQueryBuilder();
                    nestedQuery.should(mustNot.mustNot(QueryBuilders.existsQuery("paymentTransactions.paymentMethodId")));
                    shouldNestedQuery.should(mustNotNestedQuery.mustNot(QueryBuilders.nestedQuery(PAYMENT_TRANSACTIONS,
                            QueryBuilders.existsQuery(PAYMENT_TRANSACTIONS), ScoreMode.Avg)));
                } else {
                    nestedQuery.should(QueryBuilders.matchQuery("paymentTransactions.paymentMethodId", QueryParser.escape(paymentMethodId)));
                }
            } catch (NumberFormatException e) {
                ErrorCodes err = ErrorCodes.INVALID_PAYMENT_METHOD_ID;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }


        }
        nestedQuery.minimumShouldMatch(MINIMUM_SHOULD_MATCH);
        shouldNestedQuery.should(QueryBuilders.nestedQuery(PAYMENT_TRANSACTIONS, nestedQuery, ScoreMode.Avg));
        shouldNestedQuery.minimumShouldMatch(MINIMUM_SHOULD_MATCH);
        boolQueryBuilder.must(shouldNestedQuery);
    }

    private void populateSearchOrderTypes(BoolQueryBuilder boolQueryBuilder, ReturnOrderSearchRequest searchRequest) {
        String orderTypeStr = searchRequest.getOrderTypes();
        String[] orderTypes = CommonUtils.splitByComma(orderTypeStr);
        BoolQueryBuilder nestedQuery = new BoolQueryBuilder();
        for (String orderType : orderTypes) {
            nestedQuery.should(QueryBuilders.matchQuery("originOrder.type", QueryParser.escape(orderType)));
        }
        nestedQuery.minimumShouldMatch(MINIMUM_SHOULD_MATCH);
        boolQueryBuilder.must(QueryBuilders.nestedQuery("originOrder", nestedQuery, ScoreMode.Avg));
    }

    private List<NestedSearchQuery> populateNestedQueries() {
        List<NestedSearchQuery> nestedSearchQueries = new ArrayList<>();
        NestedSearchQuery query = new NestedSearchQuery();
        query.setIdQuery("bill.entries.productId");
        query.setNestedPath("bill.entries");
        query.setOtherFields(new String[]{"bill.entries.productSku", "bill.entries.productName", "bill.entries.name"});
        nestedSearchQueries.add(query);
        NestedSearchQuery query2 = new NestedSearchQuery();
        query2.setIdQuery("exchangeOrder.entries.productId");
        query2.setNestedPath("exchangeOrder.entries");
        query2.setOtherFields(new String[]{"exchangeOrder.entries.productSku", "exchangeOrder.entries.productName",
                "exchangeOrder.entries.name"});
        nestedSearchQueries.add(query2);

        NestedSearchQuery query3 = new NestedSearchQuery();
        query3.setIdQuery("bill.entries.comboId");
        query3.setNestedPath("bill.entries");
        query3.setOtherFields(new String[]{"bill.entries.comboSku", "bill.entries.comboName"});
        nestedSearchQueries.add(query3);
        NestedSearchQuery query4 = new NestedSearchQuery();
        query4.setIdQuery("exchangeOrder.entries.comboId");
        query4.setNestedPath("exchangeOrder.entries");
        query4.setOtherFields(new String[]{"exchangeOrder.entries.comboSku", "exchangeOrder.entries.comboName"});
        nestedSearchQueries.add(query4);
        return nestedSearchQueries;
    }

    @Override
    public void updateReturnOrderInfo(ReturnOrderModel returnOrder) {
        returnOrderDocumentService.updateReturnOrderInfo(returnOrder);
    }

    @Override
    public void updateWarehouseExchangeOrder() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThread);
        for (int i = 0; i < numberOfThread; i++) {
            ReturnOrderIndexRunnable returnOrderIndexRunnable = new ReturnOrderIndexRunnable(authentication, i, bulkSize, numberOfThread) {
                @Override
                public ReturnOrderDocument getConvertReturnOrderDocument(ReturnOrderModel order) {
                    ReturnOrderDocument returnOrderDocument = returnOrderDocumentService.findById(order.getId());
                    if (returnOrderDocument != null) {
                        if (returnOrderDocument.getExchangeOrder() != null) {
                            ExchangeOrder exchangeOrder = returnOrderDocument.getExchangeOrder();
                            exchangeOrder.setWarehouseId(order.getExchangeOrder().getWarehouseId());
                            returnOrderDocument.setExchangeOrder(exchangeOrder);
                        }
                        return returnOrderDocument;
                    } else {
                        return returnOrderDocumentConverter.convert(order);
                    }
                }
            };
            returnOrderIndexRunnable.setReturnOrderService(returnOrderService);
            returnOrderIndexRunnable.setReturnOrderDocumentService(returnOrderDocumentService);
            returnOrderIndexRunnable.setReturnOrderDocumentConverter(returnOrderDocumentConverter);
            executorService.execute(returnOrderIndexRunnable);
        }
    }

    @Override
    public void updatePaymentData(InvoiceKafkaData invoiceKafkaData) {
        ReturnOrderDocument returnOrderDocument = returnOrderDocumentService.findByIdAndCompanyId(Long.valueOf(invoiceKafkaData.getReferId()), invoiceKafkaData.getCompanyId());
        if (returnOrderDocument == null) {
            ErrorCodes errorCodes = ErrorCodes.INVALID_RETURN_ORDER_ID;
            throw new ServiceException(errorCodes.code(), errorCodes.message(), errorCodes.httpStatus());
        }

        List<PaymentTransactionData> dataList = returnOrderDocument.getPaymentTransactions();
        if (BillStatus.VERIFIED.code().equals(invoiceKafkaData.getStatus())) {
            PaymentTransactionData paymentTransactionData = new PaymentTransactionData();
            paymentTransactionData.setInvoiceId(invoiceKafkaData.getInvoiceId());
            paymentTransactionData.setMoneySourceType(invoiceKafkaData.getMoneySourceType());
            paymentTransactionData.setMoneySourceId(invoiceKafkaData.getMoneySourceId());
            paymentTransactionData.setReturnOrderId(returnOrderDocument.getId());
            paymentTransactionData.setType(invoiceKafkaData.getMoneySourceType());
            paymentTransactionData.setPaymentMethodId(invoiceKafkaData.getPaymentMethodId());
            paymentTransactionData.setAmount(invoiceKafkaData.getAmount());
            dataList.add(paymentTransactionData);
        } else {
            dataList.stream().filter(p -> p.getInvoiceId() == null || !p.getInvoiceId().equals(invoiceKafkaData.getInvoiceId())).collect(Collectors.toList());
        }

        returnOrderDocument.setPaymentTransactions(dataList);
        returnOrderDocumentService.save(returnOrderDocument);
        LOGGER.info("Update payment info data: returnOrderId: {}", returnOrderDocument.getId());
    }

    @Override
    public void updateSkuOrName(ProductInfoKafkaData productInfoKafkaData) {
        Pageable pageable = PageRequest.of(0, 100, Sort.Direction.ASC, "creationTime");
        ReturnOrderSearchRequest request = new ReturnOrderSearchRequest();
        request.setCompanyId(productInfoKafkaData.getCompanyId());
        request.setProduct(String.valueOf(productInfoKafkaData.getId()));
        while (true) {
            Page<ReturnOrderDocument> page = this.search(request, pageable);
            List<ReturnOrderDocument> returnOrderDocuments = page.getContent();
            if (CollectionUtils.isEmpty(returnOrderDocuments)) {
                LOGGER.debug("FINISH UPDATE SKU OR NAME OF RETURN ORDER: {} items", page.getTotalElements());
                break;
            }

            for (ReturnOrderDocument returnOrderDocument : returnOrderDocuments) {
                ReturnOrderBill bill = returnOrderDocument.getBill();
                updateProductSkuOfBill(productInfoKafkaData, bill);
                ExchangeOrder exchangeOrder = returnOrderDocument.getExchangeOrder();
                updateProductSkuOfExchangeOrder(productInfoKafkaData, exchangeOrder);

            }

            returnOrderDocumentService.saveAll(returnOrderDocuments);
            pageable = pageable.next();
        }
    }

    @Override
    public void indexOrderSource(OrderData orderData) {
        if (!OrderType.ONLINE.toString().equals(orderData.getOrderType()) || orderData.isExchange()) {
            return;
        }

        String orderCode = orderData.getOrderCode();
        OrderModel orderModel = orderService.findByCodeAndCompanyId(orderCode, orderData.getCompanyId());
        if (orderModel == null) {
            return;
        }

        List<ReturnOrderModel> returnOrderModels = returnOrderService.findAllByOriginOrder(orderModel);
        if (CollectionUtils.isEmpty(returnOrderModels)) {
            return;
        }
        List<ReturnOrderDocument> updateDocuments = new ArrayList<>();
        for (ReturnOrderModel model : returnOrderModels) {
            ReturnOrderDocument document = returnOrderDocumentService.findById(model.getId());
            if (document != null) {
                document.setOriginOrderSourceId(orderData.getOrderSourceId());
                document.setOriginOrderSourceName(orderData.getOrderSourceName());
                updateDocuments.add(document);
            }
        }
        returnOrderDocumentService.saveAll(updateDocuments);
    }

    private void updateProductSkuOfExchangeOrder(ProductInfoKafkaData productInfoKafkaData, ExchangeOrder exchangeOrder) {
        if (exchangeOrder != null && CollectionUtils.isNotEmpty(exchangeOrder.getEntries())) {
            exchangeOrder.getEntries().stream().forEach(e -> {
                if (e.getProductId().equals(productInfoKafkaData.getId())) {
                    e.setProductSku(productInfoKafkaData.getSku());
                    e.setProductName(productInfoKafkaData.getName());
                    e.setSupplierProductName(productInfoKafkaData.getSupplierProductName());
                    e.setName(VNCharacterUtils.removeAccent(productInfoKafkaData.getName()));
                }
            });
        }
    }

    private void updateProductSkuOfBill(ProductInfoKafkaData productInfoKafkaData, ReturnOrderBill bill) {
        if (bill != null && CollectionUtils.isNotEmpty(bill.getEntries())) {
            bill.getEntries().stream().forEach(e -> {
                if (e.getProductId().equals(productInfoKafkaData.getId())) {
                    e.setProductSku(productInfoKafkaData.getSku());
                    e.setProductName(productInfoKafkaData.getName());
                    e.setName(VNCharacterUtils.removeAccent(productInfoKafkaData.getName()));
                    e.setSupplierProductName(productInfoKafkaData.getSupplierProductName());
                }
            });
        }
    }

    @Override
    public OrderSearchExcelData exportExcelListReturnOrder(ReturnOrderSearchRequest request) {
        Pageable pageableRequest = PageRequest.of(0, MAX_PAGE_SIZE, new Sort(Sort.Direction.DESC, "id"));
        if (StringUtils.isNotBlank(request.getSortField())) {
            if (Sort.Direction.DESC.toString().equalsIgnoreCase(request.getSortOrder())) {
                pageableRequest = PageRequest.of(0, MAX_PAGE_SIZE, new Sort(Sort.Direction.DESC, request.getSortField()));
            } else {
                pageableRequest = PageRequest.of(0, MAX_PAGE_SIZE, new Sort(Sort.Direction.ASC, request.getSortField()));
            }
        }
        List<ReturnOrderDocument> documentList = new ArrayList<>();
        while (true) {
            Page<ReturnOrderDocument> modelPage = search(request, pageableRequest);
            if (CollectionUtils.isEmpty(modelPage.getContent())) {
                break;
            }
            documentList.addAll(modelPage.getContent());
            pageableRequest = pageableRequest.next();
        }

        OrderSearchExcelData orderSearchExcelData = exportExcelOrder(documentList, request);

        orderSearchExcelData.setContent(generateExcelData(orderSearchExcelData));
        return orderSearchExcelData;
    }

    private byte[] generateExcelData(OrderSearchExcelData dataExcel) {
        ClassPathResource resource = new ClassPathResource("templates/return_order_list.xls");
        try (InputStream is = resource.getInputStream()) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                Context context = new Context();
                context.putVar("data", dataExcel.getOrderEntryExcelData());
                JxlsHelper.getInstance().processTemplate(is, os, context);
                return os.toByteArray();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new byte[0];
    }

    private OrderSearchExcelData exportExcelOrder(List<ReturnOrderDocument> documentList, ReturnOrderSearchRequest request) {
        OrderSearchExcelData excelData = new OrderSearchExcelData();

        List<OrderEntryExcelData> orderEntryExcelDataList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(documentList)) {
            List<WarehouseData> warehouseDataList = billClient.getWarehouseByCompany(request.getCompanyId());

            Map<Long, WarehouseData> warehouseDataMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(warehouseDataList)) {
                warehouseDataMap = warehouseDataList.stream().collect(Collectors.toMap(WarehouseData::getId, Function.identity()));
            }

            Map<Long, WarehouseData> finalWarehouseDataMap = warehouseDataMap;
            documentList.forEach(order -> {
                ReturnOrderBill returnOrderBill = order.getBill();
                ExchangeOrder exchangeOrder = order.getExchangeOrder();

                if (returnOrderBill != null && CollectionUtils.isNotEmpty(returnOrderBill.getEntries())) {
                    String warehouseName = populateWarehouseName(finalWarehouseDataMap, returnOrderBill.getWarehouseId());

                    returnOrderBill.getEntries().forEach(product -> {
                        OrderEntryExcelData entryExcelData = populateRowExcelData(warehouseName, order, product);
                        entryExcelData.setType("Trả hàng");
                        orderEntryExcelDataList.add(entryExcelData);
                    });
                }

                if (exchangeOrder != null && CollectionUtils.isNotEmpty(exchangeOrder.getEntries())) {
                    String warehouseNameExchange = populateWarehouseName(finalWarehouseDataMap, exchangeOrder.getWarehouseId());

                    exchangeOrder.getEntries().forEach(product -> {
                        OrderEntryExcelData entryExcelExchange = populateRowExcelData(warehouseNameExchange, order, product);
                        entryExcelExchange.setType("Đổi hàng");
                        orderEntryExcelDataList.add(entryExcelExchange);
                    });
                }
            });
        }

        excelData.setOrderEntryExcelData(orderEntryExcelDataList);
        return excelData;
    }

    private String populateWarehouseName(Map<Long, WarehouseData> finalWarehouseDataMap, Long warehouseId) {
        String warehouseName = "";
        if (finalWarehouseDataMap.containsKey(warehouseId)) {
            warehouseName = finalWarehouseDataMap.get(warehouseId).getName();
        }

        return warehouseName;
    }

    private OrderEntryExcelData populateRowExcelData(String warehouseName, ReturnOrderDocument order, ReturnOrderEntry product) {
        OrderEntryExcelData excelData = new OrderEntryExcelData();
        excelData.setId(order.getId().toString());
        excelData.setCreatedTime(CommonUtils.dateToStr(order.getCreationTime(), "dd-MM-yyyy"));
        excelData.setCreatedName(order.getEmployeeName());
        if (order.getOriginOrder() != null) {
            excelData.setOriginOrder(order.getOriginOrder().getCode());
            excelData.setCustomerName(order.getOriginOrder().getCustomerName());
            excelData.setCustomerPhone(order.getOriginOrder().getCustomerPhone());
        }
        excelData.setNote(order.getNote());
        excelData.setOriginOrderSourceName(order.getOriginOrderSourceName());
        excelData.setWarehouseName(warehouseName);
        excelData.setProductId(product.getProductId());
        excelData.setSku(product.getProductSku());
        excelData.setName(product.getProductName());
        excelData.setSupplierProductName(product.getSupplierProductName());
        excelData.setPrice(product.getPrice());
        excelData.setQuantity(product.getQuantity());
        excelData.setDiscount(product.getDiscount());
        excelData.setProductVat(getProductVat(product));

        double returnCost = order.getBill().getFinalPrice();
        excelData.setOrderValue(returnCost);
        ExchangeOrder exchangeOrder = order.getExchangeOrder();
        double exchangeCost = 0;
        if (exchangeOrder != null) {
            exchangeCost = CommonUtils.readValue(exchangeOrder.getFinalPrice());
            if (exchangeOrder.getVatExchange() != null) {
                exchangeCost = CommonUtils.readValue(exchangeOrder.getFinalPrice()) - CommonUtils.readValue(exchangeOrder.getVatExchange());
            }
        }
        excelData.setOrderValue(exchangeCost - returnCost);

        excelData.setVat(order.getVat() != null ? order.getVat() : 0);
        excelData.setVatExchange(exchangeOrder !=null ? CommonUtils.readValue(exchangeOrder.getVatExchange()):0);
        excelData.setShippingFee(order.getShippingFee() != null ? order.getShippingFee() : 0);
        excelData.setFinalPrice(order.getAmount());

        return excelData;
    }

    private String getProductVat(ReturnOrderEntry product) {
        if(product.getProductVat() == null) {
            return StringUtils.EMPTY;
        }

        if(product.getProductVat() == com.vctek.orderservice.util.CommonUtils.PRODUCT_NO_VAT) {
            return com.vctek.orderservice.util.CommonUtils.PRODUCT_NO_VAT_NAME;
        }

        return String.valueOf(product.getProductVat());
    }

    @Value("${vctek.elasticsearch.index.orders.numberOfThread:5}")
    public void setNumberOfThread(int numberOfThread) {
        this.numberOfThread = numberOfThread;
    }

    @Value("${vctek.elasticsearch.index.orders.bulkSize:100}")
    public void setBulkSize(int bulkSize) {
        this.bulkSize = bulkSize;
    }

    @Autowired
    public void setReturnOrderService(ReturnOrderService returnOrderService) {
        this.returnOrderService = returnOrderService;
    }

    @Autowired
    public void setReturnOrderDocumentService(ReturnOrderDocumentService returnOrderDocumentService) {
        this.returnOrderDocumentService = returnOrderDocumentService;
    }

    @Autowired
    public void setReturnOrderDocumentConverter(Converter<ReturnOrderModel, ReturnOrderDocument> returnOrderDocumentConverter) {
        this.returnOrderDocumentConverter = returnOrderDocumentConverter;
    }

    @Autowired
    @Qualifier("exchangeOrderDocumentPopulator")
    public void setExchangeOrderDocumentPopulator(Populator<OrderModel, ExchangeOrder> exchangeOrderDocumentPopulator) {
        this.exchangeOrderDocumentPopulator = exchangeOrderDocumentPopulator;
    }

    @Autowired
    public void setBillClient(BillClient billClient) {
        this.billClient = billClient;
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }
}
