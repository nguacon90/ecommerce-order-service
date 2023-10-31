package com.vctek.orderservice.dto;

import com.vctek.orderservice.dto.request.AddSubOrderEntryRequest;
import com.vctek.orderservice.model.AbstractOrderModel;

import java.util.List;

public class CommerceAbstractOrderParameter {
    private AbstractOrderModel order;
    private Long productId;
    private long quantity;
    private Double basePrice;
    private Double originBasePrice;
    private Double discount;
    private String discountType;
    private boolean createNewEntry;
    private Long userId;
    private Long customerId;
    private boolean recalculate = true;
    private String guid;
    private Long companyId;
    private Long warehouseId;
    private Double vat;
    private String vatType;
    private Double weight;
    private Long comboId;
    private Integer comboGroupNumber;
    private Long entryId;
    private Double recommendedRetailPrice;
    private boolean saleOff;
    private String cardNumber;
    private boolean recalculateVat;
    private String comboType;
    private List<AddSubOrderEntryRequest> subEntries;

    public AbstractOrderModel getOrder() {
        return order;
    }

    public void setOrder(AbstractOrderModel order) {
        this.order = order;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public boolean isCreateNewEntry() {
        return createNewEntry;
    }

    public void setCreateNewEntry(boolean createNewEntry) {
        this.createNewEntry = createNewEntry;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public boolean isRecalculate() {
        return recalculate;
    }

    public void setRecalculate(boolean recalculate) {
        this.recalculate = recalculate;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Double basePrice) {
        this.basePrice = basePrice;
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

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
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

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Long getComboId() {
        return comboId;
    }

    public void setComboId(Long comboId) {
        this.comboId = comboId;
    }

    public Integer getComboGroupNumber() {
        return comboGroupNumber;
    }

    public void setComboGroupNumber(Integer comboGroupNumber) {
        this.comboGroupNumber = comboGroupNumber;
    }

    public Long getEntryId() {
        return entryId;
    }

    public void setEntryId(Long entryId) {
        this.entryId = entryId;
    }

    public Double getRecommendedRetailPrice() {
        return recommendedRetailPrice;
    }

    public void setRecommendedRetailPrice(Double recommendedRetailPrice) {
        this.recommendedRetailPrice = recommendedRetailPrice;
    }

    public Double getOriginBasePrice() {
        return originBasePrice;
    }

    public void setOriginBasePrice(Double originBasePrice) {
        this.originBasePrice = originBasePrice;
    }

    public boolean isSaleOff() {
        return saleOff;
    }

    public void setSaleOff(boolean saleOff) {
        this.saleOff = saleOff;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public boolean isRecalculateVat() {
        return recalculateVat;
    }

    public void setRecalculateVat(boolean recalculateVat) {
        this.recalculateVat = recalculateVat;
    }

    public List<AddSubOrderEntryRequest> getSubEntries() {
        return subEntries;
    }

    public void setSubEntries(List<AddSubOrderEntryRequest> subEntries) {
        this.subEntries = subEntries;
    }

    public String getComboType() {
        return comboType;
    }

    public void setComboType(String comboType) {
        this.comboType = comboType;
    }
}
