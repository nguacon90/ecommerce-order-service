package com.vctek.orderservice.controller;

import com.vctek.dto.ExcelStatusData;
import com.vctek.orderservice.facade.DownloadExcelFacade;
import com.vctek.util.ExportExcelType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/download-excels")
public class DownloadExcelController {
    private DownloadExcelFacade downloadExcelFacade;

    public DownloadExcelController(DownloadExcelFacade downloadExcelFacade) {
        this.downloadExcelFacade = downloadExcelFacade;
    }

    @GetMapping("/check-status")
    public ResponseEntity<ExcelStatusData> checkStatus(@RequestParam("type") String exportExcelType,
                                                       @RequestParam(value = "companyId", required = false) Long companyId) {

        ExcelStatusData data = downloadExcelFacade.checkExportExcelStatus(exportExcelType, companyId);
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping(value = "/download-excel")
    public ResponseEntity<byte[]> downloadExcel(@RequestParam("type") String type,
                                                @RequestParam(value = "companyId", required = false) Long companyId,
                                                HttpServletResponse response) {
        byte[] data = downloadExcelFacade.downloadExcel(type, companyId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.ms-excel"));
        response.setHeader("Content-Disposition", "attachment; filename=" + getNameByType(type));
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    private String getNameByType(String type) {
        String name = "Quaythungan_";
        if (ExportExcelType.EXPORT_ORDER_WITH_DISTRIBUTOR.code().equals(type)) {
            name += "phieu_xuat_kho_don_hang_NPP";
            return name;
        }
        return name;
    }
}
