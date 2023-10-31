package com.vctek.orderservice.facade.impl;

import com.vctek.dto.ExcelStatusData;
import com.vctek.dto.FileParameter;
import com.vctek.orderservice.facade.DownloadExcelFacade;
import com.vctek.service.DownloadExcelService;
import com.vctek.service.UserService;
import com.vctek.util.ExportExcelType;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class DownloadExcelFacadeImpl implements DownloadExcelFacade {
    private static final String EXPORT_ORDER_WITH_DISTRIBUTOR = "order_distributor.xlsx";
    private static final String EXPORT_PROMOTION_FILE_NAME = "promotions.xlsx";
    private RedisTemplate redisTemplate;
    private DownloadExcelService downloadExcelService;
    private UserService userService;

    @Override
    public ExcelStatusData checkExportExcelStatus(String exportExcelType, Long companyId) {
        ExportExcelType excelType = ExportExcelType.findByCode(exportExcelType);
        if(excelType == null) {
            return new ExcelStatusData();
        }
        FileParameter fileParameter = getFileParameter(excelType, companyId);
        ExcelStatusData statusData = new ExcelStatusData();
        statusData.setProcessingExport(isProcessingExportExcel(fileParameter));
        statusData.setFinishExport(downloadExcelService.isExistedFile(fileParameter));
        return statusData;
    }

    @Override
    public boolean isProcessingExportExcel(FileParameter fileParameter) {
        String key = downloadExcelService.generateRedisKey(fileParameter);
        if(StringUtils.isBlank(key)) return false;
        Object isProcessing = redisTemplate.opsForValue().get(key);
        return Boolean.TRUE.equals(isProcessing);
    }

    @Override
    public void setProcessExportExcel(FileParameter fileParameter, boolean export) {
        String key = downloadExcelService.generateRedisKey(fileParameter);
        if(StringUtils.isBlank(key)) return;
        redisTemplate.opsForValue().set(key, export, 4, TimeUnit.HOURS);
    }

    @Override
    public FileParameter getFileParameter(ExportExcelType excelType, Long companyId) {
        FileParameter fileParameter = new FileParameter();
        fileParameter.setExportExcelType(excelType);
        fileParameter.setUserId(userService.getCurrentUserId());
        fileParameter.setCompanyId(companyId);
        if (ExportExcelType.EXPORT_ORDER_WITH_DISTRIBUTOR.equals(excelType)) {
            fileParameter.setPathFile("/orderDistributorExcel");
            fileParameter.setFileName(EXPORT_ORDER_WITH_DISTRIBUTOR);
        } else if(ExportExcelType.EXPORT_PROMOTIONS.equals(excelType)) {
            fileParameter.setPathFile("/promotions");
            fileParameter.setFileName(EXPORT_PROMOTION_FILE_NAME);
        }
        return fileParameter;
    }

    @Override
    public byte[] downloadExcel(String type, Long companyId) {
        ExportExcelType excelType = ExportExcelType.findByCode(type);
        if (excelType == null) return new byte[0];

        FileParameter fileParameter = getFileParameter(excelType, companyId);
        byte[] bytes = downloadExcelService.readFile(fileParameter);
        downloadExcelService.deleteFile(fileParameter);
        return bytes;
    }

    @Override
    public void deleteFile(FileParameter fileParameter) {
        downloadExcelService.deleteFile(fileParameter);
    }

    @Override
    public void writeToFile(byte[] bytes, FileParameter fileParameter) {
        downloadExcelService.writeToFile(bytes, fileParameter);
    }

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Autowired
    public void setDownloadExcelService(DownloadExcelService downloadExcelService) {
        this.downloadExcelService = downloadExcelService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
