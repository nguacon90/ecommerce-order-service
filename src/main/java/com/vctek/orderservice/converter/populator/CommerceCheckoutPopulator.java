package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.CommerceCheckoutParameter;
import com.vctek.orderservice.dto.request.*;
import com.vctek.orderservice.model.CartModel;
import com.vctek.orderservice.model.PaymentTransactionModel;
import com.vctek.orderservice.service.AuthService;
import com.vctek.orderservice.service.CartService;
import com.vctek.orderservice.util.CommonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component("commerceCheckoutPopulator")
public class CommerceCheckoutPopulator implements Populator<OrderRequest, CommerceCheckoutParameter> {
    private CartService cartService;
    private AuthService authService;

    public CommerceCheckoutPopulator(CartService cartService, AuthService authService) {
        this.cartService = cartService;
        this.authService = authService;
    }

    @Override
    public void populate(OrderRequest orderRequest, CommerceCheckoutParameter commerceCheckoutParameter) {
        CartModel cart = cartService.findByCodeAndUserIdAndCompanyId(orderRequest.getCode(), authService.getCurrentUserId(),
                orderRequest.getCompanyId());
        commerceCheckoutParameter.setCart(cart);
        commerceCheckoutParameter.setNote(CommonUtils.escapeSpecialSymbols(orderRequest.getNote()));
        commerceCheckoutParameter.setDeliveryDate(orderRequest.getDeliveryDate());
        commerceCheckoutParameter.setCustomerNote(CommonUtils.escapeSpecialSymbols(orderRequest.getCustomerNote()));
        commerceCheckoutParameter.setCustomerSupportNote(CommonUtils.escapeSpecialSymbols(orderRequest.getCustomerSupportNote()));
        commerceCheckoutParameter.setShippingCompanyId(orderRequest.getShippingCompanyId());
        commerceCheckoutParameter.setOrderSourceId(orderRequest.getOrderSourceId());
        commerceCheckoutParameter.setDeliveryCost(orderRequest.getDeliveryCost());
        commerceCheckoutParameter.setCompanyShippingFee(orderRequest.getCompanyShippingFee());
        commerceCheckoutParameter.setCollaboratorShippingFee(orderRequest.getCollaboratorShippingFee());
        commerceCheckoutParameter.setCardNumber(orderRequest.getCardNumber());
        commerceCheckoutParameter.setEmployeeId(orderRequest.getEmployeeId());
        commerceCheckoutParameter.setSettingCustomerOptionIds(orderRequest.getSettingCustomerOptionIds());
        if (orderRequest.isConfirmDiscount()) {
            commerceCheckoutParameter.setConfirmDiscountBy(authService.getCurrentUserId());
        }
        populateVatInfo(orderRequest, commerceCheckoutParameter);
        populateCustomer(orderRequest, commerceCheckoutParameter);
        populatePaymentTransactions(orderRequest, commerceCheckoutParameter);
    }

    protected void populatePaymentTransactions(OrderRequest orderRequest, CommerceCheckoutParameter commerceCheckoutParameter) {
        List<PaymentTransactionRequest> payments = orderRequest.getPayments();
        Set<PaymentTransactionModel> transactions = new HashSet<>();
        if (CollectionUtils.isNotEmpty(payments)) {
            for (PaymentTransactionRequest request : payments) {
                if (request.getAmount() != null && request.getAmount() > 0) {
                    PaymentTransactionModel transaction = new PaymentTransactionModel();
                    transaction.setAmount(request.getAmount());
                    transaction.setMoneySourceId(request.getMoneySourceId());
                    transaction.setPaymentMethodId(request.getPaymentMethodId());
                    transaction.setTransactionNumber(request.getTransactionNumber());
                    transaction.setMoneySourceType(request.getType());
                    transactions.add(transaction);
                }
            }

            commerceCheckoutParameter.setPaymentTransactions(transactions);
        }
    }

    protected void populateCustomer(OrderRequest orderRequest, CommerceCheckoutParameter commerceCheckoutParameter) {
        CustomerRequest customer = orderRequest.getCustomer();
        if (customer != null) {
            customer.setCompanyId(commerceCheckoutParameter.getCart().getCompanyId());
            customer.setPhone1(customer.getPhone());
            customer.setPhone(customer.getPhone());
            customer.setAge(orderRequest.getAge());
            if (StringUtils.isBlank(customer.getName())) {
                customer.setName(customer.getPhone());
            }
            if (StringUtils.isBlank(customer.getGender())) {
                customer.setGender(orderRequest.getGender());
            }
            commerceCheckoutParameter.setCustomerRequest(customer);
            commerceCheckoutParameter.setAge(orderRequest.getAge());
            commerceCheckoutParameter.setGender(orderRequest.getGender());
            AddressRequest shippingAddressRequest = customer.getShippingAddress();
            if(shippingAddressRequest != null) {
                shippingAddressRequest.setShippingAddress(true);
                if(StringUtils.isBlank(shippingAddressRequest.getPhone1())) {
                    shippingAddressRequest.setPhone1(customer.getPhone());
                }
                if(StringUtils.isBlank(shippingAddressRequest.getCustomerName())) {
                    shippingAddressRequest.setCustomerName(customer.getName());
                }
            }
        }
    }

    protected void populateVatInfo(OrderRequest orderRequest, CommerceCheckoutParameter commerceCheckoutParameter) {
        VatRequest vatInfo = orderRequest.getVatInfo();
        if (vatInfo != null) {
            commerceCheckoutParameter.setVatNumber(vatInfo.getVatNumber());
            commerceCheckoutParameter.setVatDate(vatInfo.getVatDate());
        }
    }
}
