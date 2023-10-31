package com.vctek.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ComboData implements Serializable {
    private static final long serialVersionUID = 3865717049329240156L;
    private Long id;
    private String name;
    private Long companyId;
    private List<PriceData> prices;
    private String sku;
    private String barcode;
    private boolean generateBarcode;
    private String comboType;
    private String typeSell;
    private Date startDate;
    private Date endDate;
    private Integer maxSaleQuantity;
    private List<Long> warehouseIds;
    private List<ComboGroupProductData> comboGroupProductRequests;
    private boolean duplicateSaleProduct;
    private Double price;
    private Double wholesalePrice;
    private int totalItemQuantity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public List<PriceData> getPrices() {
        return prices;
    }

    public void setPrices(List<PriceData> prices) {
        this.prices = prices;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public boolean isGenerateBarcode() {
        return generateBarcode;
    }

    public void setGenerateBarcode(boolean generateBarcode) {
        this.generateBarcode = generateBarcode;
    }

    public String getComboType() {
        return comboType;
    }

    public void setComboType(String comboType) {
        this.comboType = comboType;
    }

    public String getTypeSell() {
        return typeSell;
    }

    public void setTypeSell(String typeSell) {
        this.typeSell = typeSell;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Integer getMaxSaleQuantity() {
        return maxSaleQuantity;
    }

    public void setMaxSaleQuantity(Integer maxSaleQuantity) {
        this.maxSaleQuantity = maxSaleQuantity;
    }

    public List<Long> getWarehouseIds() {
        return warehouseIds;
    }

    public void setWarehouseIds(List<Long> warehouseIds) {
        this.warehouseIds = warehouseIds;
    }

    public List<ComboGroupProductData> getComboGroupProductRequests() {
        return comboGroupProductRequests;
    }

    public void setComboGroupProductRequests(List<ComboGroupProductData> comboGroupProductRequests) {
        this.comboGroupProductRequests = comboGroupProductRequests;
    }

    public boolean isDuplicateSaleProduct() {
        return duplicateSaleProduct;
    }

    public void setDuplicateSaleProduct(boolean duplicateSaleProduct) {
        this.duplicateSaleProduct = duplicateSaleProduct;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public int getTotalItemQuantity() {
        return totalItemQuantity;
    }

    public void setTotalItemQuantity(int totalItemQuantity) {
        this.totalItemQuantity = totalItemQuantity;
    }

    public Double getWholesalePrice() {
        return wholesalePrice;
    }

    public void setWholesalePrice(Double wholesalePrice) {
        this.wholesalePrice = wholesalePrice;
    }
}
