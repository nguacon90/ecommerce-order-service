package com.vctek.orderservice.feignclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vctek.orderservice.util.DateUtil;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductSearchRequest {
    private Long companyId;
    private Long categoryId;
    private Long warehouseId;
    private String product;
    private Boolean hasImage;
    private Boolean hasDescription;
    private Boolean hasInventory;

    @DateTimeFormat(pattern = DateUtil.ISO_DATE_PATTERN)
    private Date fromCreatedDate;

    @DateTimeFormat(pattern = DateUtil.ISO_DATE_PATTERN)
    private Date toCreatedDate;

    private Double priceFrom;
    private Double priceTo;
    private Long inventoryStatusId;
    private Double inventoryFrom;
    private Double inventoryTo;
    private String typeSell;
    private String description;
    private String sku;
    private String ids;
    private String categoryIds;
    private Long supplierId;
    private boolean unpaged;
    private String comboType;
    private Double maxSaleQuantityFrom;
    private Double maxSaleQuantityTo;
    private String dtype;
    private boolean orderSearching;
    private boolean ignoreCombo;
    private Integer pageSize;
    private Boolean allowReward;
    private boolean searchBarcode;
    private List<Long> productIds;

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Boolean getHasImage() {
        return hasImage;
    }

    public void setHasImage(Boolean hasImage) {
        this.hasImage = hasImage;
    }

    public Date getFromCreatedDate() {
        return fromCreatedDate;
    }

    public void setFromCreatedDate(Date fromCreatedDate) {
        this.fromCreatedDate = fromCreatedDate;
    }

    public Date getToCreatedDate() {
        return toCreatedDate;
    }

    public void setToCreatedDate(Date toCreatedDate) {
        this.toCreatedDate = toCreatedDate;
    }

    public Double getPriceFrom() {
        return priceFrom;
    }

    public void setPriceFrom(Double priceFrom) {
        this.priceFrom = priceFrom;
    }

    public Double getPriceTo() {
        return priceTo;
    }

    public void setPriceTo(Double priceTo) {
        this.priceTo = priceTo;
    }

    public Double getInventoryFrom() {
        return inventoryFrom;
    }

    public void setInventoryFrom(Double inventoryFrom) {
        this.inventoryFrom = inventoryFrom;
    }

    public Double getInventoryTo() {
        return inventoryTo;
    }

    public void setInventoryTo(Double inventoryTo) {
        this.inventoryTo = inventoryTo;
    }

    public String getTypeSell() {
        return typeSell;
    }

    public void setTypeSell(String typeSell) {
        this.typeSell = typeSell;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getInventoryStatusId() {
        return inventoryStatusId;
    }

    public void setInventoryStatusId(Long inventoryStatusId) {
        this.inventoryStatusId = inventoryStatusId;
    }

    public boolean isUnpaged() {
        return unpaged;
    }

    public void setUnpaged(boolean unpaged) {
        this.unpaged = unpaged;
    }

    public Boolean getHasDescription() {
        return hasDescription;
    }

    public void setHasDescription(Boolean hasDescription) {
        this.hasDescription = hasDescription;
    }

    public Boolean getHasInventory() {
        return hasInventory;
    }

    public void setHasInventory(Boolean hasInventory) {
        this.hasInventory = hasInventory;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public void setCategoryIds(String categoryIds) {
        this.categoryIds = categoryIds;
    }

    public String getCategoryIds() {
        return categoryIds;
    }

    public String getComboType() {
        return comboType;
    }

    public void setComboType(String comboType) {
        this.comboType = comboType;
    }

    public Double getMaxSaleQuantityFrom() {
        return maxSaleQuantityFrom;
    }

    public void setMaxSaleQuantityFrom(Double maxSaleQuantityFrom) {
        this.maxSaleQuantityFrom = maxSaleQuantityFrom;
    }

    public Double getMaxSaleQuantityTo() {
        return maxSaleQuantityTo;
    }

    public void setMaxSaleQuantityTo(Double maxSaleQuantityTo) {
        this.maxSaleQuantityTo = maxSaleQuantityTo;
    }

    public String getDtype() {
        return dtype;
    }

    public void setDtype(String dtype) {
        this.dtype = dtype;
    }

    public boolean isOrderSearching() {
        return orderSearching;
    }

    public void setOrderSearching(boolean orderSearching) {
        this.orderSearching = orderSearching;
    }

    public boolean isIgnoreCombo() {
        return ignoreCombo;
    }

    public void setIgnoreCombo(boolean ignoreCombo) {
        this.ignoreCombo = ignoreCombo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Boolean isAllowReward() {
        return allowReward;
    }

    public void setAllowReward(Boolean allowReward) {
        this.allowReward = allowReward;
    }

    public boolean isSearchBarcode() {
        return searchBarcode;
    }

    public void setSearchBarcode(boolean searchBarcode) {
        this.searchBarcode = searchBarcode;
    }

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }
}
