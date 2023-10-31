package com.vctek.orderservice.elasticsearch.model;

import com.vctek.orderservice.dto.OrderSettingCustomerOptionData;
import com.vctek.orderservice.dto.TagData;
import com.vctek.orderservice.util.ElasticSearchIndex;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.Id;
import java.util.Date;
import java.util.List;

@Document(indexName = ElasticSearchIndex.ORDER_INDEX)
public class OrderSearchModel {
    @Id
    private String id;
    private String code;
    private Double totalPrice;
    private Double totalTax;
    private Long warehouseId;
    private String guid;
    private String orderType;
    private String priceType;
    private String globalDiscountValues;
    private Double subTotal;
    private Double totalDiscount;
    private Long companyId;
    private Long createdBy;
    private String createdName;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private Double vat;
    private String vatType;
    private String orderStatus;
    private Double finalPrice;
    private Double discount;
    private String discountType;
    private Double deliveryCost;
    private Double collaboratorShippingFee;
    private Double companyShippingFee;
    private Double paymentCost;
    private Double fixedDiscount;
    private String note;
    private Long shippingCompanyId;
    private String orderRetailCode;
    private String customerNote;
    private String customerSupportNote;
    private boolean exchange;
    private Long provinceId;
    private String provinceName;
    private Long districtId;
    private String districtName;
    private Long wardId;
    private String wardName;
    private String address;
    private List<Long> returnOrderIds;
    private Long returnOrderId;
    private Long orderSourceId;
    private boolean deleted;
    private String age;
    private String gender;
    private Double paidAmount;
    private Double totalRewardAmount;
    private Double rewardPoint;
    private Double redeemAmount;
    private Double refundAmount;

    private String images;

    @Field(type = FieldType.Date)
    private Date modifiedTimeLastStatus;

    @Field(type = FieldType.Date)
    private Date createdTime;

    private Long billId;

    @Field(type = FieldType.Nested)
    private List<OrderEntryData> orderEntries;

    @Field(type = FieldType.Nested)
    private List<PaymentTransactionData> paymentTransactionData;

    @Field(type = FieldType.Nested)
    private List<OrderHistory> orderHistoryData;

    @Field(type = FieldType.Nested)
    private List<TagData> tags;

    private Long employeeId;

    private String employeeName;

    @Field(type = FieldType.Date)
    private Date deliveryDate;
    private String cancelReason;
    private Long distributorId;
    private boolean finishedProduct;
    private boolean hasSaleOff;

    @Field(type = FieldType.Nested)
    private List<OrderSettingCustomerOptionData> settingCustomerOptionData;
    private String sellSignal;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Double getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(Double totalTax) {
        this.totalTax = totalTax;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getGlobalDiscountValues() {
        return globalDiscountValues;
    }

    public void setGlobalDiscountValues(String globalDiscountValues) {
        this.globalDiscountValues = globalDiscountValues;
    }

    public Double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(Double subTotal) {
        this.subTotal = subTotal;
    }

    public Double getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(Double totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedName() {
        return createdName;
    }

    public void setCreatedName(String createdName) {
        this.createdName = createdName;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public Double getVat() {
        return vat;
    }

    public void setVat(Double vat) {
        this.vat = vat;
    }

    public String getVatType() {
        return vatType;
    }

    public void setVatType(String vatType) {
        this.vatType = vatType;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public Double getDeliveryCost() {
        return deliveryCost;
    }

    public void setDeliveryCost(Double deliveryCost) {
        this.deliveryCost = deliveryCost;
    }

    public Double getPaymentCost() {
        return paymentCost;
    }

    public void setPaymentCost(Double paymentCost) {
        this.paymentCost = paymentCost;
    }

    public Double getFixedDiscount() {
        return fixedDiscount;
    }

    public void setFixedDiscount(Double fixedDiscount) {
        this.fixedDiscount = fixedDiscount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Long getBillId() {
        return billId;
    }

    public void setBillId(Long billId) {
        this.billId = billId;
    }

    public List<OrderEntryData> getOrderEntries() {
        return orderEntries;
    }

    public void setOrderEntries(List<OrderEntryData> orderEntries) {
        this.orderEntries = orderEntries;
    }

    public List<PaymentTransactionData> getPaymentTransactionData() {
        return paymentTransactionData;
    }

    public void setPaymentTransactionData(List<PaymentTransactionData> paymentTransactionData) {
        this.paymentTransactionData = paymentTransactionData;
    }

    public Long getShippingCompanyId() {
        return shippingCompanyId;
    }

    public void setShippingCompanyId(Long shippingCompanyId) {
        this.shippingCompanyId = shippingCompanyId;
    }

    public String getOrderRetailCode() {
        return orderRetailCode;
    }

    public void setOrderRetailCode(String orderRetailCode) {
        this.orderRetailCode = orderRetailCode;
    }

    public String getCustomerNote() {
        return customerNote;
    }

    public void setCustomerNote(String customerNote) {
        this.customerNote = customerNote;
    }

    public String getCustomerSupportNote() {
        return customerSupportNote;
    }

    public void setCustomerSupportNote(String customerSupportNote) {
        this.customerSupportNote = customerSupportNote;
    }

    public boolean isExchange() {
        return exchange;
    }

    public void setExchange(boolean exchange) {
        this.exchange = exchange;
    }

    public Long getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Long provinceId) {
        this.provinceId = provinceId;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public Long getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Long districtId) {
        this.districtId = districtId;
    }

    public String getDistrictName() {
        return districtName;
    }

    public void setDistrictName(String districtName) {
        this.districtName = districtName;
    }

    public Long getWardId() {
        return wardId;
    }

    public void setWardId(Long wardId) {
        this.wardId = wardId;
    }

    public String getWardName() {
        return wardName;
    }

    public void setWardName(String wardName) {
        this.wardName = wardName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<OrderHistory> getOrderHistoryData() {
        return orderHistoryData;
    }

    public void setOrderHistoryData(List<OrderHistory> orderHistoryData) {
        this.orderHistoryData = orderHistoryData;
    }

    public List<Long> getReturnOrderIds() {
        return returnOrderIds;
    }

    public void setReturnOrderIds(List<Long> returnOrderIds) {
        this.returnOrderIds = returnOrderIds;
    }

    public Long getReturnOrderId() {
        return returnOrderId;
    }

    public void setReturnOrderId(Long returnOrderId) {
        this.returnOrderId = returnOrderId;
    }

    public Long getOrderSourceId() {
        return orderSourceId;
    }

    public void setOrderSourceId(Long orderSourceId) {
        this.orderSourceId = orderSourceId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public Date getModifiedTimeLastStatus() {
        return modifiedTimeLastStatus;
    }

    public void setModifiedTimeLastStatus(Date modifiedTimeLastStatus) {
        this.modifiedTimeLastStatus = modifiedTimeLastStatus;
    }

    public Double getTotalRewardAmount() {
        return totalRewardAmount;
    }

    public void setTotalRewardAmount(Double totalRewardAmount) {
        this.totalRewardAmount = totalRewardAmount;
    }

    public Double getRewardPoint() {
        return rewardPoint;
    }

    public void setRewardPoint(Double rewardPoint) {
        this.rewardPoint = rewardPoint;
    }

    public Double getRedeemAmount() {
        return redeemAmount;
    }

    public void setRedeemAmount(Double redeemAmount) {
        this.redeemAmount = redeemAmount;
    }

    public Double getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Double refundAmount) {
        this.refundAmount = refundAmount;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Double getCollaboratorShippingFee() {
        return collaboratorShippingFee;
    }

    public void setCollaboratorShippingFee(Double collaboratorShippingFee) {
        this.collaboratorShippingFee = collaboratorShippingFee;
    }

    public Double getCompanyShippingFee() {
        return companyShippingFee;
    }

    public void setCompanyShippingFee(Double companyShippingFee) {
        this.companyShippingFee = companyShippingFee;
    }

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public Long getDistributorId() {
        return distributorId;
    }

    public void setDistributorId(Long distributorId) {
        this.distributorId = distributorId;
    }

    public List<OrderSettingCustomerOptionData> getSettingCustomerOptionData() {
        return settingCustomerOptionData;
    }

    public void setSettingCustomerOptionData(List<OrderSettingCustomerOptionData> settingCustomerOptionData) {
        this.settingCustomerOptionData = settingCustomerOptionData;
    }

    public List<TagData> getTags() {
        return tags;
    }

    public void setTags(List<TagData> tags) {
        this.tags = tags;
    }

    public boolean isFinishedProduct() {
        return finishedProduct;
    }

    public void setFinishedProduct(boolean finishedProduct) {
        this.finishedProduct = finishedProduct;
    }

    public boolean isHasSaleOff() {
        return hasSaleOff;
    }

    public void setHasSaleOff(boolean hasSaleOff) {
        this.hasSaleOff = hasSaleOff;
    }

    public String getSellSignal() {
        return sellSignal;
    }

    public void setSellSignal(String sellSignal) {
        this.sellSignal = sellSignal;
    }
}
