package com.vctek.orderservice.facade.impl;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.CheckPermissionData;
import com.vctek.orderservice.dto.OrderEntryDTO;
import com.vctek.orderservice.dto.request.CheckPermissionRequest;
import com.vctek.orderservice.dto.request.OrderSearchRequest;
import com.vctek.orderservice.dto.request.UserHasWarehouseRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.PermissionFacade;
import com.vctek.orderservice.feignclient.CheckPermissionClient;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.service.AuthService;
import com.vctek.orderservice.service.CartService;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.util.PriceType;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import com.vctek.util.PermissionCodes;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PermissionFacadeImpl implements PermissionFacade {
    private CheckPermissionClient permissionClient;
    private AuthService authService;
    private OrderService orderService;
    private CartService cartService;
    private Map<String, String> updateOrderPermissions;
    private Map<String, String> viewOrderPermissions;
    private Map<String, String> viewListOrderPermissions;
    private Map<String, String> viewAllListOrderPermissions;
    private Map<String, String> editOrderPricePermissions;
    private Map<String, String> editOrderPriceComboPermissions;
    private Map<String, String> editOrderDiscountPermissions;
    private Map<String, String> editCartDiscountPermissions;
    private Map<String, String> createOrderPermission;
    private Map<String, ErrorCodes> updateOrderPermissionErrorCodes;
    private Map<String, ErrorCodes> viewOrderPermissionErrorCodes;
    private Map<String, ErrorCodes> viewListOrderPermissionErrorCodes;
    private Map<String, ErrorCodes> editOrderPricePermissionErrorCodes;
    private Map<String, ErrorCodes> editOrderPriceComboPermissionErrorCodes;
    private Map<String, ErrorCodes> editOrderDiscountPermissionErrorCodes;
    private Map<String, ErrorCodes> editCartDiscountPermissionErrorCodes;
    private Map<String, ErrorCodes> createOrderPermissionErrorCodes;

    public PermissionFacadeImpl(CheckPermissionClient permissionClient) {
        this.permissionClient = permissionClient;
    }

    @PostConstruct
    public void init() {
        updateOrderPermissions = new HashMap<>();
        viewOrderPermissions = new HashMap<>();
        editOrderPricePermissions = new HashMap<>();
        editOrderPriceComboPermissions = new HashMap<>();
        editOrderDiscountPermissions = new HashMap<>();
        editCartDiscountPermissions = new HashMap<>();
        viewListOrderPermissions = new HashMap<>();
        viewAllListOrderPermissions = new HashMap<>();
        createOrderPermission = new HashMap<>();

        updateOrderPermissionErrorCodes = new HashMap<>();
        viewOrderPermissionErrorCodes = new HashMap<>();
        viewListOrderPermissionErrorCodes = new HashMap<>();
        editOrderPricePermissionErrorCodes = new HashMap<>();
        editOrderPriceComboPermissionErrorCodes = new HashMap<>();
        editOrderDiscountPermissionErrorCodes = new HashMap<>();
        editCartDiscountPermissionErrorCodes = new HashMap<>();
        createOrderPermissionErrorCodes = new HashMap<>();


        updateOrderPermissions.put(OrderType.RETAIL.toString(), PermissionCodes.UPDATE_BILL_RETAIL.toString());
        updateOrderPermissions.put(OrderType.ONLINE.toString(), PermissionCodes.UPDATE_ORDER.toString());
        updateOrderPermissions.put(OrderType.WHOLESALE.toString(), PermissionCodes.UPDATE_BILL_WHOLESALE.toString());

        viewOrderPermissions.put(OrderType.RETAIL.toString(), PermissionCodes.VIEW_DETAIL_BILL_RETAIL.toString());
        viewOrderPermissions.put(OrderType.ONLINE.toString(), PermissionCodes.VIEW_DETAIL_ORDER.toString());
        viewOrderPermissions.put(OrderType.WHOLESALE.toString(), PermissionCodes.VIEW_DETAIL_BILL_WHOLESALE.toString());

        viewListOrderPermissions.put(OrderType.RETAIL.toString(), PermissionCodes.VIEW_LIST_BILL_RETAIL.toString());
        viewListOrderPermissions.put(OrderType.ONLINE.toString(), PermissionCodes.VIEW_LIST_ORDER.toString());
        viewListOrderPermissions.put(OrderType.WHOLESALE.toString(), PermissionCodes.VIEW_LIST_BILL_WHOLESALE.toString());

        viewAllListOrderPermissions.put(OrderType.RETAIL.toString(), PermissionCodes.VIEW_ALL_RETAIL_ORDER_LIST.toString());
        viewAllListOrderPermissions.put(OrderType.ONLINE.toString(), PermissionCodes.VIEW_ALL_ONLINE_ORDER_LIST.toString());
        viewAllListOrderPermissions.put(OrderType.WHOLESALE.toString(), PermissionCodes.VIEW_ALL_WHOLESALE_ORDER_LIST.toString());

        editOrderPricePermissions.put(OrderType.RETAIL.toString(), PermissionCodes.EDIT_PRICE_ON_RETAIL.toString());
        editOrderPricePermissions.put(OrderType.ONLINE.toString(), PermissionCodes.EDIT_PRICE_ON_ORDER.toString());
        editOrderPricePermissions.put(OrderType.WHOLESALE.toString(), PermissionCodes.EDIT_PRICE_ON_WHOLESALE.toString());

        editOrderPriceComboPermissions.put(OrderType.RETAIL.toString(), PermissionCodes.CAN_EDIT_COMBO_PRICE_RETAIL.toString());
        editOrderPriceComboPermissions.put(OrderType.ONLINE.toString(), PermissionCodes.CAN_EDIT_COMBO_PRICE_ONLINE.toString());
        editOrderPriceComboPermissions.put(OrderType.WHOLESALE.toString(), PermissionCodes.CAN_EDIT_COMBO_PRICE_WHOLESALE.toString());

        editOrderDiscountPermissions.put(OrderType.RETAIL.toString(), PermissionCodes.EDIT_DISCOUNT_ON_RETAIL.toString());
        editOrderDiscountPermissions.put(OrderType.ONLINE.toString(), PermissionCodes.EDIT_DISCOUNT_ON_ORDER.toString());
        editOrderDiscountPermissions.put(OrderType.WHOLESALE.toString(), PermissionCodes.EDIT_DISCOUNT_ON_WHOLESALE.toString());

        editCartDiscountPermissions.put(OrderType.RETAIL.toString(), PermissionCodes.EDIT_DISCOUNT_NEW_RETAIL_BILL.toString());
        editCartDiscountPermissions.put(OrderType.ONLINE.toString(), PermissionCodes.EDIT_DISCOUNT_NEW_ONLINE_BILL.toString());
        editCartDiscountPermissions.put(OrderType.WHOLESALE.toString(), PermissionCodes.EDIT_DISCOUNT_NEW_WHOLESALE_BILL.toString());

        createOrderPermission.put(OrderType.RETAIL.toString(), PermissionCodes.CREATE_BILL_RETAIL.toString());
        createOrderPermission.put(OrderType.ONLINE.toString(), PermissionCodes.CREATE_ORDER.toString());
        createOrderPermission.put(OrderType.WHOLESALE.toString(), PermissionCodes.CREATE_BILL_WHOLESALE.toString());

        updateOrderPermissionErrorCodes.put(OrderType.RETAIL.toString(), ErrorCodes.HAS_NOT_PERMISSION_TO_UPDATE_RETAIL_BILL);
        updateOrderPermissionErrorCodes.put(OrderType.ONLINE.toString(), ErrorCodes.HAS_NOT_PERMISSION_TO_UPDATE_ORDER);
        updateOrderPermissionErrorCodes.put(OrderType.WHOLESALE.toString(), ErrorCodes.HAS_NOT_PERMISSION_TO_UPDATE_WHOLESALE_BILL);

        viewOrderPermissionErrorCodes.put(OrderType.RETAIL.toString(), ErrorCodes.HAS_NOT_PERMISSION_TO_VIEW_DETAIL_RETAIL_BILL);
        viewOrderPermissionErrorCodes.put(OrderType.ONLINE.toString(), ErrorCodes.HAS_NOT_PERMISSION_TO_VIEW_DETAIL_ORDER);
        viewOrderPermissionErrorCodes.put(OrderType.WHOLESALE.toString(), ErrorCodes.HAS_NOT_PERMISSION_TO_VIEW_DETAIL_WHOLESALE_BILL);

        viewListOrderPermissionErrorCodes.put(OrderType.RETAIL.toString(), ErrorCodes.HAS_NOT_PERMISSION_TO_VIEW_LIST_RETAIL_BILL);
        viewListOrderPermissionErrorCodes.put(OrderType.ONLINE.toString(), ErrorCodes.HAS_NOT_PERMISSION_TO_VIEW_LIST_ORDER);
        viewListOrderPermissionErrorCodes.put(OrderType.WHOLESALE.toString(), ErrorCodes.HAS_NOT_PERMISSION_TO_VIEW_LIST_WHOLESALE_BILL);

        editOrderPricePermissionErrorCodes.put(OrderType.RETAIL.toString(), ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_RETAIL_BILL_PRICE);
        editOrderPricePermissionErrorCodes.put(OrderType.ONLINE.toString(), ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_ORDER_PRICE);
        editOrderPricePermissionErrorCodes.put(OrderType.WHOLESALE.toString(), ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_WHOLESALE_BILL_PRICE);

        editOrderPriceComboPermissionErrorCodes.put(OrderType.RETAIL.toString(), ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_RETAIL_PRICE_COMBO);
        editOrderPriceComboPermissionErrorCodes.put(OrderType.ONLINE.toString(), ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_ORDER_PRICE_COMBO);
        editOrderPriceComboPermissionErrorCodes.put(OrderType.WHOLESALE.toString(), ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_WHOLESALE_PRICE_COMBO);

        editOrderDiscountPermissionErrorCodes.put(OrderType.RETAIL.toString(), ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_RETAIL_BILL_DISCOUNT);
        editOrderDiscountPermissionErrorCodes.put(OrderType.ONLINE.toString(), ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_ORDER_DISCOUNT);
        editOrderDiscountPermissionErrorCodes.put(OrderType.WHOLESALE.toString(), ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_WHOLESALE_BILL_DISCOUNT);

        editCartDiscountPermissionErrorCodes.put(OrderType.RETAIL.toString(), ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_DISCOUNT_NEW_RETAIL_BILL);
        editCartDiscountPermissionErrorCodes.put(OrderType.ONLINE.toString(), ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_DISCOUNT_NEW_ONLINE_BILL);
        editCartDiscountPermissionErrorCodes.put(OrderType.WHOLESALE.toString(), ErrorCodes.HAS_NOT_PERMISSION_TO_EDIT_DISCOUNT_NEW_WHOLESALE_BILL);

        createOrderPermissionErrorCodes.put(OrderType.RETAIL.toString(), ErrorCodes.HAS_NOT_PERMISSION_TO_CREATE_RETAIL_BILL);
        createOrderPermissionErrorCodes.put(OrderType.ONLINE.toString(), ErrorCodes.HAS_NOT_PERMISSION_TO_CREATE_ORDER);
        createOrderPermissionErrorCodes.put(OrderType.WHOLESALE.toString(), ErrorCodes.HAS_NOT_PERMISSION_TO_CREATE_WHOLESALE_BILL);
    }

    @Override
    public boolean userBelongTo(Long companyId) {
        return permissionClient.isUserBelongTo(companyId);
    }

    @Override
    public boolean checkPermission(CheckPermissionRequest request) {
        CheckPermissionData checkPermissionData = permissionClient.checkPermission(request);
        if (checkPermissionData == null) {
            return false;
        }

        return Boolean.TRUE.equals(checkPermissionData.getPermission());
    }

    @Override
    public boolean checkPermission(String permission, Long userId, Long companyId) {
        CheckPermissionRequest request = new CheckPermissionRequest();
        request.setCode(permission);
        request.setUserId(userId);
        request.setCompanyId(companyId);
        return checkPermission(request);
    }

    @Override
    public void checkPlaceOrder(Long companyId, String cartCode) {
        CartModel cart = cartService.findByCodeAndUserIdAndCompanyId(cartCode, authService.getCurrentUserId(), companyId);
        Long currentUserId = authService.getCurrentUserId();
        if (!checkCartPermissionAbstract(currentUserId, companyId, cart, createOrderPermission)) {
            ErrorCodes err = createOrderPermissionErrorCodes.get(cart.getType());
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        checkWarehousePermission(currentUserId, companyId, cart);
    }

    private void checkWarehousePermission(Long currentUserId, Long companyId, CartModel cart) {
        CheckPermissionData checkPermissionData = checkPermissionData(PermissionCodes.MANAGE_ALL_WAREHOUSES.code(), companyId, currentUserId);
        if (checkPermissionData == null || !Boolean.TRUE.equals(checkPermissionData.getPermission())) {
            List<Long> userWarehouses = authService.getUserWarehouses(currentUserId, companyId);
            if (!userWarehouses.contains(cart.getWarehouseId())) {
                ErrorCodes err = ErrorCodes.HAS_NOT_WAREHOUSE_PERMISSION_TO_CREATE_ORDER;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }

    }

    @Override
    public void checkUpdateOrder(Long companyId, String orderCode) {
        OrderModel order = checkPermissionUpdateOrder(companyId, orderCode);
        validateOnlineOrder(order);
    }

    private OrderModel checkPermissionUpdateOrder(Long companyId, String orderCode) {
        if (companyId == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        OrderModel order = orderService.findByCodeAndCompanyId(orderCode, companyId);
        this.checkOrderPermissionAbstract(companyId, order, updateOrderPermissions, updateOrderPermissionErrorCodes);
        return order;
    }

    private void validateOnlineOrder(OrderModel order) {
        if (OrderType.ONLINE.toString().equals(order.getType()) && OrderStatus.COMPLETED.code().equals(order.getOrderStatus())
                && hasEditCompletedOnlineOrder(order.getCompanyId()) && !order.isExchange()) {
            return;
        }
        OrderStatus currentStatus = OrderStatus.findByCode(order.getOrderStatus());
        if (OrderType.ONLINE.toString().equals(order.getType()) && currentStatus != null
                && OrderStatus.CONFIRMED.value() <= currentStatus.value() && currentStatus.value() <= OrderStatus.RETURNING.value()
                && hasEditConfirmOnlineOrder(order.getCompanyId()) && !order.isExchange()) {
            return;
        }
        if (OrderType.ONLINE.toString().equals(order.getType()) && !isValidEditingOnlineOrder(order)
                && !order.isExchange()) {
            ErrorCodes err = ErrorCodes.CANNOT_UPDATE_ONLINE_ORDER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }


    @Override
    public void checkViewOrderDetail(Long companyId, String orderCode) {
        OrderModel order = orderService.findByCodeAndCompanyId(orderCode, companyId);
        this.checkOrderPermissionAbstract(companyId, order, viewOrderPermissions, viewOrderPermissionErrorCodes);

        boolean hasViewAll = this.hasViewAllOrderPermission(companyId, order.getType());
        if (!hasViewAll && order.getCreateByUser() != null && !order.getCreateByUser().equals(authService.getCurrentUserId())) {
            ErrorCodes err = ErrorCodes.CANNOT_VIEW_ORDER_NOT_BELONG_TO_ACCOUNT;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Override
    public void checkUpdateOrderDiscount(Long companyId, String orderCode) {
        OrderModel order = orderService.findByCodeAndCompanyId(orderCode, companyId);
        if (order.isExchange()) {
            if (!checkOrderPermission(authService.getCurrentUserId(), companyId, PermissionCodes.EDIT_DISCOUNT_ON_EXCHANGE.toString())) {
                ErrorCodes err = ErrorCodes.HAS_NOT_PERMISSION_EDIT_DISCOUNT_ON_EXCHANGE;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
            return;
        }

        this.checkOrderPermissionAbstract(companyId, order, editOrderDiscountPermissions, editOrderDiscountPermissionErrorCodes);
        validateOnlineOrder(order);
    }

    @Override
    public void checkUpdateOrderPrice(OrderEntryDTO orderEntryDTO, String orderCode) {
        Long companyId = orderEntryDTO.getCompanyId();
        OrderModel order = orderService.findByCodeAndCompanyId(orderCode, companyId);
        if (order.isExchange()) {
            validateEditPriceOnExchange(companyId);
            return;
        }
        try {
            this.checkOrderPermissionAbstract(companyId, order, editOrderPricePermissions, editOrderPricePermissionErrorCodes);
        } catch (ServiceException e) {
            if (Boolean.TRUE.equals(orderEntryDTO.getCombo())) {
                this.checkOrderPermissionAbstract(companyId, order, editOrderPriceComboPermissions, editOrderPriceComboPermissionErrorCodes);
                return;
            }

            throw e;
        }
        validateOnlineOrder(order);
    }

    private void validateEditPriceOnExchange(Long companyId) {
        if (!checkOrderPermission(authService.getCurrentUserId(), companyId, PermissionCodes.EDIT_PRICE_ON_EXCHANGE.toString())) {
            ErrorCodes err = ErrorCodes.HAS_NOT_PERMISSION_EDIT_PRICE_ON_EXCHANGE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Override
    public void checkUpdateCartDiscount(Long companyId, String cartCode) {
        Long currentUserId = authService.getCurrentUserId();
        CartModel cart = cartService.findByCodeAndUserIdAndCompanyId(cartCode, authService.getCurrentUserId(), companyId);
        if (cart.isExchange()) {
            if (!checkOrderPermission(currentUserId, companyId, PermissionCodes.EDIT_DISCOUNT_ON_EXCHANGE.toString())) {
                ErrorCodes err = ErrorCodes.HAS_NOT_PERMISSION_EDIT_DISCOUNT_ON_EXCHANGE;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
            return;
        }

        if (!checkCartPermissionAbstract(currentUserId, companyId, cart, editOrderDiscountPermissions) && !checkCartPermissionAbstract(currentUserId, companyId, cart, editCartDiscountPermissions)) {
            ErrorCodes err = editCartDiscountPermissionErrorCodes.get(cart.getType());
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Override
    public void checkUpdateCartPrice(OrderEntryDTO orderEntryDTO, String cartCode) {
        Long currentUserId = authService.getCurrentUserId();
        Long companyId = orderEntryDTO.getCompanyId();
        CartModel cart = cartService.findByCodeAndUserIdAndCompanyId(cartCode, authService.getCurrentUserId(), companyId);

        if (cart.isExchange()) {
            validateEditPriceOnExchange(companyId);
            return;
        }

        if (checkCartPermissionAbstract(currentUserId, companyId, cart, editOrderPricePermissions)) {
            return;
        }

        if (!Boolean.TRUE.equals(orderEntryDTO.getCombo())) {
            ErrorCodes err = editOrderPricePermissionErrorCodes.get(cart.getType());
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (!checkCartPermissionAbstract(currentUserId, companyId, cart, editOrderPriceComboPermissions)) {
            ErrorCodes err = editOrderPriceComboPermissionErrorCodes.get(cart.getType());
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Override
    public boolean hasPermission(String code, Long companyId) {
        return this.checkPermission(code, authService.getCurrentUserId(), companyId);
    }

    @Override
    public void checkSearchingOrderPermission(OrderSearchRequest orderSearchRequest) {
        Long currentUserId = authService.getCurrentUserId();
        String viewAllPermission = viewAllListOrderPermissions.get(orderSearchRequest.getOrderType());
        boolean hasViewAllPermission = false;
        if (StringUtils.isNotBlank(viewAllPermission)) {
            hasViewAllPermission = checkOrderPermission(currentUserId, orderSearchRequest.getCompanyId(), viewAllPermission);
        }

        if (!hasViewAllPermission) {
            String permissionCode = viewListOrderPermissions.get(orderSearchRequest.getOrderType());
            if (StringUtils.isBlank(permissionCode)) {
                ErrorCodes err = ErrorCodes.ACCESS_DENIED;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }

            boolean hasPermission = checkOrderPermission(currentUserId, orderSearchRequest.getCompanyId(), permissionCode);
            if (!hasPermission) {
                ErrorCodes err = viewListOrderPermissionErrorCodes.get(orderSearchRequest.getOrderType());
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }

        if (orderSearchRequest.getWarehouseId() != null) {
            UserHasWarehouseRequest userHasWarehouserequest = new UserHasWarehouseRequest();
            userHasWarehouserequest.setWarehouseId(orderSearchRequest.getWarehouseId());
            userHasWarehouserequest.setCompanyId(orderSearchRequest.getCompanyId());
            if (!permissionClient.userHasWarehouse(userHasWarehouserequest)) {
                ErrorCodes err = ErrorCodes.USER_HAS_NOT_PERMISSION_ON_WAREHOUSE;
                throw new ServiceException(err.code(), err.message(), err.httpStatus(),
                        new Object[]{orderSearchRequest.getWarehouseId()});
            }
        }
    }

    @Override
    public void checkUpdateOrderInfo(Long companyId, String orderCode) {
        checkPermissionUpdateOrder(companyId, orderCode);
    }

    @Override
    public boolean hasViewAllOrderPermission(Long companyId, String orderType) {
        String permissionCode = viewAllListOrderPermissions.get(orderType);
        return checkPermission(permissionCode, authService.getCurrentUserId(), companyId);
    }

    private boolean hasEditCompletedOnlineOrder(Long companyId) {
        return checkPermission(PermissionCodes.EDIT_COMPLETED_ONLINE_ORDER.code(), authService.getCurrentUserId(), companyId);
    }

    private boolean hasEditConfirmOnlineOrder(Long companyId) {
        return checkPermission(PermissionCodes.CAN_UPDATE_INFORMATION_ORDER_CONFIRM.code(), authService.getCurrentUserId(), companyId);
    }

    private boolean isValidEditingOnlineOrder(OrderModel order) {
        OrderStatus orderStatus = OrderStatus.findByCode(order.getOrderStatus());
        return OrderType.ONLINE.toString().equals(order.getType()) &&
                orderStatus != null && orderStatus.value() < OrderStatus.CONFIRMED.value();

    }

    private boolean checkOrderPermission(Long currentUserId, Long companyId, String permissionCode) {
        if (companyId == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (StringUtils.isBlank(permissionCode)) {
            return false;
        }
        CheckPermissionData checkPermissionData = checkPermissionData(permissionCode, companyId, currentUserId);
        if (checkPermissionData == null) {
            return false;
        }

        return Boolean.TRUE.equals(checkPermissionData.getPermission());
    }

    private CheckPermissionData checkPermissionData(String permissionCode, Long companyId, Long currentUserId) {
        CheckPermissionRequest permissionRequest = new CheckPermissionRequest();
        permissionRequest.setCode(permissionCode);
        permissionRequest.setUserId(currentUserId);
        permissionRequest.setCompanyId(companyId);
        return permissionClient.checkPermission(permissionRequest);
    }

    private void validateNotNull(AbstractOrderModel order) {
        if (order == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_CODE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    private boolean checkCartPermissionAbstract(Long currentUserId, Long companyId, CartModel cart, Map<String, String> permission) {
        validateNotNull(cart);
        String permissionCode = permission.get(cart.getType());
        return checkOrderPermission(currentUserId, companyId, permissionCode);
    }

    private void checkOrderPermissionAbstract(Long companyId, OrderModel order, Map<String, String> permission, Map<String, ErrorCodes> error) {
        validateNotNull(order);
        Long currentUserId = authService.getCurrentUserId();
        String permissionCode = permission.get(order.getType());
        boolean hasPermission = checkOrderPermission(currentUserId, companyId, permissionCode);
        if (!hasPermission) {
            ErrorCodes err = error.get(order.getType());
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Override
    public void checkUpdateRecommendedRetailPriceOrder(OrderEntryDTO orderEntryDTO, String orderCode) {
        Long companyId = orderEntryDTO.getCompanyId();
        OrderModel order = orderService.findByCodeAndCompanyId(orderCode, companyId);
        validateUpdateRecommendedRetailPrice(order);
        validateOnlineOrder(order);
    }

    @Override
    public void checkUpdateRecommendedRetailPriceCart(OrderEntryDTO orderEntryDTO, String orderCode) {
        Long companyId = orderEntryDTO.getCompanyId();
        CartModel cartModel = cartService.findByCodeAndCompanyId(orderCode, companyId);
        validateUpdateRecommendedRetailPrice(cartModel);
    }

    private void validateUpdateRecommendedRetailPrice(AbstractOrderModel abstractOrderModel) {
        if (!OrderType.ONLINE.toString().equals(abstractOrderModel.getType())
                || (OrderType.ONLINE.toString().equals(abstractOrderModel.getType()) && !PriceType.DISTRIBUTOR_PRICE.toString().equals(abstractOrderModel.getPriceType()))) {
            ErrorCodes err = ErrorCodes.HAS_NOT_PERMISSION_TO_UPDATE_RECOMMENDED_RETAIL_PRICE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (abstractOrderModel.getDistributorId() == null) {
            ErrorCodes err = ErrorCodes.INVALID_DISTRIBUTOR_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        boolean canEditRecommendedRetailPrice = checkPermission(PermissionCodes.EDIT_RECOMMENDED_RETAIL_PRICE.code(), authService.getCurrentUserId(), abstractOrderModel.getCompanyId());
        if (!canEditRecommendedRetailPrice) {
            ErrorCodes err = ErrorCodes.HAS_NOT_PERMISSION_TO_UPDATE_RECOMMENDED_RETAIL_PRICE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Autowired
    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }
}
