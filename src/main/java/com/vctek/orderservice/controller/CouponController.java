package com.vctek.orderservice.controller;

import com.vctek.orderservice.couponservice.couponcodegeneration.dto.CouponCodeConfiguration;
import com.vctek.orderservice.dto.CouponCodeData;
import com.vctek.orderservice.dto.CouponData;
import com.vctek.orderservice.dto.request.CouponRequest;
import com.vctek.orderservice.facade.CouponFacade;
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

import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;
import java.util.List;

@RestController
@RequestMapping("coupons")
public class CouponController {

    private CouponFacade couponFacade;
    private Validator<CouponRequest> couponRequestValidator;

    @GetMapping
    @PreAuthorize("hasAnyPermission(#companyId, " +
            "T(com.vctek.util.PermissionCodes).VIEW_LIST_DISCOUNT.code())")
    public ResponseEntity<Page<CouponData>> findAll(@RequestParam("companyId") Long companyId,
                                                    @RequestParam(value = "name", required = false) String name,
                                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                                    @RequestParam(value = "offset", defaultValue = "20") int offset) {
        Pageable pageable = PageRequest.of(page, offset, Sort.Direction.DESC, "id");
        Page<CouponData> pageData = couponFacade.findAllBy(companyId, name, pageable);
        return new ResponseEntity<>(pageData, HttpStatus.OK);
    }

    @GetMapping("/qualifying-coupon")
    @PreAuthorize("hasAnyPermission(#companyId, " +
            "T(com.vctek.util.PermissionCodes).VIEW_LIST_DISCOUNT.code())")
    public ResponseEntity<List<CouponData>> getQualifyingCoupon(@RequestParam("companyId") Long companyId,
                                            @RequestParam(value = "sourceRuleId", required = false) Long sourceRuleId) {
        List<CouponData> couponData = couponFacade.findAllForQualifying(companyId, sourceRuleId);
        return new ResponseEntity<>(couponData, HttpStatus.OK);
    }

    @GetMapping("/{couponId}")
    @PreAuthorize("hasAnyPermission(#companyId, " +
            "T(com.vctek.util.PermissionCodes).VIEW_COUPON_DETAIL.code())")
    public ResponseEntity<CouponData> getDetail(@RequestParam("companyId") Long companyId,
                                                @PathVariable("couponId") Long couponId) {
        CouponData couponData = couponFacade.getDetail(couponId, companyId);
        return new ResponseEntity<>(couponData, HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasAnyPermission(#request.companyId, " +
            "T(com.vctek.util.PermissionCodes).CREATE_DISCOUNT.code())")
    public ResponseEntity<CouponData> createCoupon(@RequestBody CouponRequest request) {
        couponRequestValidator.validate(request);
        CouponData data = couponFacade.create(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PutMapping("/{couponId}")
    @PreAuthorize("hasAnyPermission(#request.companyId, " +
            "T(com.vctek.util.PermissionCodes).EDIT_DISCOUNT.code())")
    public ResponseEntity<CouponData> updateCoupon(@RequestBody CouponRequest request,
                                                   @PathVariable("couponId") Long couponId) {
        request.setId(couponId);
        couponRequestValidator.validate(request);
        CouponData data = couponFacade.update(request);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @PostMapping("/{couponId}/delete")
    @PreAuthorize("hasAnyPermission(#companyId, " +
            "T(com.vctek.util.PermissionCodes).EDIT_DISCOUNT.code())")
    public ResponseEntity removeCoupon(@RequestParam("companyId") Long companyId,
                                                   @PathVariable("couponId") Long couponId) {
        couponFacade.remove(couponId, companyId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @PostMapping("/generate")
    public ResponseEntity<List<CouponCodeData>> generateCouponCodes(@RequestBody CouponCodeConfiguration configuration) {
        List<CouponCodeData> data = couponFacade.generateCouponCodes(configuration);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/export-excel")
    @PreAuthorize("hasAnyPermission(#companyId, " +
            "T(com.vctek.util.PermissionCodes).VIEW_LIST_DISCOUNT.code())")
    public ResponseEntity<byte[]> exportExcel(@RequestParam("companyId") Long companyId, HttpServletResponse response) {
        byte[] data = couponFacade.exportExcel(companyId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        String name = "Coupon_" + Calendar.getInstance().getTimeInMillis() + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + name);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @Autowired
    public void setCouponFacade(CouponFacade couponFacade) {
        this.couponFacade = couponFacade;
    }

    @Autowired
    public void setCouponRequestValidator(Validator<CouponRequest> couponRequestValidator) {
        this.couponRequestValidator = couponRequestValidator;
    }
}
