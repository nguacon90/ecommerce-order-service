package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.orderservice.dto.TagData;
import com.vctek.orderservice.model.TagModel;
import com.vctek.orderservice.service.TagService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TagFacadeTest {
    private TagFacadeImpl facade;
    private TagData request;
    @Mock
    private ArgumentCaptor<TagModel> captor;
    @Mock
    private TagService service;
    @Mock
    private Converter<TagModel, TagData> converter;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        captor = ArgumentCaptor.forClass(TagModel.class);
        facade = new TagFacadeImpl();
        facade.setTagService(service);
        facade.setTagDataConverter(converter);
        request = new TagData();
        request.setCompanyId(1l);
        request.setName("name");
    }

    @Test
    public void create() {
        when(service.save(any())).thenReturn(new TagModel());
        facade.createOrUpdate(request);
        verify(service).save(captor.capture());
        TagModel tagModel = captor.getValue();
        assertEquals("name", tagModel.getName());
        assertEquals(1L, tagModel.getCompanyId(), 0);
    }

    @Test
    public void update() {
        request.setId(1l);
        when(service.save(any())).thenReturn(new TagModel());
        when(service.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(new TagModel());
        facade.createOrUpdate(request);
        verify(service).save(captor.capture());
        verify(service).findByIdAndCompanyId(anyLong(), anyLong());
    }

    @Test
    public void findAllBy_empty() {
        TagData tagData = new TagData();
        tagData.setCompanyId(2L);
        when(service.findAllBy(any(), any())).thenReturn(Page.empty());
        Page<TagData> data = facade.findAllBy(tagData, PageRequest.of(0, 20));
        verify(service).findAllBy(any(TagData.class), any(Pageable.class));
        assertEquals(0, data.getContent().size());
    }

    @Test
    public void findAllBy() {
        TagData tagData = new TagData();
        tagData.setCompanyId(2L);
        when(service.findAllBy(any(), any())).thenReturn(new PageImpl<>(Arrays.asList(new TagModel()), PageRequest.of(0, 20), 1));
        when(converter.convertAll(anyCollection())).thenReturn(Arrays.asList(new TagData()));
        Page<TagData> data = facade.findAllBy(tagData, PageRequest.of(0, 20));
        verify(service).findAllBy(any(TagData.class), any(Pageable.class));
        assertEquals(1, data.getContent().size());
    }
}
