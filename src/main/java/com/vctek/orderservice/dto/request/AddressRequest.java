package com.vctek.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressRequest {
    private Long id;
    private String customerName;
    private Long customerId;
    private Long companyId;
    private Long provinceId;
    private Long districtId;
    private Long wardId;
    private Long streetId;
    private String addressDetail;
    private String email;
    private String fax;
    private String taxCode;
    private String phone1;
    private String phone2;
    private boolean isBillingAddress;
    private boolean isShippingAddress;
    private boolean isContactAddress;
    private String mapIcon;
    private double latitude;
    private double longitude;
    private Long referId;
    private String referType;
    private String contact;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Long getProvinceId() { return provinceId; }

    public void setProvinceId(Long provinceId) { this.provinceId = provinceId; }

    public Long getDistrictId() { return districtId; }

    public void setDistrictId(Long districtId) { this.districtId = districtId; }

    public Long getWardId() { return wardId; }

    public void setWardId(Long wardId) { this.wardId = wardId; }

    public Long getStreetId() { return streetId; }

    public void setStreetId(Long streetId) { this.streetId = streetId; }

    public String getAddressDetail() { return addressDetail; }

    public void setAddressDetail(String addressDetail) { this.addressDetail = addressDetail; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getFax() { return fax; }

    public void setFax(String fax) { this.fax = fax; }

    public String getTaxCode() { return taxCode; }

    public void setTaxCode(String taxCode) { this.taxCode = taxCode; }

    public String getPhone1() { return phone1; }

    public void setPhone1(String phone1) { this.phone1 = phone1; }

    public String getPhone2() { return phone2; }

    public void setPhone2(String phone2) { this.phone2 = phone2; }

    public boolean isBillingAddress() { return isBillingAddress; }

    public void setBillingAddress(boolean billingAddress) { isBillingAddress = billingAddress; }

    public boolean isShippingAddress() { return isShippingAddress; }

    public void setShippingAddress(boolean shippingAddress) { isShippingAddress = shippingAddress; }

    public boolean isContactAddress() { return isContactAddress; }

    public void setContactAddress(boolean contactAddress) { isContactAddress = contactAddress; }

    public String getMapIcon() { return mapIcon; }

    public void setMapIcon(String mapIcon) { this.mapIcon = mapIcon; }

    public double getLatitude() { return latitude; }

    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }

    public void setLongitude(double longitude) { this.longitude = longitude; }

    public Long getReferId() {
        return referId;
    }

    public void setReferId(Long referId) {
        this.referId = referId;
    }

    public String getReferType() {
        return referType;
    }

    public void setReferType(String referType) {
        this.referType = referType;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}
