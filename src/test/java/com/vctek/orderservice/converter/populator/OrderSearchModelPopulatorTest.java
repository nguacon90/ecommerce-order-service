package com.vctek.orderservice.converter.populator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vctek.converter.Converter;
import com.vctek.dto.redis.AddressData;
import com.vctek.dto.redis.DistrictData;
import com.vctek.dto.redis.ProvinceData;
import com.vctek.dto.redis.WardData;
import com.vctek.orderservice.dto.PaymentMethodData;
import com.vctek.orderservice.dto.TagData;
import com.vctek.orderservice.dto.UserData;
import com.vctek.orderservice.elasticsearch.model.OrderSearchModel;
import com.vctek.orderservice.elasticsearch.model.PaymentTransactionData;
import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.feignclient.dto.CustomerData;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.repository.OrderEntryRepository;
import com.vctek.orderservice.repository.SubOrderEntryRepository;
import com.vctek.orderservice.repository.ToppingItemRepository;
import com.vctek.orderservice.service.*;
import com.vctek.orderservice.util.ProductDType;
import com.vctek.util.OrderType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class OrderSearchModelPopulatorTest {
    private OrderSearchModelPopulator populator;
    private AuthService authService;
    private CRMService crmService;
    private OrderEntryRepository orderEntryRepository;
    private UserData userData;
    private CustomerData customerData;
    private PaymentMethodData paymentMethodData;
    private PaymentTransactionService paymentTransactionService;
    private PaymentTransactionModel paymentTransactionModel;
    private OrderHistoryService orderHistoryService;
    private ReturnOrderService returnOrderService;
    private OrderModel source;
    private OrderSearchModel target;
    @Mock
    private ProductSearchService productSearchService;
    @Mock
    private CalculationService calculationService;
    @Mock
    private ToppingItemRepository toppingItemRepository;
    @Mock
    private SubOrderEntryRepository subOrderEntryRepository;
    @Mock
    private Converter<PaymentTransactionModel, PaymentTransactionData> paymentTransactionDataConverter;
    @Mock
    private OrderSettingCustomerOptionService settingCustomerOptionService;
    @Mock
    private TagService tagService;
    @Mock
    private Converter<TagModel, TagData> tagDataConverter;
    @Mock
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        returnOrderService = mock(ReturnOrderService.class);
        subOrderEntryRepository = mock(SubOrderEntryRepository.class);
        userData = new UserData();
        customerData = new CustomerData();
        paymentMethodData = new PaymentMethodData();
        paymentTransactionModel = new PaymentTransactionModel();
        authService = mock(AuthService.class);
        paymentTransactionService = mock(PaymentTransactionService.class);
        crmService = mock(CRMService.class);
        orderEntryRepository = mock(OrderEntryRepository.class);
        orderHistoryService = mock(OrderHistoryService.class);
        populator = new OrderSearchModelPopulator(authService, crmService,
                paymentTransactionService);
        populator.setReturnOrderService(returnOrderService);
        populator.setCalculationService(calculationService);
        populator.setProductSearchService(productSearchService);
        populator.setToppingItemRepository(toppingItemRepository);
        populator.setSubOrderEntryRepository(subOrderEntryRepository);
        populator.setOrderEntryRepository(orderEntryRepository);
        populator.setOrderPaymentTransactionConverter(paymentTransactionDataConverter);
        populator.setSettingCustomerOptionService(settingCustomerOptionService);
        populator.setOrderHistoryService(orderHistoryService);
        populator.setTagService(tagService);
        populator.setTagDataConverter(tagDataConverter);
        populator.setObjectMapper(objectMapper);

        source = new OrderModel();
        target = new OrderSearchModel();
        source.setId(1l);
        source.setCode("code");
        source.setTotalPrice(12.2);
        source.setTotalTax(13.2);
        source.setWarehouseId(1l);
        source.setGuid("guid");
        source.setType(OrderType.RETAIL.toString());
        source.setGlobalDiscountValues("global");
        source.setSubTotal(12.2);
        source.setTotalDiscount(12.2);
        source.setCompanyId(1l);
        source.setCreateByUser(1l);
        source.setSubTotalDiscount(123.2);
        source.setTotalDiscount(23.3);
        source.setCustomerId(1l);
        source.setTotalRewardAmount(3000d);
        source.setRewardPoint(3d);
        source.setRedeemAmount(5000d);
        source.setRefundAmount(4000d);
        OrderSourceModel orderSourceModel = new OrderSourceModel();
        orderSourceModel.setId(1l);
        source.setOrderSourceModel(orderSourceModel);
        paymentTransactionModel.setOrderModel(source);
        paymentTransactionModel.setPaymentMethodId(1l);
        paymentTransactionModel.setAmount(12.2);
        paymentTransactionModel.setId(1l);
        paymentTransactionModel.setMoneySourceId(1l);

        when(paymentTransactionService.findAllByOrderCode(anyString())).thenReturn(Arrays.asList(paymentTransactionModel));
        paymentMethodData.setId(1l);
        paymentMethodData.setName("tien");

        userData.setId(1l);
        userData.setName("moji");
        userData.setPhone("04948564");
        when(authService.getUserById(anyLong())).thenReturn(userData);

        ProvinceData provinceData = new ProvinceData();
        provinceData.setId(1l);
        DistrictData districtData = new DistrictData();
        districtData.setId(1l);
        WardData wardData = new WardData();
        wardData.setId(1l);
        List<AddressData> addressDataList = new ArrayList<>();
        AddressData addressData = new AddressData();
        addressData.setAddressDetail("address");
        addressData.setDistrictData(districtData);
        addressData.setProvinceData(provinceData);
        addressData.setWardData(wardData);
        addressDataList.add(addressData);
        customerData.setId(1l);
        customerData.setName("moji");
        customerData.setPhone("04948564");
        customerData.setAddress(addressDataList);
        when(crmService.getCustomer(anyLong(), anyLong())).thenReturn(customerData);
        OrderSettingCustomerOptionModel optionModel = new OrderSettingCustomerOptionModel();
        optionModel.setId(2L);
        optionModel.setName("name option");
        when(settingCustomerOptionService.findAllByOrderId(anyLong())).thenReturn(Arrays.asList(optionModel));
    }

    @Test
    public void populate_HasNotTopping() {
        List<OrderEntryModel> orderEntryModels = new ArrayList<>();
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setProductId(1l);
        orderEntryModels.add(orderEntryModel);
        ProductSearchModel productDetailData = new ProductSearchModel();
        productDetailData.setId(1l);
        productDetailData.setBarcode("code");
        productDetailData.setName("name");
        productDetailData.setSku("ABCD");
        productDetailData.setId(1l);
        productDetailData.setDefaultImageUrl("\"http://image.moji.vn/yXfc84Ae0D-GVpON4ztOcTitj0A=/http://cdn.nhanh.vn/cdn/store/11146/ps/20170815/5_589x586.png\"");
        when(productSearchService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(productDetailData);
        when(orderEntryRepository.findAllByOrderCode(anyString())).thenReturn(orderEntryModels);
        when(toppingItemRepository.findAllByOrderId(any())).thenReturn(Collections.emptySet());

        populator.populate(source, target);
        assertEquals(1, target.getOrderEntries().size());
        assertEquals(3000,target.getTotalRewardAmount(),0);
        assertEquals(3,target.getRewardPoint(),0);
        assertEquals(5000, target.getRedeemAmount(),0);
        assertEquals(4000, target.getRefundAmount(),0);
        verify(crmService).getBasicCustomerInfo(anyLong(), anyLong());
        verify(calculationService).calculateFinalDiscountOfEntry(orderEntryModel);
    }

    @Test
    public void populate_HasTopping() {
        List<OrderEntryModel> orderEntryModels = new ArrayList<>();
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setProductId(1l);
        orderEntryModel.setQuantity(3l);
        ToppingOptionModel toppingOption1 = new ToppingOptionModel();
        toppingOption1.setQuantity(2);
        ToppingItemModel toppingItem1 = new ToppingItemModel();
        toppingItem1.setProductId(111l);
        toppingItem1.setQuantity(2);
        toppingItem1.setBasePrice(10000d);
        toppingItem1.setToppingOptionModel(toppingOption1);

        toppingOption1.setToppingItemModels(new HashSet<>(Arrays.asList(toppingItem1)));
        ToppingOptionModel toppingOption2 = new ToppingOptionModel();
        toppingOption2.setQuantity(1);
        ToppingItemModel toppingItem2 = new ToppingItemModel();
        toppingItem2.setProductId(111l);
        toppingItem2.setQuantity(1);
        toppingItem2.setBasePrice(10000d);
        toppingItem2.setToppingOptionModel(toppingOption2);
        toppingOption2.setToppingItemModels(new HashSet<>(Arrays.asList(toppingItem2)));

        orderEntryModel.setToppingOptionModels(new HashSet<>(Arrays.asList(toppingOption1, toppingOption2)));
        orderEntryModels.add(orderEntryModel);
        ProductSearchModel productDetailData = new ProductSearchModel();
        productDetailData.setId(1l);
        productDetailData.setBarcode("code");
        productDetailData.setName("name");
        productDetailData.setSku("ABCD");
        productDetailData.setId(1l);
        productDetailData.setDefaultImageUrl("\"http://image.moji.vn/yXfc84Ae0D-GVpON4ztOcTitj0A=/http://cdn.nhanh.vn/cdn/store/11146/ps/20170815/5_589x586.png\"");
        when(productSearchService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(productDetailData);
        when(orderEntryRepository.findAllByOrderCode(anyString())).thenReturn(orderEntryModels);
        when(toppingItemRepository.findAllByOrderId(any())).thenReturn(new HashSet<>(Arrays.asList(toppingItem1, toppingItem2)));

        populator.populate(source, target);
        assertEquals(2, target.getOrderEntries().size());
        assertEquals(5, target.getOrderEntries().get(1).getQuantity(), 0);
        assertEquals(10000d, target.getOrderEntries().get(1).getPrice(), 0);
        verify(crmService).getBasicCustomerInfo(anyLong(), anyLong());
        verify(calculationService).calculateFinalDiscountOfEntry(orderEntryModel);
    }

    @Test
    public void populate_HasCombo() {
        List<OrderEntryModel> orderEntryModels = new ArrayList<>();
        OrderEntryModel orderEntryModel = new OrderEntryModel();
        orderEntryModel.setId(1l);
        orderEntryModel.setProductId(1l);
        orderEntryModel.setQuantity(3l);
        SubOrderEntryModel subOrderEntryModel = new SubOrderEntryModel();
        subOrderEntryModel.setOrderEntry(orderEntryModel);
        subOrderEntryModel.setProductId(111l);
        subOrderEntryModel.setQuantity(2);
        subOrderEntryModel.setPrice(10000d);
        orderEntryModel.setSubOrderEntries(new HashSet<>(Arrays.asList(subOrderEntryModel)));

        orderEntryModels.add(orderEntryModel);
        ProductSearchModel productDetailData = new ProductSearchModel();
        productDetailData.setId(1l);
        productDetailData.setDtype(ProductDType.COMBO_MODEL.name());
        productDetailData.setBarcode("code");
        productDetailData.setName("name");
        productDetailData.setSku("ABCD");
        productDetailData.setDefaultImageUrl("\"http://image.moji.vn/yXfc84Ae0D-GVpON4ztOcTitj0A=/http://cdn.nhanh.vn/cdn/store/11146/ps/20170815/5_589x586.png\"");

        ProductSearchModel productDetailData1 = new ProductSearchModel();
        productDetailData1.setId(2l);
        productDetailData1.setBarcode("abc");
        productDetailData1.setName("product");
        productDetailData1.setSku("sku");
        when(productSearchService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(productDetailData, productDetailData1);
        when(orderEntryRepository.findAllByOrderCode(anyString())).thenReturn(orderEntryModels);
        when(subOrderEntryRepository.findAllByOrderId(any())).thenReturn(Arrays.asList(subOrderEntryModel));
        when(subOrderEntryRepository.findAllByOrderEntry(any())).thenReturn(Arrays.asList(subOrderEntryModel));

        populator.populate(source, target);
        assertEquals(1, target.getOrderEntries().size());
        assertEquals(1, target.getOrderEntries().get(0).getSubOrderEntries().size(), 0);
        verify(crmService).getBasicCustomerInfo(anyLong(), anyLong());
        verify(calculationService).calculateFinalDiscountOfEntry(orderEntryModel);
    }
}
