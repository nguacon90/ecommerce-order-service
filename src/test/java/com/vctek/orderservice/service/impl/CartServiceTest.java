package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.CartInfoParameter;
import com.vctek.orderservice.dto.request.UserHasWarehouseRequest;
import com.vctek.orderservice.feignclient.CheckPermissionClient;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.CartEntryModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.repository.CartEntryRepository;
import com.vctek.orderservice.repository.CartRepository;
import com.vctek.orderservice.service.GenerateCartCodeService;
import com.vctek.orderservice.service.ModelService;
import com.vctek.orderservice.service.ProductService;
import com.vctek.orderservice.util.PriceType;
import com.vctek.util.ComboType;
import com.vctek.util.OrderType;
import com.vctek.validate.Validator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class CartServiceTest {
    @Mock
    private CartRepository cartRepository;
    private CartServiceImpl service;
    @Mock
    private CartModel cart;
    private CartInfoParameter param;
    @Mock
    private ModelService modelService;
    @Mock
    private CheckPermissionClient checkPermissionClient;
    @Mock
    private Validator<CartInfoParameter> createCartValidator;
    @Mock
    private GenerateCartCodeService generateCartCodeService;
    @Mock
    private CartEntryRepository cartEntryRepository;
    @Mock
    private AbstractOrderEntryModel entryMock;
    @Mock
    private ProductService productService;
    private List<AbstractOrderEntryModel> entries = new ArrayList<>();
    private CartInfoParameter cartParam = new CartInfoParameter();
    private ArgumentCaptor<CartModel> cartCapture = ArgumentCaptor.forClass(CartModel.class);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        param = new CartInfoParameter();
        param.setCompanyId(1l);
        param.setUserId(2l);
        param.setWarehouseId(22l);
        param.setOrderType(OrderType.RETAIL.toString());
        service = new CartServiceImpl(cartRepository, generateCartCodeService, cartEntryRepository);
        service.setCreateCartValidator(createCartValidator);
        service.setProductService(productService);
        service.setModelService(modelService);
        when(generateCartCodeService.generateCartCode(any())).thenReturn("code");
        when(cartRepository.save(any())).thenReturn(new CartModel());
    }

    @Test
    public void addNewEntry_addFirstWithNormalProduct() {
        when(productService.isFnB(anyLong())).thenReturn(false);
        AbstractOrderEntryModel entry = new CartEntryModel();
        entry.setEntryNumber(0);
        entries.add(entry);
        when(cart.getEntries()).thenReturn(entries);
        CartEntryModel entryModel = service.addNewEntry(cart, 1l, 2, false);
        assertNotNull(entryModel);
        assertEquals(1, entry.getEntryNumber(), 0);
        verify(modelService, times(2)).save(any());
    }

    @Test
    public void addNewEntry_addFirstWithNormalProduct_ImportIsTrue() {
        when(productService.isFnB(anyLong())).thenReturn(false);
        AbstractOrderEntryModel entry = new CartEntryModel();
        entry.setEntryNumber(0);
        entries.add(entry);
        when(cart.getEntries()).thenReturn(entries);
        CartEntryModel entryModel = service.addNewEntry(cart, 1l, 2, true);
        assertNotNull(entryModel);
        assertEquals(1, entry.getEntryNumber(), 0);
        verify(modelService, times(0)).save(any());
    }

    @Test
    public void addNewEntry_addLastWithFnBProduct() {
        when(productService.isFnB(anyLong())).thenReturn(true);
        AbstractOrderEntryModel entry = new CartEntryModel();
        entry.setEntryNumber(0);
        entries.add(entry);
        when(cart.getEntries()).thenReturn(entries);
        CartEntryModel entryModel = service.addNewEntry(cart, 1l, 2, false);
        assertNotNull(entryModel);
        assertEquals(0, entry.getEntryNumber(), 0);
        verify(modelService, times(0)).save(any());
    }

    @Test
    public void findAllOrCreateNewByCreatedByUser_createNew() {
        when(checkPermissionClient.userHasWarehouse(any(UserHasWarehouseRequest.class))).thenReturn(true);
        when(cartRepository.findAllByCreateByUserAndCompanyIdAndType(anyLong(), anyLong(), anyString()))
                .thenReturn(null);
        cart.setId(2l);
        when(cartRepository.save(any(CartModel.class))).thenReturn(cart);
        List<CartModel> carts = service.findAllOrCreateNewByCreatedByUser(param);
        assertEquals(1, carts.size());
        verify(cartRepository, times(2)).save(any(CartModel.class));
    }

    @Test
    public void findAllOrCreateNewByCreatedByUser_NotCreateNew() {
        when(cartRepository.findAllByCreateByUserAndTypeAndExchangeAndCompanyIdAndSellSignal(anyLong(), anyString(), eq(false), anyLong(), anyString()))
                .thenReturn(Arrays.asList(new CartModel()));
        List<CartModel> carts = service.findAllOrCreateNewByCreatedByUser(param);
        assertEquals(1, carts.size());
        verify(cartRepository, times(0)).save(any(CartModel.class));
    }

    @Test
    public void findBy() {
        param.setCode("22");
        service.findByIdAndCompanyIdAndTypeAndCreateByUser(param);
        verify(cartRepository).findByCodeAndCompanyIdAndTypeAndCreateByUser(anyString(), anyLong(), anyString(), anyLong());
    }

    @Test
    public void delete() {
        service.delete(cart);
        verify(cartRepository).delete(cart);
    }

    @Test
    public void findByIdAndUserId() {
        service.findByCodeAndUserIdAndCompanyId("1", 1l, 1l);
        verify(cartRepository).findByCodeAndCreateByUserAndCompanyId("1", 1l, 1l);
    }

    @Test
    public void refresh() {
        service.refresh(cart);
        verify(modelService).refresh(cart);
    }

    @Test
    public void save() {
        service.save(cart);
        verify(modelService).save(cart);
    }

    @Test
    public void isComboEntry_returnTrue() {
        when(entryMock.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        assertTrue(service.isComboEntry(entryMock));
    }

    @Test
    public void isComboEntry_returnTrue_notEmptySubEntries() {
        when(entryMock.getComboType()).thenReturn(null);
        when(entryMock.getSubOrderEntries()).thenReturn(new LinkedHashSet(Arrays.asList(new CartEntryModel())));
        assertTrue(service.isComboEntry(entryMock));
    }

    @Test
    public void isComboEntry_returnFalse() {
        when(entryMock.getComboType()).thenReturn(null);
        when(entryMock.getSubOrderEntries()).thenReturn(new HashSet<>());
        assertFalse(service.isComboEntry(entryMock));
    }

    @Test
    public void getOrCreateNewCart_RetailCart() {
        cartParam.setOrderType(OrderType.RETAIL.toString());
        service.getOrCreateNewCart(cartParam);
        verify(cartRepository, times(2)).save(cartCapture.capture());
        assertEquals(PriceType.RETAIL_PRICE.toString(), cartCapture.getValue().getPriceType());
    }

    @Test
    public void getOrCreateNewCart_OnlineCart_HasNotPriceType() {
        cartParam.setOrderType(OrderType.ONLINE.toString());
        service.getOrCreateNewCart(cartParam);
        verify(cartRepository, times(2)).save(cartCapture.capture());
        assertEquals(PriceType.RETAIL_PRICE.toString(), cartCapture.getValue().getPriceType());
    }

    @Test
    public void getOrCreateNewCart_OnlineCart_WithPriceType() {
        cartParam.setOrderType(OrderType.ONLINE.toString());
        cartParam.setPriceType(PriceType.WHOLESALE_PRICE.toString());
        service.getOrCreateNewCart(cartParam);
        verify(cartRepository, times(2)).save(cartCapture.capture());
        assertEquals(PriceType.WHOLESALE_PRICE.toString(), cartCapture.getValue().getPriceType());
    }
}
