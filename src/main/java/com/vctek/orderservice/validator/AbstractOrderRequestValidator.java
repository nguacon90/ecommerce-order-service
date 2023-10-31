package com.vctek.orderservice.validator;

import com.vctek.dto.request.CheckValidCardParameter;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.*;
import com.vctek.orderservice.dto.request.AvailablePointAmountRequest;
import com.vctek.orderservice.dto.request.CustomerRequest;
import com.vctek.orderservice.dto.request.OrderRequest;
import com.vctek.orderservice.dto.request.PaymentTransactionRequest;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.AbstractOrderModel;
import com.vctek.orderservice.model.SubOrderEntryModel;
import com.vctek.orderservice.service.*;
import com.vctek.util.CommonUtils;
import com.vctek.util.OrderType;
import com.vctek.util.PaymentMethodType;
import com.vctek.validate.Validator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public abstract class AbstractOrderRequestValidator implements Validator<OrderRequest> {
    public static final int MAXIMUM_NOTE_LENGTH = 500;
    public static final int MAXIMUM_PHONE_LENGTH = 20;
    protected FinanceService financeService;
    protected ProductService productService;
    protected LoyaltyService loyaltyService;
    protected CouponService couponService;
    protected AuthService authService;

    @Override
    public void validate(OrderRequest orderRequest) {
        if(StringUtils.isBlank(orderRequest.getCode())) {
            ErrorCodes err = ErrorCodes.EMPTY_CART_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        AbstractOrderModel abstractOrderModel = getOrder(orderRequest);
        if(abstractOrderModel == null) {
            ErrorCodes err = ErrorCodes.NOT_EXISTED_ORDER_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        validateEntry(abstractOrderModel);

        validateCustomerInfo(orderRequest, abstractOrderModel);

        validateNote(orderRequest);

        validateLoyaltyCardNumber(orderRequest, abstractOrderModel);

        validatePayments(orderRequest, abstractOrderModel);

        validatePaymentAmount(orderRequest, abstractOrderModel);

        validateCouponCode(abstractOrderModel);

        validateDeliveryDate(orderRequest);

    }

    protected void validateCouponCode(AbstractOrderModel abstractOrderModel) {
        ValidCouponCodeData validCouponData = couponService.getValidatedCouponCode(abstractOrderModel);
        if(!validCouponData.isValid()) {
            ErrorCodes err = ErrorCodes.EXISTED_INVALID_COUPON;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    protected void validateLoyaltyCardNumber(OrderRequest orderRequest, AbstractOrderModel abstractOrderModel) {
        String cardNumber = orderRequest.getCardNumber();
        if(StringUtils.isBlank(cardNumber)) {
            return;
        }

        CustomerRequest customer = orderRequest.getCustomer();
        if(customer == null || StringUtils.isBlank(customer.getPhone())) {
            ErrorCodes err = ErrorCodes.EMPTY_LOYALTY_PHONE_NUMBER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        String cleanCardNumber = cardNumber.trim();
        orderRequest.setCardNumber(cleanCardNumber);
        CheckValidCardParameter parameter = new CheckValidCardParameter();
        parameter.setCardNumber(cleanCardNumber);
        parameter.setCompanyId(abstractOrderModel.getCompanyId());
        parameter.setPhone(customer.getPhone());

        boolean isValidCardNumber = loyaltyService.isValid(parameter);
        if(!isValidCardNumber) {
            ErrorCodes err = ErrorCodes.INVALID_LOYALTY_CARD_NUMBER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    private void validatePaymentAmount(OrderRequest orderRequest, AbstractOrderModel abstractOrderModel) {
        String orderType = abstractOrderModel.getType();
        if(!OrderType.RETAIL.toString().equals(orderType)) {
            return;
        }

        List<PaymentTransactionRequest> payments = orderRequest.getPayments();
        if(CollectionUtils.isEmpty(payments)) {
            ErrorCodes err = ErrorCodes.EMPTY_PAYMENT_METHOD_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        double totalPaidAmount = payments.stream()
                .filter(p -> p.getAmount() != null)
                .mapToDouble(PaymentTransactionRequest::getAmount)
                .sum();
        double finalPrice = CommonUtils.readValue(abstractOrderModel.getFinalPrice());
        if(finalPrice < 0) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_FINAL_PRICE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{finalPrice});
        }

        if(!abstractOrderModel.isExchange() && totalPaidAmount < finalPrice) {
            ErrorCodes err = ErrorCodes.INVALID_PAID_AMOUNT_FOR_ORDER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{finalPrice});
        }
    }

    protected abstract AbstractOrderModel getOrder(OrderRequest orderRequest);

    protected void validateNote(OrderRequest orderRequest) {
        if(StringUtils.isNotBlank(orderRequest.getNote())
                && orderRequest.getNote().length() > MAXIMUM_NOTE_LENGTH) {
            ErrorCodes err = ErrorCodes.NOTE_OVER_MAX_LENGTH;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    protected void validateCustomerInfo(OrderRequest orderRequest, AbstractOrderModel abstractOrderModel) {
        if(!OrderType.ONLINE.toString().equals(abstractOrderModel.getType())) return;
        CustomerRequest customerRequest = orderRequest.getCustomer();
        if(customerRequest == null) {
            ErrorCodes err = ErrorCodes.EMPTY_ORDER_CUSTOMER_INFO;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        if (StringUtils.isBlank(customerRequest.getName())) {
            ErrorCodes err = ErrorCodes.EMPTY_ORDER_CUSTOMER_INFO;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        if (StringUtils.isBlank(customerRequest.getPhone())) {
            ErrorCodes err = ErrorCodes.EMPTY_ORDER_CUSTOMER_INFO;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        if(StringUtils.isNotBlank(customerRequest.getPhone()) && customerRequest.getPhone().length() > MAXIMUM_PHONE_LENGTH) {
            ErrorCodes err = ErrorCodes.INVALID_PHONE_NUMBER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    protected void validateEntry(AbstractOrderModel abstractOrderModel) {
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
                totalItemQuantity = comboData.getTotalItemQuantity() * entryModel.getQuantity();
                subEntryTotalQuantity = entryModel.getSubOrderEntries().stream()
                        .filter(se -> se.getQuantity() != null)
                        .mapToInt(SubOrderEntryModel::getQuantity).sum();
                if (totalItemQuantity != subEntryTotalQuantity) {
                    ErrorCodes err = ErrorCodes.INVALID_SUB_ORDER_ENTRY_QUANTITY;
                    throw new ServiceException(err.code(), err.message(), err.httpStatus(), new Object[]{comboData.getName()});
                }
            }
        }
    }

    protected void validatePayments(OrderRequest orderRequest, AbstractOrderModel abstractOrderModel) {
        List<PaymentTransactionRequest> payments = orderRequest.getPayments();
        if(CollectionUtils.isNotEmpty(payments)) {
            PaymentMethodData paymentMethodData = financeService.getPaymentMethodByCode(PaymentMethodType.LOYALTY_POINT.code());
            for(PaymentTransactionRequest payment : payments) {
                if (payment.getAmount() != null && payment.getAmount() > 0) {
                    if (payment.getMoneySourceId() == null) {
                        ErrorCodes err = ErrorCodes.INVALID_MONEY_SOURCE_ID;
                        throw new ServiceException(err.code(), err.message(), err.httpStatus());
                    }

                    validateMoneySource(payment, abstractOrderModel);

                    if (!abstractOrderModel.isExchange()) {
                        validateLoyaltyPoint(orderRequest, payment, paymentMethodData);
                    }
                }
            }
        }
    }

    protected void validateMoneySource(PaymentTransactionRequest payment, AbstractOrderModel abstractOrderModel) {
        Long moneySourceId = payment.getMoneySourceId();
        MoneySourceData moneySource = financeService.getMoneySource(moneySourceId, abstractOrderModel.getCompanyId());
        if(moneySource == null) {
            ErrorCodes err = ErrorCodes.INVALID_MONEY_SOURCE_ID;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
        String moneySourceType = moneySource.getType();
        if(StringUtils.isNotBlank(moneySourceType) && !moneySourceType.equals(payment.getType())) {
            payment.setType(moneySourceType);
        }
    }

    protected void validateLoyaltyPoint(OrderRequest orderRequest, PaymentTransactionRequest payment, PaymentMethodData paymentMethodData) {
        if(paymentMethodData != null && paymentMethodData.getId().equals(payment.getPaymentMethodId())) {
            double paymentAmount = CommonUtils.readValue(payment.getAmount());
            if(paymentAmount <= 0) {
                return;
            }

            if(StringUtils.isBlank(orderRequest.getCardNumber()) && paymentAmount > 0) {
                ErrorCodes err = ErrorCodes.EMPTY_LOYALTY_CARD_NUMBER;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }

            AvailablePointAmountRequest pointAmountRequest = new AvailablePointAmountRequest();
            pointAmountRequest.setCompanyId(orderRequest.getCompanyId());
            pointAmountRequest.setOrderCode(orderRequest.getCode());
            pointAmountRequest.setCardNumber(orderRequest.getCardNumber());
            AvailablePointAmountData pointAmountData = loyaltyService.computeAvailablePointAmountOf(pointAmountRequest);
            double availablePoint = CommonUtils.readValue(pointAmountData.getAvailableAmount());
            double maximumRedeemPoint = CommonUtils.readValue(pointAmountData.getPointAmount());
            double paymentPoint = paymentAmount / pointAmountData.getConversionRate();

            if(paymentPoint > availablePoint) {
                ErrorCodes err = ErrorCodes.CANNOT_EXCEED_AVAILABLE_POINTS;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }

            if(paymentPoint > maximumRedeemPoint) {
                ErrorCodes err = ErrorCodes.INVALID_POINT_FOR_ORDER;
                throw new ServiceException(err.code(), err.message(), err.httpStatus());
            }

            validatePointInteger(paymentPoint);
        }
    }

    protected void validatePointInteger(double paymentPoint) {
        double mod = paymentPoint % 1;
        if(mod != 0) {
            ErrorCodes err = ErrorCodes.INVALID_INTEGER_POINT_FOR_ORDER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    protected void validateDeliveryDate(OrderRequest request) {
        if (!OrderType.ONLINE.toString().equals(request.getOrderType())) return;
        boolean checkDeliveryDate = authService.isCheckDeliveryDate(request.getCompanyId());
        if (!checkDeliveryDate) return;
        if (request.getDeliveryDate() == null) {
            ErrorCodes err = ErrorCodes.INVALID_ORDER_DELIVERY_DATE;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }
    }

    @Autowired
    public void setFinanceService(FinanceService financeService) {
        this.financeService = financeService;
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    @Autowired
    public void setLoyaltyService(LoyaltyService loyaltyService) {
        this.loyaltyService = loyaltyService;
    }

    @Autowired
    public void setCouponService(CouponService couponService) {
        this.couponService = couponService;
    }

    @Autowired
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }
}
