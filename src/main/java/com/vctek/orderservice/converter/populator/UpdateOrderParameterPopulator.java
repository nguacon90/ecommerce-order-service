package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.orderservice.dto.UpdateOrderParameter;
import com.vctek.orderservice.dto.request.*;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.OrderSettingCustomerOptionModel;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.service.OrderSettingCustomerOptionService;
import com.vctek.orderservice.util.CommonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component("updateOrderParameterPopulator")
public class UpdateOrderParameterPopulator implements Populator<OrderRequest, UpdateOrderParameter> {
    private OrderService orderService;
    private OrderSettingCustomerOptionService orderSettingCustomerOptionService;
    private Populator<List<PaymentTransactionRequest>, OrderModel> orderPaymentTransactionRequestPopulator;

    public UpdateOrderParameterPopulator(OrderService orderService, OrderSettingCustomerOptionService orderSettingCustomerOptionService) {
        this.orderService = orderService;
        this.orderSettingCustomerOptionService = orderSettingCustomerOptionService;
    }

    @Override
    public void populate(OrderRequest orderRequest, UpdateOrderParameter updateOrderParameter) {
        OrderModel order = orderService.findByCodeAndCompanyId(orderRequest.getCode(), orderRequest.getCompanyId());
        populateSettingCustomerOptions(orderRequest, order);
        updateOrderParameter.setOrder(order);
        updateOrderParameter.setNote(CommonUtils.escapeSpecialSymbols(orderRequest.getNote()));
        updateOrderParameter.setOrderSourceId(orderRequest.getOrderSourceId());
        updateOrderParameter.setShippingCompanyId(orderRequest.getShippingCompanyId());
        updateOrderParameter.setDeliveryCost(orderRequest.getDeliveryCost());
        updateOrderParameter.setCompanyShippingFee(orderRequest.getCompanyShippingFee());
        updateOrderParameter.setCollaboratorShippingFee(orderRequest.getCollaboratorShippingFee());
        updateOrderParameter.setCustomerNote(CommonUtils.escapeSpecialSymbols(orderRequest.getCustomerNote()));
        updateOrderParameter.setCustomerSupportNote(CommonUtils.escapeSpecialSymbols(orderRequest.getCustomerSupportNote()));
        updateOrderParameter.setCardNumber(orderRequest.getCardNumber());
        updateOrderParameter.setDeliveryDate(orderRequest.getDeliveryDate());
        populateVatInfo(orderRequest, updateOrderParameter);
        populateCustomer(orderRequest, updateOrderParameter);
        populatePaymentTransactions(orderRequest, updateOrderParameter);
    }

    protected void populatePaymentTransactions(OrderRequest orderRequest, UpdateOrderParameter updateOrderParameter) {
        List<PaymentTransactionRequest> payments = orderRequest.getPayments();
        OrderModel order = updateOrderParameter.getOrder();
        orderPaymentTransactionRequestPopulator.populate(payments, order);
    }

    protected void populateCustomer(OrderRequest orderRequest, UpdateOrderParameter updateOrderParameter) {
        CustomerRequest customer = orderRequest.getCustomer();
        if(customer != null) {
            customer.setId(customer.getId());
            customer.setCompanyId(updateOrderParameter.getOrder().getCompanyId());
            customer.setPhone1(customer.getPhone());
            customer.setPhone(customer.getPhone());
            if(StringUtils.isBlank(customer.getGender())) {
                customer.setGender(orderRequest.getGender());
            }
            updateOrderParameter.setCustomerRequest(customer);
            updateOrderParameter.setAge(orderRequest.getAge());
            updateOrderParameter.setGender(orderRequest.getGender());
            if(StringUtils.isBlank(customer.getName())) {
                customer.setName(customer.getPhone());
            }

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

    protected void populateVatInfo(OrderRequest orderRequest, UpdateOrderParameter updateOrderParameter) {
        VatRequest vatInfo = orderRequest.getVatInfo();
        if(vatInfo != null) {
            updateOrderParameter.setVatNumber(vatInfo.getVatNumber());
            updateOrderParameter.setVatDate(vatInfo.getVatDate());
        }
    }

    protected void populateSettingCustomerOptions(OrderRequest orderRequest, OrderModel orderModel) {
        Set<OrderSettingCustomerOptionModel> customerOptionModels = new HashSet<>();

        for (Long settingCustomerOptionId : orderRequest.getSettingCustomerOptionIds()) {
            OrderSettingCustomerOptionModel orderSettingCustomerOptionModel = orderSettingCustomerOptionService.findByIdAndCompanyId(settingCustomerOptionId, orderRequest.getCompanyId());
            if (orderSettingCustomerOptionModel != null) {
                customerOptionModels.add(orderSettingCustomerOptionModel);
            }
        }
        orderModel.setOrderSettingCustomerOptionModels(customerOptionModels);
    }

    @Autowired
    @Qualifier("orderPaymentTransactionRequestPopulator")
    public void setOrderPaymentTransactionRequestPopulator(Populator<List<PaymentTransactionRequest>, OrderModel> orderPaymentTransactionRequestPopulator) {
        this.orderPaymentTransactionRequestPopulator = orderPaymentTransactionRequestPopulator;
    }
}
