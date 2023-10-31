package com.vctek.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.vctek.orderservice.util.DateUtil;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerRequest {
    private Long id;
    private Long companyId;
    private String phone;
    private String name;
    private String email;
    private Long addressId;
    private String addressDetail;
    private Long provinceId;
    private Long districtId;
    private Long wardId;
    private String phone1;
    private String gender;
    private String age;
    private Date firstPurchaseDate;
    private Date latestPurchaseDate;
    @DateTimeFormat(pattern = DateUtil.ISO_DATE_PATTERN)
    private Date dateOfBirth;
    private AddressRequest shippingAddress;
    private String orderType;
    private Long externalId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddressDetail() {
        return addressDetail;
    }

    public void setAddressDetail(String addressDetail) {
        this.addressDetail = addressDetail;
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

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public String getPhone1() {
        return phone1;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
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

    public Date getFirstPurchaseDate() {
        return firstPurchaseDate;
    }

    public void setFirstPurchaseDate(Date firstPurchaseDate) {
        this.firstPurchaseDate = firstPurchaseDate;
    }

    public Date getLatestPurchaseDate() {
        return latestPurchaseDate;
    }

    public void setLatestPurchaseDate(Date latestPurchaseDate) {
        this.latestPurchaseDate = latestPurchaseDate;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public AddressRequest getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(AddressRequest shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Long getExternalId() {
        return externalId;
    }

    public void setExternalId(Long externalId) {
        this.externalId = externalId;
    }
}
