package com.vctek.orderservice.facade;

import com.vctek.orderservice.dto.OrderEntryDTO;
import com.vctek.orderservice.dto.request.CheckPermissionRequest;
import com.vctek.orderservice.dto.request.OrderSearchRequest;

public interface PermissionFacade {
    boolean userBelongTo(Long companyId);

    boolean checkPermission(CheckPermissionRequest request);

    boolean checkPermission(String permission, Long userId, Long companyId);

    void checkPlaceOrder(Long companyId, String cartCode);

    void checkUpdateOrder(Long companyId, String orderCode);

    void checkViewOrderDetail(Long companyId, String orderCode);

    void checkUpdateOrderDiscount(Long companyId, String orderCode);

    void checkUpdateOrderPrice(OrderEntryDTO orderEntryDTO, String orderCode);

    void checkUpdateCartDiscount(Long companyId, String cartCode);

    void checkUpdateCartPrice(OrderEntryDTO orderEntryDTO, String cartCode);

    boolean hasPermission(String code, Long companyId);

    void checkSearchingOrderPermission(OrderSearchRequest orderSearchRequest);

    void checkUpdateOrderInfo(Long companyId, String orderCode);

    boolean hasViewAllOrderPermission(Long companyId, String orderType);

    void checkUpdateRecommendedRetailPriceOrder(OrderEntryDTO orderEntryDTO, String orderCode);

    void checkUpdateRecommendedRetailPriceCart(OrderEntryDTO orderEntryDTO, String orderCode);
}
