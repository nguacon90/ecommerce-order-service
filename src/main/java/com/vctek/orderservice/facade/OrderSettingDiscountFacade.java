package com.vctek.orderservice.facade;

import com.vctek.orderservice.dto.OrderSettingData;
import com.vctek.orderservice.dto.OrderSettingDiscountData;
import com.vctek.orderservice.dto.excel.OrderSettingDiscountErrorDTO;
import com.vctek.orderservice.dto.request.OrderSettingRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface OrderSettingDiscountFacade {
    OrderSettingData createProduct(OrderSettingRequest request);

    OrderSettingData createOrUpdateCategory(OrderSettingRequest request);

    void deleteProductSetting(Long settingId, Long companyId);

    Page<OrderSettingDiscountData> search(Long companyId, String product, Pageable pageableRequest);

    OrderSettingData findAllCategory(Long companyId);

    byte[] exportExcel(Long companyId);

    OrderSettingDiscountErrorDTO importExcel(Long companyId, MultipartFile multipartFile);

    Map<Long, OrderSettingDiscountData> getDiscountProduct(Long companyId, List<Long> productIds);
}
