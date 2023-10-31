package com.vctek.orderservice.controller;

import com.vctek.orderservice.dto.OrderSettingData;
import com.vctek.orderservice.dto.OrderSettingDiscountData;
import com.vctek.orderservice.dto.excel.OrderSettingDiscountErrorDTO;
import com.vctek.orderservice.dto.request.OrderSettingRequest;
import com.vctek.orderservice.facade.OrderSettingDiscountFacade;
import com.vctek.orderservice.util.OrderSettingType;
import com.vctek.validate.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.List;

@RestController
@RequestMapping("setting-discount")
public class OrderSettingDiscountController {

    private Validator<OrderSettingRequest> orderSettingDiscountValidator;
    private OrderSettingDiscountFacade orderSettingDiscountFacade;

    @PostMapping("/product")
    @PreAuthorize("hasAnyPermission(#request.getCompanyId(), " +
            "T(com.vctek.util.PermissionCodes).SETTING_MAXIMUM_DISCOUNT_PRODUCT.code())")
    public ResponseEntity<OrderSettingData> createOrUpdateProductSetting(@RequestBody OrderSettingRequest request) {
        request.setType(OrderSettingType.MAXIMUM_DISCOUNT_SETTING.toString());
        orderSettingDiscountValidator.validate(request);
        OrderSettingData data = orderSettingDiscountFacade.createProduct(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/category")
    @PreAuthorize("hasAnyPermission(#request.getCompanyId(), " +
            "T(com.vctek.util.PermissionCodes).SETTING_MAXIMUM_DISCOUNT_PRODUCT.code())")
    public ResponseEntity<OrderSettingData> createOrUpdateCategorySetting(@RequestBody OrderSettingRequest request) {
        request.setType(OrderSettingType.MAXIMUM_DISCOUNT_SETTING.toString());
        orderSettingDiscountValidator.validate(request);
        OrderSettingData data = orderSettingDiscountFacade.createOrUpdateCategory(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{settingId}/delete")
    @PreAuthorize("hasAnyPermission(#companyId, " +
            "T(com.vctek.util.PermissionCodes).SETTING_MAXIMUM_DISCOUNT_PRODUCT.code())")
    public ResponseEntity deleteProductSetting(@RequestParam Long companyId,
                                               @PathVariable("settingId") Long settingId) {
        orderSettingDiscountFacade.deleteProductSetting(settingId, companyId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/product/search")
    public ResponseEntity<Page<OrderSettingDiscountData>> findAllProductSetting(@RequestParam Long companyId,
                                                                                  @RequestParam(value = "product", defaultValue = "", required = false) String product,
                                                                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                                                                  @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        Pageable pageableRequest = PageRequest.of(page, pageSize, new Sort(Sort.Direction.ASC, "id"));
        Page<OrderSettingDiscountData> pageData = orderSettingDiscountFacade.search(companyId, product, pageableRequest);
        return new ResponseEntity<>(pageData, HttpStatus.OK);
    }

    @GetMapping("/category")
    public ResponseEntity<OrderSettingData> findAllCategorySetting(@RequestParam Long companyId) {
        OrderSettingData data = orderSettingDiscountFacade.findAllCategory(companyId);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel(@RequestParam("companyId") Long companyId,
                                              HttpServletResponse response) {
        byte[] data = orderSettingDiscountFacade.exportExcel(companyId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        String name = "Product_Setting_Discount" + Calendar.getInstance().getTimeInMillis() + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + name);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @PostMapping("/import-excel")
    @PreAuthorize("hasAnyPermission(#companyId, " +
            "T(com.vctek.util.PermissionCodes).SETTING_MAXIMUM_DISCOUNT_PRODUCT.code())")
    public ResponseEntity<OrderSettingDiscountErrorDTO> importExcel(
            @RequestParam("companyId") Long companyId,
            @RequestParam("file") MultipartFile multipartFile) {
        OrderSettingDiscountErrorDTO data = orderSettingDiscountFacade.importExcel(companyId, multipartFile);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @Autowired
    public void setOrderSettingDiscountFacade(OrderSettingDiscountFacade orderSettingDiscountFacade) {
        this.orderSettingDiscountFacade = orderSettingDiscountFacade;
    }

    @Autowired
    public void setOrderSettingDiscountValidator(Validator<OrderSettingRequest> orderSettingDiscountValidator) {
        this.orderSettingDiscountValidator = orderSettingDiscountValidator;
    }

}
