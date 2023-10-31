package com.vctek.orderservice.facade;

import com.vctek.orderservice.dto.AllLoyaltyRewardRateData;
import com.vctek.orderservice.dto.CategoryLoyaltyRewardRateData;
import com.vctek.orderservice.dto.DefaultLoyaltyRewardRateData;
import com.vctek.orderservice.dto.ProductLoyaltyRewardRateData;
import com.vctek.orderservice.dto.excel.ProductLoyaltyRewardRateDTO;
import com.vctek.orderservice.dto.request.LoyaltyRewardRateDetailRequest;
import com.vctek.orderservice.dto.request.LoyaltyRewardRateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface LoyaltyRewardRateFacade {
    AllLoyaltyRewardRateData findBy(Long companyId);

    DefaultLoyaltyRewardRateData createOrUpdateDefault(LoyaltyRewardRateDetailRequest loyaltyRewardRateDetailRequest);

    List<ProductLoyaltyRewardRateData> createOrUpdateProduct(LoyaltyRewardRateRequest loyaltyRewardRateRequest);

    List<CategoryLoyaltyRewardRateData> createOrUpdateCategory(LoyaltyRewardRateRequest loyaltyRewardRateRequest);

    void deleteProduct(LoyaltyRewardRateDetailRequest loyaltyRewardRateDetailRequest);

    void deleteCategory(LoyaltyRewardRateDetailRequest loyaltyRewardRateDetailRequest);

    ProductLoyaltyRewardRateDTO importExcelProduct(Long companyId, MultipartFile multipartFile);
}
