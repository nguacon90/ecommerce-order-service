package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.request.RedeemRateRequest;
import com.vctek.orderservice.feignclient.dto.ProductSearchRequest;
import com.vctek.orderservice.model.CategoryRedeemRateModel;
import com.vctek.orderservice.model.ProductRedeemRateModel;
import com.vctek.orderservice.repository.CategoryRedeemRateRepository;
import com.vctek.orderservice.repository.ProductRedeemRateRepository;
import com.vctek.orderservice.service.ProductService;
import com.vctek.redis.elastic.ProductSearchData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class ProductRedeemRateUseServiceImplTest {
    @Mock
    private ProductRedeemRateRepository rewardRateUseRepository;
    @Mock
    private CategoryRedeemRateRepository categoryRedeemRateRepository;
    @Mock
    private ProductService productService;

    private ProductRedeemRateUseServiceImpl service;

    private RedeemRateRequest loyaltyRewardRateRequest;
    private List<ProductSearchData> productSearchDataList;
    private List<ProductRedeemRateModel> productRedeemRateModels;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new ProductRedeemRateUseServiceImpl(rewardRateUseRepository, categoryRedeemRateRepository);
        service.setProductService(productService);

        loyaltyRewardRateRequest = new RedeemRateRequest();
        loyaltyRewardRateRequest.setCompanyId(1l);
        loyaltyRewardRateRequest.setListId(Arrays.asList(1l, 2l, 3l, 4l, 5l));

        productSearchDataList = new ArrayList<>();
        productRedeemRateModels = new ArrayList<>();
    }

    @Test
    public void createOrUpdate() {
        when(rewardRateUseRepository.findByCompanyIdAndProductId(anyLong(), anyLong())).thenReturn(null);
        service.createOrUpdate(loyaltyRewardRateRequest);
        verify(rewardRateUseRepository, times(5)).findByCompanyIdAndProductId(anyLong(), anyLong());
    }

    @Test
    public void findByCompanyId() {
        when(rewardRateUseRepository.findAllByCompanyId(anyLong())).thenReturn(null);
        service.findByCompanyId(1l);
        verify(rewardRateUseRepository).findAllByCompanyId(anyLong());
    }

    @Test
    public void findByProductIdAndCompanyId() {
        when(rewardRateUseRepository.findByCompanyIdAndProductId(anyLong(), anyLong())).thenReturn(null);
        service.findByProductIdAndCompanyId(1l, 2l);
        verify(rewardRateUseRepository).findByCompanyIdAndProductId(anyLong(), anyLong());
    }

    @Test
    public void delete() {
        service.delete(new ProductRedeemRateModel());
        verify(rewardRateUseRepository).delete(any(ProductRedeemRateModel.class));
    }

    @Test
    public void productCanReward_findAllProduct() {
        ProductRedeemRateModel productRedeemRateModel1 = new ProductRedeemRateModel();
        ProductRedeemRateModel productRedeemRateModel2 = new ProductRedeemRateModel();
        ProductRedeemRateModel productRedeemRateModel3 = new ProductRedeemRateModel();
        ProductRedeemRateModel productRedeemRateModel4 = new ProductRedeemRateModel();
        ProductRedeemRateModel productRedeemRateModel5 = new ProductRedeemRateModel();
        productRedeemRateModel1.setProductId(1l);
        productRedeemRateModel2.setProductId(2l);
        productRedeemRateModel3.setProductId(3l);
        productRedeemRateModel4.setProductId(4l);
        productRedeemRateModel5.setProductId(5l);
        productRedeemRateModels.add(productRedeemRateModel1);
        productRedeemRateModels.add(productRedeemRateModel2);
        productRedeemRateModels.add(productRedeemRateModel3);
        productRedeemRateModels.add(productRedeemRateModel4);
        productRedeemRateModels.add(productRedeemRateModel5);
        when(rewardRateUseRepository.findAllByCompanyIdAndProductIdIn(anyLong(), anyList())).thenReturn(productRedeemRateModels);
        Map<Long, Boolean> data = service.productCanRedeem(1l, loyaltyRewardRateRequest.getListId());
        verify(rewardRateUseRepository).findAllByCompanyIdAndProductIdIn(anyLong(), anyList());
        assertEquals(data.keySet().size(), 5, 0);
    }

    @Test
    public void productCanReward_findAnyProductRate_searchProductNotFound() {
        ProductRedeemRateModel productRedeemRateModel1 = new ProductRedeemRateModel();
        ProductRedeemRateModel productRedeemRateModel2 = new ProductRedeemRateModel();
        ProductRedeemRateModel productRedeemRateModel3 = new ProductRedeemRateModel();
        productRedeemRateModel1.setProductId(1l);
        productRedeemRateModel2.setProductId(2l);
        productRedeemRateModel3.setProductId(3l);
        productRedeemRateModels.add(productRedeemRateModel1);
        productRedeemRateModels.add(productRedeemRateModel2);
        productRedeemRateModels.add(productRedeemRateModel3);
        when(rewardRateUseRepository.findAllByCompanyIdAndProductIdIn(anyLong(), anyList())).thenReturn(productRedeemRateModels);
        when(productService.search(any(ProductSearchRequest.class))).thenReturn(new ArrayList<>());
        Map<Long, Boolean> data = service.productCanRedeem(1l, loyaltyRewardRateRequest.getListId());
        verify(rewardRateUseRepository).findAllByCompanyIdAndProductIdIn(anyLong(), anyList());
        assertEquals(data.keySet().size(), 5, 0);
        data.forEach((productId, canRedeem) -> {
            if (productId.equals(4l) || productId.equals(5l)) {
                assertEquals(canRedeem, true);
            }
        });
    }

    @Test
    public void productCanReward_findAnyProductRate_searchAnyProduct() {
        ProductRedeemRateModel productRedeemRateModel1 = new ProductRedeemRateModel();
        ProductRedeemRateModel productRedeemRateModel2 = new ProductRedeemRateModel();
        productRedeemRateModel1.setProductId(1l);
        productRedeemRateModel2.setProductId(2l);
        productRedeemRateModels.add(productRedeemRateModel1);
        productRedeemRateModels.add(productRedeemRateModel2);

        ProductSearchData productSearchData1 = new ProductSearchData();
        productSearchData1.setId(3l);
        productSearchData1.setFullCategoryIds(Arrays.asList(1l));
        productSearchData1.setMainCategoryId(1l);

        when(rewardRateUseRepository.findAllByCompanyIdAndProductIdIn(anyLong(), anyList())).thenReturn(productRedeemRateModels);
        when(productService.search(any(ProductSearchRequest.class))).thenReturn(Arrays.asList(productSearchData1));
        when(categoryRedeemRateRepository.findTopByCategoryIdAndAndCompanyId(anyLong(), anyLong())).thenReturn(Optional.of(new CategoryRedeemRateModel()));
        Map<Long, Boolean> data = service.productCanRedeem(1l, loyaltyRewardRateRequest.getListId());
        verify(rewardRateUseRepository).findAllByCompanyIdAndProductIdIn(anyLong(), anyList());
        verify(categoryRedeemRateRepository, times(1)).findTopByCategoryIdAndAndCompanyId(anyLong(), anyLong());
        assertEquals(data.keySet().size(), 5, 0);
        data.forEach((productId, canRedeem) -> {
            if (productId.equals(4l) || productId.equals(5l)) {
                assertEquals(canRedeem, true);
            }
        });
    }
}
