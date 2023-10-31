package com.vctek.orderservice.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AbstractOrderData {
    private Long id;
    private String code;
    private Date createdTime;
    private Double totalPrice;
    private Double subTotal;
    private Double subTotalFixedDiscount;
    private Double totalTax;
    private Long warehouseId;
    private String guid;
    private String type;
    private Double fixedDiscount;
    private Double promotionDiscount;
    private Double finalPrice;
    private Long createByUser;
    private Long customerId;
    private Double vat;
    private Double paymentCost;
    private Double deliveryCost;
    private Double companyShippingFee;
    private Double collaboratorShippingFee;
    private String vatNumber;
    private Date vatDate;
    private String vatType;
    private String orderStatus;
    private Long companyId;
    private Double discount;
    private String discountType;
    private List<OrderEntryData> entries = new ArrayList<>();
    private List<PaymentTransactionData> payments = new ArrayList<>();
    private long totalQuantity;
    private List<PromotionResultData> potentialOrderPromotions = new ArrayList<>();
    private List<PromotionResultData> couldFirePromotions = new ArrayList<>();
    private Date deliveryDate;
    private String customerNote;
    private String customerSupportNote;
    private List<Long> returnOrderIds;
    private boolean exchange;
    private String shippingCustomerName;
    private String shippingCustomerPhone;
    private Long shippingAddressId;
    private Long shippingProvinceId;
    private Long shippingDistrictId;
    private Long shippingWardId;
    private String shippingAddressDetail;
    private List<CouponCodeData> couponCodes = new ArrayList<>();
    private Double subTotalDiscount;
    private Double totalDiscount;
    private Double finalDiscount;
    private String gender;
    private String age;
    private Double paidAmount;
    private Double totalRewardAmount;
    private Double rewardPoint;
    private String cardNumber;
    private Double redeemAmount;
    private Double refundAmount;
    private Long employeeId;
    private String priceType;
    private String sellSignal;
    private Long externalId;
    private String externalCode;
    private List<OrderImageData> images ;
    private Long distributorId;
    private boolean reload = false;
    private boolean hasGotVat;
    private boolean hasChangeGift;
    private List<Long> settingCustomerOptionIds = new ArrayList<>();
    private OrderEntryData updatedOrderEntry;

    public List<PromotionResultData> getPotentialOrderPromotions() {
        return potentialOrderPromotions;
    }

    public void setPotentialOrderPromotions(List<PromotionResultData> potentialOrderPromotions) {
        this.potentialOrderPromotions = potentialOrderPromotions;
    }

    public List<OrderImageData> getImages() {
        return images;
    }

    public void setImages(List<OrderImageData> images) {
        this.images = images;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(Double subTotal) {
        this.subTotal = subTotal;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public Long getCreateByUser() {
        return createByUser;
    }

    public void setCreateByUser(Long createByUser) {
        this.createByUser = createByUser;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Double getVat() {
        return vat;
    }

    public void setVat(Double vat) {
        this.vat = vat;
    }

    public Double getPaymentCost() {
        return paymentCost;
    }

    public void setPaymentCost(Double paymentCost) {
        this.paymentCost = paymentCost;
    }

    public Double getDeliveryCost() {
        return deliveryCost;
    }

    public void setDeliveryCost(Double deliveryCost) {
        this.deliveryCost = deliveryCost;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public Date getVatDate() {
        return vatDate;
    }

    public void setVatDate(Date vatDate) {
        this.vatDate = vatDate;
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

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
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

    public List<OrderEntryData> getEntries() {
        return entries;
    }

    public void setEntries(List<OrderEntryData> entries) {
        this.entries = entries;
    }

    public long getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(long totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Double getFixedDiscount() {
        return fixedDiscount;
    }

    public void setFixedDiscount(Double fixedDiscount) {
        this.fixedDiscount = fixedDiscount;
    }

    public Double getPromotionDiscount() {
        return promotionDiscount;
    }

    public void setPromotionDiscount(Double promotionDiscount) {
        this.promotionDiscount = promotionDiscount;
    }

    public Double getSubTotalFixedDiscount() {
        return subTotalFixedDiscount;
    }

    public void setSubTotalFixedDiscount(Double subTotalFixedDiscount) {
        this.subTotalFixedDiscount = subTotalFixedDiscount;
    }

    public List<PaymentTransactionData> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentTransactionData> payments) {
        this.payments = payments;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
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

    public List<Long> getReturnOrderIds() {
        return returnOrderIds;
    }

    public void setReturnOrderIds(List<Long> returnOrderIds) {
        this.returnOrderIds = returnOrderIds;
    }

    public boolean isExchange() {
        return exchange;
    }

    public void setExchange(boolean exchange) {
        this.exchange = exchange;
    }

    public Long getShippingAddressId() {
        return shippingAddressId;
    }

    public void setShippingAddressId(Long shippingAddressId) {
        this.shippingAddressId = shippingAddressId;
    }

    public List<PromotionResultData> getCouldFirePromotions() {
        return couldFirePromotions;
    }

    public void setCouldFirePromotions(List<PromotionResultData> couldFirePromotions) {
        this.couldFirePromotions = couldFirePromotions;
    }

    public Double getSubTotalDiscount() {
        return subTotalDiscount;
    }

    public void setSubTotalDiscount(Double subTotalDiscount) {
        this.subTotalDiscount = subTotalDiscount;
    }

    public Double getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(Double totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public Double getFinalDiscount() {
        return finalDiscount;
    }

    public void setFinalDiscount(Double finalDiscount) {
        this.finalDiscount = finalDiscount;
    }

    public List<CouponCodeData> getCouponCodes() {
        return couponCodes;
    }

    public void setCouponCodes(List<CouponCodeData> couponCodes) {
        this.couponCodes = couponCodes;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
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

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
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

    public Double getCompanyShippingFee() {
        return companyShippingFee;
    }

    public void setCompanyShippingFee(Double companyShippingFee) {
        this.companyShippingFee = companyShippingFee;
    }

    public Double getCollaboratorShippingFee() {
        return collaboratorShippingFee;
    }

    public void setCollaboratorShippingFee(Double collaboratorShippingFee) {
        this.collaboratorShippingFee = collaboratorShippingFee;
    }

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    public String getSellSignal() {
        return sellSignal;
    }

    public void setSellSignal(String sellSignal) {
        this.sellSignal = sellSignal;
    }

    public Long getExternalId() {
        return externalId;
    }

    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }

    public String getExternalCode() {
        return externalCode;
    }

    public void setExternalCode(String externalCode) {
        this.externalCode = externalCode;
    }

    public Long getDistributorId() {
        return distributorId;
    }

    public void setDistributorId(Long distributorId) {
        this.distributorId = distributorId;
    }

    public boolean isReload() {
        return reload;
    }

    public void setReload(boolean reload) {
        this.reload = reload;
    }

    public Long getShippingProvinceId() {
        return shippingProvinceId;
    }

    public void setShippingProvinceId(Long shippingProvinceId) {
        this.shippingProvinceId = shippingProvinceId;
    }

    public Long getShippingWardId() {
        return shippingWardId;
    }

    public void setShippingWardId(Long shippingWardId) {
        this.shippingWardId = shippingWardId;
    }

    public String getShippingAddressDetail() {
        return shippingAddressDetail;
    }

    public void setShippingAddressDetail(String shippingAddressDetail) {
        this.shippingAddressDetail = shippingAddressDetail;
    }

    public Long getShippingDistrictId() {
        return shippingDistrictId;
    }

    public void setShippingDistrictId(Long shippingDistrictId) {
        this.shippingDistrictId = shippingDistrictId;
    }

    public List<Long> getSettingCustomerOptionIds() {
        return settingCustomerOptionIds;
    }

    public void setSettingCustomerOptionIds(List<Long> settingCustomerOptionIds) {
        this.settingCustomerOptionIds = settingCustomerOptionIds;
    }

    public String getShippingCustomerName() {
        return shippingCustomerName;
    }

    public void setShippingCustomerName(String shippingCustomerName) {
        this.shippingCustomerName = shippingCustomerName;
    }

    public String getShippingCustomerPhone() {
        return shippingCustomerPhone;
    }

    public void setShippingCustomerPhone(String shippingCustomerPhone) {
        this.shippingCustomerPhone = shippingCustomerPhone;
    }

    public boolean isHasGotVat() {
        return hasGotVat;
    }

    public void setHasGotVat(boolean hasGotVat) {
        this.hasGotVat = hasGotVat;
    }

    public OrderEntryData getUpdatedOrderEntry() {
        return updatedOrderEntry;
    }

    public void setUpdatedOrderEntry(OrderEntryData updatedOrderEntry) {
        this.updatedOrderEntry = updatedOrderEntry;
    }

    public boolean isHasChangeGift() {
        return hasChangeGift;
    }

    public void setHasChangeGift(boolean hasChangeGift) {
        this.hasChangeGift = hasChangeGift;
    }
}