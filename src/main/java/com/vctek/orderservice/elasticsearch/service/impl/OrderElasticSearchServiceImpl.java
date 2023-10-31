package com.vctek.orderservice.elasticsearch.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.kafka.data.InvoiceKafkaData;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.request.OrderSearchRequest;
import com.vctek.orderservice.elasticsearch.model.OrderEntryData;
import com.vctek.orderservice.elasticsearch.model.OrderSearchModel;
import com.vctek.orderservice.elasticsearch.model.PaymentTransactionData;
import com.vctek.orderservice.elasticsearch.repository.OrderSearchRepository;
import com.vctek.orderservice.elasticsearch.service.OrderElasticSearchService;
import com.vctek.orderservice.feignclient.BillClient;
import com.vctek.orderservice.feignclient.FinanceClient;
import com.vctek.orderservice.feignclient.LogisticClient;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.OrderSourceModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.model.TagModel;
import com.vctek.orderservice.service.OrderSourceService;
import com.vctek.orderservice.service.ReturnOrderService;
import com.vctek.orderservice.service.TagService;
import com.vctek.orderservice.util.ElasticSearchIndex;
import com.vctek.orderservice.util.ProductDType;
import com.vctek.util.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class OrderElasticSearchServiceImpl extends BulkIndexElasticServiceImpl implements OrderElasticSearchService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderElasticSearchServiceImpl.class);
    public static final String ERROR_PARSE_JSON_INDEX_ORDER = "Index error: orderId ({0}), error ({1})";
    private Converter<OrderModel, OrderSearchModel> orderSearchModelConverter;
    private Populator<OrderModel, OrderSearchModel> populator;
    private OrderSearchRepository orderSearchRepository;
    private ReturnOrderService returnOrderService;
    private BillClient billClient;
    private OrderSourceService orderSourceService;
    private FinanceClient financeClient;
    private LogisticClient logisticClient;
    private ObjectMapper objectMapper;
    private TagService tagService;
    private Converter<TagModel, TagData> tagDataConverter;

    @Autowired
    public OrderElasticSearchServiceImpl(OrderSearchRepository orderSearchRepository,
                                         ElasticsearchTemplate elasticsearchTemplate) {
        super(elasticsearchTemplate);
        this.orderSearchRepository = orderSearchRepository;
    }

    @Override
    public void save(OrderSearchModel model) {
        orderSearchRepository.save(model);
    }

    @Override
    public Page<OrderSearchModel> search(SearchQuery query) {
        return orderSearchRepository.search(query);
    }

    @Override
    public void bulkOrderIndex(List<OrderSearchModel> models) {
        String indexName = this.getIndexName();
        super.createIndexIfNotExisted(indexName);

        List<IndexQuery> queries = new ArrayList<>();
        for (OrderSearchModel model : models) {
            IndexQuery indexQuery = new IndexQuery();
            indexQuery.setId(model.getId() == null ? null : model.getId());
            indexQuery.setIndexName(indexName);
            indexQuery.setObject(model);
            queries.add(indexQuery);
        }

        if (CollectionUtils.isNotEmpty(queries)) {
            elasticsearchTemplate.bulkIndex(queries);
            elasticsearchTemplate.refresh(indexName);
        }
    }

    @Override
    public Optional<OrderSearchModel> findById(String orderCode) {
        return orderSearchRepository.findById(orderCode);
    }

    @Override
    public void saveAll(List<OrderSearchModel> orderSearchModels) {
        orderSearchRepository.saveAll(orderSearchModels);
    }

    @Override
    public void updatePaymentTransactionDataAndPaidAmount(OrderModel model, InvoiceKafkaData kafkaData) {
        Optional<OrderSearchModel> optional = findById(model.getCode());
        if (optional.isPresent()) {
            OrderSearchModel searchModel = optional.get();
            searchModel.setPaidAmount(model.getPaidAmount());

            List<PaymentTransactionData> dataList = searchModel.getPaymentTransactionData();
            if (BillStatus.VERIFIED.code().equals(kafkaData.getStatus())) {
                PaymentTransactionData data = new PaymentTransactionData();
                data.setInvoiceId(kafkaData.getInvoiceId());
                data.setPaymentMethodId(kafkaData.getPaymentMethodId());
                data.setAmount(kafkaData.getAmount());
                data.setMoneySourceId(kafkaData.getMoneySourceId());
                data.setMoneySourceType(kafkaData.getMoneySourceType());
                dataList.add(data);
            } else {
                dataList = dataList.stream().filter(p -> p.getInvoiceId() == null || !p.getInvoiceId().equals(kafkaData.getInvoiceId()))
                        .collect(Collectors.toList());
            }
            searchModel.setPaymentTransactionData(dataList);

            save(searchModel);
        }
    }

    @Override
    public Page<OrderSearchModel> findAll(Pageable pageable) {
        return orderSearchRepository.findAll(pageable);
    }

    @Override
    public void indexReturnOrderIds(OrderModel orderModel) {
        Optional<OrderSearchModel> optionalModel = this.findById(orderModel.getCode());
        if (!optionalModel.isPresent()) {
            return;
        }
        OrderSearchModel target = optionalModel.get();
        List<ReturnOrderModel> returnOrders = returnOrderService.findAllByOriginOrder(orderModel);
        if (CollectionUtils.isNotEmpty(returnOrders)) {
            List<Long> returnOrderIds = returnOrders.stream().map(ro -> ro.getId()).collect(Collectors.toList());
            target.setReturnOrderIds(returnOrderIds);
        } else {
            target.setReturnOrderIds(Collections.emptyList());
        }

        this.save(target);
    }

    @Override
    protected Class getClassIndex() {
        return OrderSearchModel.class;
    }

    @Override
    public String getIndexName() {
        return ElasticSearchIndex.ORDER_INDEX;
    }

    @Override
    public OrderSearchExcelData exportExcelOrder(List<OrderSearchModel> models, OrderSearchRequest request) {
        OrderSearchExcelData excelData = new OrderSearchExcelData();
        List<OrderEntryExcelData> orderEntryExcelDataList = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(models)) {
            List<WarehouseData> warehouseDataList = billClient.getWarehouseByCompany(request.getCompanyId());
            List<OrderSourceModel> orderSourceModels = orderSourceService.findAllByCompanyId(request.getCompanyId());
            populatePaymentMethodData(excelData, request);
            List<ShippingCompanyData> shippingCompanyData = logisticClient.getShippingCompanyByCompany(request.getCompanyId());

            String companyName = null;
            Map<Long, WarehouseData> warehouseDataMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(warehouseDataList)) {
                companyName = warehouseDataList.get(0).getCompanyName();
                warehouseDataMap = warehouseDataList.stream().collect(Collectors.toMap(WarehouseData::getId, Function.identity()));
            }

            Map<Long, OrderSourceModel> orderSourceModelMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(orderSourceModels)) {
                orderSourceModelMap = orderSourceModels.stream().collect(Collectors.toMap(OrderSourceModel::getId, Function.identity()));
            }
            Map<Long, String> shippingCompanyDataMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(shippingCompanyData)) {
                shippingCompanyDataMap = shippingCompanyData.stream().collect(Collectors.toMap(i -> i.getId(), i -> i.getName()));
            }
            for (OrderSearchModel order : models) {
                populateRowData(orderEntryExcelDataList, companyName, warehouseDataMap, orderSourceModelMap, shippingCompanyDataMap, order);
            }
        }

        excelData.setOrderEntryExcelData(orderEntryExcelDataList);
        return excelData;
    }

    private void populatePaymentMethodData(OrderSearchExcelData excelData, OrderSearchRequest request) {
        List<PaymentMethodData> paymentMethodData = financeClient.getPaymentMethodDataByCompanyId(request.getCompanyId());
        if (CollectionUtils.isNotEmpty(paymentMethodData)) {
            List<PaymentMethodData> dataList;
            if (OrderType.RETAIL.name().equals(request.getOrderType()) || OrderType.ONLINE.name().equals(request.getOrderType())) {
                dataList = paymentMethodData.stream().filter(i -> !PaymentMethodType.CASH.code().equals(i.getCode())).collect(Collectors.toList());
            } else {
                dataList = paymentMethodData.stream().filter(i -> !PaymentMethodType.CASH.code().equals(i.getCode())
                        && !PaymentMethodType.LOYALTY_POINT.code().equals(i.getCode())).collect(Collectors.toList());
            }
            excelData.setPaymentMethod(dataList);
        }
    }

    @Override
    public void indexOrder(OrderModel orderModel) {
        try {
            OrderSearchModel orderSearchModel;
            if (orderModel.isImportOrderProcessing()) {
                //Kafka change status order
                orderSearchModel = findByIdAndCompanyId(orderModel.getCode(), orderModel.getCompanyId());
                populator.populate(orderModel, orderSearchModel);
                orderSearchModel.setOrderRetailCode(orderModel.getOrderRetailCode());
            } else {
                orderSearchModel = orderSearchModelConverter.convert(orderModel);
            }
            this.save(orderSearchModel);
            LOGGER.debug("Index: orderCode: {}, status: {}", orderModel.getCode(), orderModel.getOrderStatus());
        } catch (RuntimeException e) {
            LOGGER.error("INDEX ORDER ERROR: " + orderModel.getCode() + ", " + e.getMessage(), e);
        }
    }

    private void populateRowData(List<OrderEntryExcelData> orderEntryExcelDataList, String
            companyName, Map<Long, WarehouseData> warehouseDataMap, Map<Long, OrderSourceModel> orderSourceModelMap, Map<Long, String> shippingCompanyDataMap, OrderSearchModel
                                         order) {
        int index = 0;
        String finalAddress = populateFullAddress(order);
        for (OrderEntryData product : order.getOrderEntries()) {
            if (!ProductDType.COMBO_MODEL.code().equals(product.getdType())) {
                OrderEntryExcelData entryExcelData = populateRowExcelData(warehouseDataMap, orderSourceModelMap, companyName, order, finalAddress, shippingCompanyDataMap, product);
                if (index == 0) {
                    populateFirstRowExcel(entryExcelData, order);
                }
                orderEntryExcelDataList.add(entryExcelData);
                index++;
                continue;
            }

            for (OrderEntryData subProduct : product.getSubOrderEntries()) {
                OrderEntryExcelData subEntryExcelData = populateRowExcelData(warehouseDataMap, orderSourceModelMap, companyName, order, finalAddress, shippingCompanyDataMap, subProduct);
                subEntryExcelData.setComboName(product.getName());
                subEntryExcelData.setComboSku(product.getSku());

                if (index == 0) {
                    populateFirstRowExcel(subEntryExcelData, order);
                }
                index++;
                orderEntryExcelDataList.add(subEntryExcelData);
            }
        }
    }

    private OrderEntryExcelData populateRowExcelData(Map<Long, WarehouseData> finalWarehouseDataMap,
                                                     Map<Long, OrderSourceModel> finalOrderSourceModelMap, String finalCompanyName,
                                                     OrderSearchModel order, String finalAddress,
                                                     Map<Long, String> shippingCompanyDataMap, OrderEntryData product) {

        OrderEntryExcelData excelData = new OrderEntryExcelData();
        excelData.setCompanyName(finalCompanyName);
        excelData.setWarehouseName(populateWarehouseName(finalWarehouseDataMap, order));
        excelData.setAddress(finalAddress);
        excelData.setOrderSource(populateOrderSource(finalOrderSourceModelMap, order));
        excelData.setOrderStatus(populateOrderStatus(order));
        excelData.setCancelReason(order.getCancelReason());
        excelData.setShippingCompanyName(populateShippingCompanyName(shippingCompanyDataMap, order));
        populateExcelOrderData(order, excelData);

        populateProductOrderExcelData(product, excelData);

        return excelData;
    }

    private void populateFirstRowExcel(OrderEntryExcelData data, OrderSearchModel
            order) {
        data.setFinalDiscount(order.getTotalDiscount());
        data.setTotalTax(order.getTotalTax());
        data.setDeliveryCost(CommonUtils.readValue(order.getDeliveryCost()));
        data.setFinalPrice(order.getFinalPrice());
        data.setTotalPriceOrder(order.getTotalPrice());
        data.setRedeemAmount(order.getRedeemAmount());

        populatePayments(data, order);
        populateTags(data, order);
        populateFinishedProductImage(data, order);
    }

    private void populateFinishedProductImage(OrderEntryExcelData data, OrderSearchModel order) {
        if (!order.isFinishedProduct() || StringUtils.isBlank(order.getImages())) return;
        try {
            List<OrderImageData> orderImageData = objectMapper.readValue(order.getImages(), new TypeReference<List<OrderImageData>>() {
            });
            List<String> images = orderImageData.stream().filter(i -> i.isFinishedProduct()).map(i -> i.getUrl()).collect(Collectors.toList());
            data.setFinishedProductImages(StringUtils.join(images, ","));
        } catch (IOException e) {
            LOGGER.error("CANNOT READ IMAGE ORDER: {}", order.getId());
        }
    }

    private void populateTags(OrderEntryExcelData data, OrderSearchModel order) {
        if (CollectionUtils.isEmpty(order.getTags())) return;
        List<String> tags = order.getTags().stream().map(i -> i.getName()).collect(Collectors.toList());
        data.setTags(StringUtils.join(tags, ","));
    }

    private void populateExcelOrderData(OrderSearchModel order, OrderEntryExcelData data) {
        data.setCreatedTime(CommonUtils.dateToStr(order.getCreatedTime(), "dd-MM-yyyy"));
        data.setType(populateOrderType(order.getOrderType()));
        data.setId(order.getCode());
        data.setCreatedName(order.getCreatedName());
        data.setCustomerName(order.getCustomerName());
        data.setCustomerPhone(order.getCustomerPhone());
        data.setCustomerNote(order.getCustomerNote());

        if (OrderStatus.COMPLETED.code().equals(order.getOrderStatus())) {
            data.setCompleteDate(CommonUtils.dateToStr(order.getModifiedTimeLastStatus(), "dd-MM-yyyy"));
        }
    }

    private String populateOrderStatus(OrderSearchModel order) {
        OrderStatus orderStatus = OrderStatus.findByCode(order.getOrderStatus());
        if (orderStatus != null) {
            return orderStatus.description();
        }
        return "";
    }

    private String populateOrderSource(Map<Long, OrderSourceModel> finalOrderSourceModelMap, OrderSearchModel
            order) {
        String orderSource = "";
        if (finalOrderSourceModelMap.containsKey(order.getOrderSourceId())) {
            orderSource = finalOrderSourceModelMap.get(order.getOrderSourceId()).getName();
        }

        return orderSource;
    }

    private String populateWarehouseName(Map<Long, WarehouseData> finalWarehouseDataMap, OrderSearchModel order) {
        String warehouseName = "";
        if (finalWarehouseDataMap.containsKey(order.getWarehouseId())) {
            warehouseName = finalWarehouseDataMap.get(order.getWarehouseId()).getName();
        }

        return warehouseName;
    }

    private String populateShippingCompanyName(Map<Long, String> shippingCompanyDataMap, OrderSearchModel order) {
        String shippingCompanyName = "";
        if (shippingCompanyDataMap.containsKey(order.getShippingCompanyId())) {
            shippingCompanyName = shippingCompanyDataMap.get(order.getShippingCompanyId());
        }
        return shippingCompanyName;
    }

    private void populateProductOrderExcelData(OrderEntryData product, OrderEntryExcelData data) {
        data.setProductId(product.getId());
        data.setBarcode(product.getBarcode());
        data.setSku(product.getSku());
        data.setName(product.getName());
        data.setSupplierProductName(product.getSupplierProductName());
        data.setPrice(product.getPrice());
        data.setQuantity(product.getQuantity());
        data.setTotalPrice(CommonUtils.readValue(product.getPrice()) * CommonUtils.readValue(product.getQuantity()));
        data.setDiscount(product.getFinalDiscount());
        data.setProductVat(getProductVat(product));
    }

    private String getProductVat(OrderEntryData product) {
        if(product.getVat() == null) {
            return StringUtils.EMPTY;
        }

        if(product.getVat() == com.vctek.orderservice.util.CommonUtils.PRODUCT_NO_VAT) {
            return com.vctek.orderservice.util.CommonUtils.PRODUCT_NO_VAT_NAME;
        }

        return String.valueOf(product.getVat());
    }

    private void populatePayments(OrderEntryExcelData data, OrderSearchModel
            order) {
        List<PaymentTransactionData> paymentTransactionData = order.getPaymentTransactionData();
        Map<Long, Double> mapPaymentData = new HashMap<>();
        Double customerCash = 0d;
        if (CollectionUtils.isNotEmpty(paymentTransactionData)) {
            for (PaymentTransactionData i : paymentTransactionData) {
                if (MoneySourceType.CASH.name().equals(i.getMoneySourceType())) {
                    customerCash += CommonUtils.readValue(i.getAmount());
                    continue;
                }

                if (!mapPaymentData.containsKey(i.getPaymentMethodId())) {
                    mapPaymentData.put(i.getPaymentMethodId(), CommonUtils.readValue(i.getAmount()));
                    continue;
                }

                Double aDouble = mapPaymentData.get(i.getPaymentMethodId());
                aDouble = aDouble + CommonUtils.readValue(i.getAmount());
                mapPaymentData.replace(i.getPaymentMethodId(), aDouble);
            }
        }

        data.setCustomerCash(customerCash);
        if (OrderType.RETAIL.name().equals(order.getOrderType()) || OrderType.WHOLESALE.name().equals(order.getOrderType())) {
            Double totalNotCash = mapPaymentData.values().stream().mapToDouble(Double::doubleValue).sum();
            data.setCash(order.getFinalPrice() - totalNotCash);
        }

        data.setPaymentMethodAmount(mapPaymentData);
    }

    private String populateFullAddress(OrderSearchModel order) {
        StringJoiner address = new StringJoiner(" - ");
        if (StringUtils.isNotBlank(order.getAddress())) {
            address.add(order.getAddress());
        }
        if (StringUtils.isNotEmpty(order.getWardName())) {
            address.add(order.getWardName());
        }
        if (StringUtils.isNotEmpty(order.getDistrictName())) {
            address.add(order.getDistrictName());
        }
        if (StringUtils.isNotEmpty(order.getProvinceName())) {
            address.add(order.getProvinceName());
        }
        return address.toString();
    }

    public String populateOrderType(String orderType) {
        String typeName = "";
        if (OrderType.ONLINE.name().equals(orderType)) {
            typeName = "Đơn hàng";
        }
        if (OrderType.WHOLESALE.name().equals(orderType)) {
            typeName = "Hóa đơn bán buôn";
        }
        if (OrderType.RETAIL.name().equals(orderType)) {
            typeName = "Hóa đơn bán lẻ";
        }

        return typeName;
    }

    @Override
    public OrderSearchModel findByIdAndCompanyId(String orderCode, Long companyId) {
        return orderSearchRepository.findByIdAndCompanyId(orderCode, companyId);
    }

    @Override
    public void bulkIndexOrderEntries(List<OrderSearchModel> orderSearchModels) {
        String indexName = getIndexName();
        List<UpdateQuery> queries = new ArrayList<>();
        orderSearchModels.forEach(model -> {
            UpdateQuery updateQuery = createUpdateQueryFrom(indexName, model);
            queries.add(updateQuery);
        });

        if (CollectionUtils.isNotEmpty(queries)) {
            elasticsearchTemplate.bulkUpdate(queries);
        }
    }

    @Override
    public void bulkIndexCustomerName(List<OrderSearchModel> orderSearchModels) {
        String indexName = getIndexName();
        List<UpdateQuery> queries = new ArrayList<>();
        orderSearchModels.forEach(model -> {
            UpdateQuery updateQuery = createUpdateCustomerNameQueryFrom(indexName, model);
            queries.add(updateQuery);
        });

        if (CollectionUtils.isNotEmpty(queries)) {
            elasticsearchTemplate.bulkUpdate(queries);
        }
    }

    @Override
    public void indexTags(OrderModel orderModel) {
        String indexName = getIndexName();
        List<UpdateQuery> queries = new ArrayList<>();
        UpdateQuery updateQuery = new UpdateQuery();
        updateQuery.setClazz(OrderSearchModel.class);
        updateQuery.setIndexName(indexName);
        updateQuery.setDoUpsert(true);
        updateQuery.setId(orderModel.getCode());
        UpdateRequest updateRequest = new UpdateRequest(updateQuery.getIndexName(), updateQuery.getType(), updateQuery.getId());
        List<TagModel> tagModels = tagService.findAllByOrder(orderModel);
        List<TagData> tagDataList = tagDataConverter.convertAll(tagModels);
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("tags", tagDataList);
            updateRequest.doc(objectMapper.writeValueAsString(map), XContentType.JSON);
        } catch (JsonProcessingException e) {
            LOGGER.error(MessageFormat.format(ERROR_PARSE_JSON_INDEX_ORDER, orderModel.getCode(),
                    e.getMessage()), e);
        }
        updateQuery.setUpdateRequest(updateRequest);
        queries.add(updateQuery);

        if (CollectionUtils.isNotEmpty(queries)) {
            elasticsearchTemplate.bulkUpdate(queries);
        }
    }

    private UpdateQuery createUpdateCustomerNameQueryFrom(String indexName, OrderSearchModel model) {
        UpdateQuery updateQuery = new UpdateQuery();
        updateQuery.setClazz(OrderSearchModel.class);
        updateQuery.setIndexName(indexName);
        updateQuery.setDoUpsert(true);
        updateQuery.setId(model.getId() == null ? null : model.getId());
        UpdateRequest updateRequest = new UpdateRequest(updateQuery.getIndexName(), updateQuery.getType(), updateQuery.getId());
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("customerName", model.getCustomerName());
            String source = objectMapper.writeValueAsString(map);
            updateRequest.doc(source, XContentType.JSON);
        } catch (JsonProcessingException e) {
            LOGGER.error(MessageFormat.format(ERROR_PARSE_JSON_INDEX_ORDER, model.getId(),
                    e.getMessage()), e);
        }
        updateQuery.setUpdateRequest(updateRequest);
        return updateQuery;
    }

    private UpdateQuery createUpdateQueryFrom(String indexName, OrderSearchModel model) {
        UpdateQuery updateQuery = new UpdateQuery();
        updateQuery.setClazz(OrderSearchModel.class);
        updateQuery.setIndexName(indexName);
        updateQuery.setDoUpsert(true);
        updateQuery.setId(model.getId() == null ? null : model.getId());
        UpdateRequest updateRequest = new UpdateRequest(updateQuery.getIndexName(), updateQuery.getType(), updateQuery.getId());
        try {
            String source = buildSourceFrom(model);
            updateRequest.doc(source, XContentType.JSON);
        } catch (JsonProcessingException e) {
            LOGGER.error(MessageFormat.format(ERROR_PARSE_JSON_INDEX_ORDER, model.getId(),
                    e.getMessage()), e);
        }
        updateQuery.setUpdateRequest(updateRequest);
        return updateQuery;
    }

    private String buildSourceFrom(OrderSearchModel model) throws JsonProcessingException {
        Map<String, Object> map = new HashMap<>();
        map.put("orderEntries", model.getOrderEntries());
        return objectMapper.writeValueAsString(map);
    }

    @Autowired
    public void setBillClient(BillClient billClient) {
        this.billClient = billClient;
    }

    @Autowired
    public void setOrderSourceService(OrderSourceService orderSourceService) {
        this.orderSourceService = orderSourceService;
    }

    @Autowired
    public void setFinanceClient(FinanceClient financeClient) {
        this.financeClient = financeClient;
    }

    @Autowired
    public void setReturnOrderService(ReturnOrderService returnOrderService) {
        this.returnOrderService = returnOrderService;
    }

    @Autowired
    public void setOrderSearchModelConverter(Converter<OrderModel, OrderSearchModel> orderSearchModelConverter) {
        this.orderSearchModelConverter = orderSearchModelConverter;
    }

    @Autowired
    public void setLogisticClient(LogisticClient logisticClient) {
        this.logisticClient = logisticClient;
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Autowired
    @Qualifier("orderSearchModelHistoryPopulator")
    public void setPopulator(Populator<OrderModel, OrderSearchModel> populator) {
        this.populator = populator;
    }

    @Autowired
    public void setTagService(TagService tagService) {
        this.tagService = tagService;
    }

    @Autowired
    public void setTagDataConverter(Converter<TagModel, TagData> tagDataConverter) {
        this.tagDataConverter = tagDataConverter;
    }
}
