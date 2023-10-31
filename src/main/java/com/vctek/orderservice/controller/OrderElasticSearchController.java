package com.vctek.orderservice.controller;

import com.vctek.dto.ExcelStatusData;
import com.vctek.orderservice.dto.OrderSearchExcelData;
import com.vctek.orderservice.dto.request.OrderSearchRequest;
import com.vctek.orderservice.elasticsearch.model.OrderSearchModel;
import com.vctek.orderservice.facade.OrderElasticSearchFacade;
import com.vctek.orderservice.facade.OrderExportExcelFacade;
import com.vctek.orderservice.facade.PermissionFacade;
import com.vctek.orderservice.util.ExportExcelType;
import com.vctek.service.UserService;
import com.vctek.util.OrderType;
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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Calendar;

@RestController
@RequestMapping("/elasticsearch/orders")
public class OrderElasticSearchController {
    private OrderElasticSearchFacade orderElasticSearchFacade;
    private OrderExportExcelFacade orderExportExcelFacade;
    private PermissionFacade permissionFacade;
    private UserService userService;

    @Autowired
    public OrderElasticSearchController(OrderElasticSearchFacade orderElasticSearchFacade, OrderExportExcelFacade orderExportExcelFacade) {
        this.orderElasticSearchFacade = orderElasticSearchFacade;
        this.orderExportExcelFacade = orderExportExcelFacade;
    }

    @GetMapping("/search")
    public ResponseEntity<Page<OrderSearchModel>> searchOrder(OrderSearchRequest orderSearchRequest,
                                                              @RequestParam("pageSize") int pageSize,
                                                              @RequestParam("page") int page
    ) {
        permissionFacade.checkSearchingOrderPermission(orderSearchRequest);

        Pageable pageableRequest = PageRequest.of(page, pageSize, new Sort(Sort.Direction.ASC, "createdTime"));
        if (StringUtils.isNotBlank(orderSearchRequest.getSortField())) {
            if (Sort.Direction.DESC.toString().equalsIgnoreCase(orderSearchRequest.getSortOrder())) {
                pageableRequest = PageRequest.of(page, pageSize, new Sort(Sort.Direction.DESC, orderSearchRequest.getSortField()));
            } else {
                pageableRequest = PageRequest.of(page, pageSize, new Sort(Sort.Direction.ASC, orderSearchRequest.getSortField()));
            }
        }

        Page<OrderSearchModel> search = orderElasticSearchFacade.search(orderSearchRequest, pageableRequest);
        Page<OrderSearchModel> pageData = search;

        return new ResponseEntity<>(pageData, HttpStatus.OK);
    }

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcelOrder(HttpServletResponse response,
                                                   OrderSearchRequest request) {
        permissionFacade.checkSearchingOrderPermission(request);
        OrderSearchExcelData data = orderElasticSearchFacade.exportExcelOrder(request, true);

        String excelName = getExcelName(request.getOrderType());

        HttpHeaders headers = getHttpHeaders(response, excelName);
        return new ResponseEntity<>(data.getContent(), headers, HttpStatus.OK);
    }

    @PostMapping(value = "/excels/export")
    public ResponseEntity requestExportExcelAllProduct(@RequestBody OrderSearchRequest request) {
        permissionFacade.checkSearchingOrderPermission(request);
        request.setExportType(ExportExcelType.EXPORT_ORDER_WIDTH_DETAIL_COMBO.toString());
        request.setUserId(userService.getCurrentUserId());
        boolean isExportExcel = orderExportExcelFacade.isExportExcel(request);

        if (Boolean.TRUE.equals(isExportExcel)) {
            return new ResponseEntity<>(HttpStatus.OK);
        }

        orderElasticSearchFacade.requestExportExcelAllProduct(request);
        orderExportExcelFacade.processExportExcel(request, true);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/excels/download")
    public ResponseEntity<byte[]> downloadExcelOrder(HttpServletResponse response, OrderSearchRequest request) {
        request.setExportType(ExportExcelType.EXPORT_ORDER_WIDTH_DETAIL_COMBO.toString());
        request.setUserId(userService.getCurrentUserId());
        byte[] data = orderElasticSearchFacade.downloadExcelOrder(request);

        String excelName = getExcelName(request.getOrderType());

        HttpHeaders headers = getHttpHeaders(response, excelName);
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @GetMapping(value = "/excels/check-status")
    public ResponseEntity<ExcelStatusData> checkExportExcelOrderStatus(OrderSearchRequest biRequest) {
        biRequest.setExportType(ExportExcelType.EXPORT_ORDER_WIDTH_DETAIL_COMBO.toString());
        biRequest.setUserId(userService.getCurrentUserId());
        ExcelStatusData excelStatusData = orderExportExcelFacade.checkStatus(biRequest);
        return new ResponseEntity<>(excelStatusData, HttpStatus.OK);
    }

    private String getExcelName(String orderType) {
        String excelName = "Hoa_don_ban_le";
        if (OrderType.ONLINE.name().equals(orderType)) {
            excelName = "Don_hang";
        }
        if (OrderType.WHOLESALE.name().equals(orderType)) {
            excelName = "Hoa_don_ban_buon";
        }
        return excelName;
    }

    @GetMapping("/excels/export-distributor")
    public ResponseEntity<byte[]> exportExcelOrderTypeDistributor(HttpServletResponse response,
                                                                  @RequestParam("orderCode") String orderCode,
                                                                  @RequestParam("companyId") Long companyId,
                                                                  @RequestParam(value = "printSettingId", required = false) Long printSettingId) {

        byte[] bytes = orderElasticSearchFacade.exportExcelOrderTypeDistributor(companyId, orderCode, printSettingId);
        HttpHeaders headers = getHttpHeaders(response, "Quaythungan_phieu_xuat_don_hang_nha_phan_phoi_");
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    private HttpHeaders getHttpHeaders(HttpServletResponse response, String nameExcel) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        String name = nameExcel + Calendar.getInstance().getTimeInMillis() + ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + name);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return headers;
    }

    @Autowired
    public void setPermissionFacade(PermissionFacade permissionFacade) {
        this.permissionFacade = permissionFacade;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
