package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.elasticsearch.model.LoyaltyRewardRateSearchModel;
import com.vctek.orderservice.model.ProductLoyaltyRewardRateModel;
import com.vctek.orderservice.service.ProductService;
import com.vctek.redis.ProductData;
import com.vctek.util.VNCharacterUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("loyaltyRewardRateSearchPopulator")
public class LoyaltyRewardRateSearchPopulator implements Populator<ProductLoyaltyRewardRateModel, LoyaltyRewardRateSearchModel> {
    private ProductService productService;

    @Override
    public void populate(ProductLoyaltyRewardRateModel source, LoyaltyRewardRateSearchModel target) {
        target.setCompanyId(source.getCompanyId());
        target.setId(source.getId());
        target.setProductId(source.getProductId());
        ProductData productDetailData = productService.getBasicProductDetail(source.getProductId());
        target.setProductName(productDetailData.getName());
        String name = VNCharacterUtils.removeAccent(target.getProductName());
        target.setRewardRate(source.getRewardRate());
        target.setProductStringName(name);
        target.setProductSku(productDetailData.getSku());
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }
}
