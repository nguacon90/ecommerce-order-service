package com.vctek.orderservice.converter.populator;

import com.vctek.converter.Converter;
import com.vctek.converter.Populator;
import com.vctek.exception.ServiceException;
import com.vctek.orderservice.dto.UserData;
import com.vctek.orderservice.elasticsearch.model.PaymentTransactionData;
import com.vctek.orderservice.elasticsearch.model.returnorder.*;
import com.vctek.orderservice.exception.ErrorCodes;
import com.vctek.orderservice.feignclient.dto.BillDetailData;
import com.vctek.orderservice.feignclient.dto.CustomerData;
import com.vctek.orderservice.feignclient.dto.ReturnOrderBillData;
import com.vctek.orderservice.model.OrderModel;
import com.vctek.orderservice.model.OrderSourceModel;
import com.vctek.orderservice.model.PaymentTransactionModel;
import com.vctek.orderservice.model.ReturnOrderModel;
import com.vctek.orderservice.service.*;
import com.vctek.redis.ProductData;
import com.vctek.util.CommonUtils;
import com.vctek.util.OrderType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component("returnOrderDocumentPopulator")
public class ReturnOrderDocumentPopulator implements Populator<ReturnOrderModel, ReturnOrderDocument> {
    private OrderService orderService;
    private AuthService authService;
    private CRMService crmService;
    private ProductService productService;
    private BillService billService;
    private Populator<OrderModel, ExchangeOrder> exchangeOrderPopulator;
    private PaymentTransactionService paymentTransactionService;
    private Converter<PaymentTransactionModel, PaymentTransactionData> returnOrderPaymentTransactionConverter;

    @Override
    public void populate(ReturnOrderModel model, ReturnOrderDocument document) {
        document.setId(model.getId());
        document.setCreationTime(model.getCreatedTime());
        document.setCompanyId(model.getCompanyId());
        document.setNote(model.getNote());
        document.setShippingFee(model.getShippingFee());
        document.setCompanyShippingFee(model.getCompanyShippingFee());
        document.setCollaboratorShippingFee(model.getCollaboratorShippingFee());
        document.setVat(model.getVat());
        OrderModel originOrder = getOriginOrder(model);
        if (originOrder == null) {
            ErrorCodes err = ErrorCodes.RETURN_ORDER_HAS_NOT_ORIGIN_ORDER;
            throw new ServiceException(err.code(), err.message(), err.httpStatus());
        }

        OrderSourceModel orderSourceModel = originOrder.getOrderSourceModel();
        document.setOriginOrderSourceId(orderSourceModel != null ? orderSourceModel.getId() : null);
        document.setOriginOrderSourceName(orderSourceModel != null ? orderSourceModel.getName() : null);
        populateEmployeeInfo(originOrder, document);
        populateOriginOrder(originOrder, document);
        populateBill(model, originOrder, document);
        populateExchangeOrder(model, document);

        ExchangeOrder exchangeOrder = document.getExchangeOrder();
        double exchangeCost = (exchangeOrder != null && exchangeOrder.getFinalPrice() != null) ?
                exchangeOrder.getFinalPrice() : 0;
        double returnCost = document.getBill() != null && document.getBill().getFinalPrice() != null ?
                document.getBill().getFinalPrice() : 0;

        double shippingFee = CommonUtils.readValue(model.getShippingFee());
        double returnVat = CommonUtils.readValue(model.getVat());
        document.setAmount(exchangeCost - (returnCost + shippingFee + returnVat));
        populatePaymentTransaction(model, document);
    }

    private void populateExchangeOrder(ReturnOrderModel model, ReturnOrderDocument document) {
        OrderModel exchangeOrder = model.getExchangeOrder();
        if (exchangeOrder != null) {
            ExchangeOrder exchangeOrderDoc = new ExchangeOrder();
            exchangeOrderPopulator.populate(exchangeOrder, exchangeOrderDoc);
            document.setExchangeOrder(exchangeOrderDoc);
            if (CollectionUtils.isNotEmpty(exchangeOrderDoc.getEntries())) {
                document.setExchangeWarehouseId(exchangeOrder.getWarehouseId());
            }
        }
    }

    protected void populateBill(ReturnOrderModel model, OrderModel originOrder, ReturnOrderDocument document) {
        Long billId = model.getBillId();
        Long companyId = originOrder.getCompanyId();
        Long returnOrderId = model.getId();
        ReturnOrderBillData returnOrderBill = billService.getReturnOrderBill(billId, companyId, returnOrderId);
        ReturnOrderBill bill = new ReturnOrderBill();
        bill.setCompanyId(returnOrderBill.getCompanyId());
        bill.setId(returnOrderBill.getId());
        bill.setWarehouseId(returnOrderBill.getWarehouseId());
        List<ReturnOrderEntry> entries = new ArrayList<>();
        double finalPrice = 0;
        List<BillDetailData> returnOrderBillEntries = returnOrderBill.getEntries();
        if (CollectionUtils.isNotEmpty(returnOrderBillEntries)) {
            ReturnOrderEntry entry;
            for (BillDetailData billDetailData : returnOrderBillEntries) {
                entry = new ReturnOrderEntry();
                entry.setProductId(billDetailData.getProductId());
                Double discount = CommonUtils.readValue(billDetailData.getDiscount());
                Double price = CommonUtils.readValue(billDetailData.getPrice());
                Long quantity = (long) CommonUtils.readValue(billDetailData.getQuantity());

                entry.setDiscount(discount);
                entry.setPrice(price);
                entry.setQuantity(quantity);
                entry.setComboId(billDetailData.getComboId());
                finalPrice += quantity * price - discount;
                populateBasicProduct(entry, billDetailData);

                entries.add(entry);
            }
        }
        bill.setEntries(entries);
        bill.setFinalPrice(finalPrice);
        document.setBill(bill);
    }

    private void populateBasicProduct(ReturnOrderEntry entry, BillDetailData billDetailData) {
        ProductData productDetailData = productService.getBasicProductDetail(billDetailData.getProductId());

        if (billDetailData.getComboId() != null) {
            ProductData comboDetailData = productService.getBasicProductDetail(billDetailData.getComboId());

            if (comboDetailData != null) {
                entry.setComboName(comboDetailData.getName());
                entry.setComboSku(comboDetailData.getSku());
            }
        }

        if (productDetailData != null) {
            entry.setProductName(productDetailData.getName());
            entry.setProductSku(productDetailData.getSku());
            entry.setName(productDetailData.getName());
            entry.setdType(productDetailData.getdType());
            entry.setSupplierProductName(productDetailData.getSupplierProductName());
        }
    }

    protected void populateOriginOrder(OrderModel originOrder, ReturnOrderDocument document) {
        OriginOrder originOrderDocument = new OriginOrder();
        originOrderDocument.setCode(originOrder.getCode());
        originOrderDocument.setType(originOrder.getType());
        Long customerId = originOrder.getCustomerId();
        if (customerId != null) {
            originOrderDocument.setCustomerId(customerId);
            CustomerData customerData = crmService.getCustomer(customerId, originOrder.getCompanyId());
            originOrderDocument.setCustomerName(customerData != null ? customerData.getName() : StringUtils.EMPTY);
            originOrderDocument.setCustomerPhone(customerData != null ? customerData.getPhone() : StringUtils.EMPTY);
        }
        document.setOriginOrder(originOrderDocument);
        document.setReturnWarehouseId(originOrder.getWarehouseId());
    }

    protected void populateEmployeeInfo(OrderModel originOrder, ReturnOrderDocument document) {
        Long createByUser = OrderType.ONLINE.toString().equals(originOrder.getType()) ?
                originOrder.getEmployeeId() : originOrder.getCreateByUser();

        if (createByUser != null) {
            document.setEmployeeId(createByUser);
            UserData employeeData = authService.getUserById(createByUser);
            document.setEmployeeName(employeeData != null ? employeeData.getName() : StringUtils.EMPTY);
        }
    }

    protected OrderModel getOriginOrder(ReturnOrderModel model) {
        OrderModel originOrder = model.getOriginOrder();
        if (originOrder == null) {
            return null;
        }

        return orderService.findById(originOrder.getId());
    }

    private void populatePaymentTransaction(ReturnOrderModel returnOrderModel, ReturnOrderDocument returnOrderDocument) {
        List<PaymentTransactionModel> paymentTransactionModels = paymentTransactionService.findAllByReturnOrder(returnOrderModel);
        if (CollectionUtils.isEmpty(paymentTransactionModels)) return;
        List<PaymentTransactionData> paymentTransactionData = returnOrderPaymentTransactionConverter.convertAll(paymentTransactionModels);
        List<PaymentTransactionData> paymentTransactionDataInvoice = paymentTransactionService.findAllPaymentInvoiceReturnOrder(returnOrderModel);
        if (CollectionUtils.isNotEmpty(paymentTransactionDataInvoice)) {
            paymentTransactionData.addAll(paymentTransactionDataInvoice);
            paymentTransactionData = paymentTransactionData.stream().distinct().collect(Collectors.toList());
        }
        returnOrderDocument.setPaymentTransactions(paymentTransactionData);
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    @Autowired
    public void setCrmService(CRMService crmService) {
        this.crmService = crmService;
    }

    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    @Autowired
    public void setBillService(BillService billService) {
        this.billService = billService;
    }

    @Autowired
    @Qualifier("exchangeOrderDocumentPopulator")
    public void setExchangeOrderPopulator(Populator<OrderModel, ExchangeOrder> exchangeOrderPopulator) {
        this.exchangeOrderPopulator = exchangeOrderPopulator;
    }

    @Autowired
    public void setPaymentTransactionService(PaymentTransactionService paymentTransactionService) {
        this.paymentTransactionService = paymentTransactionService;
    }

    @Autowired
    @Qualifier("returnOrderPaymentTransactionConverter")
    public void setReturnOrderPaymentTransactionConverter(Converter<PaymentTransactionModel, PaymentTransactionData> returnOrderPaymentTransactionConverter) {
        this.returnOrderPaymentTransactionConverter = returnOrderPaymentTransactionConverter;
    }
}
