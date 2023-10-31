package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.elasticsearch.model.ProductSearchModel;
import com.vctek.orderservice.elasticsearch.service.ProductSearchService;
import com.vctek.orderservice.model.CategoryLoyaltyRewardRateModel;
import com.vctek.orderservice.model.DefaultLoyaltyRewardRateModel;
import com.vctek.orderservice.model.ProductLoyaltyRewardRateModel;
import com.vctek.orderservice.repository.CategoryLoyaltyRewardRateRepository;
import com.vctek.orderservice.repository.DefaultLoyaltyRewardRateRepository;
import com.vctek.orderservice.repository.ProductLoyaltyRewardRateRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


public class ProductLoyaltyRewardRateServiceImplTest {
    @Mock
    private ProductLoyaltyRewardRateRepository repository;
    @Mock
    private CategoryLoyaltyRewardRateRepository categoryLoyaltyRewardRateRepository;
    @Mock
    private ProductSearchService productSearchService;
    @Mock
    private DefaultLoyaltyRewardRateRepository defaultRepository;

    private ProductLoyaltyRewardRateServiceImpl service;

    private List<ProductSearchModel> productDataList;
    @Mock
    private ProductSearchModel productDataMock2;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new ProductLoyaltyRewardRateServiceImpl();
        service.setProductSearchService(productSearchService);
        service.setCategoryLoyaltyRewardRateRepository(categoryLoyaltyRewardRateRepository);
        service.setDefaultRepository(defaultRepository);
        service.setRepository(repository);

        productDataList = new ArrayList<>();
        ProductSearchModel data = new ProductSearchModel();
        data.setId(1l);
        data.setFullCategoryIds(Arrays.asList(2l));
        productDataList.add(data);
        when(productDataMock2.getId()).thenReturn(2l);
        when(productDataMock2.getFullCategoryIds()).thenReturn(Arrays.asList(21l, 22l));
    }

    @Test
    public void getRewardRateByProductIds() {
        ProductLoyaltyRewardRateModel productModel1 = new ProductLoyaltyRewardRateModel();
        productModel1.setProductId(1l);
        productModel1.setRewardRate(1000d);
        ProductLoyaltyRewardRateModel productModel2 = new ProductLoyaltyRewardRateModel();
        productModel2.setProductId(2l);
        productModel2.setRewardRate(2000d);
        when(repository.findAllByCompanyIdAndProductIdIn(anyLong(), anySet())).thenReturn(Arrays.asList(productModel1, productModel2));
        Map<Long, Double> data = service.getRewardRateByProductIds(new HashSet<>(Arrays.asList(1l, 2l)), 2l, false);
        verify(repository).findAllByCompanyIdAndProductIdIn(anyLong(), anySet());
        assertEquals(data.keySet().size(), 2, 0);
        data.forEach((productId, rewardRate) -> {
            if (productId.equals(1l)) {
                assertEquals(Optional.of(rewardRate), Optional.of(1000d));
            }
            if (productId.equals(2l)) {
                assertEquals(Optional.of(rewardRate), Optional.of(2000d));
            }
        });
    }

    @Test
    public void getRewardRateByProductIds_with_category() {
        when(repository.findAllByCompanyIdAndProductIdIn(anyLong(), anyCollection())).thenReturn(Collections.emptyList());
        when(productSearchService.findAllByCompanyId(any())).thenReturn(productDataList);
        CategoryLoyaltyRewardRateModel model = new CategoryLoyaltyRewardRateModel();
        model.setCategoryId(2l);
        model.setRewardRate(1500d);
        when(categoryLoyaltyRewardRateRepository.findAllByCategoryIdInAndCompanyId(anyCollection(), anyLong())).thenReturn(Arrays.asList(model));
        Map<Long, Double> data = service.getRewardRateByProductIds(new HashSet<>(Collections.singleton(1L)), 2l, false);
        verify(repository).findAllByCompanyIdAndProductIdIn(anyLong(), anySet());
        assertEquals(data.keySet().size(), 1, 0);
        data.forEach((productId, rewardRate) -> {
            if (productId.equals(1l)) {
                assertEquals(Optional.of(rewardRate), Optional.of(1500d));
            }
        });
    }

    @Test
    public void getRewardRateByProductIds_setAtParentCat() {
        when(repository.findAllByCompanyIdAndProductIdIn(anyLong(), anyList())).thenReturn(Collections.emptyList());
        productDataList.add(productDataMock2);
        when(productSearchService.findAllByCompanyId(any())).thenReturn(productDataList);
        CategoryLoyaltyRewardRateModel model = new CategoryLoyaltyRewardRateModel();
        model.setCategoryId(2l);
        model.setRewardRate(1500d);

        CategoryLoyaltyRewardRateModel model2 = new CategoryLoyaltyRewardRateModel();
        model2.setCategoryId(22l);
        model2.setRewardRate(3d);
        when(categoryLoyaltyRewardRateRepository.findAllByCategoryIdInAndCompanyId(anySet(), anyLong())).thenReturn(Arrays.asList(model, model2));
        Map<Long, Double> data = service.getRewardRateByProductIds(new HashSet<>(Arrays.asList(1l, 2l)), 2l, false);
        verify(repository).findAllByCompanyIdAndProductIdIn(anyLong(), anyCollection());
        assertEquals(data.keySet().size(), 2, 0);
        data.forEach((productId, rewardRate) -> {
            if (productId.equals(1l)) {
                assertEquals(Optional.of(rewardRate), Optional.of(1500d));
            } else if (productId.equals(2l)) {
                assertEquals(Optional.of(rewardRate), Optional.of(3d));
            }
        });
    }

    @Test
    public void getRewardRateByProductIds_with_DefaultRewardRate() {
        when(repository.findAllByCompanyIdAndProductIdIn(anyLong(), anyList())).thenReturn(Collections.emptyList());
        when(productSearchService.findAllByCompanyId(any())).thenReturn(productDataList);
        when(categoryLoyaltyRewardRateRepository.findAllByCategoryIdInAndCompanyId(anyList(), anyLong())).thenReturn(Collections.emptyList());
        DefaultLoyaltyRewardRateModel defaultLoyaltyRewardRateModel = new DefaultLoyaltyRewardRateModel();
        defaultLoyaltyRewardRateModel.setRewardRate(2020d);
        when(defaultRepository.findByCompanyId(2l)).thenReturn(defaultLoyaltyRewardRateModel);
        Map<Long, Double> data = service.getRewardRateByProductIds(new HashSet<>(Arrays.asList(1l)), 2l, false);
        verify(repository).findAllByCompanyIdAndProductIdIn(anyLong(), anySet());
        assertEquals(data.keySet().size(), 1, 0);
        data.forEach((productId, rewardRate) -> {
            if (productId.equals(1l)) {
                assertEquals(Optional.of(rewardRate), Optional.of(2020d));
            }
        });
    }

    @Test
    public void getRewardRateByProductIds_with_DefaultRewardRateNull() {
        ProductLoyaltyRewardRateModel productModel1 = new ProductLoyaltyRewardRateModel();
        productModel1.setProductId(1l);
        productModel1.setRewardRate(1000d);
        when(repository.findAllByCompanyIdAndProductIdIn(anyLong(), anyList())).thenReturn(Collections.emptyList());
        when(productSearchService.findAllByCompanyId(any())).thenReturn(productDataList);
        when(categoryLoyaltyRewardRateRepository.findAllByCategoryIdInAndCompanyId(anyList(), anyLong())).thenReturn(Collections.emptyList());
        when(defaultRepository.findByCompanyId(2l)).thenReturn(null);
        Map<Long, Double> data = service.getRewardRateByProductIds(new HashSet<>(Arrays.asList(1l)), 2l, false);
        verify(repository).findAllByCompanyIdAndProductIdIn(anyLong(), anySet());
        assertEquals(data.keySet().size(), 1, 0);
        data.forEach((productId, rewardRate) -> {
            if (productId.equals(1l)) {
                assertEquals(Optional.of(rewardRate), Optional.of(0d));
            }
        });
    }

    @Test
    public void findAllByCompanyIdAndIds() {
        when(repository.findAllByCompanyIdAndProductIdIn(anyLong(), anyList())).thenReturn(Collections.emptyList());
        service.findAllByCompanyIdAndProductIds(2l, Arrays.asList(12l));
        verify(repository).findAllByCompanyIdAndProductIdIn(anyLong(), anyList());
    }
}
