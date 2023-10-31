package com.vctek.orderservice.excel;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ExcelFileReader<O> {
    List<O> read(MultipartFile multipartFile);
}
