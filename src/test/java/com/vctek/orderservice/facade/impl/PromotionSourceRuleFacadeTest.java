package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.dto.FileParameter;
import com.vctek.dto.promotion.PromotionSourceRuleDTO;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.PromotionRuleSearchParam;
import com.vctek.orderservice.dto.excel.PromotionExcelData;
import com.vctek.orderservice.dto.request.PromotionStatusRequest;
import com.vctek.orderservice.event.PublishPromotionSourceRuleEvent;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.DownloadExcelFacade;
import com.vctek.orderservice.facade.PermissionFacade;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionSourceRuleService;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.service.DroolsKIEModuleService;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerService;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.PromotionSourceRuleData;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleStatus;
import com.vctek.orderservice.service.CouponService;
import com.vctek.orderservice.service.PromotionBudgetService;
import com.vctek.service.UserService;
import com.vctek.validate.Validator;
import io.netty.util.concurrent.SingleThreadEventExecutor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class PromotionSourceRuleFacadeTest {
    private PromotionSourceRuleFacadeImpl facade;
    @Mock
    private Converter<PromotionSourceRuleData, PromotionSourceRuleModel> promotionSourceRuleConverter;
    @Mock
    private Converter<PromotionSourceRuleDTO, PromotionSourceRuleData> promotionSourceRuleDataConverter;
    @Mock
    private Converter<PromotionSourceRuleModel, PromotionSourceRuleDTO> basicPromotionSourceRuleConverter;
    @Mock
    private PromotionSourceRuleService promotionSourceRuleService;
    @Mock
    private RuleCompilerService ruleCompilerService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private PromotionSourceRuleDTO dto;
    @Mock
    private PromotionSourceRuleData promotionSourceRuleData;
    @Mock
    private PromotionSourceRuleModel model;
    @Mock
    private PromotionSourceRuleModel sourceRuleModel;
    @Mock
    private DroolsKIEModuleService droolsKIEModuleService;
    @Mock
    private PromotionRuleSearchParam params;
    @Mock
    private PromotionStatusRequest request;
    @Mock
    private Populator<PromotionSourceRuleModel, PromotionSourceRuleDTO> promotionSourceRuleConditionPopulator;
    @Mock
    private Populator<PromotionSourceRuleModel, PromotionSourceRuleDTO> promotionSourceRuleActionPopulator;
    @Mock
    private Populator<PromotionSourceRuleData, PromotionSourceRuleModel> promotionSourceRuleModelPopulator;
    @Mock
    private DroolsKIEModuleModel droolsRuleModel;
    @Mock
    private Validator<PromotionSourceRuleData> promotionSourceRuleDataValidator;
    @Mock
    private CouponService couponService;
    private String kieModuleName = "promotion-module";
    @Mock
    private DroolsKIEModuleModel kieModule;
    @Mock
    private UserService userService;
    @Mock
    private PermissionFacade permissionFacade;
    @Mock
    private DownloadExcelFacade downloadExcelFacade;
    @Mock
    private Converter<PromotionSourceRuleDTO, PromotionExcelData> promotionExcelDataConverter;
    private Executor exportExcelExecutor;
    @Mock
    private PromotionBudgetService promotionBudgetService;
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        exportExcelExecutor = Executors.newFixedThreadPool(1);
        facade = new PromotionSourceRuleFacadeImpl();
        facade.setApplicationEventPublisher(applicationEventPublisher);
        facade.setPromotionSourceRuleConverter(promotionSourceRuleConverter);
        facade.setPromotionSourceRuleService(promotionSourceRuleService);
        facade.setRuleCompilerService(ruleCompilerService);
        facade.setPromotionSourceRuleDataConverter(promotionSourceRuleDataConverter);
        facade.setDroolsKIEModuleService(droolsKIEModuleService);
        facade.setBasicPromotionSourceRuleConverter(basicPromotionSourceRuleConverter);
        facade.setPromotionSourceRuleModelPopulator(promotionSourceRuleModelPopulator);
        facade.setPromotionSourceRuleActionPopulator(promotionSourceRuleActionPopulator);
        facade.setPromotionSourceRuleConditionPopulator(promotionSourceRuleConditionPopulator);
        facade.setCouponService(couponService);
        facade.setPromotionSourceRuleDataValidator(promotionSourceRuleDataValidator);
        facade.setUserService(userService);
        facade.setPermissionFacade(permissionFacade);
        facade.setDownloadExcelFacade(downloadExcelFacade);
        facade.setPromotionExcelDataConverter(promotionExcelDataConverter);
        facade.setExportExcelExecutor(exportExcelExecutor);
        facade.setPromotionBudgetService(promotionBudgetService);
        when(model.getCompanyId()).thenReturn(1l);
        DroolsKIEModuleModel value = new DroolsKIEModuleModel();
        value.setName("promotion-module");
        when(droolsKIEModuleService.findByName(anyString())).thenReturn(value);
        when(droolsKIEModuleService.findByCompanyId(anyLong())).thenReturn(kieModule);
        when(kieModule.getName()).thenReturn(kieModuleName);
        when(userService.getCurrentUserId()).thenReturn(1l);
        when(permissionFacade.checkPermission(anyString(), anyLong(), anyLong())).thenReturn(true);
    }

    @Test
    public void createNewActiveRule() {
        when(promotionSourceRuleDataConverter.convert(dto)).thenReturn(promotionSourceRuleData);
        when(promotionSourceRuleConverter.convert(promotionSourceRuleData)).thenReturn(model);
        when(promotionSourceRuleService.save(model)).thenReturn(sourceRuleModel);
        DroolsRuleModel value = new DroolsRuleModel();
        value.setActive(true);
        when(ruleCompilerService.compile(eq(sourceRuleModel), anyString())).thenReturn(value);
        facade.createNew(dto);
        verify(ruleCompilerService).compile(eq(sourceRuleModel), anyString());
        verify(applicationEventPublisher).publishEvent(any(PublishPromotionSourceRuleEvent.class));
    }

    @Test
    public void createNewInActiveRule() {
        when(promotionSourceRuleDataConverter.convert(dto)).thenReturn(promotionSourceRuleData);
        when(promotionSourceRuleConverter.convert(promotionSourceRuleData)).thenReturn(model);
        when(promotionSourceRuleService.save(model)).thenReturn(sourceRuleModel);
        DroolsRuleModel value = new DroolsRuleModel();
        value.setActive(false);
        when(ruleCompilerService.compile(eq(sourceRuleModel), anyString())).thenReturn(value);
        facade.createNew(dto);
        verify(ruleCompilerService).compile(eq(sourceRuleModel), anyString());
        verify(applicationEventPublisher, times(0)).publishEvent(any(PublishPromotionSourceRuleEvent.class));
    }

    @Test
    public void findAll_Empty() {
        Page<PromotionSourceRuleModel> data = new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 10), 1);
        when(promotionSourceRuleService.findAll(params)).thenReturn(data);

        facade.findAll(params);
        verify(basicPromotionSourceRuleConverter, times(0)).convertAll(anyList());
    }

    @Test
    public void findAll_NotEmpty() {
        Page<PromotionSourceRuleModel> data = new PageImpl<>(Arrays.asList(new PromotionSourceRuleModel()),
                PageRequest.of(1, 10), 1);
        when(promotionSourceRuleService.findAll(params)).thenReturn(data);

        facade.findAll(params);
        verify(basicPromotionSourceRuleConverter).convertAll(anyList());
    }

    @Test
    public void changeStatus_expiredRule() {
        when(request.getCompanyId()).thenReturn(1l);
        when(request.getPromotionId()).thenReturn(1l);
        when(promotionSourceRuleService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(model);
        when(promotionSourceRuleService.isExpired(model)).thenReturn(true);

        facade.changeStatus(request);
        verify(model).setStatus(RuleStatus.INACTIVE.toString());
        verify(promotionSourceRuleService, times(2)).save(model);
        verify(applicationEventPublisher, times(0)).publishEvent(any(PublishPromotionSourceRuleEvent.class));
    }

    @Test
    public void changeStatus_emptyDroolRules() {
        when(request.getCompanyId()).thenReturn(1l);
        when(request.getPromotionId()).thenReturn(1l);
        when(promotionSourceRuleService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(model);
        when(promotionSourceRuleService.isExpired(model)).thenReturn(false);
        when(model.getDroolsRules()).thenReturn(new HashSet<>());

        facade.changeStatus(request);
        verify(model).setStatus(RuleStatus.INACTIVE.toString());
        verify(promotionSourceRuleService, times(2)).save(model);
        verify(applicationEventPublisher, times(0)).publishEvent(any(PublishPromotionSourceRuleEvent.class));
    }

    @Test
    public void changeStatus_republishRule() {
        when(request.getCompanyId()).thenReturn(1l);
        when(request.getPromotionId()).thenReturn(1l);
        when(promotionSourceRuleService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(model);
        when(promotionSourceRuleService.isExpired(model)).thenReturn(false);
        Set<DroolsRuleModel> droolsRules = new HashSet<>();
        droolsRules.add(new DroolsRuleModel());
        when(model.getDroolsRules()).thenReturn(droolsRules);

        facade.changeStatus(request);
        verify(model, times(0)).setStatus(RuleStatus.INACTIVE.toString());
        verify(promotionSourceRuleService, times(1)).save(model);
        verify(applicationEventPublisher, times(1)).publishEvent(any(PublishPromotionSourceRuleEvent.class));
    }

    @Test
    public void findById_notFound() {
        try {
            when(promotionSourceRuleService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(null);
            facade.findBy(1l, 1l);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.NOT_FOUND_DATA.code(), e.getCode());
        }
    }

    @Test
    public void findById() {
        when(promotionSourceRuleService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(model);
        when(basicPromotionSourceRuleConverter.convert(model)).thenReturn(dto);
        facade.findBy(1l, 1l);
        verify(basicPromotionSourceRuleConverter).convert(model);
        verify(promotionSourceRuleConditionPopulator).populate(model, dto);
        verify(promotionSourceRuleActionPopulator).populate(model, dto);
    }

    @Test
    public void update_notFound() {
        try {
            when(dto.getCompanyId()).thenReturn(1l);
            when(dto.getId()).thenReturn(1l);
            when(promotionSourceRuleService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(null);
            facade.update(dto);
            fail("Must throw exception");
        } catch (ServiceException e) {
            assertEquals(ErrorCodes.INVALID_PROMOTION_SOURCE_RULE_ID.code(), e.getCode());
        }
    }

    @Test
    public void update_expiredRule() {
        when(dto.getCompanyId()).thenReturn(1l);
        when(dto.getId()).thenReturn(1l);
        when(promotionSourceRuleService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(model);
        when(promotionSourceRuleService.isExpired(model)).thenReturn(true);
        when(promotionSourceRuleDataConverter.convert(dto)).thenReturn(promotionSourceRuleData);
        when(droolsKIEModuleService.findByName(anyString())).thenReturn(droolsRuleModel);
        when(promotionSourceRuleService.save(model)).thenReturn(model);

        facade.update(dto);
        verify(promotionSourceRuleModelPopulator).populate(promotionSourceRuleData, model);
        verify(ruleCompilerService).compile(model, kieModuleName);
        verify(applicationEventPublisher, times(0))
                .publishEvent(any(PublishPromotionSourceRuleEvent.class));
    }

    @Test
    public void update_activeRule() {
        when(dto.getCompanyId()).thenReturn(1l);
        when(dto.getId()).thenReturn(1l);
        when(promotionSourceRuleService.findByIdAndCompanyId(anyLong(), anyLong())).thenReturn(model);
        when(promotionSourceRuleService.isExpired(model)).thenReturn(false);
        when(promotionSourceRuleDataConverter.convert(dto)).thenReturn(promotionSourceRuleData);
        when(droolsKIEModuleService.findByName(anyString())).thenReturn(droolsRuleModel);
        when(promotionSourceRuleService.save(model)).thenReturn(model);

        facade.update(dto);
        verify(promotionSourceRuleModelPopulator).populate(promotionSourceRuleData, model);
        verify(ruleCompilerService).compile(model, kieModuleName);
        verify(applicationEventPublisher).publishEvent(any(PublishPromotionSourceRuleEvent.class));
    }

    @Test
    public void exportCurrentPage_emptyData() {
        when(promotionSourceRuleService.findAll(any(PromotionRuleSearchParam.class))).thenReturn(Page.empty());

        byte[] bytes = facade.exportCurrentPage(params);
        assertTrue(bytes.length > 0);
    }

    @Test
    public void exportCurrentPage_hasData() {
        when(promotionSourceRuleService.findAll(any(PromotionRuleSearchParam.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(new PromotionSourceRuleModel())));
        when(basicPromotionSourceRuleConverter.convertAll(anyList())).thenReturn(Arrays.asList(new PromotionSourceRuleDTO()));
        byte[] bytes = facade.exportCurrentPage(params);
        assertTrue(bytes.length > 0);
    }

    @Test
    public void exportAllPage_isProcessing() {
        when(downloadExcelFacade.isProcessingExportExcel(any(FileParameter.class))).thenReturn(true);
        when(promotionSourceRuleService.findAll(any(PromotionRuleSearchParam.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(new PromotionSourceRuleModel())), Page.empty());
        when(basicPromotionSourceRuleConverter.convertAll(anyList())).thenReturn(Arrays.asList(new PromotionSourceRuleDTO()));
        when(downloadExcelFacade.getFileParameter(any(), anyLong())).thenReturn(new FileParameter());

        facade.doExportAllPage(params);
        verify(downloadExcelFacade, times(0)).setProcessExportExcel(any(FileParameter.class), eq(true));
    }

    @Test
    public void exportAllPage() {
        when(downloadExcelFacade.isProcessingExportExcel(any(FileParameter.class))).thenReturn(false);
        when(promotionSourceRuleService.findAll(any(PromotionRuleSearchParam.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(new PromotionSourceRuleModel())), Page.empty());
        when(basicPromotionSourceRuleConverter.convertAll(anyList())).thenReturn(Arrays.asList(new PromotionSourceRuleDTO()));
        when(downloadExcelFacade.getFileParameter(any(), anyLong())).thenReturn(new FileParameter());

        facade.doExportAllPage(params);
        verify(downloadExcelFacade).setProcessExportExcel(any(FileParameter.class), eq(true));
    }
}
