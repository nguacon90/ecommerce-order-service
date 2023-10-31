package com.vctek.orderservice.validator.storefront;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CommerceCartModification;
import com.vctek.orderservice.dto.CommerceCartValidateData;
import com.vctek.orderservice.dto.CommerceEntryError;
import com.vctek.orderservice.dto.request.AddressRequest;
import com.vctek.orderservice.dto.request.CustomerRequest;
import com.vctek.orderservice.dto.request.storefront.StoreFrontCheckoutRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionResultService;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionSourceRuleService;
import com.vctek.orderservice.service.CommerceCartService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class StorefrontCheckoutValidatorTest {
    private StorefrontCheckoutValidator validator;
    @Mock
    private CommerceCartService commerceCartService;
    private String phoneRegex = "^(09|03|07|08|05)+([0-9]{8})$";
    private StoreFrontCheckoutRequest request;
    private CartModel cartModel;
    private CustomerRequest customerRequest;
    private List<AbstractOrderEntryModel> entries;
    @Mock
    private PromotionResultService promotionResultService;
    @Mock
    private PromotionSourceRuleService promotionSourceRuleService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new StorefrontCheckoutValidator();
        validator.setPromotionResultService(promotionResultService);
        validator.setPromotionSourceRuleService(promotionSourceRuleService);
        validator.setPhoneRegex(phoneRegex);
        validator.setCommerceCartService(commerceCartService);
        request = new StoreFrontCheckoutRequest();
        request.setCompanyId(2L);
        entries = new ArrayList<>();
        cartModel = new CartModel();
        cartModel.setEntries(entries);
        customerRequest = new CustomerRequest();

    }

    @Test
    public void validate_invalidCartCode() {
        try {
            validator.validate(request);
            fail("Throw new Exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_CART_ID.message(), e.getMessage());
        }
    }

    @Test
    public void validate_invalidCartModel() {
        try {
            request.setCode("code");
            when(commerceCartService.getStorefrontCart(anyString(), anyLong())).thenReturn(null);
            validator.validate(request);
            fail("Throw new Exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_EXISTED_ORDER_ID.message(), e.getMessage());
        }
    }

    @Test
    public void validateCustomerInfo_null() {
        try {
            request.setCode("code");
            when(commerceCartService.getStorefrontCart(anyString(), anyLong())).thenReturn(cartModel);
            validator.validate(request);
            fail("Throw new Exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_ORDER_CUSTOMER_INFO.message(), e.getMessage());
        }
    }

    @Test
    public void validateCustomerInfo_emptyName() {
        try {
            request.setCode("code");
            request.setCustomer(customerRequest);
            when(commerceCartService.getStorefrontCart(anyString(), anyLong())).thenReturn(cartModel);
            validator.validate(request);
            fail("Throw new Exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_ORDER_CUSTOMER_INFO.message(), e.getMessage());
        }
    }

    @Test
    public void validateCustomerInfo_ShippingAddress_null() {
        try {
            customerRequest.setName("name");
            customerRequest.setPhone("0903021034");
            request.setCode("code");
            request.setCustomer(customerRequest);
            when(commerceCartService.getStorefrontCart(anyString(), anyLong())).thenReturn(cartModel);
            validator.validate(request);
            fail("Throw new Exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_SHIPPING_PROVINCE_ID.message(), e.getMessage());
        }
    }

    @Test
    public void validateCustomerInfo_ShippingAddress_ProvinceId_null() {
        try {
            AddressRequest addressRequest = new AddressRequest();
            customerRequest.setShippingAddress(addressRequest);
            customerRequest.setName("name");
            customerRequest.setPhone("0903021034");
            request.setCode("code");
            request.setCustomer(customerRequest);
            when(commerceCartService.getStorefrontCart(anyString(), anyLong())).thenReturn(cartModel);
            validator.validate(request);
            fail("Throw new Exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_SHIPPING_PROVINCE_ID.message(), e.getMessage());
        }
    }

    @Test
    public void validateCustomerInfo_ShippingAddress_DistrictId_null() {
        try {
            AddressRequest addressRequest = new AddressRequest();
            addressRequest.setProvinceId(2L);
            customerRequest.setShippingAddress(addressRequest);
            customerRequest.setName("name");
            customerRequest.setPhone("0903021034");
            request.setCode("code");
            request.setCustomer(customerRequest);
            when(commerceCartService.getStorefrontCart(anyString(), anyLong())).thenReturn(cartModel);
            validator.validate(request);
            fail("Throw new Exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_SHIPPING_DISTRICT_ID.message(), e.getMessage());
        }
    }

    @Test
    public void validateCustomerInfo_ShippingAddress_AddressDetail_null() {
        try {
            AddressRequest addressRequest = new AddressRequest();
            addressRequest.setProvinceId(2L);
            addressRequest.setDistrictId(2L);
            customerRequest.setShippingAddress(addressRequest);
            customerRequest.setName("name");
            customerRequest.setPhone("0903021034");
            request.setCode("code");
            request.setCustomer(customerRequest);
            when(commerceCartService.getStorefrontCart(anyString(), anyLong())).thenReturn(cartModel);
            validator.validate(request);
            fail("Throw new Exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_SHIPPING_ADDRESS_DETAIL.message(), e.getMessage());
        }
    }

    @Test
    public void validateEntries_empty() {
        try {
            AddressRequest addressRequest = new AddressRequest();
            addressRequest.setProvinceId(2L);
            addressRequest.setDistrictId(2L);
            addressRequest.setAddressDetail("12A");
            addressRequest.setPhone1("0903021034");
            customerRequest.setShippingAddress(addressRequest);
            customerRequest.setName("name");
            customerRequest.setPhone("0903021034");
            request.setCode("code");
            request.setCustomer(customerRequest);
            when(commerceCartService.getStorefrontCart(anyString(), anyLong())).thenReturn(cartModel);
            validator.validate(request);
            fail("Throw new Exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.CART_HAS_NOT_ENTRIES.message(), e.getMessage());
        }
    }

    @Test
    public void validateEntries_SALE_OFF_QUANTITY_OVER_AVAILABLE_STOCK() {
        try {
            AbstractOrderEntryModel entryModel = new AbstractOrderEntryModel();
            entryModel.setId(2L);
            entryModel.setProductId(2L);
            entries.add(entryModel);
            AddressRequest addressRequest = new AddressRequest();
            addressRequest.setProvinceId(2L);
            addressRequest.setDistrictId(2L);
            addressRequest.setAddressDetail("12A");
            addressRequest.setPhone1("0903021034");
            customerRequest.setShippingAddress(addressRequest);
            customerRequest.setName("name");
            customerRequest.setPhone("0903021034");
            request.setCode("code");
            request.setCustomer(customerRequest);
            CommerceCartModification commerceCartModification = new CommerceCartModification();
            commerceCartModification.setUpdatePrice(false);
            CommerceCartValidateData commerceCartValidateData = new CommerceCartValidateData();
            commerceCartValidateData.setHasError(true);
            Map<Long, CommerceEntryError> commerceEntryErrorMap = new HashMap<>();
            CommerceEntryError commerceEntryError = new CommerceEntryError();
            commerceEntryError.setHasError(true);
            commerceEntryErrorMap.put(2L, commerceEntryError);
            commerceCartValidateData.setEntryErrors(commerceEntryErrorMap);
            when(commerceCartService.getStorefrontCart(anyString(), anyLong())).thenReturn(cartModel);
            when(commerceCartService.updateLatestPriceForEntries(any(CartModel.class))).thenReturn(commerceCartModification);
            when(commerceCartService.validate(any())).thenReturn(commerceCartValidateData);
            validator.validate(request);
            fail("Throw new Exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.SWITCH_SALE_OFF_QUANTITY_OVER_AVAILABLE_STOCK.message(), e.getMessage());
        }
    }

    @Test
    public void validate() {
        AbstractOrderEntryModel entryModel = new AbstractOrderEntryModel();
        entryModel.setId(2L);
        entryModel.setProductId(2L);
        entryModel.setQuantity(2L);
        entries.add(entryModel);
        AddressRequest addressRequest = new AddressRequest();
        addressRequest.setProvinceId(2L);
        addressRequest.setDistrictId(2L);
        addressRequest.setAddressDetail("12A");
        addressRequest.setPhone1("0903021034");
        customerRequest.setShippingAddress(addressRequest);
        customerRequest.setName("name");
        customerRequest.setPhone("0903021034");
        request.setCode("code");
        request.setCustomer(customerRequest);
        request.setShippingFeeSettingId(2L);
        request.setDeliveryCost(20000d);
        CommerceCartModification commerceCartModification = new CommerceCartModification();
        commerceCartModification.setUpdatePrice(false);
        when(commerceCartService.getStorefrontCart(anyString(), anyLong())).thenReturn(cartModel);
        when(commerceCartService.updateLatestPriceForEntries(any(CartModel.class))).thenReturn(commerceCartModification);
        when(commerceCartService.validate(any())).thenReturn(null);
        validator.validate(request);
        assertTrue(true);
    }
}
