package com.vctek.orderservice.excel;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.excel.OrderSettingDiscountDTO;
import com.vctek.orderservice.excel.mapper.OrderSettingDiscountRowMapper;
import com.vctek.orderservice.exception.ErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.excel.poi.PoiItemReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OrderSettingDiscountExcelFileReader implements ExcelFileReader<OrderSettingDiscountDTO> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderSettingDiscountExcelFileReader.class);
    public static final int LINES_TO_SKIP = 1;
    public static final int START_SHEET = 0;

    @Override
    public List<OrderSettingDiscountDTO> read(MultipartFile multipartFile) {
        try {

            Map<String, OrderSettingDiscountDTO> result = new HashMap<>();
            PoiItemReader<OrderSettingDiscountDTO> poiItemReader = new PoiItemReader<>();
            poiItemReader.setLinesToSkip(LINES_TO_SKIP);
            poiItemReader.setResource(new ByteArrayResource(multipartFile.getBytes()));
            poiItemReader.setRowMapper(new OrderSettingDiscountRowMapper());
            poiItemReader.setStartSheet(START_SHEET);
            poiItemReader.open(new ExecutionContext());

            while (true) {
                OrderSettingDiscountDTO dto = poiItemReader.read();
                if (dto == null) {
                    break;
                }

                if (dto.isNotEmpty()) {
                    result.put(dto.getProductSku(), dto);
                }
            }
            return new ArrayList<>(result.values());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            ErrorCodes err = ErrorCodes.CANNOT_READ_EXCEL_FILE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }
}
