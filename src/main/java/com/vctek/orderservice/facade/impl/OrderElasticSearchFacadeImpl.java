package com.vctek.orderservice.facade.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vctek.converter.Converter;
import com.vctek.dto.PrintSettingData;
import com.vctek.dto.redis.AddressData;
import com.vctek.exception.ServiceException;
import com.vctek.kafka.data.CustomerDto;
import com.vctek.kafka.data.ReturnOrdersDTO;
import com.vctek.kafka.message.KafkaMessageType;
import com.vctek.kafka.producer.ProductInfoKafkaData;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.request.*;
import com.vctek.orderservice.elasticsearch.index.OrderElasticIndexRunnable;
import com.vctek.orderservice.elasticsearch.index.OrderHistoryReportRunnable;
import com.vctek.orderservice.elasticsearch.index.OrderReportRunnable;
import com.vctek.orderservice.elasticsearch.model.OrderEntryData;
import com.vctek.orderservice.elasticsearch.model.OrderSearchModel;
import com.vctek.orderservice.elasticsearch.model.PaymentTransactionData;
import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.OrderElasticSearchService;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.OrderElasticSearchFacade;
import com.vctek.orderservice.facade.PermissionFacade;
import com.vctek.orderservice.feignclient.dto.CustomerData;
import com.vctek.orderservice.feignclient.dto.ProductSearchRequest;
import com.vctek.orderservice.kafka.producer.OrderProducerService;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.util.DateUtil;
import com.vctek.orderservice.util.ExportExcelType;
import com.vctek.orderservice.util.*;
import com.vctek.util.CommonUtils;
import com.vctek.util.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.jxls.common.Context;
import org.jxls.transform.poi.PoiTransformer;
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
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OrderElasticSearchFacadeImpl extends AbstractOrderElasticSearchFacade implements OrderElasticSearchFacade {
    public static final String ORDER_SOURCE_ID = "orderSourceId";
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderElasticSearchFacadeImpl.class);
    public static final String ORDER_ENTRIES = "orderEntries";
    public static final String GENDER = "gender";
    public static final String TAGS = "tags";
    private Converter<OrderModel, OrderSearchModel> orderSearchModelConverter;
    private Converter<OrderModel, OrderSearchModel> orderSearchEntryModelConverter;
    private OrderService orderService;
    private OrderHistoryService orderHistoryService;
    private OrderProducerService producerService;
    private ReturnOrderService returnOrderService;
    private OrderElasticSearchService orderElasticSearchService;
    private LogisticService logisticService;
    private ProductSearchService productSearchService;
    private CRMService crmService;
    private OrderSettingCustomerOptionService settingCustomerOptionService;
    private int numberOfThread;
    private int bulkSize;
    public static final String PRODUCT_ID = "orderEntries.id";
    public static final String WAREHOUSE_ID = "warehouseId";
    public static final String ORDER_STATUS = "orderStatus";
    public static final String MINIMUM_SHOULD_MATCH = "1";
    public static final String PRODUCT_NAME = "orderEntries.stringName";
    public static final String PRODUCT_SKU = "orderEntries.sku";
    public static final String PRODUCT_BARCODE = "orderEntries.barcode";
    public static final String NAME = "orderEntries.name";
    public static final String FINAL_PRICE = "finalPrice";
    public static final String TOTAL_DISCOUNT = "totalDiscount";
    public static final String CURRENT_STATUS = "orderHistoryData.currentStatus";
    public static final String ORDER_HISTORY = "orderHistoryData";
    public static final String MODIFIED_TIME_LAST_STATUS = "modifiedTimeLastStatus";
    public static final String CREATED_NAME = "createdName";
    public static final String CREATED_BY = "createdBy";
    public static final String CUSTOMER_SUPPORT_NOTE = "customerSupportNote";
    public static final String CUSTOMER_NOTE = "customerNote";
    private static final String PAYMENT_TRANSACTIONS = "paymentTransactionData";
    public static final String SUB_ENTRY_PRODUCT_ID = "orderEntries.subOrderEntries.id";
    public static final String SUB_ENTRY_PRODUCT_NAME = "orderEntries.subOrderEntries.stringName";
    public static final String SUB_ENTRY_PRODUCT_SKU = "orderEntries.subOrderEntries.sku";
    public static final String SUB_ENTRY_PRODUCT_BARCODE = "orderEntries.subOrderEntries.barcode";
    public static final String PRICE_TYPE = "priceType";
    public static final String CREATED_TIME = "createdTime";
    public static final String CUSTOMER_OPTION_ID = "settingCustomerOptionData.id";
    public static final String CUSTOMER_OPTIONS = "settingCustomerOptionData";
    public static final String HAS_SALEOFF = "hasSaleOff";
    public static final String ORDER_CODE = "code";
    public static final String SELL_SIGNAL = "sellSignal";
    public static final String COMPANY_ID = "companyId";
    public static final String EXCHANGE = "exchange";
    public static final String DELETED = "deleted";
    private ObjectMapper objectMapper = new ObjectMapper();
    private AuthService authService;
    private PermissionFacade permissionFacade;
    private PaymentTransactionService paymentTransactionService;

    private static final String WILD_CARD_PATTERN = "*%s*";
    private static final int MAX_PAGE_SIZE = 1000;
    private OrderFileService orderFileService;
    private Executor exportExcelExecutor;
    private static final int MAX_ITEMS = 10000;
    @Value("${vctek.config.maximumExcelRow:10000}")
    private int maximumRow = 10000;
    private Converter<OrderSearchRequest, OrderFileParameter> fileParameterConverter;

    public OrderElasticSearchFacadeImpl(
            @Qualifier("orderSearchModelConverter") Converter<OrderModel, OrderSearchModel> orderSearchModelConverter,
            OrderService orderService, OrderElasticSearchService orderElasticSearchService,
            @Value("${vctek.elasticsearch.index.orders.numberOfThread:5}") int numberOfThread,
            @Value("${vctek.elasticsearch.index.orders.bulkSize:300}") int bulkSize) {
        this.orderService = orderService;
        this.orderElasticSearchService = orderElasticSearchService;
        this.orderSearchModelConverter = orderSearchModelConverter;
        this.numberOfThread = numberOfThread;
        this.bulkSize = bulkSize;
    }

    @Override
    public void index(OrderModel orderModel) {
        LOGGER.info("Index: orderId: {}", orderModel.getId());
        OrderSearchModel model = orderSearchModelConverter.convert(orderModel);
        orderElasticSearchService.save(model);
    }

    @Override
    public void fullIndex() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThread);
        for (int i = 0; i < numberOfThread; i++) {
            OrderElasticIndexRunnable orderElasticIndexRunnable = new OrderElasticIndexRunnable(authentication, i, bulkSize, numberOfThread);
            orderElasticIndexRunnable.setOrderElasticSearchService(orderElasticSearchService);
            orderElasticIndexRunnable.setOrderSearchModelConverter(orderSearchModelConverter);
            orderElasticIndexRunnable.setOrderService(orderService);
            executorService.execute(orderElasticIndexRunnable);
        }
    }

    @Override
    public void partialIndex(OrderPartialIndexRequest request) {
        if (request.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        if (CollectionUtils.isNotEmpty(request.getOrderCodes())) {
            List<OrderModel> orders = this.orderService.findByCompanyIdAndOrderCodeIn(request.getCompanyId(), request.getOrderCodes());
            for (OrderModel orderModel : orders) {
                this.index(orderModel);
            }
        } else if (request.getOrderCode() != null) {
            OrderModel orderModel = orderService.findByCodeAndCompanyId(request.getOrderCode(), request.getCompanyId());
            if (orderModel != null) {
                this.index(orderModel);
            }
        } else if (request.getFromDate() != null || CollectionUtils.isNotEmpty(request.getOrderTypes())) {
            this.indexOrderBy(request);
        }
    }

    protected void indexOrderBy(OrderPartialIndexRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThread);
        for (int i = 0; i < numberOfThread; i++) {
            OrderElasticIndexRunnable orderElasticIndexRunnable = new OrderElasticIndexRunnable(authentication, i, bulkSize, numberOfThread) {
                @Override
                public Page<OrderModel> findOrderModel(Pageable pageable) {
                    if (CollectionUtils.isNotEmpty(request.getOrderTypes())) {
                        return orderService.findAllByAndCompanyIdAndOrderTypes(request.getCompanyId(), request.getOrderTypes(), pageable);
                    }
                    return orderService.findAllByAndCompanyIdFromDate(request.getCompanyId(), request.getFromDate(), pageable);
                }
            };
            orderElasticIndexRunnable.setOrderElasticSearchService(orderElasticSearchService);
            orderElasticIndexRunnable.setOrderSearchModelConverter(orderSearchModelConverter);
            orderElasticIndexRunnable.setOrderService(orderService);
            executorService.execute(orderElasticIndexRunnable);
        }
    }

    @Override
    public Page<OrderSearchModel> search(OrderSearchRequest request, Pageable pageableRequest) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery());
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withPageable(pageableRequest);
        boolQueryBuilder.must(QueryBuilders.matchQuery(EXCHANGE, false));
        boolQueryBuilder.must(QueryBuilders.matchQuery(DELETED, false));
        if (request.getId() != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("id", request.getId()));
        }

        if (request.getCompanyId() != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery(COMPANY_ID, request.getCompanyId()));
        }

        populateSearchDate(boolQueryBuilder, request.getFromCreatedTime(),
                request.getToCreatedTime(), ElasticSearchQueryField.ORDER_CREATED_TIME);


        populateSearchDate(boolQueryBuilder, request.getFromModifiedTimeLastStatus(),
                request.getToModifiedTimeLastStatus(), MODIFIED_TIME_LAST_STATUS);

        populateSearchDate(boolQueryBuilder, request.getFromDeliveryDate(),
                request.getToDeliveryDate(), ElasticSearchQueryField.ORDER_DELIVERY_DATE);

        populateSearchDeliveryTime(boolQueryBuilder, request);

        populateFinalPrice(boolQueryBuilder, request);

        populateTotalDiscount(boolQueryBuilder, request);

        populateSearchCreated(boolQueryBuilder, request);

        populateSearchWarehouse(request, boolQueryBuilder);

        String orderType = request.getOrderType();
        if (StringUtils.isNotBlank(orderType)) {
            boolQueryBuilder.must(QueryBuilders.queryStringQuery(QueryParser.escape(orderType)).field("orderType"));
        }


        String customerName = request.getCustomerName();
        if (StringUtils.isNotBlank(customerName)) {
            boolQueryBuilder.must(QueryBuilders.wildcardQuery("customerName", String.format(WILD_CARD_PATTERN, QueryParser.escape(customerName).toLowerCase())));
        }
        String code = request.getCode();
        if (StringUtils.isNotBlank(code)) {
            boolQueryBuilder.must(QueryBuilders.wildcardQuery("code", String.format(WILD_CARD_PATTERN, QueryParser.escape(code))));
        }

        if (StringUtils.isNotBlank(request.getOrderRetailCode())) {
            boolQueryBuilder.must(QueryBuilders.queryStringQuery(QueryParser.escape(request.getOrderRetailCode())).field("orderRetailCode"));
        }

        if (request.getShippingCompanyId() != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("shippingCompanyId", request.getShippingCompanyId()));
        }

        if (StringUtils.isNotBlank(request.getProduct())) {
            populateSearchNestedObject(boolQueryBuilder, request.getProduct(), ORDER_ENTRIES, PRODUCT_ID, SUB_ENTRY_PRODUCT_ID,
                    new String[]{NAME, PRODUCT_NAME, PRODUCT_BARCODE, PRODUCT_SKU, SUB_ENTRY_PRODUCT_BARCODE,
                            SUB_ENTRY_PRODUCT_NAME, SUB_ENTRY_PRODUCT_SKU});
        }

        populateSearchOrderStatus(request, boolQueryBuilder);

        populateSearchOrderSource(request, boolQueryBuilder);
        populateSearchOrderSettingCustomerOption(request, boolQueryBuilder);


        if (request.getCustomerId() != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("customerId", request.getCustomerId()));
        }

        if (request.getAge() != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("age", request.getAge()));
        }

        if (request.getDistributorId() != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("distributorId", request.getDistributorId()));
        }

        if (StringUtils.isNotBlank(request.getPaymentMethodId())) {
            populateSearchPaymentMethod(request, boolQueryBuilder);
        }
        populateSearchStatusHistory(boolQueryBuilder, request);
        populateSearchAddress(boolQueryBuilder, request);
        populateSearchPreOrderAndHolding(boolQueryBuilder, request);

        populateSearchNote(boolQueryBuilder, request);
        populateSearchHasCustomerInfo(boolQueryBuilder, request);
        populateSearchRange(boolQueryBuilder, request.getFromFinalPrice(), request.getToFinalPrice(), FINAL_PRICE);
        populateSearchGender(boolQueryBuilder, request);
        String priceType = request.getPriceType();
        if (StringUtils.isNotBlank(priceType)) {
            boolQueryBuilder.must(QueryBuilders.queryStringQuery(QueryParser.escape(priceType)).field(PRICE_TYPE));
        }
        populateSearchHasDeliveryDate(boolQueryBuilder, request);
        populateSearchHasCustomerShippingFee(boolQueryBuilder, request);
        populateSearchTag(boolQueryBuilder, request);
        populateSearchHasSaleOff(boolQueryBuilder, request);
        return orderElasticSearchService.search(nativeSearchQueryBuilder.withQuery(boolQueryBuilder).build());
    }

    private void populateSearchHasSaleOff(BoolQueryBuilder boolQueryBuilder, OrderSearchRequest request) {
        if (request.getHasSaleOff() == null) return;
        boolQueryBuilder.must(QueryBuilders.matchQuery(HAS_SALEOFF, request.getHasSaleOff()));
    }

    private void populateSearchTag(BoolQueryBuilder boolQueryBuilder, OrderSearchRequest request) {
        if(request.getHasTag() == null) {
            return;
        }

        if (request.getTagId() == null) {
            populateSearchOnlyHasTag(boolQueryBuilder, request);
            return;
        }

        if(Boolean.TRUE.equals(request.getHasTag())) {
            BoolQueryBuilder nestedQuery = new BoolQueryBuilder();
            nestedQuery.must(QueryBuilders.matchQuery("tags.id", request.getTagId()));
            boolQueryBuilder.must(QueryBuilders.nestedQuery(TAGS, nestedQuery, ScoreMode.Avg));
            return;
        }

        BoolQueryBuilder nestedQuery = new BoolQueryBuilder();
        nestedQuery.must(QueryBuilders.matchQuery("tags.id", request.getTagId()));
        boolQueryBuilder.mustNot(QueryBuilders.nestedQuery(TAGS, nestedQuery, ScoreMode.Avg));
    }

    private void populateSearchOnlyHasTag(BoolQueryBuilder boolQueryBuilder, OrderSearchRequest request) {
        if(Boolean.TRUE.equals(request.getHasTag())) {
            boolQueryBuilder.must(QueryBuilders.nestedQuery(TAGS, QueryBuilders.existsQuery(TAGS), ScoreMode.Avg));
            return;
        }

        boolQueryBuilder.mustNot(QueryBuilders.nestedQuery(TAGS, QueryBuilders.existsQuery(TAGS), ScoreMode.Avg));
    }

    private void populateSearchDeliveryTime(BoolQueryBuilder boolQueryBuilder, OrderSearchRequest request) {
        Long from = null;
        Long to = null;
        if (request.getFromDeliveryTime() != null) {
            from = request.getFromDeliveryTime().getTime();
        }
        if (request.getToDeliveryTime() != null) {
            to = request.getToDeliveryTime().getTime();
        }
        populateSearchRange(boolQueryBuilder, from, to, ElasticSearchQueryField.ORDER_DELIVERY_DATE);
    }

    private void populateSearchGender(BoolQueryBuilder boolQueryBuilder, OrderSearchRequest request) {
        if (StringUtils.isNotBlank(request.getGender())) {
            if ("UNIDENTIFIED".equalsIgnoreCase(request.getGender())) {
                boolQueryBuilder.mustNot(QueryBuilders.existsQuery(GENDER));
                return;
            }

            boolQueryBuilder.must(QueryBuilders.matchQuery(GENDER, request.getGender()));
        }
    }


    private void populateSearchHasCustomerInfo(BoolQueryBuilder boolQueryBuilder, OrderSearchRequest request) {
        if (request.getHasCustomerInfo() == null) {
            return;
        }

        if (request.getHasCustomerInfo()) {
            boolQueryBuilder.must(QueryBuilders.existsQuery(ElasticSearchQueryField.CUSTOMER_ID));
            return;
        }

        boolQueryBuilder.mustNot(QueryBuilders.existsQuery(ElasticSearchQueryField.CUSTOMER_ID));
    }

    private void populateSearchHasDeliveryDate(BoolQueryBuilder boolQueryBuilder, OrderSearchRequest request) {
        if (request.getHasDeliveryDate() == null) {
            return;
        }
        if (request.getHasDeliveryDate()) {
            boolQueryBuilder.must(QueryBuilders.existsQuery(ElasticSearchQueryField.ORDER_DELIVERY_DATE));
            return;
        }

        boolQueryBuilder.mustNot(QueryBuilders.existsQuery(ElasticSearchQueryField.ORDER_DELIVERY_DATE));
    }

    private void populateSearchHasCustomerShippingFee(BoolQueryBuilder boolQueryBuilder, OrderSearchRequest request) {
        if (request.getHasCustomerShippingFee() == null) {
            return;
        }

        if (request.getHasCustomerShippingFee()) {
            boolQueryBuilder.must(QueryBuilders.rangeQuery(ElasticSearchQueryField.ORDER_DELIVERY_COST).gt(0));
            return;
        }
        BoolQueryBuilder notShippingQuery = new BoolQueryBuilder();
        BoolQueryBuilder mustNotDeliveryField = new BoolQueryBuilder();
        mustNotDeliveryField.mustNot(QueryBuilders.existsQuery(ElasticSearchQueryField.ORDER_DELIVERY_COST));
        notShippingQuery.should(QueryBuilders.rangeQuery(ElasticSearchQueryField.ORDER_DELIVERY_COST).lte(0))
                .should(mustNotDeliveryField)
                .minimumShouldMatch(MINIMUM_SHOULD_MATCH);
        boolQueryBuilder.must(notShippingQuery);
    }

    private void populateSearchNote(BoolQueryBuilder boolQueryBuilder, OrderSearchRequest request) {
        if (StringUtils.isNotBlank(request.getNote())) {
            BoolQueryBuilder shouldNote = new BoolQueryBuilder();
            shouldNote.should(QueryBuilders.matchPhraseQuery(CUSTOMER_SUPPORT_NOTE, QueryParser.escape(request.getNote())))
                    .should(QueryBuilders.matchPhraseQuery(CUSTOMER_NOTE, QueryParser.escape(request.getNote())))
                    .minimumShouldMatch(MINIMUM_SHOULD_MATCH);
            boolQueryBuilder.must(shouldNote);
        }
    }

    private void populateSearchPreOrderAndHolding(BoolQueryBuilder boolQueryBuilder, OrderSearchRequest request) {
        Long productId = getProductId(request);
        if (request.getHolding() != null) {
            BoolQueryBuilder shouldHoldingStock = new BoolQueryBuilder();
            shouldHoldingStock.should(QueryBuilders.queryStringQuery(OrderStatus.CONFIRMED.code()).field(ORDER_STATUS));
            shouldHoldingStock.should(QueryBuilders.queryStringQuery(OrderStatus.PACKING.code()).field(ORDER_STATUS));
            shouldHoldingStock.should(QueryBuilders.queryStringQuery(OrderStatus.PACKAGED.code()).field(ORDER_STATUS));
            BoolQueryBuilder mustPreOrderHolding = new BoolQueryBuilder();
            mustPreOrderHolding.must(QueryBuilders.queryStringQuery(OrderStatus.PRE_ORDER.code()).field(ORDER_STATUS));
            BoolQueryBuilder searchHoldingProduct = new BoolQueryBuilder();
            searchHoldingProduct.must(QueryBuilders.matchQuery("orderEntries.holding", request.getHolding()));
            if (productId != null) {
                BoolQueryBuilder shouldQueryProduct = new BoolQueryBuilder();
                shouldQueryProduct.should(QueryBuilders.matchQuery(PRODUCT_ID, productId));
                shouldQueryProduct.should(QueryBuilders.matchQuery(SUB_ENTRY_PRODUCT_ID, productId));
                searchHoldingProduct.must(shouldQueryProduct.minimumShouldMatch(MINIMUM_SHOULD_MATCH));
            }
            mustPreOrderHolding.must(QueryBuilders.nestedQuery(ORDER_ENTRIES, searchHoldingProduct, ScoreMode.Avg));
            shouldHoldingStock.should(mustPreOrderHolding);
            shouldHoldingStock.minimumShouldMatch(MINIMUM_SHOULD_MATCH);
            boolQueryBuilder.must(shouldHoldingStock);
        }

        if (request.getPreOrder() != null) {
            boolQueryBuilder.must(QueryBuilders.queryStringQuery(OrderStatus.PRE_ORDER.code()).field(ORDER_STATUS));
            BoolQueryBuilder searchPreOrderProduct = new BoolQueryBuilder();
            searchPreOrderProduct.must(QueryBuilders.matchQuery("orderEntries.preOrder", request.getPreOrder()));
            if (productId != null) {
                BoolQueryBuilder shouldQueryProduct = new BoolQueryBuilder();
                shouldQueryProduct.should(QueryBuilders.matchQuery(PRODUCT_ID, productId));
                shouldQueryProduct.should(QueryBuilders.matchQuery(SUB_ENTRY_PRODUCT_ID, productId));
                searchPreOrderProduct.must(shouldQueryProduct.minimumShouldMatch(MINIMUM_SHOULD_MATCH));
            }
            boolQueryBuilder.must(QueryBuilders.nestedQuery(ORDER_ENTRIES, searchPreOrderProduct, ScoreMode.Avg));
        }
    }

    private void populateSearchCreated(BoolQueryBuilder boolQueryBuilder, OrderSearchRequest request) {
        boolean hasViewAll = permissionFacade.hasViewAllOrderPermission(request.getCompanyId(), request.getOrderType());
        if (!hasViewAll) {
            boolQueryBuilder.must(QueryBuilders.matchQuery(CREATED_BY, authService.getCurrentUserId()));
        }

        if (request.getCreatedBy() != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery(CREATED_BY, request.getCreatedBy()));
        }

        String createdName = request.getCreatedName();
        if (StringUtils.isNotBlank(createdName)) {
            boolQueryBuilder.must(QueryBuilders.wildcardQuery(CREATED_NAME, String.format(WILD_CARD_PATTERN, QueryParser.escape(createdName).toLowerCase())));
        }
    }

    private void populateSearchOrderStatus(OrderSearchRequest request, BoolQueryBuilder boolQueryBuilder) {
        if (StringUtils.isNotBlank(request.getOrderStatus())) {
            boolQueryBuilder.must(QueryBuilders.queryStringQuery(QueryParser.escape(request.getOrderStatus())).field(ORDER_STATUS));
        }

        if (StringUtils.isNotBlank(request.getOrderStatusList())) {
            BoolQueryBuilder inOrderStatusQuery = buildInQuery(request.getOrderStatusList(), ORDER_STATUS);
            inOrderStatusQuery.minimumShouldMatch(MINIMUM_SHOULD_MATCH);
            boolQueryBuilder.must(inOrderStatusQuery);
        }
    }

    private void populateSearchOrderSource(OrderSearchRequest request, BoolQueryBuilder boolQueryBuilder) {
        if (request.getOrderSourceId() != null) {
            if (request.getOrderSourceId() > 0) {
                boolQueryBuilder.must(QueryBuilders.matchQuery(ORDER_SOURCE_ID, request.getOrderSourceId()));
            } else {
                boolQueryBuilder.mustNot(QueryBuilders.existsQuery(ORDER_SOURCE_ID));
            }
        }

        if (CollectionUtils.isNotEmpty(request.getOrderSourceIds())) {
            BoolQueryBuilder shouldQuery = new BoolQueryBuilder();
            for (Long orderSourceId : request.getOrderSourceIds()) {
                if (orderSourceId > 0) {
                    shouldQuery.should(QueryBuilders.matchQuery(ORDER_SOURCE_ID, orderSourceId));
                } else {
                    BoolQueryBuilder orderSourceIdQuery = new BoolQueryBuilder();
                    shouldQuery.should(orderSourceIdQuery.mustNot(QueryBuilders.existsQuery(ORDER_SOURCE_ID)));
                }
            }
            shouldQuery.minimumShouldMatch(MINIMUM_SHOULD_MATCH);
            boolQueryBuilder.must(shouldQuery);
        }
    }

    private void populateSearchOrderSettingCustomerOption(OrderSearchRequest request, BoolQueryBuilder boolQueryBuilder) {
        if (CollectionUtils.isNotEmpty(request.getCustomerOptionIds())) {
            BoolQueryBuilder shouldQuery = new BoolQueryBuilder();
            if (request.getCustomerOptionIds().size() == 1 && request.getCustomerOptionIds().get(0) <= 0) {
                List<OrderSettingCustomerOptionModel> optionModels = settingCustomerOptionService.findAllByCompanyNotHasOrder(request.getCompanyId());
                if (CollectionUtils.isNotEmpty(optionModels)) {
                    for (OrderSettingCustomerOptionModel option : optionModels) {
                        shouldQuery.should(QueryBuilders.matchQuery(CUSTOMER_OPTION_ID, option.getId()));
                    }
                    boolQueryBuilder.mustNot(QueryBuilders.nestedQuery(CUSTOMER_OPTIONS, shouldQuery, ScoreMode.Avg));
                }
                return;
            }
            for (Long customerOptionId : request.getCustomerOptionIds()) {
                shouldQuery.should(QueryBuilders.matchQuery(CUSTOMER_OPTION_ID, customerOptionId));
            }
            shouldQuery.minimumShouldMatch(MINIMUM_SHOULD_MATCH);
            boolQueryBuilder.must(QueryBuilders.nestedQuery(CUSTOMER_OPTIONS, shouldQuery, ScoreMode.Avg));
        }
    }

    @Override
    public Page<OrderSearchModel> searchForUpdateIndex(OrderSearchRequest request, Pageable pageableRequest) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery());
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withPageable(pageableRequest);
        boolQueryBuilder.must(QueryBuilders.matchQuery(EXCHANGE, false));
        boolQueryBuilder.must(QueryBuilders.matchQuery(DELETED, false));
        boolQueryBuilder.must(QueryBuilders.matchQuery(COMPANY_ID, request.getCompanyId()));
        if (StringUtils.isNotBlank(request.getProduct())) {
            populateSearchNestedObject(boolQueryBuilder, request.getProduct(), ORDER_ENTRIES, PRODUCT_ID, SUB_ENTRY_PRODUCT_ID,
                    new String[]{NAME, PRODUCT_NAME, PRODUCT_BARCODE, PRODUCT_SKU, SUB_ENTRY_PRODUCT_BARCODE,
                            SUB_ENTRY_PRODUCT_NAME, SUB_ENTRY_PRODUCT_SKU});
        }

        if (request.getCustomerId() != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("customerId", request.getCustomerId()));
        }

        return orderElasticSearchService.search(nativeSearchQueryBuilder.withQuery(boolQueryBuilder).build());
    }

    @Override
    public void indexReturnOrderIds(ReturnOrdersDTO returnOrdersDTO) {
        String originOrderCode = returnOrdersDTO.getOriginOrderCode();
        Long companyId = returnOrdersDTO.getCompanyId();
        Optional<OrderSearchModel> orderSearchModelOptional = orderElasticSearchService.findById(originOrderCode);
        if (!orderSearchModelOptional.isPresent()) {
            return;
        }
        OrderSearchModel orderSearchModel = orderSearchModelOptional.get();
        OrderModel orderModel = orderService.findByCodeAndCompanyIdAndDeleted(originOrderCode, companyId, false);
        if (orderModel != null) {
            List<ReturnOrderModel> returnOrders = returnOrderService.findAllByOriginOrder(orderModel);
            List<Long> returnOrderIds = returnOrders.stream().map(ReturnOrderModel::getId).collect(Collectors.toList());
            orderSearchModel.setReturnOrderIds(returnOrderIds);
            orderElasticSearchService.save(orderSearchModel);
        }
    }

    @Override
    public void updateSkuOrName(ProductInfoKafkaData productInfoKafkaData) {
        Pageable pageable = PageRequest.of(0, 100, Sort.Direction.ASC, CREATED_TIME);
        OrderSearchRequest request = new OrderSearchRequest();
        request.setCompanyId(productInfoKafkaData.getCompanyId());
        request.setProduct(String.valueOf(productInfoKafkaData.getId()));
        while (true) {
            Page<OrderSearchModel> page = this.searchForUpdateIndex(request, pageable);
            List<OrderSearchModel> orderSearchModels = page.getContent();
            if (CollectionUtils.isEmpty(orderSearchModels)) {
                LOGGER.debug("FINISH UPDATE SKU OR NAME: {} items", page.getTotalElements());
                break;
            }

            for (OrderSearchModel order : orderSearchModels) {
                List<OrderEntryData> orderEntries = order.getOrderEntries();
                if (CollectionUtils.isEmpty(orderEntries)) {
                    continue;
                }

                for (OrderEntryData entryData : orderEntries) {
                    if (entryData.getId().equals(productInfoKafkaData.getId())) {
                        entryData.setSku(productInfoKafkaData.getSku());
                        entryData.setName(productInfoKafkaData.getName());
                        entryData.setStringName(VNCharacterUtils.removeAccent(productInfoKafkaData.getName()));
                        entryData.setSupplierProductName(productInfoKafkaData.getSupplierProductName());
                    }
                    List<OrderEntryData> subOrderEntries = entryData.getSubOrderEntries();
                    updateSkuOrNameOfSubEntries(productInfoKafkaData, subOrderEntries);
                }
            }

            orderElasticSearchService.bulkIndexOrderEntries(orderSearchModels);
            pageable = pageable.next();
        }
    }

    private void updateSkuOrNameOfSubEntries(ProductInfoKafkaData productInfoKafkaData, List<OrderEntryData> subOrderEntries) {
        if (CollectionUtils.isEmpty(subOrderEntries)) {
            return;
        }
        for (OrderEntryData entryData : subOrderEntries) {
            if (entryData.getId().equals(productInfoKafkaData.getId())) {
                entryData.setSku(productInfoKafkaData.getSku());
                entryData.setName(productInfoKafkaData.getName());
                entryData.setStringName(VNCharacterUtils.removeAccent(productInfoKafkaData.getName()));
                entryData.setSupplierProductName(productInfoKafkaData.getSupplierProductName());
            }
        }
    }

    @Override
    public void createOrderReport(OrderReportRequest orderReportRequest) {
        KafkaMessageType kafkaMessageType = getKafkaMessageType(orderReportRequest);

        if (CollectionUtils.isNotEmpty(orderReportRequest.getOrderCodes())) {
            for (String orderCode : orderReportRequest.getOrderCodes()) {
                OrderModel model = orderService.findByCodeAndCompanyIdAndDeleted(orderCode, orderReportRequest.getCompanyId(), false);
                if (model != null) {
                    producerService.recalculateOrderReport(model, kafkaMessageType, null);
                }
            }
            return;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThread);
        for (int i = 0; i < numberOfThread; i++) {
            OrderReportRunnable orderReportRunnable = new OrderReportRunnable(authentication, i, bulkSize, numberOfThread);
            orderReportRunnable.setOrderReportRequest(orderReportRequest);
            orderReportRunnable.setOrderService(orderService);
            orderReportRunnable.setProducerService(producerService);
            orderReportRunnable.setKafkaMessageType(kafkaMessageType);
            executorService.execute(orderReportRunnable);
        }
    }

    private KafkaMessageType getKafkaMessageType(OrderReportRequest orderReportRequest) {
        if (KafkaMessageType.RECALCULATE_ALL_ORDER_REPORT.toString().equalsIgnoreCase(orderReportRequest.getKafkaMessageType())) {
            return KafkaMessageType.RECALCULATE_ALL_ORDER_REPORT;
        }
        if (KafkaMessageType.RECALCULATE_FACT_PROMOTION.toString().equalsIgnoreCase(orderReportRequest.getKafkaMessageType())) {
            return KafkaMessageType.RECALCULATE_FACT_PROMOTION;
        }

        if (KafkaMessageType.RECALCULATE_COLLABORATOR_DISCOUNT.toString().equalsIgnoreCase(orderReportRequest.getKafkaMessageType())) {
            return KafkaMessageType.RECALCULATE_COLLABORATOR_DISCOUNT;
        }

        if (KafkaMessageType.ORDER_TAG.toString().equalsIgnoreCase(orderReportRequest.getKafkaMessageType())) {
            return KafkaMessageType.ORDER_TAG;
        }

        throw new ServiceException("INVALID_KAFKA_TYPE", "INVALID_KAFKA_TYPE", HttpStatus.BAD_REQUEST.value());
    }

    @Override
    public void createOrderHistoryReport(OrderReportRequest orderReportRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThread, new CustomizableThreadFactory("SendOrderStatusHistory"));
        for (int i = 0; i < numberOfThread; i++) {
            OrderHistoryReportRunnable orderReportRunnable = new OrderHistoryReportRunnable(authentication, i, 500, numberOfThread);
            orderReportRunnable.setOrderReportRequest(orderReportRequest);
            orderReportRunnable.setOrderHistoryService(orderHistoryService);
            orderReportRunnable.setProducerService(producerService);
            executorService.execute(orderReportRunnable);
        }
    }

    @Override
    public OrderSearchExcelData exportExcelOrder(OrderSearchRequest request, boolean directly) {
        List<OrderSearchModel> modelList = new ArrayList<>();
        Pageable pageableRequest = PageRequest.of(0, MAX_PAGE_SIZE, new Sort(Sort.Direction.ASC, CREATED_TIME));
        if (StringUtils.isNotBlank(request.getSortField())) {
            if (Sort.Direction.DESC.toString().equalsIgnoreCase(request.getSortOrder())) {
                pageableRequest = PageRequest.of(0, MAX_PAGE_SIZE, new Sort(Sort.Direction.DESC, request.getSortField()));
            } else {
                pageableRequest = PageRequest.of(0, MAX_PAGE_SIZE, new Sort(Sort.Direction.ASC, request.getSortField()));
            }
        }

        while (true) {
            Page<OrderSearchModel> orderSearchModels = search(request, pageableRequest);
            if (CollectionUtils.isEmpty(orderSearchModels.getContent()) || (directly && modelList.size() >= MAX_ITEMS)) {
                break;
            }
            modelList.addAll(orderSearchModels.getContent());
            pageableRequest = pageableRequest.next();
        }

        OrderSearchExcelData orderSearchExcelData = orderElasticSearchService.exportExcelOrder(modelList, request);

        orderSearchExcelData.setContent(generateExcelData(orderSearchExcelData, request.getOrderType()));
        LOGGER.info("Export Orders: {} items", modelList.size());
        return orderSearchExcelData;
    }

    private void exportExcel(OrderSearchRequest request, OrderFileParameter fileParameter) {
        orderFileService.deleteFile(fileParameter);
        List<OrderEntryExcelData> orderEntryExcelData = new ArrayList<>();
        int countNum = 0;
        Pageable pageableRequest = PageRequest.of(0, MAX_PAGE_SIZE, new Sort(Sort.Direction.ASC, CREATED_TIME));
        if (StringUtils.isNotBlank(request.getSortField())) {
            if (Sort.Direction.DESC.toString().equalsIgnoreCase(request.getSortOrder())) {
                pageableRequest = PageRequest.of(0, MAX_PAGE_SIZE, new Sort(Sort.Direction.DESC, request.getSortField()));
            } else {
                pageableRequest = PageRequest.of(0, MAX_PAGE_SIZE, new Sort(Sort.Direction.ASC, request.getSortField()));
            }
        }

        List<PaymentMethodData> paymentMethodData = new ArrayList<>();
        while (true) {
            Page<OrderSearchModel> orderSearchModels = search(request, pageableRequest);
            if (CollectionUtils.isEmpty(orderSearchModels.getContent())) {
                break;
            }

            OrderSearchExcelData orderSearchExcelData = orderElasticSearchService.exportExcelOrder(orderSearchModels.getContent(), request);
            orderEntryExcelData.addAll(orderSearchExcelData.getOrderEntryExcelData());
            paymentMethodData = orderSearchExcelData.getPaymentMethod();
            if (orderEntryExcelData.size() >= maximumRow) {
                List<OrderEntryExcelData> orderEntryExcelMaximum = orderEntryExcelData.subList(0, maximumRow);
                List<OrderEntryExcelData> orderEntryExcelResidual = orderEntryExcelData.subList(maximumRow, orderEntryExcelData.size());
                request.setFileNum(countNum);
                writeExcelOrderToFile(orderEntryExcelMaximum, paymentMethodData, request);
                orderEntryExcelData = orderEntryExcelResidual;
                countNum++;
                pageableRequest = pageableRequest.next();
                continue;
            }
            pageableRequest = pageableRequest.next();
        }

        if (CollectionUtils.isNotEmpty(orderEntryExcelData)) {
            request.setFileNum(countNum);
            writeExcelOrderToFile(orderEntryExcelData, paymentMethodData, request);
            countNum++;
        }

        OrderFileParameter mergeFileParameter = fileParameterConverter.convert(request);
        orderFileService.mergeFile(mergeFileParameter, countNum - 1);
    }

    private String getTemplateWithOrderType(String orderType) {
        String template = ExcelTemplateFile.RETAIL_LIST_TEMPLATE.filePath();
        if (OrderType.ONLINE.name().equals(orderType)) {
            template = ExcelTemplateFile.ORDER_LIST_TEMPLATE.filePath();
        }

        if (OrderType.WHOLESALE.name().equals(orderType)) {
            template = ExcelTemplateFile.WHOLESALE_LIST_TEMPLATE.filePath();
        }
        return template;
    }

    private void writeExcelOrderToFile(List<OrderEntryExcelData> orderEntryExcelData, List<PaymentMethodData> paymentMethodData, OrderSearchRequest request) {
        ClassPathResource resource = new ClassPathResource(getTemplateWithOrderType(request.getOrderType()));
        try (InputStream is = resource.getInputStream()) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                Context context = PoiTransformer.createInitialContext();
                context.putVar("dataList", orderEntryExcelData);
                context.putVar("paymentMethodData", paymentMethodData);
                Workbook workbook = WorkbookFactory.create(is);
                PoiTransformer transformer = PoiTransformer.createTransformer(workbook);
                transformer.setOutputStream(os);
                JxlsHelper.getInstance().processTemplate(context, transformer);
                byte[] bytes = os.toByteArray();
                OrderFileParameter fileParameter = new OrderFileParameter();
                fileParameter.setUserId(request.getUserId());
                fileParameter.setExportExcelType(ExportExcelType.EXPORT_ORDER_WIDTH_DETAIL_COMBO);
                fileParameter.setFileNum(request.getFileNum());
                fileParameter.setOrderType(request.getOrderType());
                orderFileService.writeToFile(bytes, fileParameter);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private byte[] generateExcelData(OrderSearchExcelData orderSearchExcelData, String orderType) {
        String template = getTemplateWithOrderType(orderType);
        ClassPathResource resource = new ClassPathResource(template);
        try (InputStream is = resource.getInputStream()) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                Context context = new Context();
                context.putVar("dataList", orderSearchExcelData.getOrderEntryExcelData());
                JxlsHelper.getInstance().processTemplate(is, os, context);
                return os.toByteArray();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return new byte[0];
    }

    @Override
    public void fullIndexOrderEntry() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThread);
        for (int i = 0; i < numberOfThread; i++) {
            OrderElasticIndexRunnable orderElasticIndexRunnable = new OrderElasticIndexRunnable(authentication, i, bulkSize, numberOfThread) {
                @Override
                public OrderSearchModel convertOrderSearchModel(OrderModel order) {
                    Optional<OrderSearchModel> orderSearchModelOptional = orderElasticSearchService.findById(order.getCode());
                    if (orderSearchModelOptional.isPresent()) {
                        OrderSearchModel orderSearchModel = orderSearchModelOptional.get();
                        OrderSearchModel result = orderSearchEntryModelConverter.convert(order);
                        orderSearchModel.setOrderEntries(result.getOrderEntries());
                        return orderSearchModel;
                    } else {
                        return super.convertOrderSearchModel(order);
                    }
                }
            };
            orderElasticIndexRunnable.setOrderElasticSearchService(orderElasticSearchService);
            orderElasticIndexRunnable.setOrderSearchModelConverter(orderSearchModelConverter);
            orderElasticIndexRunnable.setOrderService(orderService);
            executorService.execute(orderElasticIndexRunnable);
        }
    }

    @Override
    public void fullIndexOrderPaymentData(Long companyId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThread);
        for (int i = 0; i < numberOfThread; i++) {
            OrderElasticIndexRunnable orderElasticIndexRunnable = new OrderElasticIndexRunnable(authentication, i, bulkSize, numberOfThread) {

                @Override
                protected Page<OrderModel> findOrderModel(Pageable pageable) {
                    return orderService.findAllByCompanyIdAndType(pageable, companyId, OrderType.ONLINE.toString());
                }

                @Override
                public OrderSearchModel convertOrderSearchModel(OrderModel order) {
                    Optional<OrderSearchModel> orderSearchModelOptional = orderElasticSearchService.findById(order.getCode());
                    if (orderSearchModelOptional.isPresent()) {
                        OrderSearchModel orderSearchModel = orderSearchModelOptional.get();
                        double paidAmount = 0d;
                        List<PaymentTransactionData> paymentTransactionData = paymentTransactionService.findAllPaymentInvoiceOrder(order);
                        orderSearchModel.setPaymentTransactionData(paymentTransactionData);
                        for (PaymentTransactionData payment : paymentTransactionData) {
                            if (OrderType.ONLINE.name().equals(order.getType())) {
                                paidAmount += CommonUtils.readValue(payment.getAmount());
                            }
                        }
                        orderSearchModel.setPaidAmount(paidAmount);
                        return orderSearchModel;
                    } else {
                        return super.convertOrderSearchModel(order);
                    }
                }
            };
            orderElasticIndexRunnable.setOrderElasticSearchService(orderElasticSearchService);
            orderElasticIndexRunnable.setOrderSearchModelConverter(orderSearchModelConverter);
            orderElasticIndexRunnable.setOrderService(orderService);
            executorService.execute(orderElasticIndexRunnable);
        }
    }

    @Override
    public void requestExportExcelAllProduct(OrderSearchRequest request) {
        final OrderFileParameter orderFileParameter = fileParameterConverter.convert(request);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        exportExcelExecutor.execute(() -> {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            try {
                this.exportExcel(request, orderFileParameter);
            } finally {
                orderFileService.setProcessExportExcel(orderFileParameter, false);
            }
        });
    }

    @Override
    public byte[] downloadExcelOrder(OrderSearchRequest request) {
        OrderFileParameter fileParameter = fileParameterConverter.convert(request);
        byte[] bytes = orderFileService.readFile(fileParameter);
        orderFileService.deleteFile(fileParameter);
        return bytes;
    }

    private void populateSearchWarehouse(OrderSearchRequest request, BoolQueryBuilder boolQueryBuilder) {
        if (request.getWarehouseId() != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery(WAREHOUSE_ID, request.getWarehouseId()));
            return;
        }

        if (StringUtils.isNotBlank(request.getWarehouseIds())) {
            searchWarehouseWithList(request, boolQueryBuilder);
            return;
        }

        boolean hasPermission = permissionFacade.hasPermission(PermissionCodes.MANAGE_ALL_WAREHOUSES.code(),
                request.getCompanyId());
        if (!hasPermission) {
            List<Long> warehouses = authService.getAllWarehouseOfCurrentUser(request.getCompanyId());
            if (CollectionUtils.isEmpty(warehouses)) {
                ErrorCodes err = ErrorCodes.ACCESS_DENIED;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
            BoolQueryBuilder inWarehouseQuery = new BoolQueryBuilder();
            for (Long warehouseId : warehouses) {
                inWarehouseQuery.should(QueryBuilders.matchQuery(WAREHOUSE_ID, warehouseId));
            }
            inWarehouseQuery.minimumShouldMatch(MINIMUM_SHOULD_MATCH);
            boolQueryBuilder.must(inWarehouseQuery);
        }
    }

    private void searchWarehouseWithList(OrderSearchRequest request, BoolQueryBuilder boolQueryBuilder) {
        String[] warehouseIdList = CommonUtils.splitByComma(request.getWarehouseIds());
        BoolQueryBuilder inWarehouseQuery = new BoolQueryBuilder();
        for (String warehouseId : warehouseIdList) {
            try {
                inWarehouseQuery.should(QueryBuilders.matchQuery(WAREHOUSE_ID, Long.parseLong(warehouseId)));
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid warehouse id: {}", warehouseId);
            }
            inWarehouseQuery.should(QueryBuilders.matchQuery(WAREHOUSE_ID, warehouseId));
        }
        inWarehouseQuery.minimumShouldMatch(MINIMUM_SHOULD_MATCH);
        boolQueryBuilder.must(inWarehouseQuery);
    }

    private void populateSearchPaymentMethod(OrderSearchRequest request, BoolQueryBuilder boolQueryBuilder) {
        BoolQueryBuilder nestedQueryBuilder = new BoolQueryBuilder();
        BoolQueryBuilder mustNotNestedQuery = new BoolQueryBuilder();
        BoolQueryBuilder shouldNestedQuery = new BoolQueryBuilder();
        try {
            long paymentMethodId = Long.parseLong(request.getPaymentMethodId());
            if (paymentMethodId <= 0) {
                BoolQueryBuilder mustNot = new BoolQueryBuilder();
                nestedQueryBuilder.must(mustNot.mustNot(QueryBuilders.existsQuery("paymentTransactionData.paymentMethodId")));
                shouldNestedQuery.should(mustNotNestedQuery.mustNot(QueryBuilders.nestedQuery(PAYMENT_TRANSACTIONS,
                        QueryBuilders.existsQuery(PAYMENT_TRANSACTIONS), ScoreMode.Avg)));
            } else {
                nestedQueryBuilder.must(QueryBuilders.matchQuery("paymentTransactionData.paymentMethodId", paymentMethodId));
            }

        } catch (NumberFormatException e) {
            ErrorCodes err = ErrorCodes.INVALID_PAYMENT_METHOD_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        nestedQueryBuilder.must(QueryBuilders.rangeQuery("paymentTransactionData.amount").gte(1));

        shouldNestedQuery.should(QueryBuilders.nestedQuery(PAYMENT_TRANSACTIONS, nestedQueryBuilder, ScoreMode.Avg));
        shouldNestedQuery.minimumShouldMatch(MINIMUM_SHOULD_MATCH);
        boolQueryBuilder.must(shouldNestedQuery);

    }


    protected void populateStatusHistoryDate(BoolQueryBuilder nestedQuery, OrderSearchRequest request) {
        Date fromModifiedTimeStatus = request.getFromModifiedTimeStatus();
        Date toModifiedTimeStatus = request.getToModifiedTimeStatus();
        if (fromModifiedTimeStatus != null && toModifiedTimeStatus != null) {
            nestedQuery.must(QueryBuilders.rangeQuery(ElasticSearchQueryField.ORDER_HISTORY_MODIFIED_TIME)
                    .gte(DateUtil.getDateWithoutTime(fromModifiedTimeStatus).getTime())
                    .lte(DateUtil.getEndDay(toModifiedTimeStatus).getTime()));
        } else if (fromModifiedTimeStatus != null) {
            nestedQuery.must(QueryBuilders.rangeQuery(ElasticSearchQueryField.ORDER_HISTORY_MODIFIED_TIME)
                    .gte(DateUtil.getDateWithoutTime(fromModifiedTimeStatus).getTime()));
        } else if (toModifiedTimeStatus != null) {
            nestedQuery.must(QueryBuilders.rangeQuery(ElasticSearchQueryField.ORDER_HISTORY_MODIFIED_TIME)
                    .lte(DateUtil.getEndDay(toModifiedTimeStatus).getTime()));
        }
    }

    protected void populateSearchStatusHistory(BoolQueryBuilder boolQueryBuilder, OrderSearchRequest request) {
        String currentStatus = request.getCurrentStatus();
        if (StringUtils.isNotBlank(currentStatus) || request.getFromModifiedTimeStatus() != null || request.getToModifiedTimeStatus() != null) {
            BoolQueryBuilder nestedQuery = new BoolQueryBuilder();
            if (StringUtils.isNotBlank(currentStatus)) {
                nestedQuery.must(QueryBuilders.matchQuery(CURRENT_STATUS, QueryParser.escape(currentStatus)));
            }
            populateStatusHistoryDate(nestedQuery, request);
            boolQueryBuilder.must(QueryBuilders.nestedQuery(ORDER_HISTORY, nestedQuery, ScoreMode.Avg));
        }


        if (StringUtils.isNotBlank(request.getCurrentStatusList())) {
            BoolQueryBuilder nestedQuery = buildInQuery(request.getCurrentStatusList(), CURRENT_STATUS);
            populateStatusHistoryDate(nestedQuery, request);
            nestedQuery.minimumShouldMatch(MINIMUM_SHOULD_MATCH);
            boolQueryBuilder.must(QueryBuilders.nestedQuery(ORDER_HISTORY, nestedQuery, ScoreMode.Avg));
        }
    }

    private BoolQueryBuilder buildInQuery(String request, String param) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        String[] currentStatusList = CommonUtils.splitByComma(request);
        for (String current : currentStatusList) {
            try {
                boolQueryBuilder.should(QueryBuilders.queryStringQuery(QueryParser.escape(current)).field(param));
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid order status: {}", current);
            }
        }
        return boolQueryBuilder;
    }

    protected void populateSearchAddress(BoolQueryBuilder boolQueryBuilder, OrderSearchRequest request) {
        if (request.getProvinceId() != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("provinceId", request.getProvinceId()));
        }
        if (request.getDistrictId() != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("districtId", request.getDistrictId()));
        }
        if (request.getWardId() != null) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("wardId", request.getWardId()));
        }
        String address = request.getAddress();
        if (StringUtils.isNotBlank(address)) {
            boolQueryBuilder.must(QueryBuilders.queryStringQuery(QueryParser.escape(address)).field("address"));
        }
    }

    protected void populateFinalPrice(BoolQueryBuilder boolQueryBuilder, OrderSearchRequest request) {
        if (StringUtils.isBlank(request.getFinalPrice())) {
            return;
        }

        try {
            OrderParser orderParser = objectMapper.readValue(request.getFinalPrice(), OrderParser.class);
            if (StringUtils.isNotBlank(orderParser.getEqual())) {
                boolQueryBuilder.must(QueryBuilders.matchQuery(FINAL_PRICE, orderParser.getEqual()));
            }
            if (StringUtils.isNotBlank(orderParser.getGt())) {
                boolQueryBuilder.must(QueryBuilders.rangeQuery(FINAL_PRICE)
                        .gt(orderParser.getGt()));
            }
            if (StringUtils.isNotBlank(orderParser.getLt())) {
                boolQueryBuilder.must(QueryBuilders.rangeQuery(FINAL_PRICE)
                        .lt(orderParser.getLt()));
            }
        } catch (IOException e) {
            ErrorCodes err = ErrorCodes.INVALID_JSON_FORMAT_OF_SEARCH_FIELD;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    protected void populateTotalDiscount(BoolQueryBuilder boolQueryBuilder, OrderSearchRequest request) {
        if (StringUtils.isBlank(request.getTotalDiscount())) {
            return;
        }
        try {
            OrderParser orderParser = objectMapper.readValue(request.getTotalDiscount(), OrderParser.class);
            if (StringUtils.isNotBlank(orderParser.getEqual())) {
                boolQueryBuilder.must(QueryBuilders.matchQuery(TOTAL_DISCOUNT, orderParser.getEqual()));
            }
            if (StringUtils.isNotBlank(orderParser.getGt())) {
                boolQueryBuilder.must(QueryBuilders.rangeQuery(TOTAL_DISCOUNT)
                        .gt(orderParser.getGt()));
            }
            if (StringUtils.isNotBlank(orderParser.getLt())) {
                boolQueryBuilder.must(QueryBuilders.rangeQuery(TOTAL_DISCOUNT)
                        .lt(orderParser.getLt()));
            }
        } catch (IOException e) {
            ErrorCodes err = ErrorCodes.INVALID_JSON_FORMAT_OF_SEARCH_FIELD;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Override
    public byte[] exportExcelOrderTypeDistributor(Long companyId, String orderCode, Long printSettingId) {
        OrderModel model = orderService.findByCodeAndCompanyId(orderCode, companyId);
        if (model == null || !OrderType.ONLINE.toString().equals(model.getType())
                || (OrderType.ONLINE.toString().equals(model.getType()) && model.getDistributorId() == null)) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        PrintSettingData settingData = new PrintSettingData();
        if (printSettingId != null) {
            settingData = crmService.getPrintSettingById(printSettingId, model.getCompanyId());
        }

        return exportExcelOrderDistributor(model, settingData);
    }

    @Override
    public void updateCustomerName(CustomerDto customerDto) {
        Pageable pageable = PageRequest.of(0, 100, Sort.Direction.ASC, CREATED_TIME);
        OrderSearchRequest request = new OrderSearchRequest();
        request.setCompanyId(customerDto.getCompanyId());
        request.setCustomerId(customerDto.getCustomerId());
        while (true) {
            Page<OrderSearchModel> page = this.searchForUpdateIndex(request, pageable);
            List<OrderSearchModel> orderSearchModels = page.getContent();
            if (CollectionUtils.isEmpty(orderSearchModels)) {
                LOGGER.debug("FINISH UPDATE CUSTOMER NAME: {} items", page.getTotalElements());
                break;
            }

            for (OrderSearchModel order : orderSearchModels) {
                order.setCustomerName(customerDto.getName());
            }

            orderElasticSearchService.bulkIndexCustomerName(orderSearchModels);
            pageable = pageable.next();
        }
    }

    private byte[] exportExcelOrderDistributor(OrderModel model, PrintSettingData settingData) {
        WarehouseData warehouseData = logisticService.findByIdAndCompanyId(model.getWarehouseId(), model.getCompanyId());
        AddressData addressData = new AddressData();
        if (model.getShippingAddressId() != null) {
            addressData = crmService.getAddress(model.getShippingAddressId());
        }
        CustomerData customerData = new CustomerData();
        if (model.getCustomerId() != null) {
            customerData = crmService.getCustomer(model.getCustomerId(), model.getCompanyId());
        }

        DistributorData distributorData = logisticService.getDetailDistributor(model.getDistributorId(), model.getCompanyId());
        InvoiceInformationData receiptAddress = new InvoiceInformationData();
        if (CollectionUtils.isNotEmpty(distributorData.getInvoices())) {
            receiptAddress = distributorData.getInvoices().get(0);
        }
        List<OrderEntryExcelData> entries = populateExcelDataEntries(model);
        ClassPathResource resource = new ClassPathResource("templates/order_distributor.xlsx");
        long finalPrice = Math.round(model.getFinalPrice());
        try (InputStream is = resource.getInputStream()) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                Context context = new Context();
                List<String> transferDates = populateTransferDate(model);
                context.putVar("transferDates", transferDates);
                context.putVar("entries", entries);
                context.putVar("exportExcelDTO", model);
                context.putVar("addressData", addressData);
                context.putVar("customerData", customerData);
                context.putVar("receiptAddress", receiptAddress);
                context.putVar("warehouseName", warehouseData.getName());
                context.putVar("printCompanyName", settingData.getPrintCompanyName());
                context.putVar("printCompanyAddress", settingData.getPrintCompanyAddress());
                context.putVar("numberToWord", NumberToWords.convertToWord(finalPrice));
                JxlsHelper.getInstance().processTemplate(is, os, context);
                return os.toByteArray();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new byte[0];
    }

    private List<String> populateTransferDate(OrderModel model) {
        List<String> transferDates = new ArrayList<>();
        Date createdTime = model.getCreatedTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(createdTime);
        transferDates.add(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
        transferDates.add(String.valueOf(cal.get(Calendar.MONTH) + 1));
        transferDates.add(String.valueOf(cal.get(Calendar.YEAR)));
        return transferDates;
    }

    private List<OrderEntryExcelData> populateExcelDataEntries(OrderModel model) {
        Set<Long> productIds = model.getEntries().stream().map(i -> i.getProductId()).collect(Collectors.toSet());
        for (AbstractOrderEntryModel entry : model.getEntries()) {
            Set<Long> proIds = entry.getSubOrderEntries().stream().map(i -> i.getProductId()).collect(Collectors.toSet());
            productIds.addAll(proIds);
            if (CollectionUtils.isNotEmpty(entry.getToppingOptionModels())) {
                for (ToppingOptionModel toppingOptionModel : entry.getToppingOptionModels()) {
                    Set<Long> productToppngIds = toppingOptionModel.getToppingItemModels().stream().map(i -> i.getProductId()).collect(Collectors.toSet());
                    productIds.addAll(productToppngIds);
                }
            }
        }
        ProductSearchRequest searchRequest = new ProductSearchRequest();
        searchRequest.setCompanyId(model.getCompanyId());
        searchRequest.setUnpaged(true);
        searchRequest.setPageSize(productIds.size());
        searchRequest.setIds(StringUtils.join(productIds, ","));
        List<ProductSearchModel> productDataList = productSearchService.findAllByCompanyId(searchRequest);
        Map<Long, ProductSearchModel> productMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(productDataList)) {
            productMap = productDataList.stream().collect(Collectors.toMap(i -> i.getId(), Function.identity()));
        }

        List<OrderEntryExcelData> excelDataList = new ArrayList<>();
        for (AbstractOrderEntryModel entry : model.getEntries()) {
            OrderEntryExcelData data = populateRowExcel(entry, productMap);
            if (CollectionUtils.isNotEmpty(entry.getSubOrderEntries())) {
                data.setQuantity(null);
            }
            excelDataList.add(data);
            populateSubOrderEntry(excelDataList, entry, productMap);
            populateToppingItem(excelDataList, entry, productMap);
        }
        return excelDataList;
    }

    private void populateToppingItem(List<OrderEntryExcelData> excelDataList, AbstractOrderEntryModel entry, Map<Long, ProductSearchModel> productMap) {
        if (CollectionUtils.isEmpty(entry.getToppingOptionModels())) return;
        for (ToppingOptionModel toppingOptionModel : entry.getToppingOptionModels()) {
            if (CollectionUtils.isEmpty(toppingOptionModel.getToppingItemModels())) {
                continue;
            }
            for (ToppingItemModel toppingItemModel : toppingOptionModel.getToppingItemModels()) {
                OrderEntryExcelData data = new OrderEntryExcelData();
                Long quantity = (long) toppingItemModel.getQuantity() * toppingOptionModel.getQuantity();
                data.setQuantity(quantity);
                data.setDiscount(toppingItemModel.getDiscount());
                data.setDiscountType(toppingItemModel.getDiscountType());
                data.setPrice(toppingItemModel.getBasePrice());
                Double totalPrice = quantity * toppingItemModel.getBasePrice();
                Double fixedDiscount = CurrencyUtils.computeValue(toppingItemModel.getDiscount(), toppingItemModel.getDiscountType(), totalPrice);
                data.setFinalPrice(totalPrice - fixedDiscount);
                populateBasicProduct(productMap, toppingItemModel.getProductId(), data);
                excelDataList.add(data);
            }
        }
    }

    private void populateBasicProduct(Map<Long, ProductSearchModel> productMap, Long productId, OrderEntryExcelData data) {
        data.setProductId(productId);
        if (productMap.containsKey(productId)) {
            data.setBarcode(productMap.get(productId).getBarcode());
            data.setName(productMap.get(productId).getName());
            data.setUnitName(productMap.get(productId).getUnitName());
        }
    }

    private void populateSubOrderEntry(List<OrderEntryExcelData> excelDataList, AbstractOrderEntryModel entry, Map<Long, ProductSearchModel> productMap) {
        if (CollectionUtils.isEmpty(entry.getSubOrderEntries())) return;
        for (SubOrderEntryModel subOrderEntry : entry.getSubOrderEntries()) {
            OrderEntryExcelData data = new OrderEntryExcelData();
            data.setQuantity(subOrderEntry.getQuantity().longValue());
            populateBasicProduct(productMap, subOrderEntry.getProductId(), data);
            excelDataList.add(data);
        }
    }

    private OrderEntryExcelData populateRowExcel(AbstractOrderEntryModel entry, Map<Long, ProductSearchModel> productMap) {
        OrderEntryExcelData data = new OrderEntryExcelData();
        data.setQuantity(entry.getQuantity());
        data.setRecommendedRetailPrice(entry.getRecommendedRetailPrice());
        data.setDiscount(entry.getDiscount());
        data.setDiscountType(entry.getDiscountType());
        data.setPrice(entry.getBasePrice());
        data.setFinalPrice(entry.getFinalPrice());
        populateBasicProduct(productMap, entry.getProductId(), data);
        return data;
    }

    @Override
    public void fullIndexOrderEntrySaleOff() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        exportExcelExecutor.execute(() -> {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            try {
                Pageable pageable = PageRequest.of(0, MAX_PAGE_SIZE);
                while (true) {
                    Page<OrderSearchModel> page = orderElasticSearchService.findAll(pageable);
                    if (CollectionUtils.isEmpty(page.getContent())) {
                        break;
                    }
                    List<OrderSearchModel> models = page.getContent();
                    for (OrderSearchModel model : models) {
                        model.setHasSaleOff(false);
                    }
                    orderElasticSearchService.saveAll(models);
                    pageable = pageable.next();
                }
            } catch (RuntimeException e) {
                LOGGER.error(e.getMessage(), e);
            }
        });
    }

    @Override
    public Page<OrderSearchModel> orderStorefrontSearch(OrderSearchRequest request, Pageable pageableRequest) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery());
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withPageable(pageableRequest);
        boolQueryBuilder.must(QueryBuilders.matchQuery(EXCHANGE, false));
        boolQueryBuilder.must(QueryBuilders.matchQuery(CREATED_BY, request.getCreatedBy()));
        boolQueryBuilder.must(QueryBuilders.queryStringQuery(QueryParser.escape(request.getOrderType())).field("orderType"));
        boolQueryBuilder.must(QueryBuilders.matchQuery(DELETED, false));
        boolQueryBuilder.must(QueryBuilders.matchQuery(COMPANY_ID, request.getCompanyId()));
        boolQueryBuilder.must(QueryBuilders.matchQuery(SELL_SIGNAL, request.getSellSignal()));
        boolQueryBuilder.mustNot(QueryBuilders.matchQuery(ORDER_STATUS, OrderStatus.CHANGE_TO_RETAIL.code()));

        if (StringUtils.isNotBlank(request.getProduct())) {
            String[] fields = new String[]{NAME, PRODUCT_NAME, PRODUCT_BARCODE, PRODUCT_SKU, SUB_ENTRY_PRODUCT_BARCODE,
                    SUB_ENTRY_PRODUCT_NAME, SUB_ENTRY_PRODUCT_SKU };
            BoolQueryBuilder nestedQuery = new BoolQueryBuilder();
            try {
                BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
                long id = Long.parseLong(request.getProduct());
                nestedQuery.should(QueryBuilders.matchQuery(PRODUCT_ID, id));
                nestedQuery.should(QueryBuilders.matchQuery(SUB_ENTRY_PRODUCT_ID, id));
                buildShouldQueries(request.getProduct(), fields, nestedQuery).minimumShouldMatch(MINIMUM_SHOULD_MATCH);
                queryBuilder.should(QueryBuilders.wildcardQuery(ORDER_CODE, String.format(WILD_CARD_PATTERN, QueryParser.escape(request.getProduct()))));
                queryBuilder.should(QueryBuilders.nestedQuery(ORDER_ENTRIES, nestedQuery, ScoreMode.Avg));
                boolQueryBuilder.must(queryBuilder);
            } catch (NumberFormatException e) {
                buildShouldQueries(request.getProduct(), fields, nestedQuery).minimumShouldMatch(MINIMUM_SHOULD_MATCH);
                boolQueryBuilder.must(QueryBuilders.nestedQuery(ORDER_ENTRIES, nestedQuery, ScoreMode.Avg));
            }
        }
        populateSearchOrderStatus(request, boolQueryBuilder);
        return orderElasticSearchService.search(nativeSearchQueryBuilder.withQuery(boolQueryBuilder).build());
    }

    @Override
    public void fullIndexOrderSellSignal(Long companyId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThread);
        for (int i = 0; i < numberOfThread; i++) {
            OrderElasticIndexRunnable orderElasticIndexRunnable = new OrderElasticIndexRunnable(authentication, i, bulkSize, numberOfThread) {
                @Override
                protected Page<OrderModel> findOrderModel(Pageable pageable) {
                    return orderService.findAllByCompanyIdAndType(pageable, companyId, OrderType.ONLINE.toString());
                }

                @Override
                public OrderSearchModel convertOrderSearchModel(OrderModel order) {
                    Optional<OrderSearchModel> orderSearchModelOptional = orderElasticSearchService.findById(order.getCode());
                    if (orderSearchModelOptional.isPresent()) {
                        OrderSearchModel orderSearchModel = orderSearchModelOptional.get();
                        orderSearchModel.setSellSignal(order.getSellSignal());
                        return orderSearchModel;
                    } else {
                        return super.convertOrderSearchModel(order);
                    }
                }
            };
            orderElasticIndexRunnable.setOrderElasticSearchService(orderElasticSearchService);
            orderElasticIndexRunnable.setOrderSearchModelConverter(orderSearchModelConverter);
            orderElasticIndexRunnable.setOrderService(orderService);
            executorService.execute(orderElasticIndexRunnable);
        }
    }

    @Autowired
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    @Autowired
    public void setPermissionFacade(PermissionFacade permissionFacade) {
        this.permissionFacade = permissionFacade;
    }

    @Autowired
    public void setReturnOrderService(ReturnOrderService returnOrderService) {
        this.returnOrderService = returnOrderService;
    }

    @Autowired
    public void setProducerService(OrderProducerService producerService) {
        this.producerService = producerService;
    }

    @Autowired
    public void setOrderHistoryService(OrderHistoryService orderHistoryService) {
        this.orderHistoryService = orderHistoryService;
    }

    @Autowired
    public void setOrderSearchEntryModelConverter(Converter<OrderModel, OrderSearchModel> orderSearchEntryModelConverter) {
        this.orderSearchEntryModelConverter = orderSearchEntryModelConverter;
    }

    @Autowired
    public void setPaymentTransactionService(PaymentTransactionService paymentTransactionService) {
        this.paymentTransactionService = paymentTransactionService;
    }

    @Autowired
    public void setExportExcelExecutor(Executor exportExcelExecutor) {
        this.exportExcelExecutor = exportExcelExecutor;
    }

    @Autowired
    public void setFileParameterConverter(Converter<OrderSearchRequest, OrderFileParameter> fileParameterConverter) {
        this.fileParameterConverter = fileParameterConverter;
    }

    @Autowired
    public void setOrderFileService(OrderFileService orderFileService) {
        this.orderFileService = orderFileService;
    }

    @Autowired
    public void setLogisticService(LogisticService logisticService) {
        this.logisticService = logisticService;
    }

    @Autowired
    public void setProductSearchService(ProductSearchService productSearchService) {
        this.productSearchService = productSearchService;
    }

    @Autowired
    public void setCrmService(CRMService crmService) {
        this.crmService = crmService;
    }

    @Autowired
    public void setSettingCustomerOptionService(OrderSettingCustomerOptionService settingCustomerOptionService) {
        this.settingCustomerOptionService = settingCustomerOptionService;
    }
}
