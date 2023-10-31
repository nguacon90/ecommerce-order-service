package com.vctek.orderservice.controller;

import com.vctek.orderservice.dto.LoyaltyRewardSearchExcelData;
import com.vctek.orderservice.dto.request.LoyaltyRewardRateElasticRequest;
import com.vctek.orderservice.elasticsearch.model.LoyaltyRewardRateSearchModel;
import com.vctek.orderservice.facade.LoyaltyRewardRateSearchFacade;
import org.apache.commons.lang3.StringUtils;
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

@RestController
@RequestMapping("/elasticsearch/loyalty-reward-rate")
public class LoyaltyRewardRateElasticSearchController {
    private LoyaltyRewardRateSearchFacade facade;

    public LoyaltyRewardRateElasticSearchController(LoyaltyRewardRateSearchFacade facade) {
        this.facade = facade;
    }

    @PostMapping("/full-index")
    public ResponseEntity fullIndex() {
        facade.fullIndex();
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<LoyaltyRewardRateSearchModel>> search(LoyaltyRewardRateElasticRequest request,
                                                                          @RequestParam("pageSize") int pageSize,
                                                                          @RequestParam("page") int page) {
        Pageable pageableRequest = PageRequest.of(page, pageSize, new Sort(Sort.Direction.ASC, "id"));
        if (StringUtils.isNotBlank(request.getSortField())) {
            if (Sort.Direction.DESC.toString().equalsIgnoreCase(request.getSortOrder())) {
                pageableRequest = PageRequest.of(page, pageSize, new Sort(Sort.Direction.DESC, request.getSortField()));
            } else {
                pageableRequest = PageRequest.of(page, pageSize, new Sort(Sort.Direction.ASC, request.getSortField()));
            }
        }
        Page<LoyaltyRewardRateSearchModel> pageData = facade.search(request, pageableRequest);
        return new ResponseEntity<>(pageData, HttpStatus.OK);
    }

    @GetMapping("/export-excel")
    public ResponseEntity exportExcel(HttpServletResponse response,
                                      @RequestParam Long companyId) {
        LoyaltyRewardSearchExcelData data = facade.exportExcel(companyId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        String name = String.format("Qtn_tich_diem_san_pham.xls");
        response.setHeader("Content-Disposition", "attachment; filename=" + name);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return new ResponseEntity<>(data.getContent(), headers, HttpStatus.OK);
    }
}
