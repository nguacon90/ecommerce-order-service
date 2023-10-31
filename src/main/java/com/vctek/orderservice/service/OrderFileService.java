package com.vctek.orderservice.service;


import com.vctek.orderservice.dto.request.OrderFileParameter;

public interface OrderFileService {

    void writeToFile(byte[] contents, OrderFileParameter orderFileParameter);

    byte[] readFile(OrderFileParameter orderFileParameter);

    void deleteFile(OrderFileParameter orderFileParameter);

    boolean isExistedFile(OrderFileParameter orderFileParameter);

    void mergeFile(OrderFileParameter orderFileParameter, int numberOfFileToMerge);

    boolean isProcessingExportExcel(OrderFileParameter orderFileParameter);

    void setProcessExportExcel(OrderFileParameter orderFileParameter, boolean isProcessing);
}
