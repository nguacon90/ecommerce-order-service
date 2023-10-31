package com.vctek.orderservice.validator;

import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.ComboData;
import com.vctek.orderservice.dto.MoneySourceData;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.dto.request.ReturnOrderEntryRequest;
import com.vctek.orderservice.dto.request.ReturnOrderRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.LoyaltyCardData;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.service.*;
import com.vctek.util.CardStatus;
import com.vctek.util.CommonUtils;
import com.vctek.util.OrderStatus;
import com.vctek.util.OrderType;
import com.vctek.validate.Validator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("returnOrderRequestValidator")
public class ReturnOrderRequestValidator implements Validator<ReturnOrderRequest> {
    private OrderService orderService;
    private CartService cartService;
    private AuthService authService;
    private FinanceService financeService;
    private LoyaltyService loyaltyService;
    private ProductService productService;

    @Override
    public void validate(ReturnOrderRequest returnOrderRequest) throws ServiceException {
        if(returnOrderRequest.getCompanyId() == null) {
            ErrorCodes err = ErrorCodes.EMPTY_COMPANY_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        String originOrderCode = returnOrderRequest.getOriginOrderCode();
        if(StringUtils.isBlank(originOrderCode)) {
            ErrorCodes err = ErrorCodes.INVALID_ORIGIN_ORDER_CODE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        OrderModel originOrder = orderService.findByCodeAndCompanyId(originOrderCode, returnOrderRequest.getCompanyId());
        if(originOrder == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORIGIN_ORDER_CODE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(OrderType.ONLINE.toString().equals(originOrder.getType()) &&
                !OrderStatus.COMPLETED.code().equals(originOrder.getOrderStatus())) {
            ErrorCodes err = ErrorCodes.CANNOT_CREATE_RETURN_FOR_UNCOMPLETED_ONLINE_ORDER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if(StringUtils.isNotBlank(returnOrderRequest.getNote()) &&
                returnOrderRequest.getNote().length() > AbstractOrderRequestValidator.MAXIMUM_NOTE_LENGTH) {
            ErrorCodes err = ErrorCodes.NOTE_OVER_MAX_LENGTH;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        if (returnOrderRequest.getReturnOrderId() == null) {
            validateReturnOrderEntry(originOrder, returnOrderRequest);
        }


        validateExchangeCart(returnOrderRequest);

        validateExchangeLoyaltyCard(returnOrderRequest);

        validatePayments(originOrder, returnOrderRequest);
    }

    private void validateExchangeLoyaltyCard(ReturnOrderRequest returnOrderRequest) {
        String exchangeLoyaltyCard = returnOrderRequest.getExchangeLoyaltyCard();
        if(StringUtils.isNotBlank(exchangeLoyaltyCard)) {
            LoyaltyCardData cardData = loyaltyService.findByCardNumber(exchangeLoyaltyCard, returnOrderRequest.getCompanyId());
            if(!CardStatus.ACTIVE.code().equals(cardData.getStatus())) {
                ErrorCodes err = ErrorCodes.INACTIVE_LOYALTY_CARD;
                throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{exchangeLoyaltyCard});
            }

            if(StringUtils.isBlank(cardData.getAssignedPhone())) {
                ErrorCodes err = ErrorCodes.LOYALTY_CARD_HAS_NOT_ASSIGNED;
                throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{exchangeLoyaltyCard});
            }
        }
    }

    private void validateReturnOrderEntry(OrderModel originOrder, ReturnOrderRequest returnOrderRequest) {
        List<ReturnOrderEntryRequest> returnOrderEntries = returnOrderRequest.getReturnOrderEntries();
        if(CollectionUtils.isEmpty(returnOrderEntries)) {
            ErrorCodes err = ErrorCodes.EMPTY_RETURN_ORDER_ENTRY;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        Map<Long, AbstractOrderEntryModel> orderEntryMap = originOrder.getEntries().stream()
                .collect(Collectors.toMap(AbstractOrderEntryModel::getId, e -> e));

        for(ReturnOrderEntryRequest req : returnOrderEntries) {
            AbstractOrderEntryModel entryModel = orderEntryMap.get(req.getOrderEntryId());
            if(entryModel == null ) {
                ErrorCodes err = ErrorCodes.INVALID_ENTRY_NUMBER;
                throw new ServiceException(err.code(), err.message(), err.httpStatus(),
                        new Object[]{req.getEntryNumber()});
            }
            long availableReturnQty = entryModel.getQuantity() - CommonUtils.readValue(entryModel.getReturnQuantity());
            if(req.getQuantity() == null || req.getQuantity() <= 0 ||
                    req.getQuantity() > availableReturnQty) {
                ErrorCodes err = ErrorCodes.INVALID_RETURN_ORDER_ENTRY_QUANTITY;
                throw new ServiceException(err.code(), err.message(), err.httpStatus(),
                        new Object[]{entryModel.getProductId(), entryModel.getQuantity()});
            }
        }
    }

    private void validatePayments(OrderModel originOrder, ReturnOrderRequest returnOrderRequest) {
        List<PaymentTransactionRequest> payments = returnOrderRequest.getPayments();
        if(CollectionUtils.isEmpty(payments)) {
            return;
        }

        for(PaymentTransactionRequest payment : payments) {
            if(payment.getAmount() != null && payment.getAmount() > 0 && payment.getMoneySourceId() == null) {
                ErrorCodes err = ErrorCodes.INVALID_MONEY_SOURCE_ID;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
            MoneySourceData moneySource = financeService.getMoneySource(payment.getMoneySourceId(), originOrder.getCompanyId());
            if(moneySource == null) {
                ErrorCodes err = ErrorCodes.INVALID_MONEY_SOURCE_ID;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }

            if(payment.getAmount() != null && payment.getAmount() > 0
                    && "CASH".equalsIgnoreCase(moneySource.getType()) && payment.getWarehouseId() == null) {
                ErrorCodes err = ErrorCodes.EMPTY_CASH_PAYMENT_WAREHOUSE_ID;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
        }
    }

    private void validateExchangeCart(ReturnOrderRequest returnOrderRequest) {
        String exchangeCartCode = returnOrderRequest.getExchangeCartCode();
        if(returnOrderRequest.isExchange()) {
            if(StringUtils.isBlank(exchangeCartCode)) {
                ErrorCodes err = ErrorCodes.INVALID_EXCHANGE_CART_CODE;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }
            if(returnOrderRequest.getReturnOrderId() == null) {
                CartModel exchangeCart = cartService.findByCodeAndUserIdAndCompanyId(exchangeCartCode, authService.getCurrentUserId(),
                        returnOrderRequest.getCompanyId());
                if(exchangeCart == null) {
                    ErrorCodes err = ErrorCodes.INVALID_EXCHANGE_CART_CODE;
                    throw new ServiceException(err.code(), err.message(), err.httpStatus());
                }

                validateComboExchange(exchangeCart);
            }
        }
    }

    protected void validateComboExchange(AbstractOrderModel abstractOrderModel) {
        List<AbstractOrderEntryModel> entries = abstractOrderModel.getEntries();
        if(CollectionUtils.isEmpty(entries)) {
            ErrorCodes err = ErrorCodes.CART_HAS_NOT_ENTRIES;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        ComboData comboData;
        long totalItemQuantity;
        int subEntryTotalQuantity;
        for (AbstractOrderEntryModel entryModel : entries) {
            if (StringUtils.isNotEmpty(entryModel.getComboType())) {
                comboData = productService.getCombo(entryModel.getProductId(), abstractOrderModel.getCompanyId());
                if (comboData == null) {
                    ErrorCodes err = ErrorCodes.INVALID_COMBO_ID;
                    throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{entryModel.getProductId()});
                }
                subEntryTotalQuantity = entryModel.getSubOrderEntries().stream().filter(se -> se.getQuantity() != null)
                        .mapToInt(SubOrderEntryModel::getQuantity).sum();
                totalItemQuantity = comboData.getTotalItemQuantity() * entryModel.getQuantity();
                if (totalItemQuantity != subEntryTotalQuantity) {
                    ErrorCodes err = ErrorCodes.INVALID_SUB_ORDER_ENTRY_QUANTITY;
                    throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{comboData.getName()});
                }
            }
        }
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    public void setCartService(CartService cartService) {
        this.cartService = cartService;
    }

    @Autowired
    public void setFinanceService(FinanceService financeService) {
        this.financeService = financeService;
    }

    @Autowired
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    @Autowired
    public void setLoyaltyService(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

}
