package com.vctek.orderservice.controller;

import com.vctek.dto.promotion.PromotionSourceRuleDTO;
import com.vctek.orderservice.dto.PromotionRuleSearchParam;
import com.vctek.orderservice.dto.request.PromotionStatusRequest;
import com.vctek.orderservice.facade.PromotionSourceRuleFacade;
import com.vctek.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;

@RestController
@RequestMapping("/promotions")
public class PromotionController {
    private PromotionSourceRuleFacade promotionSourceRuleFacade;
    private Validator<PromotionSourceRuleDTO> promotionSourceRuleValidator;
    private Validator<PromotionStatusRequest> promotionStatusRequestValidator;
    public PromotionController(PromotionSourceRuleFacade promotionSourceRuleFacade) {
        this.promotionSourceRuleFacade = promotionSourceRuleFacade;
    }

    @GetMapping
    @PreAuthorize("hasAnyPermission(#companyId, " +
            "T(com.vctek.util.PermissionCodes).VIEW_LIST_DISCOUNT.code(), " +
            "T(com.vctek.util.PermissionCodes).VIEW_ALL_PROMOTIONS.code())")
    public ResponseEntity<Page<PromotionSourceRuleDTO>> findAll(PromotionRuleSearchParam param,
                                                                @RequestParam("companyId") Long companyId,
                                                                @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                                @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        PageRequest pageable = PageRequest.of(page, pageSize);
        param.setCompanyId(companyId);
        param.setPageable(pageable);
        Page<PromotionSourceRuleDTO> data = promotionSourceRuleFacade.findAll(param);

        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasAnyPermission(#request.companyId, " +
            "T(com.vctek.util.PermissionCodes).CREATE_DISCOUNT.code())")
    public ResponseEntity<PromotionSourceRuleDTO> createNewPromotion(@RequestBody PromotionSourceRuleDTO request) {
        promotionSourceRuleValidator.validate(request);
        PromotionSourceRuleDTO data = promotionSourceRuleFacade.createNew(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/{promotionId}")
    @PreAuthorize("hasAnyPermission(#companyId, " +
            "T(com.vctek.util.PermissionCodes).VIEW_PROMOTION_DETAIL.code())")
    public ResponseEntity<PromotionSourceRuleDTO> getDetail(@PathVariable("promotionId") Long promotionId,
                                                            @RequestParam("companyId") Long companyId) {
        PromotionSourceRuleDTO data = promotionSourceRuleFacade.findBy(promotionId, companyId);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{promotionId}/status")
    @PreAuthorize("hasAnyPermission(#promotionStatusRequest.companyId, " +
            "T(com.vctek.util.PermissionCodes).EDIT_DISCOUNT.code())")
    public ResponseEntity changeStatus(@PathVariable("promotionId") Long promotionId,
                                       @RequestBody PromotionStatusRequest promotionStatusRequest) {
        promotionStatusRequest.setPromotionId(promotionId);
        promotionStatusRequestValidator.validate(promotionStatusRequest);

        promotionSourceRuleFacade.changeStatus(promotionStatusRequest);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{promotionId}")
    @PreAuthorize("hasAnyPermission(#request.companyId, " +
            "T(com.vctek.util.PermissionCodes).EDIT_DISCOUNT.code())")
    public ResponseEntity<PromotionSourceRuleDTO> update(@PathVariable("promotionId") Long promotionId,
                                       @RequestBody PromotionSourceRuleDTO request) {
        request.setId(promotionId);
        promotionSourceRuleValidator.validate(request);
        PromotionSourceRuleDTO data = promotionSourceRuleFacade.update(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/export-excel/current-page")
    @PreAuthorize("hasAnyPermission(#companyId, " +
            "T(com.vctek.util.PermissionCodes).VIEW_LIST_DISCOUNT.code(), " +
            "T(com.vctek.util.PermissionCodes).VIEW_ALL_PROMOTIONS.code())")
    public ResponseEntity<byte[]> exportCurrentPage(PromotionRuleSearchParam param,
                                                    @RequestParam("companyId") Long companyId,
                                                    @RequestParam(value = "page", defaultValue = "0") Integer page,
                                                    @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize,
                                                    HttpServletResponse response) {
        PageRequest pageable = PageRequest.of(page, pageSize);
        param.setCompanyId(companyId);
        param.setPageable(pageable);
        byte[] data = promotionSourceRuleFacade.exportCurrentPage(param);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        String name = "Promotion_" + Calendar.getInstance().getTimeInMillis() + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + name);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return new ResponseEntity<>(data, headers, HttpStatus.OK);

    }

    @GetMapping("/export-excel/all-page")
    @PreAuthorize("hasAnyPermission(#companyId, " +
            "T(com.vctek.util.PermissionCodes).VIEW_LIST_DISCOUNT.code(), " +
            "T(com.vctek.util.PermissionCodes).VIEW_ALL_PROMOTIONS.code())")
    public ResponseEntity requestExportAllPage(PromotionRuleSearchParam param,
                                               @RequestParam("companyId") Long companyId) {
        param.setCompanyId(companyId);
        promotionSourceRuleFacade.doExportAllPage(param);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @Autowired
    public void setPromotionSourceRuleValidator(Validator<PromotionSourceRuleDTO> promotionSourceRuleValidator) {
        this.promotionSourceRuleValidator = promotionSourceRuleValidator;
    }

    @Autowired
    public void setPromotionStatusRequestValidator(Validator<PromotionStatusRequest> promotionStatusRequestValidator) {
        this.promotionStatusRequestValidator = promotionStatusRequestValidator;
    }

}
