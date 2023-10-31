package com.vctek.orderservice.controller;

import com.vctek.orderservice.dto.OrderSearchExcelData;
import com.vctek.orderservice.dto.request.ReturnOrderSearchRequest;
import com.vctek.orderservice.elasticsearch.model.returnorder.ReturnOrderDocument;
import com.vctek.orderservice.facade.ReturnOrderDocumentFacade;
import org.apache.commons.lang3.StringUtils;
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

@RestController
@RequestMapping("/elasticsearch/return-orders")
public class ReturnOrderDocumentController {
    private ReturnOrderDocumentFacade returnOrderDocumentFacade;

    @GetMapping
    @PreAuthorize("hasAnyPermission(#returnOrderSearchRequest.companyId, " +
            "T(com.vctek.util.PermissionCodes).VIEW_LIST_BILL_EXCHANGE.code())")
    public ResponseEntity<Page<ReturnOrderDocument>> search(ReturnOrderSearchRequest returnOrderSearchRequest,
                                                            @RequestParam("pageSize") int pageSize,
                                                            @RequestParam("page") int page) {
        Pageable pageableRequest = PageRequest.of(page, pageSize, new Sort(Sort.Direction.DESC, "id"));
        if(StringUtils.isNotBlank(returnOrderSearchRequest.getSortField())) {
            if(Sort.Direction.DESC.toString().equalsIgnoreCase(returnOrderSearchRequest.getSortOrder())) {
                pageableRequest = PageRequest.of(page, pageSize,
                        new Sort(Sort.Direction.DESC, returnOrderSearchRequest.getSortField()));
            } else {
                pageableRequest = PageRequest.of(page, pageSize,
                        new Sort(Sort.Direction.ASC, returnOrderSearchRequest.getSortField()));
            }
        }

        Page<ReturnOrderDocument> pageData = returnOrderDocumentFacade.search(returnOrderSearchRequest, pageableRequest);

        return new ResponseEntity(pageData, HttpStatus.OK);
    }

    @PostMapping("/update-warehouse-exchange-order")
    public ResponseEntity updateWarehouseExchangeOrder() {
        returnOrderDocumentFacade.updateWarehouseExchangeOrder();
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/export-excel")
    @PreAuthorize("hasAnyPermission(#returnOrderSearchRequest.companyId, " +
            "T(com.vctek.util.PermissionCodes).VIEW_LIST_BILL_EXCHANGE.code())")
    public ResponseEntity<byte[]> ExportExcelListReturnOrder(HttpServletResponse response, ReturnOrderSearchRequest returnOrderSearchRequest) {
        OrderSearchExcelData data = returnOrderDocumentFacade.exportExcelListReturnOrder(returnOrderSearchRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        String name = "Quaythungan_danh_sach_phieu_doi_tra.xls";
        response.setHeader("Content-Disposition", "attachment; filename=" + name);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return new ResponseEntity<>(data.getContent(), headers, HttpStatus.OK);
    }

    @Autowired
    public void setReturnOrderDocumentFacade(ReturnOrderDocumentFacade returnOrderDocumentFacade) {
        this.returnOrderDocumentFacade = returnOrderDocumentFacade;
    }
}
