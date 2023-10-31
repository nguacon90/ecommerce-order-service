package com.vctek.orderservice.elasticsearch.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vctek.redis.PriceData;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(indexName = "products")
public class ProductSearchModel extends ElasticItemModel {

    private Long companyId;
    private String name;
    private Long parentId;
    private Long supplierId;
    private String barcode;
    private String typeSell;
    private String supplierProductName;
    private String supplierProductSku;
    private String supplierProductBarcode;
    private Long mainCategoryId;
    private String mainCategoryCode;
    private String importType;
    private boolean deleted;
    private boolean allowReward;
    @Field(type = FieldType.Keyword)
    private String sku;

    private String dtype;

    @Field(type = FieldType.Keyword)
    private String inventoryWarehouseIds;

    @Field(type = FieldType.Keyword)
    private String inventoryStatusWarehouseIds;

    private boolean isBaseProduct;
    private Integer remainInventory;
    private Double averageImportPrice;
    private String comboType;


    @Field(type = FieldType.Date)
    private Date creationTime;

    @Field(type = FieldType.Date)
    private Date modifiedTime;

    private Integer receiptQuantity;
    private Integer retailQuantity;
    private Integer onlineQuantity;
    private Integer wholesaleQuantity;
    private Long externalId;
    private Double wholesalePrice;
    private boolean allowSell;

    @Field(type = FieldType.Nested, includeInParent = true)
    private List<PriceData> prices = new ArrayList<>();
    private String unitName;
    private String productType;
    private String defaultImageUrl;
    private List<Long> fullCategoryIds = new ArrayList();
    private List<String> onsiteImageUrls = new ArrayList<>();
    private Double weight;

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getDtype() {
        return dtype;
    }

    public void setDtype(String dtype) {
        this.dtype = dtype;
    }

    public boolean isBaseProduct() {
        return isBaseProduct;
    }

    public void setBaseProduct(boolean baseProduct) {
        isBaseProduct = baseProduct;
    }

    public Integer getRemainInventory() {
        return remainInventory;
    }

    public void setRemainInventory(Integer remainInventory) {
        this.remainInventory = remainInventory;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Integer getReceiptQuantity() {
        return receiptQuantity;
    }

    public void setReceiptQuantity(Integer receiptQuantity) {
        this.receiptQuantity = receiptQuantity;
    }

    public Integer getRetailQuantity() {
        return retailQuantity;
    }

    public void setRetailQuantity(Integer retailQuantity) {
        this.retailQuantity = retailQuantity;
    }

    public Integer getOnlineQuantity() {
        return onlineQuantity;
    }

    public void setOnlineQuantity(Integer onlineQuantity) {
        this.onlineQuantity = onlineQuantity;
    }

    public Integer getWholesaleQuantity() {
        return wholesaleQuantity;
    }

    public void setWholesaleQuantity(Integer wholesaleQuantity) {
        this.wholesaleQuantity = wholesaleQuantity;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Long supplierId) {
        this.supplierId = supplierId;
    }

    public Long getMainCategoryId() {
        return mainCategoryId;
    }

    public void setMainCategoryId(Long mainCategoryId) {
        this.mainCategoryId = mainCategoryId;
    }

    public String getSupplierProductName() {
        return supplierProductName;
    }

    public void setSupplierProductName(String supplierProductName) {
        this.supplierProductName = supplierProductName;
    }

    public String getSupplierProductSku() {
        return supplierProductSku;
    }

    public void setSupplierProductSku(String supplierProductSku) {
        this.supplierProductSku = supplierProductSku;
    }

    public String getSupplierProductBarcode() {
        return supplierProductBarcode;
    }

    public void setSupplierProductBarcode(String supplierProductBarcode) {
        this.supplierProductBarcode = supplierProductBarcode;
    }

    public String getMainCategoryCode() {
        return mainCategoryCode;
    }

    public void setMainCategoryCode(String mainCategoryCode) {
        this.mainCategoryCode = mainCategoryCode;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Double getAverageImportPrice() {
        return averageImportPrice;
    }

    public void setAverageImportPrice(Double averageImportPrice) {
        this.averageImportPrice = averageImportPrice;
    }

    public String getInventoryWarehouseIds() {
        return inventoryWarehouseIds;
    }

    public void setInventoryWarehouseIds(String inventoryWarehouseIds) {
        this.inventoryWarehouseIds = inventoryWarehouseIds;
    }

    public String getInventoryStatusWarehouseIds() {
        return inventoryStatusWarehouseIds;
    }

    public void setInventoryStatusWarehouseIds(String inventoryStatusWarehouseIds) {
        this.inventoryStatusWarehouseIds = inventoryStatusWarehouseIds;
    }

    public String getImportType() {
        return importType;
    }

    public void setImportType(String importType) {
        this.importType = importType;
    }

    public String getTypeSell() {
        return typeSell;
    }

    public void setTypeSell(String typeSell) {
        this.typeSell = typeSell;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getComboType() {
        return comboType;
    }

    public void setComboType(String comboType) {
        this.comboType = comboType;
    }

    public List<PriceData> getPrices() {
        return prices;
    }

    public void setPrices(List<PriceData> prices) {
        this.prices = prices;
    }

    public Long getExternalId() {
        return externalId;
    }

    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }

    public Double getWholesalePrice() {
        return wholesalePrice;
    }

    public void setWholesalePrice(Double wholesalePrice) {
        this.wholesalePrice = wholesalePrice;
    }

    public boolean isAllowSell() {
        return allowSell;
    }

    public void setAllowSell(boolean allowSell) {
        this.allowSell = allowSell;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public List<Long> getFullCategoryIds() {
        return fullCategoryIds;
    }

    public void setFullCategoryIds(List<Long> fullCategoryIds) {
        this.fullCategoryIds = fullCategoryIds;
    }

    public boolean isAllowReward() {
        return allowReward;
    }

    public void setAllowReward(boolean allowReward) {
        this.allowReward = allowReward;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getDefaultImageUrl() {
        return defaultImageUrl;
    }

    public void setDefaultImageUrl(String defaultImageUrl) {
        this.defaultImageUrl = defaultImageUrl;
    }

    public List<String> getOnsiteImageUrls() {
        return onsiteImageUrls;
    }

    public void setOnsiteImageUrls(List<String> onsiteImageUrls) {
        this.onsiteImageUrls = onsiteImageUrls;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }
}
