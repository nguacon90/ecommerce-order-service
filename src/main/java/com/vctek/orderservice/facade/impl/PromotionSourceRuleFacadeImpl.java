package com.vctek.orderservice.facade.impl;

import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.dto.FileParameter;
import com.vctek.dto.promotion.PromotionSourceRuleDTO;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CommercePromotionData;
import com.vctek.orderservice.dto.PromotionRuleSearchParam;
import com.vctek.orderservice.dto.excel.PromotionExcelData;
import com.vctek.orderservice.dto.request.PromotionStatusRequest;
import com.vctek.orderservice.event.PublishPromotionSourceRuleEvent;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.DownloadExcelFacade;
import com.vctek.orderservice.facade.PermissionFacade;
import com.vctek.orderservice.facade.PromotionSourceRuleFacade;
import com.vctek.orderservice.model.OrderStorefrontSetupModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.service.PromotionSourceRuleService;
import com.vctek.orderservice.promotionengine.ruleengine.exception.RuleCompilerException;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsKIEModuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.model.DroolsRuleModel;
import com.vctek.orderservice.promotionengine.ruleengine.service.DroolsKIEModuleService;
import com.vctek.orderservice.promotionengine.ruleengineservice.compiler.RuleCompilerService;
import com.vctek.orderservice.promotionengine.ruleengineservice.data.PromotionSourceRuleData;
import com.vctek.orderservice.promotionengine.ruleengineservice.enums.RuleStatus;
import com.vctek.orderservice.service.CouponService;
import com.vctek.orderservice.service.OrderStorefrontSetupService;
import com.vctek.orderservice.service.PromotionBudgetService;
import com.vctek.orderservice.util.PromotionDefinitionCode;
import com.vctek.service.UserService;
import com.vctek.util.ExportExcelType;
import com.vctek.util.OrderType;
import com.vctek.util.PermissionCodes;
import com.vctek.util.PriceType;
import com.vctek.validate.Validator;
import org.apache.commons.collections4.CollectionUtils;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Component
public class PromotionSourceRuleFacadeImpl implements PromotionSourceRuleFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionSourceRuleFacadeImpl.class);
    private DroolsKIEModuleService droolsKIEModuleService;
    private Converter<PromotionSourceRuleData, PromotionSourceRuleModel> promotionSourceRuleConverter;
    private Converter<PromotionSourceRuleDTO, PromotionSourceRuleData> promotionSourceRuleDataConverter;
    private PromotionSourceRuleService promotionSourceRuleService;
    private RuleCompilerService ruleCompilerService;
    private ApplicationEventPublisher applicationEventPublisher;
    private CouponService couponService;
    private UserService userService;
    private Converter<PromotionSourceRuleModel, PromotionSourceRuleDTO> basicPromotionSourceRuleConverter;
    private Populator<PromotionSourceRuleModel, PromotionSourceRuleDTO> promotionSourceRuleConditionPopulator;
    private Populator<PromotionSourceRuleModel, PromotionSourceRuleDTO> promotionSourceRuleActionPopulator;

    private Converter<PromotionSourceRuleModel, CommercePromotionData> commercePromotionDataConverter;
    private Populator<PromotionSourceRuleData, PromotionSourceRuleModel> promotionSourceRuleModelPopulator;
    private Validator<PromotionSourceRuleData> promotionSourceRuleDataValidator;
    private PermissionFacade permissionFacade;
    private Converter<PromotionSourceRuleDTO, PromotionExcelData> promotionExcelDataConverter;
    private DownloadExcelFacade downloadExcelFacade;
    private Executor exportExcelExecutor;
    private OrderStorefrontSetupService orderStorefrontSetupService;
    private PromotionBudgetService promotionBudgetService;

    @Override
    @Transactional
    public PromotionSourceRuleDTO createNew(PromotionSourceRuleDTO promotionSourceRuleDTO) {
        PromotionSourceRuleData promotionSourceRuleData = promotionSourceRuleDataConverter.convert(promotionSourceRuleDTO);
        promotionSourceRuleDataValidator.validate(promotionSourceRuleData);
        PromotionSourceRuleModel model = promotionSourceRuleConverter.convert(promotionSourceRuleData);
        model.setVersion(0l);

        DroolsKIEModuleModel kieModule = droolsKIEModuleService.findByCompanyId(promotionSourceRuleDTO.getCompanyId());
        PromotionSourceRuleModel sourceRuleModel = promotionSourceRuleService.save(model);
        promotionBudgetService.createPromotionBudget(sourceRuleModel, promotionSourceRuleData);
        try {
            DroolsRuleModel droolsRuleModel = ruleCompilerService.compile(sourceRuleModel, kieModule.getName());

            if (droolsRuleModel.isActive()) {
                applicationEventPublisher.publishEvent(new PublishPromotionSourceRuleEvent(sourceRuleModel, droolsRuleModel, kieModule));
            }

            promotionSourceRuleDTO.setId(sourceRuleModel.getId());
            return promotionSourceRuleDTO;
        } catch (RuleCompilerException e) {
            LOGGER.error(e.getMessage(), e);
            ErrorCodes err = ErrorCodes.INVALID_PROMOTION_FORMAT;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Override
    public Page<PromotionSourceRuleDTO> findAll(PromotionRuleSearchParam param) {
        Long currentUserId = userService.getCurrentUserId();
        boolean viewAll = permissionFacade.checkPermission(PermissionCodes.VIEW_ALL_PROMOTIONS.code(), currentUserId, param.getCompanyId());
        if(!viewAll) {
            param.setCurrentUserId(currentUserId);
        }
        Page<PromotionSourceRuleModel> page = promotionSourceRuleService.findAll(param);
        List<PromotionSourceRuleModel> sourceRules = page.getContent();

        if (CollectionUtils.isNotEmpty(sourceRules)) {
            List<PromotionSourceRuleDTO> promotionSourceRuleDTOS = basicPromotionSourceRuleConverter.convertAll(sourceRules);
            return new PageImpl<>(promotionSourceRuleDTOS, page.getPageable(), page.getTotalElements());
        }

        return new PageImpl<>(Collections.emptyList(), page.getPageable(), page.getTotalElements());
    }

    @Override
    @Transactional
    public void changeStatus(PromotionStatusRequest statusRequest) {
        PromotionSourceRuleModel sourceRuleModel = promotionSourceRuleService.findByIdAndCompanyId(statusRequest.getPromotionId(),
                statusRequest.getCompanyId());
        sourceRuleModel.setActive(statusRequest.isActive());
        promotionSourceRuleService.save(sourceRuleModel);

        if (promotionSourceRuleService.isExpired(sourceRuleModel)) {
            sourceRuleModel.setStatus(RuleStatus.INACTIVE.toString());
            promotionSourceRuleService.save(sourceRuleModel);
            return;
        }

        Set<DroolsRuleModel> droolsRules = sourceRuleModel.getDroolsRules();
        if (CollectionUtils.isEmpty(droolsRules)) {
            sourceRuleModel.setStatus(RuleStatus.INACTIVE.toString());
            promotionSourceRuleService.save(sourceRuleModel);
            return;
        }

        DroolsKIEModuleModel kieModule = droolsKIEModuleService.findByCompanyId(statusRequest.getCompanyId());
        applicationEventPublisher.publishEvent(new PublishPromotionSourceRuleEvent(sourceRuleModel,
                droolsRules.iterator().next(), kieModule));
    }

    @Override
    public PromotionSourceRuleDTO findBy(Long promotionId, Long companyId) {
        PromotionSourceRuleModel sourceRuleModel = promotionSourceRuleService.findByIdAndCompanyId(promotionId, companyId);
        if (sourceRuleModel == null) {
            ErrorCodes err = ErrorCodes.NOT_FOUND_DATA;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        Long currentUserId = userService.getCurrentUserId();
        boolean viewAll = permissionFacade.checkPermission(PermissionCodes.VIEW_ALL_PROMOTIONS.code(), currentUserId, companyId);
        if(!viewAll && sourceRuleModel.getCreatedBy() != null && !sourceRuleModel.getCreatedBy().equals(currentUserId)) {
            ErrorCodes err = ErrorCodes.INVALID_PROMOTION_SOURCE_RULE_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        PromotionSourceRuleDTO sourceRuleDTO = basicPromotionSourceRuleConverter.convert(sourceRuleModel);
        promotionSourceRuleConditionPopulator.populate(sourceRuleModel, sourceRuleDTO);
        promotionSourceRuleActionPopulator.populate(sourceRuleModel, sourceRuleDTO);
        return sourceRuleDTO;
    }

    @Override
    @Transactional
    public PromotionSourceRuleDTO update(PromotionSourceRuleDTO promotionSourceRuleDTO) {
        PromotionSourceRuleModel sourceRuleModel = promotionSourceRuleService.findByIdAndCompanyId(promotionSourceRuleDTO.getId(),
                promotionSourceRuleDTO.getCompanyId());
        if (sourceRuleModel == null) {
            ErrorCodes err = ErrorCodes.INVALID_PROMOTION_SOURCE_RULE_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(!promotionSourceRuleService.hasCondition(promotionSourceRuleDTO, PromotionDefinitionCode.QUALIFIER_COUPONS.code())) {
            couponService.removeCouponToSourceRule(sourceRuleModel);
        }

        boolean isExpired = promotionSourceRuleService.isExpired(sourceRuleModel);
        PromotionSourceRuleData promotionSourceRuleData = promotionSourceRuleDataConverter.convert(promotionSourceRuleDTO);
        promotionSourceRuleDataValidator.validate(promotionSourceRuleData);
        String oldSourceRuleCode = sourceRuleModel.getCode();
        promotionSourceRuleModelPopulator.populate(promotionSourceRuleData, sourceRuleModel);
        sourceRuleModel.setCode(oldSourceRuleCode);//Should not populate code again

        DroolsKIEModuleModel kieModule = droolsKIEModuleService.findByCompanyId(promotionSourceRuleDTO.getCompanyId());
        PromotionSourceRuleModel savedModel = promotionSourceRuleService.save(sourceRuleModel);
        try {
            DroolsRuleModel droolsRuleModel = ruleCompilerService.compile(savedModel, kieModule.getName());
            if (!isExpired) {
                applicationEventPublisher.publishEvent(new PublishPromotionSourceRuleEvent(sourceRuleModel, droolsRuleModel, kieModule));
            }

            return promotionSourceRuleDTO;
        } catch (RuleCompilerException e) {
            LOGGER.error(e.getMessage(), e);
            ErrorCodes err = ErrorCodes.INVALID_PROMOTION_FORMAT;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Override
    public byte[] exportCurrentPage(PromotionRuleSearchParam param) {
        Long currentUserId = userService.getCurrentUserId();
        boolean viewAll = permissionFacade.checkPermission(PermissionCodes.VIEW_ALL_PROMOTIONS.code(), currentUserId, param.getCompanyId());
        if(!viewAll) {
            param.setCurrentUserId(currentUserId);
        }

        List<PromotionExcelData> promotionList = new ArrayList<>();
        Page<PromotionSourceRuleModel> page = promotionSourceRuleService.findAll(param);
        if(CollectionUtils.isNotEmpty(page.getContent())) {
            List<PromotionSourceRuleDTO> sourceRuleDTOList = new ArrayList<>();
            page.getContent().forEach(model -> {
                PromotionSourceRuleDTO sourceRuleDTO = basicPromotionSourceRuleConverter.convert(model);
                promotionSourceRuleConditionPopulator.populate(model, sourceRuleDTO);
                sourceRuleDTOList.add(sourceRuleDTO);
            });
            promotionList = promotionExcelDataConverter.convertAll(sourceRuleDTOList);
        }

        ClassPathResource resource = new ClassPathResource("templates/promotion_template.xlsx");
        try (InputStream is = resource.getInputStream()) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                Context context = new Context();
                context.putVar("promotionList", promotionList);
                JxlsHelper.getInstance().processTemplate(is, os, context);
                return os.toByteArray();
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new byte[0];
    }

    @Override
    public void doExportAllPage(PromotionRuleSearchParam param) {
        FileParameter fileParameter = downloadExcelFacade.getFileParameter(ExportExcelType.EXPORT_PROMOTIONS, param.getCompanyId());
        boolean processingExportExcel = downloadExcelFacade.isProcessingExportExcel(fileParameter);
        if(processingExportExcel) {
            return;
        }
        Long currentUserId = userService.getCurrentUserId();
        boolean viewAll = permissionFacade.checkPermission(PermissionCodes.VIEW_ALL_PROMOTIONS.code(), currentUserId, param.getCompanyId());
        if(!viewAll) {
            param.setCurrentUserId(currentUserId);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        exportExcelExecutor.execute(() -> {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            try {
                this.exportAllPage(fileParameter, param);
            } finally {
                downloadExcelFacade.setProcessExportExcel(fileParameter, false);
            }
        });
        downloadExcelFacade.setProcessExportExcel(fileParameter, true);
    }

    @Override
    public List<PromotionSourceRuleDTO> findAllActivePromotionsForStorefront(Long companyId) {
        List<PromotionSourceRuleModel> promotionSourceRuleModels = promotionSourceRuleService.findAllActiveOf(companyId, Calendar.getInstance().getTime());
        if(CollectionUtils.isEmpty(promotionSourceRuleModels)) {
            return new ArrayList<>();
        }
        List<PromotionSourceRuleDTO> commercePromotionData = basicPromotionSourceRuleConverter.convertAll(promotionSourceRuleModels);
        OrderStorefrontSetupModel settingModel = orderStorefrontSetupService.findByCompanyId(companyId);
        if(settingModel == null) {
            return new ArrayList<>();
        }
        return this.filterAppliedPromotions(commercePromotionData, settingModel);
    }

    @Override
    public CommercePromotionData getDetail(Long companyId, Long promotionId) {
        PromotionSourceRuleModel sourceRuleModel = promotionSourceRuleService.findByIdAndCompanyId(promotionId, companyId);
        if(sourceRuleModel == null) {
            return new CommercePromotionData();
        }

        CommercePromotionData commercePromotionData = commercePromotionDataConverter.convert(sourceRuleModel);
        OrderStorefrontSetupModel settingModel = orderStorefrontSetupService.findByCompanyId(companyId);
        if(settingModel == null) {
            return new CommercePromotionData();
        }

        List<CommercePromotionData> promotionDataList = this.filterAppliedPromotions(Arrays.asList(commercePromotionData), settingModel);
        if(CollectionUtils.isEmpty(promotionDataList)) {
            return new CommercePromotionData();
        }
        return promotionDataList.get(0);
    }

    @Override
    public PromotionSourceRuleDTO findById(Long promotionSourceRuleId, Long companyId) {
        PromotionSourceRuleModel sourceRuleModel = promotionSourceRuleService.findByIdAndCompanyId(promotionSourceRuleId, companyId);
        if(sourceRuleModel == null) {
            return null;
        }
        PromotionSourceRuleDTO sourceRuleDTO = basicPromotionSourceRuleConverter.convert(sourceRuleModel);
        promotionSourceRuleConditionPopulator.populate(sourceRuleModel, sourceRuleDTO);
        promotionSourceRuleActionPopulator.populate(sourceRuleModel, sourceRuleDTO);
        return sourceRuleDTO;
    }

    private <T extends PromotionSourceRuleDTO> List<T> filterAppliedPromotions(List<T> commercePromotionData, OrderStorefrontSetupModel settingModel) {
        Long orderSourceId = settingModel.getOrderSourceId();
        Long warehouseId = settingModel.getWarehouseId();
        return commercePromotionData.stream().filter(p -> {
            if(CollectionUtils.isNotEmpty(p.getOrderTypes()) && !p.getOrderTypes().contains(OrderType.ONLINE.toString())) {
                return false;
            }

            if(CollectionUtils.isNotEmpty(p.getPriceTypes()) && !p.getPriceTypes().contains(PriceType.RETAIL_PRICE.toString())) {
                return false;
            }

            if(CollectionUtils.isNotEmpty(p.getExcludeOrderSourceIds()) && p.getExcludeOrderSourceIds().contains(orderSourceId)) {
                return false;
            }

            if(CollectionUtils.isNotEmpty(p.getWarehouseIds()) && !p.getWarehouseIds().contains(warehouseId)) {
                return false;
            }

            return true;
        }).collect(Collectors.toList());
    }

    private void exportAllPage(FileParameter fileParameter, PromotionRuleSearchParam param) {
        PageRequest pageable = PageRequest.of(0, 300, Sort.Direction.DESC, "id");
        param.setPageable(pageable);
        List<PromotionSourceRuleDTO> sourceRuleDTOList = new ArrayList<>();
        while (true) {
            Page<PromotionSourceRuleModel> page = promotionSourceRuleService.findAll(param);
            if(CollectionUtils.isEmpty(page.getContent())) {
                break;
            }

            page.getContent().forEach(model -> {
                PromotionSourceRuleDTO sourceRuleDTO = basicPromotionSourceRuleConverter.convert(model);
                promotionSourceRuleConditionPopulator.populate(model, sourceRuleDTO);
                sourceRuleDTOList.add(sourceRuleDTO);
            });
            pageable = (PageRequest) pageable.next();
            param.setPageable(pageable);
        }
        if(CollectionUtils.isEmpty(sourceRuleDTOList)) {
            return;
        }

        List<PromotionExcelData> promotionList = promotionExcelDataConverter.convertAll(sourceRuleDTOList);
        ClassPathResource resource = new ClassPathResource("templates/promotion_template.xlsx");
        try (InputStream is = resource.getInputStream()) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                Context context = new Context();
                context.putVar("promotionList", promotionList);
                JxlsHelper.getInstance().processTemplate(is, os, context);
                downloadExcelFacade.writeToFile(os.toByteArray(), fileParameter);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Autowired
    public void setPromotionSourceRuleConverter(Converter<PromotionSourceRuleData, PromotionSourceRuleModel> promotionSourceRuleConverter) {
        this.promotionSourceRuleConverter = promotionSourceRuleConverter;
    }

    @Autowired
    public void setPromotionSourceRuleService(PromotionSourceRuleService promotionSourceRuleService) {
        this.promotionSourceRuleService = promotionSourceRuleService;
    }

    @Autowired
    public void setRuleCompilerService(RuleCompilerService ruleCompilerService) {
        this.ruleCompilerService = ruleCompilerService;
    }

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Autowired
    public void setPromotionSourceRuleDataConverter(Converter<PromotionSourceRuleDTO, PromotionSourceRuleData> promotionSourceRuleDataConverter) {
        this.promotionSourceRuleDataConverter = promotionSourceRuleDataConverter;
    }

    @Autowired
    public void setDroolsKIEModuleService(DroolsKIEModuleService droolsKIEModuleService) {
        this.droolsKIEModuleService = droolsKIEModuleService;
    }

    @Autowired
    public void setBasicPromotionSourceRuleConverter(Converter<PromotionSourceRuleModel, PromotionSourceRuleDTO> basicPromotionSourceRuleConverter) {
        this.basicPromotionSourceRuleConverter = basicPromotionSourceRuleConverter;
    }

    @Autowired
    @Qualifier("promotionSourceRuleConditionPopulator")
    public void setPromotionSourceRuleConditionPopulator(Populator<PromotionSourceRuleModel, PromotionSourceRuleDTO> promotionSourceRuleConditionPopulator) {
        this.promotionSourceRuleConditionPopulator = promotionSourceRuleConditionPopulator;
    }

    @Autowired
    @Qualifier("promotionSourceRuleActionPopulator")
    public void setPromotionSourceRuleActionPopulator(Populator<PromotionSourceRuleModel, PromotionSourceRuleDTO> promotionSourceRuleActionPopulator) {
        this.promotionSourceRuleActionPopulator = promotionSourceRuleActionPopulator;
    }

    @Autowired
    @Qualifier("promotionSourceRulePopulator")
    public void setPromotionSourceRuleModelPopulator(Populator<PromotionSourceRuleData, PromotionSourceRuleModel> promotionSourceRuleModelPopulator) {
        this.promotionSourceRuleModelPopulator = promotionSourceRuleModelPopulator;
    }

    @Autowired
    public void setCouponService(CouponService couponService) {
        this.couponService = couponService;
    }

    @Autowired
    public void setPromotionSourceRuleDataValidator(Validator<PromotionSourceRuleData> promotionSourceRuleDataValidator) {
        this.promotionSourceRuleDataValidator = promotionSourceRuleDataValidator;
    }

    @Autowired
    public void setPermissionFacade(PermissionFacade permissionFacade) {
        this.permissionFacade = permissionFacade;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setPromotionExcelDataConverter(Converter<PromotionSourceRuleDTO, PromotionExcelData> promotionExcelDataConverter) {
        this.promotionExcelDataConverter = promotionExcelDataConverter;
    }

    @Autowired
    public void setDownloadExcelFacade(DownloadExcelFacade downloadExcelFacade) {
        this.downloadExcelFacade = downloadExcelFacade;
    }

    @Autowired
    public void setExportExcelExecutor(Executor exportExcelExecutor) {
        this.exportExcelExecutor = exportExcelExecutor;
    }

    @Autowired
    public void setCommercePromotionDataConverter(Converter<PromotionSourceRuleModel, CommercePromotionData> commercePromotionDataConverter) {
        this.commercePromotionDataConverter = commercePromotionDataConverter;
    }

    @Autowired
    public void setOrderStorefrontSetupService(OrderStorefrontSetupService orderStorefrontSetupService) {
        this.orderStorefrontSetupService = orderStorefrontSetupService;
    }

    @Autowired
    public void setPromotionBudgetService(PromotionBudgetService promotionBudgetService) {
        this.promotionBudgetService = promotionBudgetService;
    }
}
