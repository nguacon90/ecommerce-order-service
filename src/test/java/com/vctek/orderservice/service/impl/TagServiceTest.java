package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.TagData;
import com.vctek.orderservice.model.TagModel;
import com.vctek.orderservice.repository.TagRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TagServiceTest {
    @Mock
    private TagRepository repository;
    private TagServiceImpl service;
    private TagModel model;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new TagServiceImpl(repository);
        model = new TagModel();
    }

    @Test
    public void save() {
        System.out.println(Integer.MAX_VALUE);
        service.save(model);
        verify(repository).save(any(TagModel.class));
    }

    @Test
    public void findByIdAndCompanyId() {
        service.findByIdAndCompanyId(2L, 2L);
        verify(repository).findByIdAndCompanyId(anyLong(), anyLong());
    }

    @Test
    public void findAllBy_emptyName() {
        TagData tagData = new TagData();
        tagData.setCompanyId(2L);
        service.findAllBy(tagData, PageRequest.of(0, 20));
        verify(repository, times(1)).findAllByCompanyId(anyLong(), any(Pageable.class));
        verify(repository, times(0)).findAllByNameContainingAndCompanyId(anyString(), anyLong(), any(Pageable.class));
    }

    @Test
    public void findAllBy_notEmptyName() {
        TagData tagData = new TagData();
        tagData.setCompanyId(2L);
        tagData.setName("name");
        service.findAllBy(tagData, PageRequest.of(0, 20));
        verify(repository, times(0)).findAllByCompanyId(anyLong(), any(Pageable.class));
        verify(repository, times(1)).findAllByNameContainingAndCompanyId(anyString(), anyLong(), any(Pageable.class));
    }

    @Test
    public void findByCompanyIdAndName() {
        service.findByCompanyIdAndName(2L, "name");
        verify(repository).findByCompanyIdAndName(anyLong(), anyString());
    }
}

