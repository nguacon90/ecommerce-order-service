package com.vctek.orderservice.model;


import com.vctek.orderservice.promotionengine.promotionservice.model.PromotionOrderEntryConsumedModel;
import com.vctek.orderservice.promotionengine.promotionservice.util.DiscountValue;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "order_entry")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@EntityListeners({AuditingEntityListener.class})
public class AbstractOrderEntryModel extends ItemModel {

    @Column(name = "base_price")
    private Double basePrice;

    @Column(name = "origin_base_price")
    private Double originBasePrice;

    @Column(name = "quantity")
    private Long quantity;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "entry_number")
    private Integer entryNumber;

    @Column(name = "totalPrice")
    private Double totalPrice;

    @Column(name = "final_price")
    private Double finalPrice;

    @Column(name = "calculated")
    private boolean calculated;

    @Column(name = "discount")
    private Double discount;

    @Column(name = "discount_type")
    private String discountType;

    @Column(name = "fixed_discount")
    private Double fixedDiscount;

    @Column(name = "total_discount")
    private Double totalDiscount;

    @Column(name = "discount_order_to_item")
    private Double discountOrderToItem;

    @Column(name = "discount_values_internal")
    private String discountValuesInternal;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "order_code")
    private String orderCode;

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

    @Column(name = "weight")
    private Double weight;

    @Column(name = "is_pre_order")
    private boolean isPreOrder;

    @Column(name = "is_holding")
    private boolean isHolding;

    @Column(name = "holding_stock")
    private Long holdingStock;

    @Column(name = "combo_type")
    private String comboType;

    @Column(name = "give_away")
    private boolean giveAway;

    @Column(name = "fixed_price")
    private boolean fixedPrice;

    @Column(name = "reward_amount")
    private Double rewardAmount;

    @Column(name = "return_quantity")
    private Long returnQuantity;

    @Column(name = "sale_off")
    private boolean saleOff;

    @Column(name = "vat")
    private Double vat;

    @Column(name = "vat_type")
    private String vatType;

    @OneToMany(mappedBy = "orderEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PromotionOrderEntryConsumedModel> consumedEntries = new HashSet<>();

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", unique = true)
    private AbstractOrderModel order;

    @OneToMany(mappedBy = "orderEntry", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<SubOrderEntryModel> subOrderEntries = new HashSet<>();

    @OneToMany(mappedBy = "orderEntry", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<ToppingOptionModel> toppingOptionModels = new HashSet<>();

    @Column(name = "recommended_retail_price")
    private Double recommendedRetailPrice;


    public Double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Double basePrice) {
        this.basePrice = basePrice;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getEntryNumber() {
        return entryNumber;
    }

    public void setEntryNumber(Integer entryNumber) {
        this.entryNumber = entryNumber;
    }

    public AbstractOrderModel getOrder() {
        return order;
    }

    public void setOrder(AbstractOrderModel order) {
        this.order = order;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public boolean isCalculated() {
        return calculated;
    }

    public void setCalculated(boolean calculated) {
        this.calculated = calculated;
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

    public void setDiscountValues(List<DiscountValue> discountValues) {
        String values = DiscountValue.toString(discountValues);
        this.discountValuesInternal = values;
        this.setCalculated(false);
    }

    /**
     * @return
     * Discount of promotions
     */
    public List<DiscountValue> getDiscountValues() {
        String values = this.discountValuesInternal;
        List l = (List)DiscountValue.parseDiscountValueCollection(values);
        return l != null ? l : Collections.emptyList();
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Double getDiscountOrderToItem() {
        return discountOrderToItem;
    }

    public void setDiscountOrderToItem(Double discountOrderToItem) {
        this.discountOrderToItem = discountOrderToItem;
    }

    public Double getFixedDiscount() {
        return fixedDiscount;
    }

    public void setFixedDiscount(Double fixedDiscount) {
        this.fixedDiscount = fixedDiscount;
    }

    public Double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(Double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

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

    public Double getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(Double totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Set<PromotionOrderEntryConsumedModel> getConsumedEntries() {
        return consumedEntries;
    }

    public void setConsumedEntries(Set<PromotionOrderEntryConsumedModel> consumedEntries) {
        this.consumedEntries.clear();
        this.consumedEntries.addAll(consumedEntries);
    }

    public boolean isPreOrder() {
        return isPreOrder;
    }

    public void setPreOrder(boolean preOrder) {
        isPreOrder = preOrder;
    }

    public boolean isHolding() {
        return isHolding;
    }

    public void setHolding(boolean holding) {
        isHolding = holding;
    }

    public Long getHoldingStock() {
        return holdingStock;
    }

    public void setHoldingStock(Long holdingStock) {
        this.holdingStock = holdingStock;
    }

    public String getComboType() {
        return comboType;
    }

    public void setComboType(String comboType) {
        this.comboType = comboType;
    }

    public Set<SubOrderEntryModel> getSubOrderEntries() {
        return subOrderEntries;
    }

    public void setSubOrderEntries(Set<SubOrderEntryModel> subOrderEntries) {
        this.subOrderEntries = subOrderEntries;
    }

    public boolean isGiveAway() {
        return giveAway;
    }

    public void setGiveAway(boolean giveAway) {
        this.giveAway = giveAway;
    }

    public Set<ToppingOptionModel> getToppingOptionModels() {
        return toppingOptionModels;
    }

    public void setToppingOptionModels(Set<ToppingOptionModel> toppingOptionModels) {
        this.toppingOptionModels = toppingOptionModels;
    }

    public boolean isFixedPrice() {
        return fixedPrice;
    }

    public void setFixedPrice(boolean fixedPrice) {
        this.fixedPrice = fixedPrice;
    }

    public Double getRewardAmount() {
        return rewardAmount;
    }

    public void setRewardAmount(Double rewardMount) {
        this.rewardAmount = rewardMount;
    }

    public Long getReturnQuantity() {
        return returnQuantity;
    }

    public void setReturnQuantity(Long returnQuantity) {
        this.returnQuantity = returnQuantity;
    }

    public Double getOriginBasePrice() {
        return originBasePrice;
    }

    public void setOriginBasePrice(Double originBasePrice) {
        this.originBasePrice = originBasePrice;
    }

    public Double getRecommendedRetailPrice() {
        return recommendedRetailPrice;
    }

    public void setRecommendedRetailPrice(Double recommendedRetailPrice) {
        this.recommendedRetailPrice = recommendedRetailPrice;
    }

    public boolean isSaleOff() {
        return saleOff;
    }

    public void setSaleOff(boolean saleOff) {
        this.saleOff = saleOff;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractOrderEntryModel)) return false;
        AbstractOrderEntryModel abstractOrderEntryModel = (AbstractOrderEntryModel) o;
        if(this.getId() == null && abstractOrderEntryModel.getId() == null) return false;
        return Objects.equals(getId(), abstractOrderEntryModel.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
