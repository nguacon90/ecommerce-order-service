package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Populator;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.ReturnOrderCommerceParameter;
import com.vctek.orderservice.dto.request.*;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.BillDetailRequest;
import com.vctek.orderservice.feignclient.dto.BillRequest;
import com.vctek.orderservice.model.*;
import com.vctek.orderservice.service.AuthService;
import com.vctek.orderservice.service.CartService;
import com.vctek.orderservice.service.OrderService;
import com.vctek.orderservice.util.DiscountType;
import com.vctek.util.BillType;
import com.vctek.util.CommonUtils;
import com.vctek.util.OrderType;
import com.vctek.util.ReceiptDeliveryReason;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("returnOrderCommerceParameterPopulator")
public class ReturnOrderCommerceParameterPopulator implements Populator<ReturnOrderRequest, ReturnOrderCommerceParameter> {

    private CartService cartService;
    private OrderService orderService;
    private AuthService authService;

    @Override
    public void populate(ReturnOrderRequest source, ReturnOrderCommerceParameter target) {
        target.setNote(source.getNote());
        target.setCompanyId(source.getCompanyId());
        target.setReturnOrderRequest(source);
        populateOriginOrder(source, target);
        populateExchangeCart(source, target);
        populatePaymentTransaction(source, target);
        populateExchangePaymentTransaction(source, target);
        populateBillRequest(source, target);
    }

    protected void populateBillRequest(ReturnOrderRequest source, ReturnOrderCommerceParameter target) {
        OrderModel originOrder = target.getOriginOrder();
        BillRequest billRequest = new BillRequest();
        billRequest.setTransactionDate(Calendar.getInstance().getTime());
        billRequest.setWarehouseId(originOrder.getWarehouseId());
        billRequest.setCompanyId(originOrder.getCompanyId());
        billRequest.setType(BillType.RECEIPT_BILL.code());
        billRequest.setOrderType(OrderType.RETURN_ORDER.toString());
        billRequest.setReasonCode(getReasonBill(originOrder));
        billRequest.setOrderCode(originOrder.getCode());
        billRequest.setOriginReturnBillId(originOrder.getBillId());

        Set<BillDetailRequest> billDetails = populateBillDetailRequest(originOrder, source);
        double finalCost = billDetails.stream().mapToDouble(BillDetailRequest::getFinalPrice).sum();
        billRequest.setFinalCost(finalCost);
        billRequest.setBillDetails(billDetails);
        target.setBillRequest(billRequest);
    }

    protected String getReasonBill(OrderModel orderModel) {

        if (OrderType.ONLINE.toString().equals(orderModel.getType())) {
            return ReceiptDeliveryReason.RECEIPT_RETURN_ONLINE_ORDER_REASON.code();
        }

        if (OrderType.WHOLESALE.toString().equals(orderModel.getType())) {
            return ReceiptDeliveryReason.RECEIPT_RETURN_WHOLESALE_ORDER_REASON.code();
        }

        return ReceiptDeliveryReason.RECEIPT_RETURN_RETAIL_ORDER_REASON.code();
    }

    protected Set<BillDetailRequest> populateBillDetailRequest(OrderModel originOrder, ReturnOrderRequest source) {
        Set<BillDetailRequest> billDetailRequests = new HashSet<>();
        List<AbstractOrderEntryModel> entries = originOrder.getEntries();
        List<ReturnOrderEntryRequest> returnOrderEntryList = source.getReturnOrderEntries();
        BillDetailRequest request;
        for (ReturnOrderEntryRequest returnOrderEntryRequest : returnOrderEntryList) {
            OrderEntryModel orderEntry = findByEntryId(entries, returnOrderEntryRequest.getOrderEntryId());
            if (CollectionUtils.isNotEmpty(orderEntry.getSubOrderEntries())) {
                List<BillDetailRequest> comboBillDetails = populateComboEntries(returnOrderEntryRequest, orderEntry);
                billDetailRequests.addAll(comboBillDetails);
                continue;
            }

            request = new BillDetailRequest();
            request.setQuantity(returnOrderEntryRequest.getQuantity());
            request.setPrice(orderEntry.getBasePrice());
            request.setProductId(orderEntry.getProductId());
            request.setProductName(returnOrderEntryRequest.getProductName());
            request.setProductSku(returnOrderEntryRequest.getProductSku());
            Double discount = returnOrderEntryRequest.getFinalDiscount();
            request.setDiscount(discount);
            request.setDiscountType(DiscountType.CASH.toString());
            request.setFinalPrice(computeFinalPrice(orderEntry, returnOrderEntryRequest, discount));
            request.setOrderEntryId(orderEntry.getId());
            request.setOriginBasePrice(orderEntry.getOriginBasePrice());
            request.setSaleOff(orderEntry.isSaleOff());

            billDetailRequests.add(request);
            populateBillDetailRequestWithTopping(billDetailRequests, originOrder, returnOrderEntryRequest);
        }

        return billDetailRequests;
    }

    protected List<BillDetailRequest> populateComboEntries(ReturnOrderEntryRequest returnOrderEntryRequest, OrderEntryModel orderEntry) {
        List<BillDetailRequest> comboDetails = new ArrayList<>();

        Set<SubOrderEntryModel> subOrderEntries = orderEntry.getSubOrderEntries();
        Long comboId = orderEntry.getProductId();
        Integer returnComboEntryQty = returnOrderEntryRequest.getQuantity();

        BillDetailRequest request;
        for (SubOrderEntryModel comboEntry : subOrderEntries) {
            request = new BillDetailRequest();
            request.setComboId(comboId);
            request.setComboQuantity(returnComboEntryQty);
            request.setPrice(comboEntry.getPrice());
            request.setProductId(comboEntry.getProductId());
            request.setQuantity(computeComboEntryQuantity(orderEntry, comboEntry, returnComboEntryQty));
            request.setSubOrderEntryId(comboEntry.getId());
            request.setOrderEntryId(orderEntry.getId());
            request.setOriginBasePrice(comboEntry.getOriginPrice());
            comboDetails.add(request);
        }
        double finalDiscount = CommonUtils.readValue(returnOrderEntryRequest.getFinalDiscount());
        int size = comboDetails.size();
        double remainDiscount = finalDiscount;
        sortComboDetailByOriginBasePrice(comboDetails);
        double comboPrice = orderEntry.getBasePrice();

        for (int i = 0; i < size; i++) {
            double discount = 0;
            BillDetailRequest detailRequest = comboDetails.get(i);
            if (i == size - 1) {
                discount = remainDiscount > 0 ? remainDiscount : 0;
                detailRequest.setDiscount(discount);
                detailRequest.setDiscountType(DiscountType.CASH.toString());
                double finalPrice = CommonUtils.readValue(detailRequest.getPrice()) * CommonUtils.readValue(detailRequest.getQuantity()) -
                        CommonUtils.readValue(detailRequest.getDiscount());
                detailRequest.setFinalPrice(finalPrice);
                break;
            }

            if (remainDiscount > 0) {
                discount = Math.round(detailRequest.getPrice() / comboPrice * finalDiscount);
                remainDiscount -= discount;
            }
            detailRequest.setDiscount(discount);
            detailRequest.setDiscountType(DiscountType.CASH.toString());
            double finalPrice = CommonUtils.readValue(detailRequest.getPrice()) * CommonUtils.readValue(detailRequest.getQuantity()) -
                    CommonUtils.readValue(detailRequest.getDiscount());
            detailRequest.setFinalPrice(finalPrice);
        }
        return comboDetails;
    }

    private void sortComboDetailByOriginBasePrice(List<BillDetailRequest> comboDetails) {
        Collections.sort(comboDetails, (o1, o2) -> {
            if (o1.getOriginBasePrice() != null && o2.getOriginBasePrice() != null) {
                if(o2.getOriginBasePrice().equals(o1.getOriginBasePrice())) {
                    return o2.getProductId().compareTo(o1.getProductId());
                }

                return o2.getOriginBasePrice().compareTo(o1.getOriginBasePrice());
            }

            return 0;
        });
    }

    private Integer computeComboEntryQuantity(OrderEntryModel orderEntry, SubOrderEntryModel comboEntry, Integer returnComboEntryQty) {
        int comboQuantity = orderEntry.getQuantity().intValue();
        int productInComboQty = comboEntry.getQuantity();
        return productInComboQty / comboQuantity * returnComboEntryQty;
    }

    private void populateBillDetailRequestWithTopping(Set<BillDetailRequest> billDetailRequests, OrderModel originOrder, ReturnOrderEntryRequest returnOrderEntryRequest) {
        for (ToppingOptionRequest optionRequest : returnOrderEntryRequest.getToppingOptions()) {
            for (ToppingItemRequest itemRequest : optionRequest.getToppingItems()) {
                BillDetailRequest request = new BillDetailRequest();
                request.setQuantity(itemRequest.getTotalQuantity());
                request.setPrice(itemRequest.getPrice());
                request.setProductId(itemRequest.getProductId());
                request.setProductName(itemRequest.getProductName());
                double discountOrderToItem = CommonUtils.readValue(itemRequest.getDiscountOrderToItem());
                Double fixedDiscount = com.vctek.orderservice.promotionengine.util.CommonUtils.calculateValueByCurrencyType(itemRequest.getTotalPrice(), itemRequest.getDiscount(),
                        itemRequest.getDiscountType());
                request.setDiscount(discountOrderToItem + fixedDiscount);
                request.setDiscountType(DiscountType.CASH.toString());
                request.setFinalPrice(itemRequest.getTotalPrice());
                request.setOrderEntryId(returnOrderEntryRequest.getId());
                request.setToppingOptionId(optionRequest.getId());
                billDetailRequests.add(request);
            }
        }
    }

    private double computeFinalPrice(OrderEntryModel orderEntry, ReturnOrderEntryRequest returnOrderEntryRequest, Double discount) {
        Double basePrice = orderEntry.getBasePrice() == null ? 0 : orderEntry.getBasePrice();
        return basePrice * returnOrderEntryRequest.getQuantity() - discount;
    }

    protected OrderEntryModel findByEntryId(List<AbstractOrderEntryModel> entries, Long orderEntryId) {
        Optional<OrderEntryModel> entryModelOptional = entries.stream().filter(e -> e.getId()
                .equals(orderEntryId)).map(e -> (OrderEntryModel) e).findFirst();
        if (entryModelOptional.isPresent()) {
            return entryModelOptional.get();
        }

        ErrorCodes err = ErrorCodes.INVALID_ENTRY_NUMBER;
        throw new ServiceException(err.code(), err.message(), err.httpStatus());
    }

    protected void populatePaymentTransaction(ReturnOrderRequest source, ReturnOrderCommerceParameter target) {
        List<PaymentTransactionRequest> payments = source.getPayments();
        if (CollectionUtils.isNotEmpty(payments)) {
            Set<PaymentTransactionModel> transactions = populateTransactions(payments);
            target.setPaymentTransactions(transactions);
        }
    }

    protected void populateExchangePaymentTransaction(ReturnOrderRequest source, ReturnOrderCommerceParameter target) {
        List<PaymentTransactionRequest> payments = source.getExchangePayments();
        if (CollectionUtils.isNotEmpty(payments)) {
            Set<PaymentTransactionModel> exchangeTransactions = populateTransactions(payments);
            target.setExchangePaymentTransactions(exchangeTransactions);
        }
    }

    private Set<PaymentTransactionModel> populateTransactions(List<PaymentTransactionRequest> payments) {
        Set<PaymentTransactionModel> transactions = new HashSet<>();
        for (PaymentTransactionRequest request : payments) {
            if (request.getAmount() != null && request.getAmount() > 0) {
                PaymentTransactionModel transaction = new PaymentTransactionModel();
                transaction.setAmount(request.getAmount());
                transaction.setMoneySourceId(request.getMoneySourceId());
                transaction.setPaymentMethodId(request.getPaymentMethodId());
                transaction.setTransactionNumber(request.getTransactionNumber());
                transaction.setMoneySourceType(request.getType());
                transaction.setTransactionDate(request.getTransactionDate());
                transaction.setWarehouseId(request.getWarehouseId());
                transactions.add(transaction);
            }
        }
        return transactions;
    }

    protected void populateExchangeCart(ReturnOrderRequest source, ReturnOrderCommerceParameter target) {
        if (source.isExchange()) {
            Long currentUserId = authService.getCurrentUserId();
            if (source.getReturnOrderId() == null) {
                CartModel exchangeCart = cartService.findByCodeAndUserIdAndCompanyId(source.getExchangeCartCode(), currentUserId,
                        source.getCompanyId());
                if (StringUtils.isNotBlank(source.getExchangeLoyaltyCard())) {
                    exchangeCart.setCardNumber(source.getExchangeLoyaltyCard());
                }
                target.setExchangeCart(exchangeCart);
            }
        }
    }

    protected void populateOriginOrder(ReturnOrderRequest source, ReturnOrderCommerceParameter target) {
        OrderModel originOrder = orderService.findByCodeAndCompanyIdAndDeleted(source.getOriginOrderCode(), source.getCompanyId(),
                false);
        target.setOriginOrder(originOrder);
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
