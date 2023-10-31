package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.TagData;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.TagModel;
import com.vctek.orderservice.service.TagService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class TagRequestValidatorTest {
    private TagRequestValidator validator;
    @Mock
    private TagService service;
    private TagData request;
    @Mock
    private TagModel existedTagMock;

    @Before
    public void setUp() {
        request = new TagData();
        MockitoAnnotations.initMocks(this);
        validator = new TagRequestValidator();
        validator.setMaxTagLength(5);
        validator.setService(service);
    }

    @Test
    public void validate_emptyCompanyId() {
        try {
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_COMPANY_ID.message(), e.getMessage());
        }
    }

    @Test
    public void validate_emptyName() {
        try {
            request.setCompanyId(2l);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EMPTY_TAG_NAME.message(), e.getMessage());
        }
    }

    @Test
    public void validate_NameOverMaxLength() {
        try {
            request.setCompanyId(2l);
            request.setName("123456");
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.TAG_NAME_OVER_MAX_LENGTH.message(), e.getMessage());
        }
    }

    @Test
    public void validate_ExistedTagName() {
        try {
            request.setCompanyId(2l);
            request.setName("12345");
            when(existedTagMock.getId()).thenReturn(222l);
            when(service.findByCompanyIdAndName(anyLong(), anyString())).thenReturn(Arrays.asList(existedTagMock));
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EXISTED_TAG_NAME.message(), e.getMessage());
        }
    }

    @Test
    public void validate_Update_ExistedTagName() {
        try {
            request.setCompanyId(2l);
            request.setName("12345");
            request.setId(111l);
            when(existedTagMock.getId()).thenReturn(222l);
            when(service.findByCompanyIdAndName(anyLong(), anyString())).thenReturn(Arrays.asList(existedTagMock));
            when(service.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(existedTagMock);
            validator.validate(request);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.EXISTED_TAG_NAME.message(), e.getMessage());
        }
    }

    @Test
    public void validate_create_success() {
        request.setCompanyId(2l);
        request.setName("12345");
        when(existedTagMock.getId()).thenReturn(222l);
        when(service.findByCompanyIdAndName(anyLong(), anyString())).thenReturn(new ArrayList<>());
        validator.validate(request);
        assertTrue("success", true);
    }

    @Test
    public void validate_update_success() {
        request.setCompanyId(2l);
        request.setName("12345");
        request.setId(222l);
        when(existedTagMock.getId()).thenReturn(222l);
        when(service.findByCompanyIdAndName(anyLong(), anyString())).thenReturn(Arrays.asList(existedTagMock));
        when(service.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(existedTagMock);
        validator.validate(request);
        assertTrue("success", true);
    }
}
