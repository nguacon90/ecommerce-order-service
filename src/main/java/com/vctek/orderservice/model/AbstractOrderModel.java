package com.vctek.orderservice.model;

import com.vctek.orderservice.couponservice.model.CouponRedemptionModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionResultModel;
import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionSourceRuleModel;
import com.vctek.orderservice.promotionengine.promotionservice.util.DiscountValue;
import com.vctek.orderservice.promotionengine.util.CurrencyIsoCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "orders")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@EntityListeners({AuditingEntityListener.class})
public class AbstractOrderModel extends ItemModel {

    @Column(name = "calculated")
    private boolean calculated;

    @Column(name = "code")
    private String code;

    @Column(name = "currency_code")
    private String currencyCode = CurrencyIsoCode.VND.toString();

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "sub_total")
    private Double subTotal;

    @Column(name = "sub_total_discount")
    private Double subTotalDiscount;

    @Column(name = "total_tax")
    private Double totalTax;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "guid")
    private String guid;

    @Column(name = "order_type")
    private String type;

    @Column(name = "fixed_discount")
    private Double fixedDiscount;

    @Column(name = "images")
    private String images;


    @Column(name = "total_discount")
    private Double totalDiscount;

    @Column(name = "total_topping_discount")
    private Double totalToppingDiscount;

    @Column(name = "final_price")
    private Double finalPrice;

    @Column(name = "created_by")
    private Long createByUser;

    @Column(name = "customerId")
    private Long customerId;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "vat")
    private Double vat;

    @Column(name = "payment_cost")
    private Double paymentCost;

    @Column(name = "delivery_cost")
    private Double deliveryCost;

    @Column(name = "company_shipping_fee")
    private Double companyShippingFee;

    @Column(name = "collaborator_shipping_fee")
    private Double collaboratorShippingFee;

    @Column(name = "vat_number")
    private String vatNumber;

    @Column(name = "vat_date")
    private Date vatDate;

    @Column(name = "vat_type")
    private String vatType;

    @Column(name = "order_status")
    private String orderStatus;

    @Column(name = "global_discount_values")
    private String globalDiscountValues;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "discount")
    private Double discount;

    @Column(name = "discount_type")
    private String discountType;

    @Column(name = "note")
    private String note;

    @Column(name = "is_exchange")
    private boolean exchange;

    @Column(name = "shipping_customer_name")
    private String shippingCustomerName;

    @Column(name = "shipping_customer_phone")
    private String shippingCustomerPhone;

    @Column(name = "shipping_address_id")
    private Long shippingAddressId;

    @Column(name = "shipping_province_id")
    private Long shippingProvinceId;

    @Column(name = "shipping_district_id")
    private Long shippingDistrictId;

    @Column(name = "shipping_ward_id")
    private Long shippingWardId;

    @Column(name = "shipping_address_detail")
    private String shippingAddressDetail;

    @Column(name = "paid_amount")
    private Double paidAmount;

    @Column(name = "sell_signal")
    private String sellSignal;

    @Column(name = "external_code")
    private String externalCode;

    @Column(name = "external_id")
    private Long externalId;

    @OneToMany(mappedBy = "order", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private Set<OrderHasCouponCodeModel> orderHasCouponCodeModels;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AbstractOrderEntryModel> entries = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PromotionResultModel> promotionResults = new HashSet<>();

    @OneToMany(mappedBy = "orderModel", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PaymentTransactionModel> paymentTransactions = new HashSet<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderHistoryModel> orderHistory = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CouponRedemptionModel> couponRedemptionModels = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "orders_has_promotions",
            joinColumns = {@JoinColumn(name = "order_id")},
            inverseJoinColumns = {@JoinColumn(name = "promotion_source_rule_id")}
    )
    private Set<PromotionSourceRuleModel> couldFirePromotions = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "order_has_setting_customer_option",
            joinColumns = {@JoinColumn(name = "order_id")},
            inverseJoinColumns = {@JoinColumn(name = "order_setting_customer_option_id")}
    )
    private Set<OrderSettingCustomerOptionModel> orderSettingCustomerOptionModels = new HashSet<>();

    @Column(name = "applied_rule_id")
    private Long appliedPromotionSourceRuleId;

    @Column(name = "created_time")
    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;

    @Column(name = "modified_time")
    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifiedTime;

    @LastModifiedBy
    @Column(name = "modified_by")
    private Long modifiedBy;

    @Column(name = "delivery_date")
    private Date deliveryDate;

    @Column(name = "customer_note")
    private String customerNote;

    @Column(name = "customer_support_note")
    private String customerSupportNote;

    @Column(name = "deleted")
    private boolean deleted;

    @Column(name = "total_reward_amount")
    private Double totalRewardAmount;

    @Column(name ="reward_point")
    private Double rewardPoint;

    @Column(name ="card_number")
    private String cardNumber;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "price_type")
    private String priceType;

    @Column(name = "distributor_id")
    private Long distributorId;

    @Column(name = "has_got_vat")
    private boolean hasGotVat;

    @Column(name = "has_change_gift")
    private boolean hasChangeGift;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JoinTable(
            name = "order_has_tag",
            joinColumns = {@JoinColumn(name = "order_id")},
            inverseJoinColumns = {@JoinColumn(name = "tag_id")}
    )
    private Set<TagModel> tags = new HashSet<>();

    @ManyToOne
    @JoinColumn(name="order_source_id")
    private OrderSourceModel orderSourceModel;

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Long getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Long modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public List<AbstractOrderEntryModel> getEntries() {
        return this.entries;
    }

    public void setEntries(List<AbstractOrderEntryModel> entries) {
        this.entries = entries;
    }

    public boolean isCalculated() {
        return calculated;
    }

    public void setCalculated(boolean calculated) {
        this.calculated = calculated;
    }

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
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

    public void setDiscountValues(List<DiscountValue> discountValues) {
        String values = DiscountValue.toString(discountValues);
        this.setGlobalDiscountValues(values);
        this.setCalculated(false);
    }

    /**
     * @return
     * Discount of promotions
     */
    public List<DiscountValue> getDiscountValues() {
        String values = this.getGlobalDiscountValues();
        List l = (List)DiscountValue.parseDiscountValueCollection(values);
        return l != null ? l : Collections.emptyList();
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
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

    public Set<PromotionResultModel> getPromotionResults() {
        return promotionResults;
    }

    public void setPromotionResults(Set<PromotionResultModel> promotionResults) {
        this.promotionResults = promotionResults;
    }

    public Double getFixedDiscount() {
        return fixedDiscount;
    }

    public void setFixedDiscount(Double fixedDiscount) {
        this.fixedDiscount = fixedDiscount;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public Double getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(Double totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public Set<PaymentTransactionModel> getPaymentTransactions() {
        return paymentTransactions;
    }

    public void setPaymentTransactions(Set<PaymentTransactionModel> paymentTransactions) {
        this.paymentTransactions = paymentTransactions;
    }

    public Double getSubTotalDiscount() {
        return subTotalDiscount;
    }

    public void setSubTotalDiscount(Double subTotalDiscount) {
        this.subTotalDiscount = subTotalDiscount;
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

    public List<OrderHistoryModel> getOrderHistory() {
        return orderHistory;
    }

    public void setOrderHistory(List<OrderHistoryModel> orderHistory) {
        this.orderHistory = orderHistory;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Long getShippingAddressId() {
        return shippingAddressId;
    }

    public void setShippingAddressId(Long shippingAddressId) {
        this.shippingAddressId = shippingAddressId;
    }

    public Set<CouponRedemptionModel> getCouponRedemptionModels() {
        return couponRedemptionModels;
    }

    public void setCouponRedemptionModels(Set<CouponRedemptionModel> couponRedemptionModels) {
        this.couponRedemptionModels = couponRedemptionModels;
    }

    public Set<PromotionSourceRuleModel> getCouldFirePromotions() {
        return couldFirePromotions;
    }

    public void setCouldFirePromotions(Set<PromotionSourceRuleModel> couldFirePromotions) {
        this.couldFirePromotions = couldFirePromotions;
    }

    public Long getAppliedPromotionSourceRuleId() {
        return appliedPromotionSourceRuleId;
    }

    public void setAppliedPromotionSourceRuleId(Long appliedPromotionSourceRuleId) {
        this.appliedPromotionSourceRuleId = appliedPromotionSourceRuleId;
    }

    public Set<OrderHasCouponCodeModel> getOrderHasCouponCodeModels() {
        return orderHasCouponCodeModels;
    }

    public void setOrderHasCouponCodeModels(Set<OrderHasCouponCodeModel> orderHasCouponCodeModels) {
        this.orderHasCouponCodeModels = orderHasCouponCodeModels;
    }

    public Double getTotalToppingDiscount() {
        return totalToppingDiscount;
    }

    public void setTotalToppingDiscount(Double totalToppingDiscount) {
        this.totalToppingDiscount = totalToppingDiscount;
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

    public Set<OrderSettingCustomerOptionModel> getOrderSettingCustomerOptionModels() {
        return orderSettingCustomerOptionModels;
    }

    public void setOrderSettingCustomerOptionModels(Set<OrderSettingCustomerOptionModel> orderSettingCustomerOptionModels) {
        this.orderSettingCustomerOptionModels = orderSettingCustomerOptionModels;
    }

    public Set<TagModel> getTags() {
        return tags;
    }

    public void setTags(Set<TagModel> tags) {
        this.tags = tags;
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

    public OrderSourceModel getOrderSourceModel() {
        return orderSourceModel;
    }

    public void setOrderSourceModel(OrderSourceModel orderSourceModel) {
        this.orderSourceModel = orderSourceModel;
    }

    public boolean isHasChangeGift() {
        return hasChangeGift;
    }

    public void setHasChangeGift(boolean hasChangeGift) {
        this.hasChangeGift = hasChangeGift;
    }
}
