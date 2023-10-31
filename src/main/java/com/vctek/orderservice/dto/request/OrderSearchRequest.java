package com.vctek.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vctek.orderservice.util.DateUtil;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderSearchRequest implements Serializable {
    private Long id;

    @DateTimeFormat(pattern = DateUtil.ISO_DATE_PATTERN)
    private Date fromCreatedTime;

    @DateTimeFormat(pattern = DateUtil.ISO_DATE_PATTERN)
    private Date toCreatedTime;
    private Long createdBy;
    private String createdName;
    private Long verifiedBy;
    private Long warehouseId;
    private String warehouseIds;
    private String customerName;
    private String code;
    private String product;
    private String sortOrder;
    private String sortField;
    private boolean unpaged;
    private Long companyId;
    private String finalPrice;
    private String orderType;
    private String priceType;
    private Long distributorId;
    private String totalDiscount;
    private Double fromFinalPrice;
    private Double toFinalPrice;
    private Boolean hasCustomerInfo;
    private String gender;

    private Long shippingCompanyId;
    private String customerNote;
    private String orderRetailCode;
    @DateTimeFormat(pattern = DateUtil.ISO_DATE_PATTERN)
    private Date fromModifiedTimeStatus;

    @DateTimeFormat(pattern = DateUtil.ISO_DATE_PATTERN)
    private Date toModifiedTimeStatus;
    private Long provinceId;
    private Long districtId;
    private Long wardId;
    private String address;
    private String currentStatus;
    private String orderStatus;
    private String currentStatusList;
    private String orderStatusList;
    private Long orderSourceId;
    private List<Long> orderSourceIds;
    private Long customerId;
    private String age;
    private String paymentMethodId;
    @DateTimeFormat(pattern = DateUtil.ISO_DATE_PATTERN)
    private Date fromModifiedTimeLastStatus;

    @DateTimeFormat(pattern = DateUtil.ISO_DATE_PATTERN)
    private Date toModifiedTimeLastStatus;

    private Boolean holding;
    private Boolean preOrder;
    private String note;

    @DateTimeFormat(pattern = DateUtil.ISO_DATE_PATTERN)
    private Date fromDeliveryDate;

    @DateTimeFormat(pattern = DateUtil.ISO_DATE_PATTERN)
    private Date toDeliveryDate;

    @DateTimeFormat(pattern = DateUtil.ISO_DATE_TIME_PATTERN)
    private Date fromDeliveryTime;

    @DateTimeFormat(pattern = DateUtil.ISO_DATE_TIME_PATTERN)
    private Date toDeliveryTime;
    private Long userId;
    private String exportType;
    private Integer fileNum;
    private int page = 0;
    private int pageSize = 20;
    private int pageExcel = 0;
    private Boolean hasDeliveryDate;
    private Boolean hasCustomerShippingFee;
    private List<Long> customerOptionIds;
    private Long tagId;
    private Boolean hasTag;
    private Boolean hasSaleOff;
    private String sellSignal;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getFromCreatedTime() {
        return fromCreatedTime;
    }

    public void setFromCreatedTime(Date fromCreatedTime) {
        this.fromCreatedTime = fromCreatedTime;
    }

    public Date getToCreatedTime() {
        return toCreatedTime;
    }

    public void setToCreatedTime(Date toCreatedTime) {
        this.toCreatedTime = toCreatedTime;
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

    public Long getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(Long verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getWarehouseIds() {
        return warehouseIds;
    }

    public void setWarehouseIds(String warehouseIds) {
        this.warehouseIds = warehouseIds;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public boolean isUnpaged() {
        return unpaged;
    }

    public void setUnpaged(boolean unpaged) {
        this.unpaged = unpaged;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(String finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(String totalDiscount) {
        this.totalDiscount = totalDiscount;
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

    public Date getFromModifiedTimeStatus() {
        return fromModifiedTimeStatus;
    }

    public void setFromModifiedTimeStatus(Date fromModifiedTimeStatus) {
        this.fromModifiedTimeStatus = fromModifiedTimeStatus;
    }

    public Date getToModifiedTimeStatus() {
        return toModifiedTimeStatus;
    }

    public void setToModifiedTimeStatus(Date toModifiedTimeStatus) {
        this.toModifiedTimeStatus = toModifiedTimeStatus;
    }

    public Long getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(Long provinceId) {
        this.provinceId = provinceId;
    }

    public Long getDistrictId() {
        return districtId;
    }

    public void setDistrictId(Long districtId) {
        this.districtId = districtId;
    }

    public Long getWardId() {
        return wardId;
    }

    public void setWardId(Long wardId) {
        this.wardId = wardId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getCurrentStatusList() {
        return currentStatusList;
    }

    public void setCurrentStatusList(String currentStatusList) {
        this.currentStatusList = currentStatusList;
    }

    public String getOrderStatusList() {
        return orderStatusList;
    }

    public void setOrderStatusList(String orderStatusList) {
        this.orderStatusList = orderStatusList;
    }

    public Long getOrderSourceId() {
        return orderSourceId;
    }

    public void setOrderSourceId(Long orderSourceId) {
        this.orderSourceId = orderSourceId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public Date getFromModifiedTimeLastStatus() {
        return fromModifiedTimeLastStatus;
    }

    public void setFromModifiedTimeLastStatus(Date fromModifiedTimeLastStatus) {
        this.fromModifiedTimeLastStatus = fromModifiedTimeLastStatus;
    }

    public Date getToModifiedTimeLastStatus() {
        return toModifiedTimeLastStatus;
    }

    public void setToModifiedTimeLastStatus(Date toModifiedTimeLastStatus) {
        this.toModifiedTimeLastStatus = toModifiedTimeLastStatus;
    }

    public Boolean getHolding() {
        return holding;
    }

    public void setHolding(Boolean holding) {
        this.holding = holding;
    }

    public Boolean getPreOrder() {
        return preOrder;
    }

    public void setPreOrder(Boolean preOrder) {
        this.preOrder = preOrder;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<Long> getOrderSourceIds() {
        return orderSourceIds;
    }

    public void setOrderSourceIds(List<Long> orderSourceIds) {
        this.orderSourceIds = orderSourceIds;
    }

    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    public Date getFromDeliveryDate() {
        return fromDeliveryDate;
    }

    public void setFromDeliveryDate(Date fromDeliveryDate) {
        this.fromDeliveryDate = fromDeliveryDate;
    }

    public Date getToDeliveryDate() {
        return toDeliveryDate;
    }

    public void setToDeliveryDate(Date toDeliveryDate) {
        this.toDeliveryDate = toDeliveryDate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getExportType() {
        return exportType;
    }

    public void setExportType(String exportType) {
        this.exportType = exportType;
    }

    public Integer getFileNum() {
        return fileNum;
    }

    public void setFileNum(Integer fileNum) {
        this.fileNum = fileNum;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageExcel() {
        return pageExcel;
    }

    public void setPageExcel(int pageExcel) {
        this.pageExcel = pageExcel;
    }

    public Boolean getHasDeliveryDate() {
        return hasDeliveryDate;
    }

    public void setHasDeliveryDate(Boolean hasDeliveryDate) {
        this.hasDeliveryDate = hasDeliveryDate;
    }

    public Double getFromFinalPrice() {
        return fromFinalPrice;
    }

    public void setFromFinalPrice(Double fromFinalPrice) {
        this.fromFinalPrice = fromFinalPrice;
    }

    public Double getToFinalPrice() {
        return toFinalPrice;
    }

    public void setToFinalPrice(Double toFinalPrice) {
        this.toFinalPrice = toFinalPrice;
    }

    public Boolean getHasCustomerInfo() {
        return hasCustomerInfo;
    }

    public void setHasCustomerInfo(Boolean hasCustomerInfo) {
        this.hasCustomerInfo = hasCustomerInfo;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCustomerNote() {
        return customerNote;
    }

    public void setCustomerNote(String customerNote) {
        this.customerNote = customerNote;
    }

    public Boolean getHasCustomerShippingFee() {
        return hasCustomerShippingFee;
    }

    public void setHasCustomerShippingFee(Boolean hasCustomerShippingFee) {
        this.hasCustomerShippingFee = hasCustomerShippingFee;
    }

    public Date getFromDeliveryTime() {
        return fromDeliveryTime;
    }

    public void setFromDeliveryTime(Date fromDeliveryTime) {
        this.fromDeliveryTime = fromDeliveryTime;
    }

    public Date getToDeliveryTime() {
        return toDeliveryTime;
    }

    public void setToDeliveryTime(Date toDeliveryTime) {
        this.toDeliveryTime = toDeliveryTime;
    }

    public Long getDistributorId() {
        return distributorId;
    }

    public void setDistributorId(Long distributorId) {
        this.distributorId = distributorId;
    }

    public List<Long> getCustomerOptionIds() {
        return customerOptionIds;
    }

    public void setCustomerOptionIds(List<Long> customerOptionIds) {
        this.customerOptionIds = customerOptionIds;
    }

    public Long getTagId() {
        return tagId;
    }

    public void setTagId(Long tagId) {
        this.tagId = tagId;
    }

    public Boolean getHasTag() {
        return hasTag;
    }

    public void setHasTag(Boolean hasTag) {
        this.hasTag = hasTag;
    }

    public Boolean getHasSaleOff() {
        return hasSaleOff;
    }

    public void setHasSaleOff(Boolean hasSaleOff) {
        this.hasSaleOff = hasSaleOff;
    }

    public String getSellSignal() {
        return sellSignal;
    }

    public void setSellSignal(String sellSignal) {
        this.sellSignal = sellSignal;
    }
}
