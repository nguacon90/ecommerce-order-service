package com.vctek.orderservice.facade;

import com.vctek.dto.ExcelStatusData;
import com.vctek.dto.FileParameter;
import com.vctek.util.ExportExcelType;

public interface DownloadExcelFacade {
    ExcelStatusData checkExportExcelStatus(String exportExcelType, Long companyId);

    boolean isProcessingExportExcel(FileParameter fileParameter);

    void setProcessExportExcel(FileParameter fileParameter, boolean export);

    FileParameter getFileParameter(ExportExcelType excelType, Long companyId);

    byte[] downloadExcel(String type, Long companyId);

    void deleteFile(FileParameter fileParameter);

    void writeToFile(byte[] bytes, FileParameter fileParameter);
}
