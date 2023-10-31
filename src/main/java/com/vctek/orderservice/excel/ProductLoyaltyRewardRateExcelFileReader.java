package com.vctek.orderservice.excel;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.excel.ProductLoyaltyRewardRateDTO;
import com.vctek.orderservice.excel.mapper.ProductLoyaltyRewardRateRowMapper;
import com.vctek.orderservice.exception.ErrorCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.excel.poi.PoiItemReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductLoyaltyRewardRateExcelFileReader implements ExcelFileReader<ProductLoyaltyRewardRateDTO> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductLoyaltyRewardRateExcelFileReader.class);
    public static final int LINES_TO_SKIP = 1;
    public static final int START_SHEET = 0;

    @Override
    public List<ProductLoyaltyRewardRateDTO> read(MultipartFile multipartFile) {
        try {
            List<ProductLoyaltyRewardRateDTO> result = new ArrayList<>();
            PoiItemReader<ProductLoyaltyRewardRateDTO> poiItemReader = new PoiItemReader<>();
            poiItemReader.setLinesToSkip(LINES_TO_SKIP);
            poiItemReader.setResource(new ByteArrayResource(multipartFile.getBytes()));
            poiItemReader.setRowMapper(new ProductLoyaltyRewardRateRowMapper());
            poiItemReader.setStartSheet(START_SHEET);
            poiItemReader.open(new ExecutionContext());
            while (true) {
                ProductLoyaltyRewardRateDTO dto = poiItemReader.read();
                if(dto == null) {
                    break;
                }

                if(dto.isNotEmpty()) {
                    result.add(dto);
                }
            }
            return result;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            ErrorCodes err = ErrorCodes.CANNOT_READ_EXCEL_FILE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }
}
