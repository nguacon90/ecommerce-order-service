package com.vctek.orderservice.aop;

import com.vctek.dto.CheckCreateTransferWarehouseData;
import com.vctek.dto.request.CheckCreateTransferWarehouseRequest;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.OrderEntryDTO;
import com.vctek.orderservice.dto.request.*;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.facade.PermissionFacade;
import com.vctek.orderservice.feignclient.CompanyClient;
import com.vctek.orderservice.feignclient.dto.ProductStockData;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.OrderHistoryModel;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.AuthService;
import com.vctek.orderservice.service.InventoryService;
import com.vctek.orderservice.service.OrderHistoryService;
import com.vctek.orderservice.service.OrderService;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import com.vctek.util.PermissionCodes;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Aspect
public class ValidateModifiedOrderAspect extends AbstractValidateAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateModifiedOrderAspect.class);
    private OrderService orderService;
    private OrderHistoryService orderHistoryService;
    private int hourOfDayForModified;
    private int twoDayInMillis = 172800000;
    private PermissionFacade permissionFacade;
    private AuthService authService;
    @Before("execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.addEntryToOrder(..)) && args(orderEntryDTO)" +
            "|| execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.updateEntry(..)) && args(orderEntryDTO)" +
            "|| execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.updatePriceOrderEntry(..)) && args(orderEntryDTO)" +
            "|| execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.updateDiscountOfEntry(..)) && args(orderEntryDTO)")
    public void validateUpdateOrderEntry(OrderEntryDTO orderEntryDTO) {
        if (orderEntryDTO == null) {
            return;
        }

        validateValidToModified(orderEntryDTO.getOrderCode(), orderEntryDTO.getCompanyId());
    }

    @Before("execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.updateDiscountOfOrder(..)) " +
            "&& args(cartDiscountRequest)")
    public void validateUpdateOrderDiscount(CartDiscountRequest cartDiscountRequest) {
        if (cartDiscountRequest == null) {
            return;
        }

        validateValidToModified(cartDiscountRequest.getCode(), cartDiscountRequest.getCompanyId());
    }

    @Before("execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.updateVatOfOrder(..)) " +
            "&& args(vatRequest)")
    public void validateUpdateOrderVat(VatRequest vatRequest) {
        if (vatRequest == null) {
            return;
        }
        validateValidToModified(vatRequest.getCode(), vatRequest.getCompanyId());
    }

    @Before("execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.applyCoupon(..)) && args(appliedCouponRequest)" +
            "|| execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.removeCoupon(..)) && args(appliedCouponRequest)")
    public void validateUpdateCoupon(AppliedCouponRequest appliedCouponRequest) {
        if (appliedCouponRequest == null) {
            return;
        }

        validateValidToModified(appliedCouponRequest.getOrderCode(), appliedCouponRequest.getCompanyId());
    }

    @Before("execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.addProductToCombo(..)) && args(comboRequest)" +
            "|| execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.addComboToOrderIndirectly(..)) && args(comboRequest)")
    public void validateModifiedCombo(AddSubOrderEntryRequest comboRequest) {
        if (comboRequest == null) {
            return;
        }

        validateValidToModified(comboRequest.getOrderCode(), comboRequest.getCompanyId());
    }

    @Before("execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.removeSubEntry(..)) " +
            "&& args(removeSubOrderEntryRequest)")
    public void validateRemoveProductOfCombo(RemoveSubOrderEntryRequest removeSubOrderEntryRequest) {
        if (removeSubOrderEntryRequest == null) {
            return;
        }

        validateValidToModified(removeSubOrderEntryRequest.getOrderCode(), removeSubOrderEntryRequest.getCompanyId());
    }

    @Before("execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.markEntrySaleOff(..)) " +
            "&& args(entrySaleOffRequest)")
    public void validateMarkEntrySaleOff(EntrySaleOffRequest entrySaleOffRequest) {
        if (entrySaleOffRequest == null) {
            return;
        }

        validateValidToModified(entrySaleOffRequest.getOrderCode(), entrySaleOffRequest.getCompanyId());
    }


    @Before("execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.changeStatusOrder(..)) " +
            "&& args(changeOrderStatusRequest)")
    public void validateChangeStatusOrder(ChangeOrderStatusRequest changeOrderStatusRequest) {
        if (changeOrderStatusRequest == null) {
            return;
        }

        OrderModel orderModel = orderService.findByCodeAndCompanyIdAndDeleted(changeOrderStatusRequest.getOrderCode(),
                changeOrderStatusRequest.getCompanyId(), false);
        if (orderModel == null) {
            return;
        }

        if (orderModel.isExchange()) {
            ErrorCodes err = ErrorCodes.CANNOT_MODIFIED_DIRECT_EXCHANGE_ORDER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        validateValidToModified(changeOrderStatusRequest.getOrderCode(), changeOrderStatusRequest.getCompanyId());
        validateChangeStatusSettingCreateTransferWarehouse(changeOrderStatusRequest.getOrderStatus(), orderModel);
    }

    private void validateChangeStatusSettingCreateTransferWarehouse(String orderStatus, OrderModel orderModel) {
        CheckCreateTransferWarehouseRequest request = new CheckCreateTransferWarehouseRequest();
        request.setOrderCode(orderModel.getCode());
        request.setWarehouseId(orderModel.getWarehouseId());
        request.setCompanyId(orderModel.getCompanyId());
        request.setOrderStatus(orderStatus);
        request.setCurrentOrderStatus(orderModel.getOrderStatus());
        CheckCreateTransferWarehouseRequest requestValid = new CheckCreateTransferWarehouseRequest();
        requestValid.setRequests(Arrays.asList(request));
        requestValid.setCompanyId(orderModel.getCompanyId());
        Map<String, CheckCreateTransferWarehouseData> hasValidCreateTransferWarehouse = logisticService.checkValidCreateTransferWarehouse(requestValid);
        CheckCreateTransferWarehouseData data = hasValidCreateTransferWarehouse.get(orderModel.getCode());
        if (data == null) return;
        if (data.isHasCreateTransferWarehouse()) {
            ErrorCodes err = ErrorCodes.HAS_NOT_CHANGE_STATUS_IN_SETTING;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{data.getOrderStatus()});
        }

        if (!orderStatus.equals(data.getOrderStatus())) return;
        if (Boolean.FALSE.equals(data.isDeliveryWarehouseActive())) {
            ErrorCodes err = ErrorCodes.INACTIVE_DELIVERY_WAREHOUSE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{data.getOrderStatus()});
        }
        logisticService.validateTransferLessZero(orderModel, data.getDeliveryWarehouseId());
    }

    @Before("execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.updateOrderInfo(..)) " +
            "&& args(orderRequest)")
    public void validateUpdateOrderInfo(OrderRequest orderRequest) {
        if (orderRequest == null) {
            return;
        }
        OrderModel orderModel = orderService.findByCodeAndCompanyIdAndDeleted(orderRequest.getCode(), orderRequest.getCompanyId(), false);
        if (orderModel == null) {
            return;
        }

        if (orderModel.isExchange()) {
            ErrorCodes err = ErrorCodes.CANNOT_MODIFIED_DIRECT_EXCHANGE_ORDER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        validateHasReturnOrder(orderModel);

        validateActiveWarehouseOf(orderModel);

        if (OrderType.RETAIL.toString().equals(orderModel.getType()) && OrderStatus.CHANGE_TO_RETAIL.code().equals(orderModel.getOrderStatus())) {
            ErrorCodes err = ErrorCodes.CANNOT_UPDATE_CHANGE_TO_RETAIL_ORDER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Before("execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.changeOrderToRetail(..)) " +
            "&& args(orderCode, companyId)")
    public void validateUpdateChangeToRetail(String orderCode, Long companyId) {
        if (StringUtils.isBlank(orderCode)) {
            return;
        }
        OrderModel orderModel = orderService.findByCodeAndCompanyIdAndDeleted(orderCode, companyId, false);
        if (orderModel == null) {
            return;
        }
        validateValidToModified(orderCode, companyId);
        validateChangeStatusSettingCreateTransferWarehouse(OrderStatus.CHANGE_TO_RETAIL.code(), orderModel);
    }

    @Before("execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.updateRedeemOnline(..)) " +
            "&& args(orderCode, companyId, request)")
    public void validateUpdateRedeemOnline(String orderCode, Long companyId, PaymentTransactionRequest request) {
        if (StringUtils.isBlank(orderCode)) {
            return;
        }

        if (request.getAmount() == null) {
            return;
        }
        OrderModel orderModel = orderService.findByCodeAndCompanyIdAndDeleted(orderCode, companyId, false);
        validateOrderStatusNotModified(orderModel);

        boolean canUpdateInformationOrderConfirm = permissionFacade.checkPermission(PermissionCodes.CAN_UPDATE_INFORMATION_ORDER_CONFIRM.code(), authService.getCurrentUserId(), companyId);
        OrderStatus currentStatus = OrderStatus.findByCode(orderModel.getOrderStatus());
        boolean checkPermission = currentStatus.value() >= OrderStatus.CONFIRMED.value() && currentStatus.value() <= OrderStatus.ORDER_RETURN.value() && !canUpdateInformationOrderConfirm;
        if (!OrderStatus.COMPLETED.toString().equals(currentStatus.toString()) && currentStatus.value() > OrderStatus.RETURNING.value() || checkPermission) {
            ErrorCodes err = ErrorCodes.NOT_ACCEPT_MODIFIED_ORDER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    private void validateOrderStatusNotModified(OrderModel orderModel) {
        if (orderModel == null) {
            return;
        }

        validateHasReturnOrder(orderModel);

        if (!OrderType.ONLINE.toString().equals(orderModel.getType())) {
            ErrorCodes err = ErrorCodes.NOT_ACCEPT_MODIFIED_ORDER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Before("execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.cancelRedeem(..)) && args(orderCode, companyId)")
    public void validateCancelRedeemOnline(String orderCode, Long companyId) {
        validRedeemOnline(orderCode, companyId, true);
    }

    @Before("execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.createRedeemOnline(..)) && args(orderCode, companyId, request)")
    public void validateCreateRedeemOnline(String orderCode, Long companyId, PaymentTransactionRequest request) {
        if (request.getAmount() == null) {
            return;
        }
        validRedeemOnline(orderCode, companyId, false);
    }

    @Before("execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.updateSettingCustomerToOrder(..)) && args(request) " +
            "|| execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.updateShippingFee(..)) && args(request)")
    public void validateUpdateSettingCustomerAndShippingFee(OrderRequest request) {
        if (request == null) {
            return;
        }

        validateValidToModified(request.getCode(), request.getCompanyId());
    }


    @Before("execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.addVAT(..)) && args(companyId, orderCode, addVat)")
    public void validateAddVat(Long companyId, String orderCode, boolean addVat) {
        validateValidToModified(orderCode, companyId);
    }

    private void validRedeemOnline(String orderCode, Long companyId, boolean isCancel) {
        if (StringUtils.isBlank(orderCode)) {
            return;
        }
        OrderModel orderModel = orderService.findByCodeAndCompanyIdAndDeleted(orderCode, companyId, false);
        validateOrderStatusNotModified(orderModel);

        boolean canUpdateInformationOrderConfirm = permissionFacade.checkPermission(PermissionCodes.CAN_UPDATE_INFORMATION_ORDER_CONFIRM.code(), authService.getCurrentUserId(), companyId);
        OrderStatus currentStatus = OrderStatus.findByCode(orderModel.getOrderStatus());
        boolean checkPermission = (currentStatus.value() >= OrderStatus.CONFIRMED.value() && currentStatus.value() <= OrderStatus.RETURNING.value() && !canUpdateInformationOrderConfirm) || currentStatus.value() > OrderStatus.RETURNING.value();
        boolean checkStatus = isCancel ? checkPermission : checkPermission && !currentStatus.equals(OrderStatus.COMPLETED);
        if (checkStatus) {
            ErrorCodes err = ErrorCodes.NOT_ACCEPT_MODIFIED_ORDER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }


    protected void validateValidToModified(String orderCode, Long companyId) {
        try {
            OrderModel orderModel = orderService.findByCodeAndCompanyIdAndDeleted(orderCode, companyId, false);
            if (orderModel == null) {
                return;
            }

            validateLockOrderOnline(orderModel);

            validateHasReturnOrder(orderModel);

            validateActiveWarehouseOf(orderModel);

            String type = orderModel.getType();
            if (OrderType.RETAIL.toString().equals(type) && OrderStatus.CHANGE_TO_RETAIL.code().equals(orderModel.getOrderStatus())) {
                ErrorCodes err = ErrorCodes.CANNOT_UPDATE_CHANGE_TO_RETAIL_ORDER;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }

            Date checkingTime = null;
            if (OrderType.RETAIL.toString().equals(type) || OrderType.WHOLESALE.toString().equals(type)) {
                checkingTime = orderModel.getCreatedTime();
            } else if (OrderType.ONLINE.toString().equals(type) &&
                    OrderStatus.COMPLETED.code().equals(orderModel.getOrderStatus())) {

                boolean canEditCompletedOnlineOrder = permissionFacade.checkPermission(PermissionCodes.EDIT_COMPLETED_ONLINE_ORDER.code(), authService.getCurrentUserId(), companyId);
                if (canEditCompletedOnlineOrder) {
                    return;
                }
                Optional<OrderHistoryModel> firstSuccessStatus = orderHistoryService.findFirstSuccessStatusOf(orderModel);
                if (firstSuccessStatus.isPresent()) {
                    OrderHistoryModel orderHistoryModel = firstSuccessStatus.get();
                    checkingTime = orderHistoryModel.getModifiedTime();
                }
            }

            if (checkingTime != null && !isValidTimeForModify(checkingTime)) {
                ErrorCodes err = ErrorCodes.OVER_TIME_TO_MODIFY_ORDER;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }

        } catch (ServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    protected boolean isValidTimeForModify(Date checkingTime) {

        if (checkingTime == null) {
            return true;
        }

        Calendar checkingCal = Calendar.getInstance();
        checkingCal.setTime(checkingTime);

        Calendar currentCal = this.getCurrentCal();
        if ((currentCal.getTimeInMillis() - checkingCal.getTimeInMillis()) > twoDayInMillis) {
            return false;
        }

        int checkingDay = checkingCal.get(Calendar.DAY_OF_MONTH);
        checkingCal.set(Calendar.DAY_OF_MONTH, checkingDay + 1);
        checkingCal.set(Calendar.HOUR_OF_DAY, hourOfDayForModified);
        checkingCal.set(Calendar.MINUTE, 0);
        checkingCal.set(Calendar.SECOND, 0);
        checkingCal.set(Calendar.MILLISECOND, 0);
        if (currentCal.after(checkingCal)) {
            return false;
        }

        return true;
    }

    private void validateHasReturnOrder(OrderModel order) {
        Set<ReturnOrderModel> returnOrders = order.getReturnOrders();
        if (CollectionUtils.isNotEmpty(returnOrders)) {
            ErrorCodes err = ErrorCodes.CANNOT_UPDATE_BECAUSE_ORDER_HAS_RETURN_ORDER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    private void validateLockOrderOnline(OrderModel orderModel) {
        if (OrderType.ONLINE.toString().equals(orderModel.getType()) && orderModel.isImportOrderProcessing()) {
            ErrorCodes err = ErrorCodes.ORDER_PROCESS_IMPORT_CHANGE_STATUS_CANNOT_UPDATE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Before("execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.updateEntry(..)) && args(dto)")
    public void validateRemoveEntryOrderOnlineWithStatusShipping(OrderEntryDTO dto) {
        if (dto.getQuantity() > 0) return;
        OrderModel orderModel = orderService.findByCodeAndCompanyId(dto.getOrderCode(), dto.getCompanyId());
        if (orderModel == null) return;
        validateRemoveProductOrderOnlineWithStatusShipping(orderModel);
    }

    @Before("execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.removeSubEntry(..)) && args(request)")
    public void validateRemoveSubEntryOrderOnlineWithStatusShipping(RemoveSubOrderEntryRequest request) {
        OrderModel orderModel = orderService.findByCodeAndCompanyId(request.getOrderCode(), request.getCompanyId());
        if (orderModel == null) return;
        validateRemoveProductOrderOnlineWithStatusShipping(orderModel);
    }

    @Before("execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.updateToppingOption(..)) && args(orderCode, request)")
    public void validateRemoveToppingOptionOrderOnlineWithStatusShipping(String orderCode, ToppingOptionRequest request) {
        if (request.getQuantity() > 0) return;
        OrderModel orderModel = orderService.findByCodeAndCompanyId(orderCode, request.getCompanyId());
        if (orderModel == null) return;
        validateRemoveProductOrderOnlineWithStatusShipping(orderModel);
    }

    @Before("execution(* com.vctek.orderservice.facade.impl.OrderFacadeImpl.removeToppingItems(..)) && args(orderCode, entryId, optionId, itemId, companyId)")
    public void validateRemoveToppingItemOrderOnlineWithStatusShipping(String orderCode, Long entryId, Long optionId, Long itemId, Long companyId) {
        OrderModel orderModel = orderService.findByCodeAndCompanyId(orderCode, companyId);
        if (orderModel == null) return;
        validateRemoveProductOrderOnlineWithStatusShipping(orderModel);
    }

    private void validateRemoveProductOrderOnlineWithStatusShipping(OrderModel orderModel) {
        if (!OrderType.ONLINE.toString().equals(orderModel.getType())) return;
        if (OrderStatus.SHIPPING.code().equals(orderModel.getOrderStatus()) || OrderStatus.RETURNING.code().equals(orderModel.getOrderStatus())) {
            ErrorCodes err = ErrorCodes.NOT_ACCEPT_MODIFIED_ORDER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    protected Calendar getCurrentCal() {
        return Calendar.getInstance();
    }


    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    public void setOrderHistoryService(OrderHistoryService orderHistoryService) {
        this.orderHistoryService = orderHistoryService;
    }

    @Value("${vctek.config.timeToModifyOrder:12}")
    public void setHourOfDayForModified(int hourOfDayForModified) {
        this.hourOfDayForModified = hourOfDayForModified;
    }

    @Autowired
    public void setPermissionFacade(PermissionFacade permissionFacade) {
        this.permissionFacade = permissionFacade;
    }

    @Autowired
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }
}
